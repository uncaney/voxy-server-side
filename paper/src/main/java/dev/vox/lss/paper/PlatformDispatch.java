package dev.vox.lss.paper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class PlatformDispatch {
    private PlatformDispatch() {}

    public static final boolean IS_FOLIA = checkClass("io.papermc.paper.threadedregions.RegionizedServer");

    private static boolean checkClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runRepeating(Plugin plugin, Runnable task, long delay, long period) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
    }

    public static void runOnGlobal(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    public static void runOnRegion(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, scheduledTask -> task.run());
    }

    public static boolean runOnEntity(Plugin plugin, Entity entity, Runnable task, Runnable retired) {
        return entity.getScheduler().run(plugin, scheduledTask -> task.run(), retired) != null;
    }
}
