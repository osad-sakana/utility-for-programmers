package osadsakana.utilitiesforprogrammers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import osadsakana.utilitiesforprogrammers.client.ClientEvents;
import osadsakana.utilitiesforprogrammers.client.KeyBindings;
import osadsakana.utilitiesforprogrammers.client.hud.FocusBorderOverlay;
import osadsakana.utilitiesforprogrammers.client.hud.HudOverlay;
import osadsakana.utilitiesforprogrammers.client.render.GridRenderer;
import osadsakana.utilitiesforprogrammers.client.render.HighlightRenderer;
import osadsakana.utilitiesforprogrammers.client.render.TargetHighlightRenderer;

/**
 * Entry point of the UtilitiesForProgrammers mod.
 *
 * <p>This mod is client-side only ({@code "environment": "client"} in
 * {@code fabric.mod.json}); it registers nothing on the logical server and
 * therefore can be used when joining vanilla or modded servers that do not have it
 * installed.
 */
public final class UtilitiesForProgrammers implements ClientModInitializer {

    public static final String MOD_ID = "utilitiesforprogrammers";

    private static final Identifier HUD_LAYER =
            Identifier.fromNamespaceAndPath(MOD_ID, "hud");
    private static final Identifier FOCUS_BORDER_LAYER =
            Identifier.fromNamespaceAndPath(MOD_ID, "focus_border");

    @Override
    public void onInitializeClient() {
        // Persisted client configuration (config/utilitiesforprogrammers-client.json).
        Config.load();

        for (KeyMapping mapping : KeyBindings.all()) {
            KeyMappingHelper.registerKeyMapping(mapping);
        }

        HudElementRegistry.addLast(HUD_LAYER, new HudOverlay());
        HudElementRegistry.addLast(FOCUS_BORDER_LAYER, new FocusBorderOverlay());

        // Per-tick key handling, HUD snapshot capture and config hot-reload polling.
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTickPost);

        // World-space geometry submission (block-update highlight, target highlight, grid).
        LevelRenderEvents.COLLECT_SUBMITS.register(HighlightRenderer::onCollectSubmits);
        LevelRenderEvents.COLLECT_SUBMITS.register(GridRenderer::onCollectSubmits);
        LevelRenderEvents.COLLECT_SUBMITS.register(TargetHighlightRenderer::onCollectSubmits);
    }
}
