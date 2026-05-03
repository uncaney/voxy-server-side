# voxy-server-side-ekaii

ekaii fork of [VoX/lod-server-support](https://github.com/VoX/lod-server-support) (the open-source MIT upstream of the LOD-streaming-server-plugin family).

> Sends pre-generated chunk LOD column data from server to clients running the [Voxy](https://github.com/MCRcortex/voxy) client mod, so distant terrain is rendered without players having to explore first.

## Lineage

The Modrinth project [voxy-server-side](https://modrinth.com/plugin/voxy-server-side) by **Xantha** is a 1:1 rebrand of VoX's open-source `lod-server-support` (verified 2026-05-04 by class-list diff: 88/88 source files match after `dev.vox.lss → dev.xantha.vss` rename, only difference is Xantha stripped VoX's 13 test files). VoX called this out publicly via PR #4 (since renamed to "Closed") and reached out via the softer PR #5. **VoX is the legitimate upstream.** Xantha's JAR + Modrinth presence captured user mindshare without contributing back. Our fork bases on VoX directly.

## Why this fork exists

The upstream targets paper-api 1.21.11 and has **zero Folia awareness**. To run on our `creaekaii` server (which uses Luminol 26.1.2, a Folia derivative), the plugin must:

1. Bump paper-api 1.21.11 → 26.1.2 (matches our Luminol stack).
2. Add `folia-supported: true` + runtime detection so a single jar runs on both Paper and Folia/Luminol.
3. Fix correctness bugs identified during the deep code review (~30 P0 findings across `common/`, `fabric/`, and `paper/`).

## Layout

| Path | Contents |
|---|---|
| `work/` | **Working tree — fork of VoX/lod-server-support.** Multi-module Gradle (common + fabric + paper). Edits land here. |
| `upstream-jars/` | Pristine 0.3.0 binaries from Modrinth (Xantha's rebrand). Reference for wire-protocol comparison. |
| `docs/CODE_REVIEW.md` | All findings (P0/P1/P2) from 4 parallel deep-read agents. |
| `docs/ARCHITECTURE.md` | Architecture brief (protocol v15, threading, mixins, dirty tracking). |
| `docs/IMPROVEMENT_AXES.md` | Prioritized roadmap for upstream PRs vs. fork-only patches. |
| `docs/HANDOFF.md` | Multi-session continuation pointer (autonomous-loop). |
| `docs/forensic/decompiled-xantha-0.3.0/` | jadx output of Xantha's 0.3.0 JAR. Forensic exhibit only — proof of the rebrand. **Not a working base.** |
| `LICENSE` | MIT (preserved verbatim from VoX upstream). |

## Build (baseline upstream, no edits yet)

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
PATH=$JAVA_HOME/bin:$PATH \
  cd work && ./gradlew :common:build :paper:shadowJar :fabric:build -x runGameTest -x runClientGameTest
```

Outputs:
- `work/common/build/libs/common-0.3.0.jar`
- `work/paper/build/libs/lod-server-support-paper.jar` (currently 1.21.11; bump to 26.1.2 in progress)
- `work/fabric/build/libs/lod-server-support-fabric.jar` (already 26.1.2)

## Status

- [x] Pivoted workspace from decompiled-Xantha to VoX-source. Decompile kept under `docs/forensic/`.
- [x] Deep code review (4 parallel agents, ~70 findings). See `docs/CODE_REVIEW.md`.
- [x] Baseline upstream build green (JDK 25, Gradle 9.4, paperweight 2.0.0-beta.19).
- [ ] Bump paper module from paper-api 1.21.11 → 26.1.2.
- [ ] Folia port (single-jar runtime detection via `PlatformDispatch` helper).
- [ ] Fix high-impact P0 bugs (executor leak, dedup race on disconnect, dimensionStringCache leak).
- [ ] CI: `.forgejo/workflows/build.yml` (node:20-bookworm + setup-java@v4 JDK 25 + upload-artifact@v3).
- [ ] Smoke harness on Luminol 26.1.2 + Voxy client.
- [ ] Tag release `v0.3.0-ekaii-1.0.0`. Push to forgejo + github mirrors.
- [ ] Submit Folia port as upstream PR to `VoX/lod-server-support`.

## Wire protocol contract (must preserve)

Protocol version **15**. Channels: `lss:handshake_c2s`, `lss:session_config`, `lss:batch_chunk_req`, `lss:batch_response`, `lss:voxel_column`, `lss:cancel_request`, `lss:bandwidth_update`, `lss:dirty_columns`. Position packing `((long)cx << 32) | (cz & 0xFFFFFFFFL)`. Response type bytes `0=RATE_LIMITED, 1=UP_TO_DATE, 2=NOT_GENERATED`. Dimension ordinals `0=OW, 1=Nether, 2=End, -1=custom + writeUtf(256)`. Bounds: `MAX_BATCH_CHUNK_REQUESTS=1024, MAX_BATCH_RESPONSES=4096, MAX_DIRTY_COLUMNS=10240, MAX_SECTIONS_SIZE=2 MiB`. Any change to these breaks all upstream / Xantha / our-fork clients.
