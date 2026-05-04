package dev.vox.lss.paper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia/Paper unified scheduler dispatch.
 *
 * <p>The {@link org.bukkit.scheduler.BukkitScheduler} legacy API ({@code Bukkit.getScheduler()},
 * {@link org.bukkit.scheduler.BukkitRunnable}) throws {@code UnsupportedOperationException} on
 * Folia and Folia-derived servers (Luminol, etc.). The Paper region/entity scheduler API
 * ({@code GlobalRegionScheduler}, {@code RegionScheduler}, {@code EntityScheduler}) is exposed
 * by both vanilla Paper and Folia: on vanilla Paper, every region scheduler routes to the
 * single main thread; on Folia, they route to the appropriate regionised thread. So using the
 * region/entity API everywhere produces a single jar that runs on both.
 *
 * <p>The only caller-visible difference is chunk-region affinity: on Folia, code on the global
 * region thread cannot legally call {@link net.minecraft.world.level.chunk.ChunkSource#getChunkNow}
 * for an arbitrary chunk — that chunk is owned by its own region. Use {@link #IS_FOLIA} to gate
 * the synchronous probe optimisation; the disk reader pool handles the fallback.
 */
public final class PlatformDispatch {
    private PlatformDispatch() {}

    /** True if running on Folia (or a Folia derivative such as Luminol). Detected once at class init. */
    public static final boolean IS_FOLIA = checkClass("io.papermc.paper.threadedregions.RegionizedServer");

    private static boolean checkClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Schedule a task to run every {@code period} ticks after an initial {@code delay} ticks
     * on the global region (the single main thread on vanilla Paper, the global region thread on Folia).
     */
    public static void runRepeating(Plugin plugin, Runnable task, long delay, long period) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
    }

    /**
     * Run a task once on the global region thread. Equivalent to legacy {@code Bukkit.getScheduler().runTask(...)}
     * but Folia-safe.
     */
    public static void runOnGlobal(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    /**
     * Run a task on the region thread that owns the given chunk in the given world.
     * On vanilla Paper this is the main thread; on Folia this is the chunk's region thread.
     * Use this whenever the task accesses MC chunk state, the chunk's block entities,
     * or the chunk's loaded entities.
     */
    public static void runOnRegion(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, scheduledTask -> task.run());
    }

    /**
     * Run a task on the region thread that currently owns the given entity (which may move
     * between regions on Folia). The {@code retired} runnable fires if the entity is removed
     * before the task runs (kicked, despawned, dimension-changed). Pass {@code null} if you
     * don't need a retired callback. Returns true if the task was scheduled, false if the
     * entity was already retired.
     */
    public static boolean runOnEntity(Plugin plugin, Entity entity, Runnable task, Runnable retired) {
        return entity.getScheduler().run(plugin, scheduledTask -> task.run(), retired) != null;
    }
}
