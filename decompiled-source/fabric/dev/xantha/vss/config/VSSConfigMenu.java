package dev.xantha.vss.config;

import dev.xantha.vss.common.VSSConstants;
import dev.xantha.vss.networking.client.VSSClientNetworking;
import java.util.Objects;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.option.Range;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/config/VSSConfigMenu.class */
public class VSSConfigMenu implements ConfigEntryPoint {
    public void registerConfigLate(ConfigBuilder builder) {
        VSSClientConfig cfg = VSSClientConfig.CONFIG;
        Objects.requireNonNull(cfg);
        StorageEventHandler save = cfg::save;
        String version = (String) FabricLoader.getInstance().getModContainer(VSSConstants.MOD_ID).map(c -> {
            return c.getMetadata().getVersion().getFriendlyString();
        }).orElse("unknown");
        ModOptionsBuilder mod = builder.registerModOptions(VSSConstants.MOD_ID, "Voxy Server Side", version).setIcon(Identifier.parse("vss:icon.png"));
        OptionPageBuilder page = builder.createOptionPage();
        page.setName(Component.translatable("vss.config.page"));
        OptionGroupBuilder receiveGroup = builder.createOptionGroup();
        BooleanOptionBuilder receiveOption = builder.createBooleanOption(Identifier.parse("vss:receive_server_lods"));
        receiveOption.setName(Component.translatable("vss.config.receive_server_lods"));
        receiveOption.setTooltip(Component.empty().append(Component.translatable("vss.config.receive_server_lods.tooltip")).append(Component.literal("\n\n§cNote: The server must match the client version and have VSS installed.")));
        receiveOption.setImpact(OptionImpact.HIGH);
        receiveOption.setDefaultValue(true);
        receiveOption.setBinding(v -> {
            cfg.receiveServerLods = v.booleanValue();
        }, () -> {
            return Boolean.valueOf(cfg.receiveServerLods);
        });
        receiveOption.setStorageHandler(save);
        receiveOption.setEnabledProvider(s_opt -> {
            return Boolean.valueOf(!VSSClientNetworking.versionMismatchFlag);
        }, new Identifier[0]);
        receiveGroup.addOption(receiveOption);
        page.addOptionGroup(receiveGroup);
        Identifier[] enabledDep = {Identifier.parse("vss:receive_server_lods")};
        OptionGroupBuilder distanceGroup = builder.createOptionGroup();
        IntegerOptionBuilder distanceOption = builder.createIntegerOption(Identifier.parse("vss:lod_distance"));
        distanceOption.setName(Component.translatable("vss.config.lod_distance"));
        distanceOption.setTooltip(Component.translatable("vss.config.lod_distance.tooltip"));
        distanceOption.setDefaultValue(0);
        distanceOption.setRange(new Range(0, VSSConstants.MAX_LOD_DISTANCE, 1));
        distanceOption.setValueFormatter(v2 -> {
            if (v2 == 0) {
                return Component.translatable("vss.config.lod_distance.server_default");
            }
            return Component.literal(Integer.toString(v2));
        });
        distanceOption.setBinding(v3 -> {
            cfg.lodDistanceChunks = v3.intValue();
        }, () -> {
            return Integer.valueOf(cfg.lodDistanceChunks);
        });
        distanceOption.setStorageHandler(save);
        distanceOption.setEnabledProvider(s -> {
            return Boolean.valueOf(s.readBooleanOption(enabledDep[0]));
        }, enabledDep);
        distanceGroup.addOption(distanceOption);
        page.addOptionGroup(distanceGroup);
        OptionGroupBuilder offThreadGroup = builder.createOptionGroup();
        BooleanOptionBuilder offThreadOption = builder.createBooleanOption(Identifier.parse("vss:off_thread_processing"));
        offThreadOption.setName(Component.translatable("vss.config.off_thread_processing"));
        offThreadOption.setTooltip(Component.translatable("vss.config.off_thread_processing.tooltip"));
        offThreadOption.setDefaultValue(true);
        offThreadOption.setBinding(v4 -> {
            cfg.offThreadSectionProcessing = v4.booleanValue();
        }, () -> {
            return Boolean.valueOf(cfg.offThreadSectionProcessing);
        });
        offThreadOption.setStorageHandler(save);
        offThreadOption.setEnabledProvider(s2 -> {
            return Boolean.valueOf(s2.readBooleanOption(enabledDep[0]));
        }, enabledDep);
        offThreadGroup.addOption(offThreadOption);
        page.addOptionGroup(offThreadGroup);
        mod.addPage(page);
    }
}
