package dev.xantha.vss.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.xantha.vss.common.VSSLogger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/config/JsonConfig.class */
public abstract class JsonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected abstract String getFileName();

    protected void validate() {
    }

    public void save() {
        try {
            Path path = resolvePath();
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.writeString(path, GSON.toJson(this), new OpenOption[0]);
        } catch (Exception e) {
            VSSLogger.error("Failed to save config " + getFileName(), e);
        }
    }

    private Path resolvePath() {
        return FabricLoader.getInstance().getConfigDir().resolve(getFileName());
    }

    protected static <T extends JsonConfig> T load(Class<T> type, String fileName) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        boolean fileExists = Files.isRegularFile(path, new LinkOption[0]);
        if (fileExists) {
            try {
                String json = Files.readString(path);
                T t = (T) GSON.fromJson(json, type);
                if (t != null) {
                    t.validate();
                    t.save();
                    return t;
                }
                VSSLogger.warn("Config " + fileName + " was empty or invalid, using defaults");
            } catch (Exception e) {
                VSSLogger.error("Failed to read config " + fileName + ", using defaults", e);
            }
        }
        try {
            T config = type.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            if (!fileExists) {
                config.save();
            }
            return config;
        } catch (ReflectiveOperationException e2) {
            throw new RuntimeException("Cannot instantiate config " + type.getName(), e2);
        }
    }
}
