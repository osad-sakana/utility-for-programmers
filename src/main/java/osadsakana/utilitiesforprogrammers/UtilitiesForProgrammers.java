package osadsakana.utilitiesforprogrammers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import osadsakana.utilitiesforprogrammers.client.ClientEvents;
import osadsakana.utilitiesforprogrammers.client.render.HighlightRenderer;

/**
 * Entry point of the UtilitiesForProgrammers mod.
 *
 * <p>This mod is client-side only ({@link Dist#CLIENT}); it registers nothing on
 * the logical server and therefore can be used when joining vanilla or modded
 * servers that do not have it installed.
 */
@Mod(value = UtilitiesForProgrammers.MOD_ID, dist = Dist.CLIENT)
public class UtilitiesForProgrammers {

    public static final String MOD_ID = "utilitiesforprogrammers";

    public UtilitiesForProgrammers(IEventBus modEventBus, ModContainer modContainer) {
        // Persisted client configuration (config/utilitiesforprogrammers-client.toml).
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        // Mod-bus: key mappings and the HUD GUI layer.
        modEventBus.addListener(ClientEvents::onRegisterKeyMappings);
        modEventBus.addListener(ClientEvents::onRegisterGuiLayers);

        // Game-bus: per-tick key handling and HUD snapshot capture.
        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTickPost);
        // Game-bus: 3D block-update highlight rendering.
        NeoForge.EVENT_BUS.addListener(HighlightRenderer::onRenderLevelStage);
    }
}
