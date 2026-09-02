package osadsakana.utilitiesforprogrammers.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.hud.HudData;
import osadsakana.utilitiesforprogrammers.client.tracking.BlockChangeTracker;

/**
 * Central client-side per-tick handling: key presses, HUD snapshot capture and
 * config hot-reload polling. Registered on {@code ClientTickEvents.END_CLIENT_TICK}.
 */
public final class ClientEvents {

    /** Re-check the config file's modification time once a second (20 ticks). */
    private static final int CONFIG_POLL_INTERVAL_TICKS = 20;

    private static boolean togglesInitialized = false;
    private static int ticksSinceConfigPoll = 0;

    public static void onClientTickPost(Minecraft mc) {
        if (!togglesInitialized) {
            // Keep rendering when the window loses focus (don't auto-pause).
            WindowController.disablePauseOnLostFocus(mc);
            togglesInitialized = true;
        }

        if (++ticksSinceConfigPoll >= CONFIG_POLL_INTERVAL_TICKS) {
            ticksSinceConfigPoll = 0;
            Config.reloadIfChanged();
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
        while (KeyBindings.TOGGLE_ALL.consumeClick()) {
            final boolean on = ToggleState.toggleEnabled();
            // Pin/unpin the window alongside the rest of the mod's features.
            WindowController.applyAlwaysOnTop(mc, on);
            feedback(mc, "UtilitiesForProgrammers", on);
        }
        while (KeyBindings.TOGGLE_FREEZE.consumeClick()) {
            final boolean frozen = ToggleState.toggleFrozen();
            FreezeClock.setFrozen(frozen);
            // External-operation mode: release the cursor so other windows can be
            // operated (the MouseHandler mixin keeps it released, the KeyboardInput
            // mixin freezes movement). Re-grab on disable (flag already false).
            if (frozen) {
                mc.mouseHandler.releaseMouse();
            } else {
                mc.mouseHandler.grabMouse();
            }
            feedback(mc, "Freeze", frozen);
        }
    }

    /** Show a short on/off confirmation above the hotbar. */
    static void feedback(Minecraft mc, String label, boolean enabled) {
        if (mc.player == null) {
            return;
        }
        final String state = enabled ? "ON" : "OFF";
        mc.player.sendOverlayMessage(Component.literal("[UFP] " + label + ": " + state));
    }

    private ClientEvents() {
    }
}
