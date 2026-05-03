package dev.xantha.vss.networking.client;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.common.VSSLogger;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/networking/client/ColumnCacheStore.class */
public class ColumnCacheStore {
    private static final int FORMAT_VERSION = 3;
    private static final int MAX_CACHE_ENTRIES = 2000000;
    private static final Pattern SANITIZE_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");
    private static final Path CACHE_DIR = FabricLoader.getInstance().getConfigDir().resolve(VSSConstants.MOD_ID).resolve("cache");
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "VSS-CacheIO");
        t.setDaemon(true);
        return t;
    });

    public static Long2LongOpenHashMap load(String serverAddress, ResourceKey<Level> dimension) {
        DataInputStream in;
        int version;
        Long2LongOpenHashMap map = new Long2LongOpenHashMap();
        map.defaultReturnValue(-1L);
        Path file = getCacheFile(serverAddress, dimension);
        if (!Files.exists(file, new LinkOption[0])) {
            return map;
        }
        try {
            in = new DataInputStream(Files.newInputStream(file, new OpenOption[0]));
            try {
                version = in.readInt();
            } finally {
            }
        } catch (IOException e) {
            VSSLogger.warn("Failed to load column cache from " + String.valueOf(file), e);
        }
        if (version != FORMAT_VERSION && version != 2 && version != 1) {
            VSSLogger.warn("Column cache " + String.valueOf(file) + " has unsupported version " + version + ", discarding");
            in.close();
            return map;
        }
        int count = in.readInt();
        if (count < 0 || count > MAX_CACHE_ENTRIES) {
            VSSLogger.warn("Column cache " + String.valueOf(file) + " has invalid entry count " + count + ", discarding");
            in.close();
            return map;
        }
        map.ensureCapacity(count);
        for (int i = 0; i < count; i++) {
            long pos = in.readLong();
            long value = in.readLong();
            if (version == 2) {
                map.put(pos, value >> 8);
            } else {
                map.put(pos, value);
            }
        }
        String migration = version < FORMAT_VERSION ? " (migrated from v" + version + ")" : "";
        VSSLogger.info("Loaded " + count + " cached column entries for " + dimensionKey(dimension) + migration);
        in.close();
        return map;
    }

    public static CompletableFuture<Long2LongOpenHashMap> loadAsync(String serverAddress, ResourceKey<Level> dimension) {
        return CompletableFuture.supplyAsync(() -> {
            return load(serverAddress, dimension);
        }, IO_EXECUTOR);
    }

    public static void saveAsync(String serverAddress, ResourceKey<Level> dimension, Long2LongOpenHashMap columns) {
        if (columns.isEmpty()) {
            return;
        }
        Long2LongOpenHashMap copy = new Long2LongOpenHashMap(columns);
        copy.defaultReturnValue(-1L);
        IO_EXECUTOR.execute(() -> {
            save(serverAddress, dimension, copy);
        });
    }

    public static void save(String serverAddress, ResourceKey<Level> dimension, Long2LongOpenHashMap columns) {
        if (columns.isEmpty()) {
            return;
        }
        Path file = getCacheFile(serverAddress, dimension);
        Path tmpFile = file.resolveSibling(String.valueOf(file.getFileName()) + ".tmp");
        try {
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            DataOutputStream out = new DataOutputStream(Files.newOutputStream(tmpFile, new OpenOption[0]));
            try {
                out.writeInt(FORMAT_VERSION);
                out.writeInt(columns.size());
                ObjectIterator it = columns.long2LongEntrySet().iterator();
                while (it.hasNext()) {
                    Long2LongMap.Entry entry = (Long2LongMap.Entry) it.next();
                    out.writeLong(entry.getLongKey());
                    out.writeLong(entry.getLongValue());
                }
                out.close();
                Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                VSSLogger.info("Saved " + columns.size() + " cached column entries for " + dimensionKey(dimension));
            } finally {
            }
        } catch (IOException e) {
            VSSLogger.warn("Failed to save column cache to " + String.valueOf(file), e);
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException e2) {
                VSSLogger.warn("Failed to clean up temporary cache file " + String.valueOf(tmpFile), e2);
            }
        }
    }

    public static void clearForServer(String serverAddress) {
        Path dir = getServerDir(serverAddress);
        if (Files.exists(dir, new LinkOption[0])) {
            try {
                DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
                try {
                    for (Path file : stream) {
                        Files.deleteIfExists(file);
                    }
                    Files.deleteIfExists(dir);
                    VSSLogger.info("Cleared column cache for server " + serverAddress);
                    if (stream != null) {
                        stream.close();
                    }
                } finally {
                }
            } catch (IOException e) {
                VSSLogger.warn("Failed to clear column cache for " + serverAddress, e);
            }
        }
    }

    public static void clearAll() {
        if (Files.exists(CACHE_DIR, new LinkOption[0])) {
            try {
                DirectoryStream<Path> servers = Files.newDirectoryStream(CACHE_DIR);
                try {
                    for (Path serverDir : servers) {
                        if (Files.isDirectory(serverDir, new LinkOption[0])) {
                            DirectoryStream<Path> files = Files.newDirectoryStream(serverDir);
                            try {
                                for (Path file : files) {
                                    Files.deleteIfExists(file);
                                }
                                if (files != null) {
                                    files.close();
                                }
                                Files.deleteIfExists(serverDir);
                            } catch (Throwable th) {
                                if (files != null) {
                                    try {
                                        files.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                }
                                throw th;
                            }
                        }
                    }
                    VSSLogger.info("Cleared all column caches");
                    if (servers != null) {
                        servers.close();
                    }
                } finally {
                }
            } catch (IOException e) {
                VSSLogger.warn("Failed to clear all column caches", e);
            }
        }
    }

    private static Path getServerDir(String serverAddress) {
        return CACHE_DIR.resolve(sanitizeForFilePath(serverAddress));
    }

    private static Path getCacheFile(String serverAddress, ResourceKey<Level> dimension) {
        return getServerDir(serverAddress).resolve(dimensionKey(dimension) + ".bin");
    }

    private static String dimensionKey(ResourceKey<Level> dimension) {
        return sanitizeForFilePath(dimension.identifier().toString());
    }

    static String sanitizeForFilePath(String name) {
        return SANITIZE_PATTERN.matcher(name).replaceAll("_");
    }
}
