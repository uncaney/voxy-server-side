package dev.xantha.vss.paper;

import dev.xantha.vss.common.VSSLogger;
import dev.xantha.vss.common.tracking.DirtyColumnTracker;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/* JADX INFO: loaded from: voxy-server-side-paper-0.3.0.jar:dev/xantha/vss/paper/PaperWorldHandler.class */
public class PaperWorldHandler {
    private final Plugin plugin;
    private final DirtyColumnTracker dirtyTracker;
    private final Map<String, Method> eventMethods = new ConcurrentHashMap();
    private final Set<String> noMethodClasses = ConcurrentHashMap.newKeySet();
    private final Listener dummyListener = new Listener(this) { // from class: dev.xantha.vss.paper.PaperWorldHandler.1
        {
            Objects.requireNonNull(this);
        }
    };

    public PaperWorldHandler(Plugin plugin, DirtyColumnTracker dirtyTracker) {
        this.plugin = plugin;
        this.dirtyTracker = dirtyTracker;
    }

    public void registerUpdateListeners(List<String> eventClassNames) {
        for (String className : eventClassNames) {
            try {
                this.plugin.getServer().getPluginManager().registerEvent(Class.forName(className).asSubclass(Event.class), this.dummyListener, EventPriority.MONITOR, (listener, event) -> {
                    handleUpdateEvent(event);
                }, this.plugin, true);
                VSSLogger.info("Registered dirty chunk listener for " + className);
            } catch (ClassNotFoundException e) {
                VSSLogger.warn("Update event class not found: " + className);
            } catch (Exception e2) {
                VSSLogger.error("Failed to register update event: " + className, e2);
            }
        }
    }

    private void handleUpdateEvent(Event event) {
        String className = event.getClass().getName();
        if (this.noMethodClasses.contains(className)) {
            return;
        }
        Method method = this.eventMethods.get(className);
        if (method == null) {
            method = discoverMethod(event);
            if (method == null) {
                this.noMethodClasses.add(className);
                return;
            }
            this.eventMethods.put(className, method);
        }
        try {
            Object result = method.invoke(event, new Object[0]);
            if (result instanceof List) {
                List<?> list = (List) result;
                for (Object item : list) {
                    submitFromObject(item);
                }
            } else {
                submitFromObject(result);
            }
        } catch (Exception e) {
            VSSLogger.debug("Failed to extract position from " + className, e);
        }
    }

    private void submitFromObject(Object obj) {
        if (obj instanceof Block) {
            Block block = (Block) obj;
            this.dirtyTracker.markDirty(block.getWorld().getKey().toString(), block.getX() >> 4, block.getZ() >> 4);
            return;
        }
        if (obj instanceof BlockState) {
            BlockState state = (BlockState) obj;
            this.dirtyTracker.markDirty(state.getWorld().getKey().toString(), state.getX() >> 4, state.getZ() >> 4);
        } else {
            if (obj instanceof Location) {
                Location loc = (Location) obj;
                if (loc.getWorld() != null) {
                    this.dirtyTracker.markDirty(loc.getWorld().getKey().toString(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
                    return;
                }
                return;
            }
            if (obj instanceof Chunk) {
                Chunk chunk = (Chunk) obj;
                this.dirtyTracker.markDirty(chunk.getWorld().getKey().toString(), chunk.getX(), chunk.getZ());
            }
        }
    }

    private static Method discoverMethod(Event event) {
        Class<?> clazz = event.getClass();
        for (String name : new String[]{"blockList", "getBlocks", "getBlock", "getLocation", "getChunk"}) {
            try {
                Method m = clazz.getMethod(name, new Class[0]);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
            }
        }
        VSSLogger.debug("No position method found on event class " + clazz.getName());
        return null;
    }
}
