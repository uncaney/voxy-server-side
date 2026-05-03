# HANDOFF — voxy-server-side-ekaii

Persistent state for multi-session work. Always read this first when resuming.

## Why this project exists

User asked 2026-05-04 for a deep investigation of Voxy Server Side (Modrinth `84zcagOb`, MIT, by Xantha) → rebuild on `forgejo.ekaii.fr/exo/` → identify improvement axes. Upstream is closed-source despite MIT; we have full rights via the license to decompile, modify, redistribute.

Workspace: `/Users/paulchauvat/voxy-server-side-ekaii/`. Sibling `/Users/paulchauvat/voxy-server-side-research/` holds raw decompile output and unzipped JARs (kept for forensic reference; not in repo).

## Where I left off

Session 1 (2026-05-04):
- [x] Downloaded paper + fabric 0.3.0 JARs.
- [x] Decompiled all three (paper, fabric, common) with jadx → 146 .java files, 4 inner-class WARN, 0 ERROR.
- [x] Wrote `docs/ARCHITECTURE.md` (full protocol + threading + dirty + mixins + Folia verdict).
- [x] Wrote `docs/IMPROVEMENT_AXES.md` (P0..P9).
- [x] Staged decompiled sources under `decompiled-source/{paper,fabric,common}/`.
- [x] Pristine binaries archived in `upstream-jars/`.
- [ ] Forgejo repo created + initial push: **next step**.
- [ ] Multi-module Gradle skeleton (paperweight-userdev 2.0 + fabric-loom 1.16.x).
- [ ] CI: `.forgejo/workflows/build.yml`.
- [ ] P0: Folia/Luminol port.
- [ ] Smoke harness against Luminol 26.1.2.

## Next concrete step

```
TOKEN=$(cat ~/.secrets/forgejo-ekaii.txt)
curl -fsS -X POST -H "Authorization: token $TOKEN" \
  -H "Content-Type: application/json" \
  https://forgejo.ekaii.fr/api/v1/admin/users/exo/repos \
  -d '{"name":"voxy-server-side","description":"ekaii fork of Xantha'\''s Voxy Server Side — Folia/Luminol port + decompiled sources","private":false,"default_branch":"main","auto_init":false}'

cd /Users/paulchauvat/voxy-server-side-ekaii
git init -b main
git add LICENSE README.md docs/ decompiled-source/ upstream-jars/ .gitignore
git commit -m "Initial: stage decompiled sources + architecture docs from Xantha 0.3.0"
git remote add forgejo "https://admin_ekaii:${TOKEN}@forgejo.ekaii.fr/exo/voxy-server-side.git"
git push -u forgejo main
```

Then mirror to `github.com/uncaney/voxy-server-side` (token at `~/.secrets/github-uncaney.txt`).

## Decisions locked

- **Repo name**: `exo/voxy-server-side` (no `-ekaii` suffix on forgejo to keep parity with upstream Modrinth slug; the `-ekaii` is implicit because owner is `exo`).
- **License**: keep upstream MIT verbatim. Add a NOTICE clarifying ekaii modifications.
- **Author rewrite on commits**: `exo <exo@chauvat.com>` (per `feedback_forgejo_authorship` pattern).
- **Build chain**: same as `axiom-paper-folia` — paperweight-userdev 2.0 for paper, fabric-loom 1.16.x for fabric, common as plain Java module.
- **Target MC version**: 26.1.2 (matches our Luminol stack and is already upstream's target for paper).
- **JDK**: 21 build-time, 25 run-time. Same as DH plugin port.

## Decisions deferred

- Should we merge Paper + Folia into a single jar (`folia-supported: true` + runtime detection) or ship two? Lean: single jar, runtime branch — matches `axiom-paper-folia` pattern and avoids version skew.
- Compression P1 — when, and what algo? Lean: zstd-1, behind `compression: zstd-1` config flag, bumped to PROTOCOL_VERSION 16 (would break clients running unmodified Voxy mod). Defer until P0 is green.
- Should we publish a Modrinth project of our own under a distinct slug? Lean: no, until we have material improvements. Just expose the forgejo+github mirrors.

## How to resume in a new session

1. Read this file first.
2. Read `docs/ARCHITECTURE.md` for the technical model.
3. Read `docs/IMPROVEMENT_AXES.md` for the prioritized work list.
4. Check `git log` for what's actually shipped vs what's in this doc (this doc decays — code is truth).
5. Pick the topmost incomplete item from "Next concrete step" or "Status" in `README.md`.
