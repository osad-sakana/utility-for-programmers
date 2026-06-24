package osadsakana.utilitiesforprogrammers.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;
import osadsakana.utilitiesforprogrammers.UtilitiesForProgrammers;

/**
 * Key bindings registered by the mod.
 *
 * <p>Defaults are chosen from keys that are <b>unbound in vanilla 1.21.10</b> so
 * they never conflict with normal Minecraft controls: H (HUD), G (grid),
 * L (highLight), O (on-top), K (freeze / external-operation mode). All can be
 * reassigned in Options &gt; Controls.
 *
 * <p>In 1.21.10 a key binding category is a {@link KeyMapping.Category} object
 * (no longer a translation-key string); it is registered via
 * {@code RegisterKeyMappingsEvent#registerCategory}.
 */
public final class KeyBindings {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            ResourceLocation.fromNamespaceAndPath(UtilitiesForProgrammers.MOD_ID, "main"));

    public static final KeyMapping TOGGLE_ALWAYS_ON_TOP = make(
            "key.utilitiesforprogrammers.toggle_always_on_top", GLFW.GLFW_KEY_O);

    public static final KeyMapping TOGGLE_FREEZE = make(
            "key.utilitiesforprogrammers.toggle_freeze", GLFW.GLFW_KEY_K);

    public static final KeyMapping TOGGLE_HUD = make(
            "key.utilitiesforprogrammers.toggle_hud", GLFW.GLFW_KEY_H);

    public static final KeyMapping TOGGLE_GRID = make(
            "key.utilitiesforprogrammers.toggle_grid", GLFW.GLFW_KEY_G);

    public static final KeyMapping TOGGLE_HIGHLIGHT = make(
            "key.utilitiesforprogrammers.toggle_highlight", GLFW.GLFW_KEY_L);

    private static KeyMapping make(String descriptionId, int defaultKey) {
        return new KeyMapping(
                descriptionId,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                defaultKey,
                CATEGORY);
    }

    public static KeyMapping[] all() {
        return new KeyMapping[] {
                TOGGLE_ALWAYS_ON_TOP,
                TOGGLE_FREEZE,
                TOGGLE_HUD,
                TOGGLE_GRID,
                TOGGLE_HIGHLIGHT,
        };
    }

    private KeyBindings() {
    }
}
