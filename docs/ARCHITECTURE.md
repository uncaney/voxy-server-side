# Architecture — Voxy Server Side 0.3.0

Verified by reading every class under `decompiled-source/` (jadx 1.5.x output of the upstream JARs). File:line citations target the decompiled tree.

## 0. Top-level shape

Three artifacts ship from the same project:
- `voxy-server-side-paper.jar` — Bukkit `JavaPlugin`, ~141 KB. Targets paper-api 1.21+ (the manifest declares `paperweight-mappings-namespace: mojang`). Embeds its own copy of the platform-agnostic `common/` classes.
- `voxy-server-side-fabric.jar` — Fabric mod, ~212 KB. Targets MC 26.1+, Fabric Loader ≥ 0.18.4, Fabric API ≥ 0.146.0. Includes 4 mixins + a nested `common-0.3.0.jar`.
- `common-0.3.0.jar` (nested inside the fabric jar, also duplicated into the paper jar) — 22 platform-agnostic classes: request router, off-thread processor, dirty tracking, timestamp cache, bandwidth limiter, diagnostics formatter.

There is **no separate `paper-server-side` Modrinth artifact** — the Paper jar is *self-contained*; common classes are inlined.

## 1. Wire protocol (v15, raw uncompressed `FriendlyByteBuf`)

Eight typed payloads, all flowing over Bukkit plugin-message channels (Paper) or Fabric custom payload channels (Fabric). Identical channel names + framing on both sides:

| Channel | Dir | Payload |
|---|---|---|
| `vss:handshake_c2s` | C2S | `varInt protocolVersion, varInt capabilities` |
| `vss:session_config` | S2C | `varInt protocolVersion, bool enabled, varInt lodDistanceChunks, varInt serverCapabilities, varInt syncRateLimitPP, varInt syncConcurrencyPP, varInt genRateLimitPP, varInt genConcurrencyPP, bool generationEnabled, varLong playerBandwidthLimit` |
| `vss:batch_chunk_req` | C2S | `varInt count (≤1024)`, then per-i: `varInt requestId, long packedPos, long clientTimestamp` |
| `vss:batch_response` | S2C | `varInt count (≤4096)`, per-i: `byte responseType (0=RATE_LIMITED, 1=UP_TO_DATE, 2=NOT_GENERATED), varInt requestId` |
| `vss:voxel_column` | S2C | `varInt requestId, int chunkX, int chunkZ, varInt dimOrdinal (-1=custom; if -1, utf256 dimId), long columnTimestamp, byteArray sectionBytes (max 2 MiB)` |
| `vss:cancel_request` | C2S | `varInt requestId` |
| `vss:bandwidth_update` | C2S | `varLong desiredRate` |
| `vss:dirty_columns` | S2C | `varInt count (≤10240)`, then `count × long packedPos` |

Section bytes are raw NMS `LevelChunkSection.write()` output. **No compression** — a 25-byte per-column overhead is reserved (`VSSConstants.ESTIMATED_COLUMN_OVERHEAD_BYTES`). This is the single biggest perf delta vs. e.g. zstd/LZ4 on full 16³ palettes.

Despite the accessor name `decompressedSections()` on `VoxelColumnS2CPayload`, the bytes are not compressed — they're pre-decoded by reference to NMS palette codecs.

## 2. Threading model (Paper variant)

```
Netty thread (plugin message receive) ──► main thread (Bukkit dispatches)
                                              │
                                              ▼
   onPluginMessageReceived → service.handleBatchRequest
                                              │
                                  ConcurrentLinkedQueue ◄── per-player AbstractPlayerRequestState
                                              │
                                              ▼
   BukkitRunnable.runTaskTimer(1L,1L) ── service.tick()  [every server tick, MAIN THREAD]
       │
       ├─ snapshot players
       ├─ probe up to 512 chunks/player via level.getChunkSource().getChunkNow(...)  ← MAIN-THREAD CHUNK ACCESS
       ├─ post TickSnapshot to off-thread processor
       ├─ drain ready payloads → flushSendQueue → nmsPlayer.connection.send(...)
       └─ (every dirtyBroadcastIntervalSeconds * 20) PaperDirtyColumnBroadcaster.tick()
                                              │
                                              ▼
   VSS Processing Thread [single, daemon]
       │
       ├─ IncomingRequestRouter.processIncomingRequests
       │     ├─ ColumnTimestampCache: equal-or-newer ts → UP_TO_DATE response
       │     ├─ probe-resolved chunk → serializeAndEnqueue
       │     └─ acquire RateLimiterSet semaphore → submit disk OR generation
       │
       └─ drainDiskResultsForAllPlayers → readyPayloads queue
                                              │
                                              ▼
   VSS Disk Reader pool [1..64 daemon threads, prio MIN]
       │
       ├─ chunkMap.read(ChunkPos)  ← async NMS region-file read
       ├─ NbtSectionSerializer.readAndSerializeSections
       └─ push SimpleReadResult onto per-player result queue
                                              │
                                              ▼
   Generation path (Paper):
   level.getWorld().getChunkAtAsync(cx, cz, true, false)
       └─.whenComplete(...) → Bukkit.getScheduler().runTask(plugin, ...)  ← MAIN THREAD AGAIN
                                              │
                                              ▼
   PaperSectionSerializer.serializeColumn  → ready payload
```

**Key invariants**:
- Tick driver is one `BukkitRunnable.runTaskTimer(1L, 1L)`. Single global tick.
- Disk reads ARE off the tick thread. Good.
- Generation completion bounces back to the main thread for serialization. Necessary because palette serialization touches `level.registryAccess()`.
- Send is from the main tick thread via `nmsPlayer.connection.send(...)`. Netty thread-safe.

## 3. Threading model (Fabric variant)

Same conceptual pipeline but driven by:
- `ServerTickEvents.END_SERVER_TICK` for the tick driver (instead of BukkitRunnable).
- `addTicketWithRadius` + main-thread `getChunkNow` poll for generation (`ChunkGenerationService.tick()`), instead of `getChunkAtAsync` callback.
- `ChunkMap.save` mixin for dirty tracking (instead of Paper's reflective Bukkit event listeners).

## 4. Bandwidth & rate limiting

Three layers:
- `SharedBandwidthLimiter` — one global token bucket sized to `bytesPerSecondLimitGlobal` (default 100 MB/s, max 1 GB/s). `getPerPlayerAllocation(activeCount) = availableTokens / activeCount` → equal share among active handshaken players.
- `PlayerBandwidthTracker` — per-player token bucket, burst = allocation/4. Refilled per `canSend` call.
- Final per-tick allocation = `min(perPlayerAllocation, bytesPerSecondLimitPerPlayer, max(1, clientDesiredRate))`. The client signals desired rate via `BandwidthUpdateC2SPayload`.

`RateLimiterSet` / `ConcurrencyLimiter` are per-player **concurrency semaphores**, not throughput. Two of them: `syncOnLoad` (cap 200) + `generation` (cap 16). `ConcurrencyLimiter` is intentionally not thread-safe — only the single off-thread processor touches it.

`SendActionBatcher` coalesces same-tick `RateLimited`/`UpToDate`/`NotGenerated` actions into a single `BatchResponseS2CPayload`.

## 5. Dirty-column tracking

- **`DirtyColumnTracker`** (common) — `synchronized` map `dimension → LongOpenHashSet`. `markDirty` is synchronized; `drainDirty` returns array + clears.
- **Fabric**: `ChunkMapSaveHook` mixin injects at RETURN of `ChunkMap.save(ChunkAccess)`, marks dirty only on a `true` return (vanilla actually wrote the chunk). Tied to save events, not mutations.
- **Paper**: `PaperWorldHandler` registers Bukkit event listeners reflectively against a hard-coded list (`PaperConfig.updateEvents`): `BlockPlace, BlockBreak, BlockExplode, BlockPistonExtend, BlockPistonRetract, StructureGrow, ChunkPopulate`. Misses NMS-driven changes that don't fire Bukkit events (worldgen post-population edits, tickable block setBlockState bypassing Bukkit, custom plugin direct `setBlock`-no-physics).
- **Broadcast cadence**: every `dirtyBroadcastIntervalSeconds * 20` ticks (default 10s), iterate online players, filter by Chebyshev distance to dirty positions (≤ player's lodDistance), ship `DirtyColumnsS2CPayload`. Also invalidates per-player `diskReadDone` and the `ColumnTimestampCache`.

## 6. ColumnTimestampCache

Per-dimension LRU `Long2LongOpenHashMap`. Saves to `<world>/data/vss-timestamps.bin` every ~5 min via a single `SAVE_EXECUTOR` thread, plus on shutdown. On load, `insertionTimes` are reset to `epochSeconds()` so eviction-LRU restarts (fresh from a restart's perspective). Capacity per dimension: `perDimensionTimestampCacheSizeMB * 65 536` entries.

## 7. Server-side platform abstraction

`common/dev/xantha/vss/common/processing/` is the platform-agnostic core:

| Class | Role |
|---|---|
| `OffThreadProcessor<PS, RR>` | abstract, single-threaded daemon driving the request lifecycle. 5 abstract hooks. |
| `IncomingRequestRouter<PS>` | pure-functional; takes 2 functional submitters (disk, generation) injected by platform. |
| `AbstractChunkDiskReader<R>` | thread-pool plumbing + result queue. Subclass: `PaperChunkDiskReader`, `ChunkDiskReader`. |
| `AbstractPlayerRequestState<Q>` | per-player queues + rate limiters + send queue. Subclass: `PaperPlayerRequestState`, `PlayerRequestState`. |
| `RateLimiterSet`, `ConcurrencyLimiter` | per-player concurrency. |
| `DedupTracker` | suppresses redundant in-flight requests for the same column. |
| `SendActionBatcher` | batches no-data responses. |
| `ColumnTimestampCache` | per-dimension TS LRU. |
| `DirtyColumnTracker` | thread-safe dirty set. |
| `SharedBandwidthLimiter` | global+per-player token bucket. |

Implementation gap: there is no shared `Platform` / `Scheduler` interface. Each platform implements `OffThreadProcessor`, `AbstractChunkDiskReader`, `AbstractPlayerRequestState` independently. A Folia port adds a fourth set of subclasses or a runtime branch inside `Paper*`.

## 8. Voxy client compat (zero compile-time dep)

`compat/VoxyCompat` uses `Class.forName` + `MethodHandles` to look up:

```
me.cortex.voxy.commonImpl.WorldIdentifier.of(Level)
me.cortex.voxy.common.world.service.VoxelIngestService.rawIngest(WorldIdentifier, LevelChunkSection, int, int, int, DataLayer, DataLayer)
```

If Voxy is absent, `init()` returns false + warns; VSS continues to run. All ingest happens through the public `VSSApi.registerColumnConsumer(...)` SAM API, so a third-party LOD consumer (e.g. a future custom client) can register directly.

## 9. Mixins (fabric only)

| Mixin | Target | Purpose |
|---|---|---|
| `AccessorChunkMap` | `ChunkMap` | `@Accessor ServerLevel getLevel()` (package-private field accessor) |
| `AccessorServerChunkCache` | `ServerChunkCache` | `@Accessor ChunkMap getChunkMap()` (currently unused — likely dead code) |
| `ChunkMapSaveHook` | `ChunkMap.save(ChunkAccess)` | `@Inject(at = @At("RETURN"))` — marks dirty when save returned `true`. |
| `IntegratedServerLanHook` | `IntegratedServer.publishServer` | `@Inject(at = @At("RETURN"))` — calls `VSSServerNetworking.startServiceForLan(...)` so "Open to LAN" works. |

## 10. Folia-readiness verdict

**False — does not even load on Folia.**

1. `BukkitRunnable.runTaskTimer(this, 1L, 1L)` (`VSSPaperPlugin:57`) and `Bukkit.getScheduler().runTask(plugin, ...)` (`PaperChunkGenerationService:166`) target the legacy global scheduler, which Folia rejects with `UnsupportedOperationException`.
2. Hot-path access from the global tick thread to player + chunk state: `state.getPlayer().level()`, `player.getBlockX/Z()`, `level.getChunkSource().getChunkNow(...)`, `nmsPlayer.connection.send(...)`. On Folia these are owned by the player's region thread / chunk's region thread.
3. `plugin.yml` declares **no** `folia-supported: true` — verified zero matches across all decompiled source. Folia refuses the plugin at load.

See `IMPROVEMENT_AXES.md` for the port plan.
