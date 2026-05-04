#!/usr/bin/env bash
# Smoke harness — boots a Luminol 26.1.2 server with the freshly-built ekaii
# LSS jar dropped in plugins/, watches run.log for plugin enable + zero
# exceptions. PASS = plugin loads, "Folia/Luminol mode" suffix appears,
# command registers, no exceptions.
#
# Usage:  bash test-harness/run-smoke.sh [path-to-luminol-paperclip.jar]
#
# Requires JDK 25 at /opt/homebrew/opt/openjdk@25 (macOS arm64) or set
# JAVA_HOME yourself to a JDK 25 install.
set -uo pipefail

REPO_DIR=$(cd "$(dirname "$0")/.." && pwd)
LUMINOL_JAR="${1:-$HOME/luminol-ekaii/test-server/luminol-paperclip-26.1.2.local-SNAPSHOT.jar}"
JDK25="${JDK25:-/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home}"

JAR_NAME="$REPO_DIR/paper/build/libs/lod-server-support-paper.jar"
if [[ ! -f "$JAR_NAME" ]]; then
  echo "FAIL: paper jar not found at $JAR_NAME — run ./gradlew :paper:shadowJar first" >&2
  exit 2
fi

if [[ ! -f "$LUMINOL_JAR" ]]; then
  echo "FAIL: Luminol paperclip not found at: $LUMINOL_JAR" >&2
  echo "      Pass the path as arg 1, or build Luminol per project_luminol_ekaii." >&2
  exit 2
fi

if [[ ! -x "$JDK25/bin/java" ]]; then
  echo "FAIL: JDK 25 not found at $JDK25 — set JDK25 env var" >&2
  exit 2
fi

WORK=$(mktemp -d -t lss-smoke.XXXXXX)
trap 'rm -rf "$WORK"' EXIT
echo "smoke workspace: $WORK"

cp "$LUMINOL_JAR" "$WORK/luminol.jar"
mkdir -p "$WORK/plugins"
cp "$JAR_NAME" "$WORK/plugins/"
echo 'eula=true' > "$WORK/eula.txt"
cat > "$WORK/server.properties" <<'EOF'
online-mode=false
server-port=25788
motd=LSS smoke
view-distance=4
simulation-distance=4
gamemode=creative
spawn-protection=0
generate-structures=false
broadcast-rcon-to-ops=false
EOF

cd "$WORK"
"$JDK25/bin/java" -Xms512M -Xmx2G -DPaper.IgnoreJavaVersion=true \
  -jar luminol.jar --nogui > run.log 2>&1 &
PID=$!

# wait up to 90s for "Done ("
for i in $(seq 1 90); do
  if grep -qE 'Done \(' run.log 2>/dev/null; then break; fi
  sleep 1
done

# give plugin a moment to settle, then stop
sleep 5
kill "$PID" 2>/dev/null || true
wait "$PID" 2>/dev/null || true

echo "=== last 40 lines of run.log ==="
tail -40 run.log

echo "=== verdict ==="
fail=0
if ! grep -qE 'LOD Server Support \(Paper\) enabled' run.log; then
  echo "FAIL: 'LOD Server Support (Paper) enabled' never logged" >&2; fail=1
fi
if ! grep -qE 'Folia/Luminol mode' run.log; then
  echo "FAIL: PlatformDispatch.IS_FOLIA was false on Luminol — runtime detection broken" >&2; fail=1
fi
if grep -qE '(Could not load .plugins|Error occurred while enabling LodServerSupport|Disabled LodServerSupport)' run.log; then
  echo "FAIL: plugin load/enable error" >&2; fail=1
fi
if grep -qE 'is not Folia compatible' run.log; then
  echo "FAIL: Folia compatibility refusal" >&2; fail=1
fi
# Treat any LSS-tagged exception as a failure (plugin's own code throwing)
if grep -qE '(LodServerSupport|LSSPaperPlugin|dev\.vox\.lss\.paper\b).*(Exception|Throwable)' run.log; then
  echo "FAIL: plugin emitted an exception:" >&2
  grep -nE '(LodServerSupport|LSSPaperPlugin|dev\.vox\.lss\.paper\b).*(Exception|Throwable)' run.log >&2
  fail=1
fi
# Generic IllegalStateException with our class in the stack indicates region-thread issue
if grep -nE 'dev\.vox\.lss\.paper\.' run.log | grep -qE 'IllegalStateException|UnsupportedOperationException'; then
  echo "FAIL: region-thread or scheduler violation in our code" >&2; fail=1
fi

if [[ $fail -eq 0 ]]; then
  echo "PASS"
  exit 0
else
  exit 1
fi
