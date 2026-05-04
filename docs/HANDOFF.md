# HANDOFF — voxy-server-side-ekaii

Persistent state for multi-session work. **Always read this first when resuming.**

## Mission

Fork [VoX/lod-server-support](https://github.com/VoX/lod-server-support) under `forgejo.ekaii.fr/exo/voxy-server-side`, port to **paper-api 26.1.2 + Folia/Luminol**, fix high-impact bugs found in deep review, ship a single-jar release.

## Status — v0.3.0-ekaii-1.0.0 SHIPPED 2026-05-04

Released on both remotes with jar assets attached:
- **Forgejo (canonical)**: https://forgejo.ekaii.fr/exo/voxy-server-side/releases/tag/v0.3.0-ekaii-1.0.0
- **GitHub (mirror)**: https://github.com/uncaney/voxy-server-side/releases/tag/v0.3.0-ekaii-1.0.0

What's in the release:
- `lod-server-support-paper.jar` — 142 KB. paper-api 26.1.2.build.59-stable. `folia-supported: true`. Single jar runs on vanilla Paper, Folia, and Luminol 26.1.2.
- `lod-server-support-fabric.jar` — 221 KB. MC 26.1.2 / Fabric Loader 0.18.5+ / Fabric API 0.146+. Wire-compat with Paper jar.
- Wire protocol **v15 unchanged** — fully compatible with VoX upstream and Xantha's voxy-server-side rebrand.

## What this session shipped

Session 2 (2026-05-04, autonomous mode):
- [x] Workspace pivot (decompiled-Xantha → forensic, VoX upstream → working tree).
- [x] Deep code review (4 parallel agents) → `docs/CODE_REVIEW.md` (~30 P0 + ~30 P1 + ~10 P2 + ~20 test gaps).
- [x] Baseline upstream build green.
- [x] **Bump paper module 1.21.11 → 26.1.2** (paperweight beta.21, dev-bundle build.59-stable, JDK 25, `section.write(buf)` arity).
- [x] **Folia port** via `PlatformDispatch` helper + `IS_FOLIA` runtime detection. Tick driver moved to `GlobalRegionScheduler`. Generation callback dispatches to chunk's region scheduler on Folia. `probeLoadedChunks` no-ops on Folia (disk reader pool serves with ~1 tick latency).
- [x] **Targeted bug fixes**: F11 (WeakHashMap dimensionStringCache), F22 (per-payload error handling in flushSendQueue), F23 (BatchResponseS2C encoder bound), F12 (volatile ChunkMapSaveHook lss$cachedDimension).
- [x] **Smoke harness** on Luminol 26.1.2 paperclip — PASS.
- [x] **CI workflow** at `.forgejo/workflows/build.yml` (node:20-bookworm + setup-java@v4 JDK 25 + upload-artifact@v3 + tag-triggered Forgejo release auto-asset upload).
- [x] **Pushed + tagged + released** on forgejo.ekaii.fr/exo + github.com/uncaney.

## Open work (not blocking the v1.0.0 release)

These are high-value follow-ups documented in `docs/CODE_REVIEW.md`. Pick from the punch list when starting a new session.

P0 fixes still open:
- **C1**: `OffThreadProcessor.SAVE_EXECUTOR` is static, never shut down → race with synchronous shutdown save. (`common/processing/OffThreadProcessor.java:62-66, 213-217, 440-456`)
- **C2/C9**: `ColumnTimestampCache.load` resets all `insertionTimes` to `now` → defeats LRU eviction post-restart. (`common/voxel/ColumnTimestampCache.java:170, 189-197`)
- **C7 / F8**: `DedupTracker.removePlayer` of primary leaves attached players' pending requests orphaned forever. Combined with `RequestProcessingService.removePlayer` race on net-disconnect.
- **F2**: `ChunkMapSaveHook` skips final shutdown saves because `requestService=null` is set in `SERVER_STOPPING` before the chunk save flush. Move to static singleton DirtyColumnTracker.

P1 perf:
- **C20** / **F19**: `evictOldest` is O(k×N); replace with min-heap.
- **C22**: `snapshotForSave` does full `Long2LongOpenHashMap` deep copy every 5 min — up to 384 MB transient at max config.

Test gaps (~20 production classes uncovered): see `docs/CODE_REVIEW.md` test plan E1..E12.

Upstream PR to VoX:
- **P0 Folia port** is exactly the contribution VoX would benefit from. The PlatformDispatch helper + plugin.yml `folia-supported: true` + minor `probeLoadedChunks` Folia branch is a clean, single-purpose PR that extends VoX's reach.
- Branch suggestion: `git checkout -b folia-port-via-platform-dispatch`, cherry-pick the Folia commit only (excludes our paper-api 26.1.2 bump and bug fixes), open PR.

## Decisions locked

- Working tree base: `git clone https://github.com/VoX/lod-server-support.git work` then `rm -rf work/.git`. Hard fork in our own history.
- Wire protocol: **DO NOT MODIFY**. `LSSConstants.PROTOCOL_VERSION = 15` stays.
- Naming: keep `dev.vox.lss` package names (preserves binary compat + simplifies upstream PR).
- Repo slug: `exo/voxy-server-side` on forgejo, `uncaney/voxy-server-side` on github.
- JDK: build with **JDK 25**. Common+Paper compile to release 25 (paper-api 26.x requires JDK 25). Fabric to release 25.
- Gradle: **9.4.0** (wrapper-pinned).
- paperweight-userdev: `2.0.0-beta.21` (data-version 8 needed for paper 26.1.2 dev-bundles).

## How to resume

1. Read this file.
2. Read `docs/CODE_REVIEW.md` (search "P0-" or "C1" / "F2" etc. for blockers).
3. `cd work && git status && git log --oneline -5`.
4. Run baseline build to confirm sanity:
   ```bash
   JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
   PATH=$JAVA_HOME/bin:$PATH \
   cd work && ./gradlew :common:build :paper:shadowJar :fabric:build -x runGameTest -x runClientGameTest
   ```
5. Run smoke harness:
   ```bash
   bash work/test-harness/run-smoke.sh
   # Expected: "=== verdict ===" then "PASS"
   ```
6. Pick a P0 from the open-work section above.
