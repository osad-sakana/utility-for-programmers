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
 * they never conflict with normal Minecraft controls: H toggles every feature of
 * the mod on/off, K toggles the freeze / external-operation mode. Both can be
 * reassigned in Options &gt; Controls.
 *
 * <p>In 1.21.10 a key binding category is a {@link KeyMapping.Category} object
 * (no longer a translation-key string); it is registered via
 * {@code RegisterKeyMappingsEvent#registerCategory}.
 */
public final class KeyBindings {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            ResourceLocation.fromNamespaceAndPath(UtilitiesForProgrammers.MOD_ID, "main"));

    /** Master switch: enables/disables all of the mod's features at once. */
    public static final KeyMapping TOGGLE_ALL = make(
            "key.utilitiesforprogrammers.toggle_all", GLFW.GLFW_KEY_H);

    public static final KeyMapping TOGGLE_FREEZE = make(
            "key.utilitiesforprogrammers.toggle_freeze", GLFW.GLFW_KEY_K);

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
                TOGGLE_ALL,
                TOGGLE_FREEZE,
        };
    }

    private KeyBindings() {
    }
}
