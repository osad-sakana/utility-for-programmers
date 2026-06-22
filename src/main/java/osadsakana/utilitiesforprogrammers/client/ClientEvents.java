package osadsakana.utilitiesforprogrammers.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import osadsakana.utilitiesforprogrammers.UtilitiesForProgrammers;
import osadsakana.utilitiesforprogrammers.client.hud.HudData;
import osadsakana.utilitiesforprogrammers.client.hud.HudOverlay;
import osadsakana.utilitiesforprogrammers.client.tracking.BlockChangeTracker;

/**
 * Central client-side event wiring: key-mapping/GUI-layer registration on the mod
 * bus, and per-tick handling (key presses + HUD snapshot capture) on the game bus.
 */
public final class ClientEvents {

    private static final ResourceLocation HUD_LAYER =
            ResourceLocation.fromNamespaceAndPath(UtilitiesForProgrammers.MOD_ID, "hud");

    private static boolean togglesInitialized = false;

    // ----- mod-bus registration -----------------------------------------------

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(KeyBindings.CATEGORY);
        for (KeyMapping mapping : KeyBindings.all()) {
            event.register(mapping);
        }
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(HUD_LAYER, new HudOverlay());
    }

    // ----- game-bus per-tick handling -----------------------------------------

    public static void onClientTickPost(ClientTickEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();

        if (!togglesInitialized) {
            ToggleState.initFromConfig();
            // Keep rendering when the window loses focus (don't auto-pause).
            WindowController.disablePauseOnLostFocus(mc);
            togglesInitialized = true;
        }

        handleKeys(mc);

        if (mc.level == null) {
            // Returned to the main menu / disconnected: drop stale highlights.
            BlockChangeTracker.clear();
            return;
        }

        // Refresh the HUD snapshot unless frozen (freezing holds the last values).
        if (mc.player != null && !ToggleState.isFrozen()) {
            HudData.set(HudData.capture(mc));
        }
    }

    private static void handleKeys(Minecraft mc) {
        while (KeyBindings.TOGGLE_HUD.consumeClick()) {
            feedback(mc, "HUD", ToggleState.toggleHud());
        }
        while (KeyBindings.TOGGLE_HIGHLIGHT.consumeClick()) {
            feedback(mc, "Block-update highlight", ToggleState.toggleHighlight());
        }
        while (KeyBindings.TOGGLE_GRID.consumeClick()) {
            feedback(mc, "Relative grid", ToggleState.toggleGrid());
        }
        while (KeyBindings.TOGGLE_ALWAYS_ON_TOP.consumeClick()) {
            feedback(mc, "Always-on-top", WindowController.toggleAlwaysOnTop(mc));
        }
        while (KeyBindings.TOGGLE_FREEZE.consumeClick()) {
            final boolean frozen = ToggleState.toggleFrozen();
            FreezeClock.setFrozen(frozen);
            feedback(mc, "Freeze", frozen);
        }
    }

    /** Show a short on/off confirmation above the hotbar. */
    static void feedback(Minecraft mc, String label, boolean enabled) {
        if (mc.player == null) {
            return;
        }
        final String state = enabled ? "ON" : "OFF";
        mc.player.displayClientMessage(
                Component.literal("[UFP] " + label + ": " + state), true);
    }

    private ClientEvents() {
    }
}
