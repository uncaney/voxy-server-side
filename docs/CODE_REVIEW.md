# Code Review — VoX/lod-server-support 0.3.0

Generated 2026-05-04 by 4 parallel deep-read agents on the cloned upstream tree under `work/`. **All file:line citations refer to `work/...`.**

Every finding has: **severity** (P0=correctness, P1=stability/perf, P2=cleanup), location, and proposed fix. **Status** flag tracks whether fixed in this fork (`[ ]` unfixed, `[x]` fixed, `[u]` upstream-only / out of scope).

## Common module — `work/common/src/main/java/dev/vox/lss/common/`

### P0 — Correctness

- [ ] **C1.** `OffThreadProcessor.SAVE_EXECUTOR` is static, never shut down. On JVM exit may race with synchronous shutdown save against same `lss-timestamps.bin` (different snapshot ATOMIC_MOVEs racing on same target). [`processing/OffThreadProcessor.java:62-66, 213-217, 440-456`]. **Fix:** instance-field executor; drain + awaitTermination *before* synchronous final save.
- [ ] **C2.** `ColumnTimestampCache.load` resets all `insertionTimes` to `now` → defeats LRU eviction post-restart. [`voxel/ColumnTimestampCache.java:170, 189-197`]. **Fix:** persist `insertionTimes` alongside `timestamps`; bump `FORMAT_VERSION` to 2 with v1 fallback.
- [ ] **C3.** `ColumnTimestampCache.load` `in.skipBytes(entryCount * 16)` 32-bit overflow on malformed file. [`voxel/ColumnTimestampCache.java:186`]. **Fix:** loop with `(long) entryCount * 16L`.
- [ ] **C4.** `OffThreadProcessor.processingLoop` swallows `InterruptedException` and returns without final cleanup → leaks pending limiter slots and never delivers final responses. [`processing/OffThreadProcessor.java:181-191`]. **Fix:** set `interrupted` flag, break, run final cleanup pass; restore interrupt status.
- [ ] **C5.** `snapshotLock.wait(timeout)` in `OffThreadProcessor` races with `shutdown()` interrupt — sentinel may be lost. [`processing/OffThreadProcessor.java:168-177`]. **Fix:** in catch, recheck `pendingSnapshot`; have `shutdown()` post sentinel before `interrupt()`.
- [ ] **C6.** `pendingByPosition.put` overwrite path leaks one waiting-queue slot if old request was queued. [`processing/AbstractPlayerRequestState.java:169-178`]. **Fix:** detect and release rate-limiter slot for replaced; assert in debug.
- [ ] **C7.** `DedupTracker.removePlayer` of *primary* leaves attached players' pending requests orphaned forever. [`processing/DedupTracker.java:61-76` + `processing/OffThreadProcessor.java:266-278`]. **Fix:** on primary disconnect, promote one of the attached players to primary.
- [ ] **C8.** `IncomingRequestRouter.resolvedFromTimestamp` uses `0` as both "absent" and "epoch" and "generation marker" — sentinel collision. [`processing/IncomingRequestRouter.java:222-225`]. **Fix:** explicit `containsKey` check; typed enum for generation marker.
- [ ] **C9.** `evictIfOversized` tie-break is strict-less-than on equal insertion times → eviction picks undefined victim. [`voxel/ColumnTimestampCache.java:99-106`]. **Fix:** monotonic counter for insertion times instead of wall clock.
- [ ] **C10.** `PlayerBandwidthTracker.canSend` burst cap = `allocationBytes/4`; with sub-4-byte allocation tokens stuck at 0. [`processing/PlayerBandwidthTracker.java:19-33`]. **Fix:** floor burstCap to packet size; pre-init availableTokens.
- [ ] **C11.** `ColumnTimestampCache.load` non-atomic mid-dimension corruption — partial load persists on next save. [`voxel/ColumnTimestampCache.java:166-204`]. **Fix:** load into temp map, swap atomically.
- [ ] **C12.** `DirtyColumnTracker.drainDirty` returns `null` for empty set, leaves entry in map. [`tracking/DirtyColumnTracker.java:22-28`]. **Fix:** remove entry on drain; return `long[]` not null.

### P1 — Stability / perf

- [ ] **C13.** `pollDiskResult` drains all results for one player before next; player A starves player B. [`processing/OffThreadProcessor.java:288-326`]. **Fix:** cap drained per player per tick.
- [ ] **C14.** `tryConcurrencyOrEnqueue` queue cap = `syncRate + genRate` (rate units used as concurrency). [`processing/IncomingRequestRouter.java:181-184`]. **Fix:** explicit `waitingQueueCapacity` config.
- [ ] **C15.** `ConcurrentLinkedQueue.size()` O(n) used in `getDiagnostics`. [`processing/AbstractChunkDiskReader.java:67-73`]. **Fix:** AtomicInteger size counter.
- [ ] **C16.** `SendActionBatcher.PlayerBatch.forEach` aliases internal arrays — caller must respect `count`. Footgun. [`processing/SendActionBatcher.java:35-58`]. **Fix:** copyOf or document hard.
- [ ] **C17.** `PriorityQueue` for sendQueue boxes per add/poll; `MAX_SEND_QUEUE_SIZE = 100k` allows pathological. [`processing/AbstractPlayerRequestState.java:43, 101-107`]. **Fix:** ArrayDeque if monotonic; bulk-add heapify.
- [ ] **C18.** `SharedBandwidthLimiter.totalBytesSent` non-volatile but read from diagnostics threads. [`SharedBandwidthLimiter.java:11-47`]. **Fix:** make `volatile`.
- [ ] **C19.** Per-type pending counter pair (`sync`/`gen`) inconsistent transiently when type changes. [`processing/AbstractPlayerRequestState.java:48, 53-54`]. **Fix:** packed AtomicLong or document.
- [ ] **C20.** `evictOldest` allocates 2 × `long[count]` and is O(k×N). [`voxel/ColumnTimestampCache.java:85-115`]. **Fix:** min-heap of size `count`.
- [ ] **C21.** `DiskReaderDiagnostics` 6 AtomicLongs share cacheline → false sharing. [`processing/DiskReaderDiagnostics.java:8-13`]. **Fix:** LongAdder for hot counters.
- [ ] **C22.** `snapshotForSave` deep-copies the entire timestamp map every 5 min; up to 384 MB transient at max config. [`voxel/ColumnTimestampCache.java:210-221`]. **Fix:** stream-write under brief read-lock; double-buffer.
- [ ] **C23.** `AbstractChunkDiskReader.shutdown()` calls `shutdownNow()` and clears playerResults — pending results lost without sending RateLimited. [`processing/AbstractChunkDiskReader.java:77-88`]. **Fix:** graceful shutdown then forced.
- [ ] **C24.** `droppedGenerationReady` queue is unbounded → OOM on stuck processing thread. [`processing/OffThreadProcessor.java:41, 95-105`]. **Fix:** cap + drop oldest with warn.

### P2 — Cleanup

- [u] **C25.** `epochSeconds()` uses wall clock; NTP jumps affect freshness comparisons. **Fix:** internal monotonic counter. (Mostly subsumed by C9.)
- [ ] **C26.** `markDiskReadDone` on empty-column responses suppresses re-check until dirty broadcast. [`processing/IncomingRequestRouter.java:198-214`]. **Fix:** clear `diskReadDone` on dirty mark.
- [ ] **C27.** `dimensionStringToOrdinal` fallback to -1 silently maps unknown dims. [`LSSConstants.java:78, 91-98`]. **Fix:** explicit string fallback enforced in wire format.
- [ ] **C28.** `DirtyColumnTracker` uses one global `synchronized` lock across all dimensions. [`tracking/DirtyColumnTracker.java:14-29`]. **Fix:** ConcurrentHashMap + per-dim sync.
- [u] **C29.** `cycleNow` recorded once per cycle; same-cycle puts use start-of-cycle time. [`processing/OffThreadProcessor.java:60, 195`]. **Fix:** see C9.
- [ ] **C30.** `ProcessingDiagnostics` 17 volatile fields per cycle → ~3-5× cost vs plain. [`processing/ProcessingDiagnostics.java:11-30`]. **Fix:** plain ints + single release fence.

## Fabric server / networking / mixin — `work/fabric/src/main/java/dev/vox/lss/`

### P0 — Correctness

- [ ] **F1.** `IntegratedServerLanHook` has no symmetric "LAN closed" cleanup. [`mixin/IntegratedServerLanHook.java:13-19`]. **Fix:** document idempotency; defensive null-check on `server.isStopped()`.
- [ ] **F2.** `ChunkMapSaveHook` skips final shutdown saves because `requestService=null` is set in `SERVER_STOPPING` *before* the level chunk save flush. [`mixin/ChunkMapSaveHook.java:18-30` + `LSSServerNetworking.java:116-123`]. **Fix:** keep `DirtyColumnTracker` as static singleton independent of service lifecycle, or move null-set to `SERVER_STOPPED`.
- [ ] **F3.** `ChunkDiskReader` reads `level.registryAccess()` from off-thread — relies on registry being fully loaded; partial during shutdown. [`networking/server/ChunkDiskReader.java:42-49`, `NbtSectionSerializer.java:50-55`]. **Fix:** reject new disk reads after `SERVER_STOPPING`.
- [ ] **F4.** `submitReadDirect` calls accessor mixin from off-thread; relies on MC implementation detail. [`networking/server/ChunkDiskReader.java:43`]. **Fix:** hoist `chunkMap` extraction to main-thread snapshot building.
- [ ] **F5.** `BatchChunkRequestC2SPayload` codec doesn't pre-check buffer has enough bytes for declared count → IOOBE on truncated packet. [`networking/payloads/BatchChunkRequestC2SPayload.java:30-41`]. **Fix:** validate `buf.readableBytes() >= count * 17` before reading rows.
- [ ] **F6.** `DirtyColumnsS2CPayload` decoder silently truncates on count > MAX_POSITIONS — encode/decode asymmetry. [`networking/payloads/DirtyColumnsS2CPayload.java:25-39`]. **Fix:** encoder asserts; decoder rejects.
- [ ] **F7.** `VoxelColumnS2CPayload` uses identity comparison `dim == Level.OVERWORLD`. [`networking/payloads/VoxelColumnS2CPayload.java:67-99`]. **Fix:** `.equals()`.
- [ ] **F8.** `removePlayer` from disconnect listener races with off-thread cycle: dedup groups where disconnected player is primary are never released; attached players stuck. [`networking/server/RequestProcessingService.java:100-108, 148-150`, `LSSServerNetworking.java:137`]. **Fix:** queue removal for next tick instead of synchronous remove.
- [ ] **F9.** `LSSServerCommands` permission check uses Fabric API `Permissions.COMMANDS_GAMEMASTER` — verify behavior on integrated/LAN. [`networking/server/LSSServerCommands.java:9, 18`]. **Fix:** document op-level requirement.
- [ ] **F10.** `IntegratedServerLanHook` is sole entry on integrated server; mixin failure → silent LSS absence on LAN. **Fix:** see F1.
- [ ] **F11.** `dimensionStringCache` in `RequestProcessingService` strong-refs `ServerLevel`, never pruned → memory leak on dynamic dim unload. [`networking/server/RequestProcessingService.java:50, 225-226`]. **Fix:** `WeakHashMap` or invalidate on `ServerWorldEvents.UNLOAD`.
- [ ] **F12.** `ChunkMapSaveHook.lss$cachedDimension` non-volatile but technically multi-thread-touchable in defensive scenarios. [`mixin/ChunkMapSaveHook.java:16, 23-26`]. **Fix:** mark `volatile`.

### P1 — Stability

- [ ] **F13.** Pool saturation on shutdown emits `RateLimited` to client instead of silent drop. [`networking/server/ChunkDiskReader.java:56-62`]. **Fix:** check `isShutdown()` first.
- [ ] **F14.** `VoxelColumnS2CPayload.MAX_SECTIONS_SIZE = 2 MiB` may bite on Cubic Chunks-style modded dims. [`networking/payloads/VoxelColumnS2CPayload.java:24-25, 101`]. **Fix:** document; consider 8 MiB.
- [ ] **F15.** `tick()` mutates `players` map mid-iteration (post-iter, so safe) — undocumented invariant. [`networking/server/RequestProcessingService.java:148-150`]. **Fix:** comment.
- [ ] **F16.** `ConcurrencyLimiter.release()` silently floors at 0 — hides bugs. [`common/.../ConcurrencyLimiter.java:11`]. **Fix:** debug-only assertion.
- [ ] **F17.** `pendingByPosition.put` replacement decrements counter but not rate-limiter slot. [`common/.../AbstractPlayerRequestState.java:169-178`]. (Same as C6.)
- [ ] **F18.** `ChunkGenerationService.LSS_GEN_TICKET = new TicketType(0, 2)` — magic flag literal; needs review per Folia-target MC. [`networking/server/ChunkGenerationService.java:101-122`]. **Fix:** document flag bits; consider registering ticket type properly.
- [ ] **F19.** `ChunkGenerationService.submitGeneration` piggyback path bypasses `maxPerPlayerActive` cap. [`networking/server/ChunkGenerationService.java:67-94`]. **Fix:** apply per-player cap before piggyback.
- [ ] **F20.** `dimensionLevelMap` in `FabricOffThreadProcessor` never prunes stale levels. [`networking/server/FabricOffThreadProcessor.java:24-43`]. **Fix:** weak refs or `forgetDimension` on world unload.
- [ ] **F21.** `LSSServerNetworking` handshake handler sends SessionConfig before checking protocol — confusing client when version mismatches. [`networking/server/LSSServerNetworking.java:42-73`]. **Fix:** check protocol first; emit `protocolVersion=0` SessionConfig on mismatch.
- [ ] **F22.** `flushSendQueue` clears entire queue on single send error — wipes hundreds of pending payloads on transient netty backpressure. [`networking/server/RequestProcessingService.java:374-395`]. **Fix:** drop only the failing payload.
- [ ] **F23.** `BatchResponseS2CPayload` encoder doesn't enforce `MAX_BATCH_RESPONSES = 4096` bound — server could write oversized count that decoder rejects. [`networking/payloads/BatchResponseS2CPayload.java:9-13, 20-25`]. **Fix:** assert in encoder; split in `SendActionBatcher`.

### P2 — Cleanup

- [ ] **F24.** `JsonConfig.load` rewrites file on every startup — strips comments. [`config/JsonConfig.java:43-47`]. **Fix:** only rewrite if `validate()` changed something.
- [ ] **F25.** Dimension stringification scattered in 3 places (broadcaster, request service, mixin). [`networking/server/DirtyColumnBroadcaster.java:46-48`]. **Fix:** centralize in helper.
- [u] **F26.** `BenchmarkHook` halts JVM on disconnect — documented behavior, not a bug.

## Paper module + Folia readiness — `work/paper/src/main/java/dev/vox/lss/paper/`

### P0 — Folia/26.1.2 blockers

- [ ] **P0-1.** `LSSPaperPlugin.java:57-65` — `BukkitRunnable.runTaskTimer` will throw `UnsupportedOperationException` on Folia. **Fix:** `PlatformDispatch.runRepeating(...)` helper that branches Folia/Paper.
- [ ] **P0-2.** `PaperChunkGenerationService.java:111` — `Bukkit.getScheduler().runTask` after `getChunkAtAsync` whenComplete. **Fix:** `runOnRegion(world, cx, cz, ...)` on Folia.
- [ ] **P0-3.** `PaperRequestProcessingService.tick()` reads `state.getPlayer().isRemoved()`, `player.level()`, `player.getBlockX()` from global tick — region-owned on Folia. **Fix:** cache region-owned state on `PaperPlayerRequestState` via per-player region tasks.
- [ ] **P0-4.** `probeLoadedChunks` calls `level.getChunkSource().getChunkNow(...)` from global tick — chunk-region-only on Folia. [`PaperRequestProcessingService.java:298-321`]. **Fix:** per-chunk fan-out via `runOnRegion`.
- [ ] **P0-5.** `PaperDirtyColumnBroadcaster.tick` reads player position from global tick. [`PaperDirtyColumnBroadcaster.java:38-90`]. **Fix:** per-entity hop.
- [ ] **P0-6.** `PaperSectionSerializer.java:64` — `section.write(buf, null, 0)` arity wrong for 26.1.2. **Fix:** `section.write(buf)`.
- [ ] **P0-7.** `PaperNbtSectionSerializer.java:88` — same arity. **Fix:** same.
- [ ] **P0-8.** `paper/build.gradle:14` — `paperweight.paperDevBundle('1.21.11-R0.1-SNAPSHOT')`. **Fix:** bump to 26.1.2.
- [ ] **P0-9.** `plugin.yml` — missing `folia-supported: true`. **Fix:** add (only after edits land).

### P1

- [ ] **P1-1.** `nmsPlayer.connection.send(...)` from non-region thread on Folia. [`PaperPayloadHandler.java:190, 213`]. **Fix:** wrap per-player send loops in `runOnEntity`.
- [ ] **P1-2.** `getPlayerList().getPlayer(uuid)` on global thread — touching returned `ServerPlayer` unsafe on Folia. [`PaperRequestProcessingService.java:209`]. **Fix:** use `Bukkit.getPlayer(uuid)` boolean form.
- [ ] **P1-3.** `drainGenerationTicketRequests` reads `player.level()` etc. from global. [`PaperRequestProcessingService.java:357-368`]. **Fix:** use cached `state.getCachedLevel()`.
- [ ] **P1-4.** Redundant casts in 26.1.2: `(ServerLevel) player.level()`, `(ServerChunkCache) level.getChunkSource()`. **Fix:** drop.

### P2

- [u] **P2-1.** Could centralize via `ChunkSaveEvent` instead of reflective listener list — but that increases dirty-broadcast tail latency. Keep current model.

## Test pyramid — `work/fabric/src/test/`

### Coverage gaps (all P0/P1 production classes)

- [ ] **T1.** ZERO unit tests for `OffThreadProcessor`, `IncomingRequestRouter`, `IncomingRequest`, `SendActionBatcher`, `AbstractPlayerRequestState`, `PlayerBandwidthTracker`, `DedupTracker`, `RateLimiterSet`, `AbstractChunkDiskReader`. Only `ConcurrencyLimiter` covered.
- [ ] **T2.** ZERO tests for `RequestProcessingService`, `ChunkGenerationService`, `ChunkDiskReader`, `NbtSectionSerializer`, `SectionSerializer`, `DirtyColumnBroadcaster` (only `RequestProcessingService` startup is smoke-tested).
- [ ] **T3.** No test for `ChunkGenerationService.generationTimeoutSeconds` enforcement.
- [ ] **T4.** No test for handshake protocol-version mismatch path.

### Suggested tests (priorities, with proposed-effort tags from analysis agent):
- **E10** `ChunkGenerationTimeoutGametest` (P0, 2h) — stuck generator → NOT_GENERATED + slot release.
- **E6** `DedupTrackerTwoPlayersTest` (P0, 2h) — 2 players same column → 1 disk read, both notified.
- **E4** `SharedBandwidthBoundaryTest` (P0, 30m).
- **E1** `HandshakeWrongVersionTest` (P1, 30m).
- **E2/E3** `BatchRequestOversizedTest` / `NegativeCountTest` (P1, 30m + 5m).
- **E5** `SharedBandwidthMidTickPlayerCountChangeTest` (P1, 30m).
- **E7** `RateLimiterSetFairnessTest` (P1, 2h).
- **E9** `DirtyBroadcasterHighChurnGametest` (P1, 2h).
- **E12** `FoliaSmokeHarness` (P2 — separate effort, 1-2 days).

## Severity totals (after dedup)

- **P0**: ~30 (12 common + 12 fabric + 9 paper-Folia/26.1.2 - some overlap with C-series F-series).
- **P1**: ~30.
- **P2**: ~10.
- **Test gaps**: ~20 production classes uncovered.

## Wire protocol invariants (must preserve)

Channel IDs `lss:*` namespace. Protocol v15. `FriendlyByteBuf` encoding (varInt LEB128, varLong LEB128, writeUtf = varInt char-length + UTF-8, writeByteArray = varInt byte-length + bytes). Position packing: `((long)cx << 32) | (cz & 0xFFFFFFFFL)`. Response type bytes: `0=RATE_LIMITED, 1=UP_TO_DATE, 2=NOT_GENERATED`. Dimension ordinals: `0=Overworld, 1=Nether, 2=End, -1=custom (then writeUtf 256)`. Caps: `MAX_BATCH_CHUNK_REQUESTS=1024, MAX_BATCH_RESPONSES=4096, MAX_DIRTY_COLUMNS=10240, MAX_SECTIONS_SIZE=2 MiB`. **Any change to these breaks all upstream / Xantha / our-fork clients on the wire.**
