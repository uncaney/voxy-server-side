package dev.xantha.vss.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.xantha.vss.common.VSSConstants;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;

/* JADX INFO: loaded from: voxy-server-side-fabric-0.3.0.jar:dev/xantha/vss/config/VSSModMenuIntegration.class */
public class VSSModMenuIntegration implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            try {
                ModOptions page = (ModOptions) ConfigManager.CONFIG.getModOptions().stream().filter(a -> {
                    return a.configId().equals(VSSConstants.MOD_ID);
                }).findFirst().orElse(null);
                if (page == null) {
                    return null;
                }
                return VideoSettingsScreen.createScreen(parent, (OptionPage) page.pages().get(0));
            } catch (Throwable th) {
                return null;
            }
        };
    }
}
