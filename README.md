# voxy-server-side-ekaii

ekaii fork / mirror of [Voxy Server Side](https://modrinth.com/plugin/voxy-server-side) by **Xantha** (MIT).

> Sends pre-generated chunk LOD column data from server to clients running the [Voxy](https://github.com/MCRcortex/voxy) client mod, so distant terrain is rendered without players having to explore first.

## Why this fork exists

Upstream is **closed-source** (MIT license shipped in the JAR but no public repo). We need a maintainable source-tree to:

1. Run on **Folia / Luminol 26.1.2** (upstream is `BukkitRunnable`-based — does not even load on Folia).
2. Pair it with our existing `distant-horizons-server-plugin-ekaii` and `luminol-ekaii` stack on `creaekaii`.
3. Eventually integrate with our `voxy-vulkan-research` macOS-native client port.

## Layout

| Path | Contents |
|---|---|
| `upstream-jars/` | Pristine 0.3.0 JARs from Modrinth (paper + fabric + nested common). Reference binaries. |
| `decompiled-source/{paper,fabric,common}/` | jadx-decompiled Java. 146 files, 4 inner-class jadx WARN, no JADX ERROR. Compiles cleanly modulo paperweight + loom setup. |
| `work/` | Multi-module Gradle project (paper + fabric + common). To be filled in subsequent sessions. |
| `docs/` | `ARCHITECTURE.md`, `IMPROVEMENT_AXES.md`, `HANDOFF.md`. |
| `LICENSE` | Upstream MIT, copyright Xantha 2026. Preserved verbatim. |

## Upstream provenance

- Project ID `84zcagOb` on Modrinth.
- Fabric 0.3.0 (`gcsu1Tiz`, 2026-04-16) — MC 26.1 / 26.1.1 / 26.1.2.
- Paper 0.3.0 (`S1TM384w`, 2026-05-02) — MC 26.1.2.
- Author: Xantha (no public Git repo — searched GitHub / GitLab / Codeberg).
- License: MIT (full text in `LICENSE`). Decompilation + redistribution + modification permitted.

## Status

- [x] Investigation done — see `docs/ARCHITECTURE.md`.
- [x] Improvement axes identified — see `docs/IMPROVEMENT_AXES.md`.
- [ ] Gradle multi-module skeleton (paperweight-userdev 2.0 + fabric-loom 1.16.x).
- [ ] Folia/Luminol port (P0).
- [ ] CI: `.forgejo/workflows/build.yml` (node:20-bookworm + setup-java@v4 + upload-artifact@v3).
- [ ] Smoke harness: drop on Luminol 26.1.2 + Voxy 0.2.14-alpha client.

## Pairing

Designed to talk to the **Voxy client mod ≥ 0.2.14-alpha**. Wire protocol version is **15** (constant `dev.xantha.vss.common.VSSConstants.PROTOCOL_VERSION`). Client and server must match exactly — no ViaVersion translation, the client emergency-disables on first decode error.
