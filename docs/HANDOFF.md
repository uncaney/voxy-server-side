# HANDOFF — voxy-server-side-ekaii

Persistent state for multi-session work. **Always read this first when resuming.**

## Mission

Fork [VoX/lod-server-support](https://github.com/VoX/lod-server-support) under `forgejo.ekaii.fr/exo/voxy-server-side`, port to **paper-api 26.1.2 + Folia/Luminol**, fix high-impact bugs found in deep review, ship a single-jar release that runs on Paper + Folia + Luminol 26.1.2.

User authorized autonomous mode 2026-05-04: deep analysis → port → fixes → tests → release. No checkpoints; only stop on hard blocker or genuine completion.

## Where I left off

Session 1 (2026-05-04, fresh start):
- [x] Investigated Xantha's voxy-server-side (Modrinth `84zcagOb`) — discovered it's a 1:1 rebrand of VoX/lod-server-support after Paul pointed at PR #4 event 24701268276.
- [x] Pivoted workspace: cloned VoX upstream into `work/`, moved decompiled-Xantha to `docs/forensic/`.
- [x] Deep code review (4 parallel agents): see `docs/CODE_REVIEW.md`. ~30 P0 + ~30 P1 + ~10 P2 + ~20 test gaps.
- [x] Baseline upstream build green (JDK 25, Gradle 9.4, paperweight 2.0.0-beta.19): all 3 jars compile.
- [x] Repos created earlier: `forgejo.ekaii.fr/exo/voxy-server-side`, `github.com/uncaney/voxy-server-side` — currently hold the *decompiled-Xantha* tree (initial commit `d056b2e5`); will be force-pushed with the *VoX-pivot* tree once the port is ready.

## Next concrete steps (in order)

1. **Commit the VoX-pivot to local git** (decompiled tree → forensic, work/ → live tree).
2. **Bump paper module** to paper-api `26.1.2-R0.1-SNAPSHOT`. Edits:
   - `paper/build.gradle:14` `paperDevBundle('1.21.11-R0.1-SNAPSHOT')` → `'26.1.2-R0.1-SNAPSHOT'`.
   - `paper/.../PaperSectionSerializer.java:64`: `section.write(buf, null, 0)` → `section.write(buf)`.
   - `paper/.../PaperNbtSectionSerializer.java:88`: same.
   - Confirm build green: `./gradlew :paper:shadowJar`.
   - Tag this checkpoint locally as `paper-26.1.2-port-no-folia`.
3. **Introduce `PlatformDispatch` helper** at `paper/src/main/java/dev/vox/lss/paper/PlatformDispatch.java`:
   - `IS_FOLIA = Class.forName("io.papermc.paper.threadedregions.RegionizedServer")`.
   - `runRepeating(plugin, runnable, delay, period)`.
   - `runOnGlobal(plugin, runnable)`.
   - `runOnRegion(plugin, world, chunkX, chunkZ, runnable)`.
   - `runOnEntity(plugin, entity, runnable, retiredHandler)`.
4. **Branch the 8 Folia-relevant call sites** (per `docs/CODE_REVIEW.md` paper section). Edit per CODE_REVIEW P0-1..P0-5 and P1-1..P1-3.
5. **Add `folia-supported: true`** to `paper/src/main/resources/plugin.yml`. **Last** — only after all dispatches are wired.
6. **Apply high-impact bug fixes** (during the same iteration loop, not separate phase):
   - F8 / C7 (RequestProcessingService disconnect race + DedupTracker primary-leaves orphan).
   - F11 (`dimensionStringCache` weak refs).
   - C1 (instance-field `SAVE_EXECUTOR` + ordered shutdown).
   - F2 (`ChunkMapSaveHook` survives `SERVER_STOPPING`).
   - F22 (`flushSendQueue` per-payload error handling).
   - F23 (BatchResponseS2C encoder bound).
7. **Build green** (`:fabric:build :paper:shadowJar :common:build` + Tier 1 + Tier 2 tests).
8. **Smoke harness** on `~/luminol-ekaii/test-server/luminol-paperclip-26.1.2.local-SNAPSHOT.jar`. Confirm:
   - Plugin enables; no exceptions; `/lsslod stats` works.
   - A handshake from a fake client gets a `SessionConfigS2CPayload` with `enabled=true, protocolVersion=15`.
   - A `BatchChunkRequestC2SPayload` for `(0,0)` produces a `VoxelColumnS2CPayload` within 5s.
9. **Forgejo CI**: `.forgejo/workflows/build.yml` (node:20-bookworm + setup-java@v4 JDK 25 + upload-artifact@v3 + tag-triggered release).
10. **Force-push** the new tree over the decompiled-Xantha initial commit on forgejo + github. **Tag** `v0.3.0-ekaii-1.0.0`.
11. **Open upstream PR** to `VoX/lod-server-support` with the Folia port (single-jar pattern).

## Decisions locked

- Working tree base: `git clone https://github.com/VoX/lod-server-support.git work` (no submodule — the .git dir was rm'd; we're a hard fork in our own history).
- Wire protocol: **DO NOT MODIFY** — `LSSConstants.PROTOCOL_VERSION = 15` stays. Any change breaks Xantha clients + upstream LSS clients on Modrinth.
- Naming: keep `dev.vox.lss` package names (preserves binary compat with existing JARs, simplifies upstream PR).
- Repo slug: `exo/voxy-server-side` on forgejo, `uncaney/voxy-server-side` on github (legacy from session 1; the user-facing identity matches Modrinth's expectation).
- JDK: build with **JDK 25** (`/opt/homebrew/opt/openjdk@25`). Common+Paper compile to release 21; Fabric to release 25.
- Gradle: **9.4.0** (wrapper-pinned).
- paperweight-userdev: 2.0.0-beta.19 (works on 26.1.2; per `coreprotect-ekaii` precedent).

## Decisions deferred

- Wire-compression P1 (zstd-1) → bumps to PROTOCOL_VERSION 16 — would break clients. Out of scope for this fork; defer until post-1.0 with coordinated client patch.
- Modrinth publishing → only after release green and proven on creaekaii.
- Whether to upstream the Folia port as a single PR or split per-call-site → single PR with 11-step commit history matching CODE_REVIEW.

## How to resume

1. Read this file.
2. Read `docs/CODE_REVIEW.md` for the punch list (search "P0-" for blockers).
3. `cd work && git status && git log --oneline -5` to see where the local fork is.
4. Run baseline build to confirm sanity:
   ```bash
   JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
   PATH=$JAVA_HOME/bin:$PATH \
   cd work && ./gradlew :common:build :paper:shadowJar :fabric:build -x runGameTest -x runClientGameTest
   ```
5. Pick the next unchecked item from "Next concrete steps".
