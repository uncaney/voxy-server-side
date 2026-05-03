# Improvement axes — voxy-server-side-ekaii

Ranked by ROI / blocking-ness for the ekaii stack. Each axis cites the upstream code it touches.

## P0 — Folia / Luminol 26.1.2 port

**Why**: our `creaekaii` server runs Luminol (Folia-derived). Upstream VSS does not load on Folia at all. This is the single change that unlocks deployment.

**Touch points**:

1. **`plugin.yml`** — add `folia-supported: true`. Without this Folia refuses the plugin at load.
2. **Tick driver** (`VSSPaperPlugin:43-57`) — replace `new BukkitRunnable() { ... }.runTaskTimer(this, 1L, 1L)` with:
   ```java
   getServer().getGlobalRegionScheduler()
       .runAtFixedRate(this, task -> service.tick(), 1L, 1L);
   ```
3. **Per-player work** in `PaperRequestProcessingService.processPlayerLifecycle:259-294` — split. Player state read (`player.level()`, `getBlockX/Z()`, `isRemoved()`) MUST run on the player's region. Use `player.getScheduler().run(plugin, task -> { ... }, null)`. Snapshot the values into a `PlayerTickData` and ship that to the off-thread processor (already the model — just inject the snapshot from per-player region tasks instead of from the global tick).
4. **Chunk probe** in `PaperRequestProcessingService.probeLoadedChunks:324` — `level.getChunkSource().getChunkNow(cx, cz)` from the global tick is invalid on Folia. Either: dispatch each probe via `world.getRegionScheduler().run(plugin, world, cx, cz, task -> { ... })` (high overhead, 512×Nplayers per tick) **or** keep the off-tick `IncomingRequestRouter` path and skip the global probe phase entirely (probe is an optimization, not a correctness requirement — the disk-read path is the canonical fallback).
5. **Generation callback** (`PaperChunkGenerationService.launchAsyncLoad:160-203`) — replace `Bukkit.getScheduler().runTask(plugin, ...)` with `world.getRegionScheduler().run(plugin, world, cx, cz, task -> { serializer + result push })`.
6. **Send** (`PaperPayloadHandler.dispatch:300-329`) — `nmsPlayer.connection.send(...)` from a non-owning region. Folia tolerates Netty channel writes from any thread (it's the gameplay state it polices), so this is mostly fine — but cross-check on Luminol's strict mode.
7. **Dirty-column broadcast** (`PaperDirtyColumnBroadcaster.tick:41-98`) — same split: dispatch per-player on player's entityScheduler.
8. **Bukkit event listeners** (`PaperWorldHandler.registerUpdateListeners`) — events fire on region threads on Folia. `DirtyColumnTracker.markDirty` is `synchronized` so this is already safe.

**Recommendation**: introduce a `PlatformScheduler` interface in `common/`, with `Bukkit` and `Folia` implementations. Detect at runtime via `Class.forName("io.papermc.paper.threadedregions.RegionizedServer")` so the **same JAR runs on both** Paper and Folia/Luminol — like our `axiom-paper-folia` port.

**Effort**: 1-2 sessions. Smoke harness: drop on `~/luminol-ekaii/test-server/luminol-paperclip-26.1.2.local-SNAPSHOT.jar`.

## P1 — Compression on the wire

**Why**: a fully-occupied 16³ section in raw NMS palette format is ~5 KiB. A typical 256-chunk-radius LOD scan ships ~80 MiB of data per player on first connect (256² chunks × ~24 sections × 5 KiB). zstd-3 typically gets 4-6× on Minecraft chunk NBT.

**Touch points**:
- `VoxelColumnS2CPayload:72-109` — wrap `sectionBytes` write/read with zstd. Add a `byte compression` field (0=none, 1=zstd) to keep room for adaptive choice. Bump `PROTOCOL_VERSION` to 16.
- `BatchResponseS2CPayload` is small enough to skip.
- `PaperSectionSerializer.serializeColumn:104` and `NbtSectionSerializer.readAndSerializeSections:79` — output a compressed byte[] instead of raw.

**Caveats**: server-side CPU cost (1 thread per disk reader, currently up to 64). zstd-1 is typically 400 MB/s/core; comfortable. Keep behind a config flag with sane default `compression: zstd-1`.

**Effort**: half a session.

## P2 — Paper-side dirty tracking via Paper API ChunkSaveEvent

**Why**: upstream Paper variant uses a hard-coded reflective Bukkit-event list (`PaperConfig.updateEvents`) — misses NMS-direct changes. Fabric variant uses a `ChunkMap.save` mixin (canonical). Paper exposes `ChunkSaveEvent` since paper-api 1.20.5 — this matches the Fabric semantics 1:1.

**Touch points**:
- `PaperWorldHandler:40-117` — replace the reflective listener registration with `@EventHandler public void onChunkSave(io.papermc.paper.event.world.ChunkSaveEvent e)`. Drop the entire reflection block.
- Remove `updateEvents` from `PaperConfig`.

**Effort**: ~1 hour.

## P3 — Sectioned bytes that can survive ViaVersion

**Why**: client emergency-disables on first decode error if the server's NMS palette differs from the client's. Any ViaVersion-translated session breaks instantly. A version-tagged section format with a thin palette adapter on the client would be more robust.

**Touch points**: not surgical — would need a proper palette IR + per-version codec. Probably not worth doing for our fork unless we go upstream.

## P4 — Open the source publicly + Forgejo CI

**Why**: upstream is closed-source despite MIT, blocking community contribution. Our fork can publish the rebuilt sources as a working-tree upstream for the community. Pattern matches `distant-horizons-server-plugin-ekaii`.

**Touch points**:
- Multi-module Gradle build (paperweight-userdev 2.0 + fabric-loom 1.16.x), single source set per module.
- `.forgejo/workflows/build.yml` — node:20-bookworm + setup-java@v4 (JDK 21) + upload-artifact@v3 + tag-triggered release.
- Mirror to `github.com/uncaney/voxy-server-side-ekaii` for visibility.

**Effort**: 1 session (most of which is dialing in paperweight on the runner — Coolify runner already has it from `coreprotect-ekaii`).

## P5 — Bytecode-equivalent rebuild verification

**Why**: build something that proves "rebuilt from decompiled sources matches upstream behavior". Differential testing with `javap -v` per-class plus a smoke harness fingerprint catches regressions.

**Touch points**:
- `work/test-harness/run-smoke.sh` like the DH plugin's harness — boot Luminol 26.1.2 + the rebuilt jar + a synthetic Voxy-mock client that performs the handshake and a single `BatchChunkRequestC2SPayload(requestId=1, pos=0,0, ts=0)` — assert `VoxelColumnS2CPayload(requestId=1)` returns within 5s.
- Compare class lists + method signatures between rebuilt and upstream JAR; allow inner-class number drift.

**Effort**: half a session.

## P6 — ColumnTimestampCache load-time `insertionTimes` reset

Minor. `ColumnTimestampCache.load:195-239` resets `insertionTimes` to `epochSeconds()` for every loaded entry → first-pass eviction post-restart blows away the entire cache because everyone's "insertion" looks fresh. Should preserve persisted insertion times so eviction LRU still favors the genuinely-old entries.

**Effort**: 30 min.

## P7 — Better defaults for slow worlds

`generationTimeoutSeconds = 60` is aggressive on cold worlds with custom-gen mods (terralith, etc.). Bump default to 180. Make `enableChunkGeneration = false` the default for non-creative servers — generation can balloon I/O on small VPSes.

## P8 — Pair with our DH server plugin

Our `distant-horizons-server-plugin-ekaii` and `voxy-server-side-ekaii` ship overlapping concerns: pre-render distant terrain. They use **different** clients (DH ↔ DH client mod, VSS ↔ Voxy). Coexistence works (different channels, different storage on the client) but doubles the server I/O. Worth measuring on `plugbench-ekaii` once the Folia port is green.

## P9 — Tie-in with voxy-vulkan-research / voxy-26-fork

Our macOS-native Voxy-Vulkan port is the missing piece for an end-to-end macOS-native LOD stack. Once `voxy-26-fork` reaches Phase 5 (LOD chunk source), we can demonstrate **VSS server → Vulkan-rendered macOS client**, which is something upstream cannot do (Voxy auto-disables on Mac due to OpenGL 4.6 requirement).

This is more of a "story" than an axis — it doesn't change VSS code, but it changes what VSS unblocks.
