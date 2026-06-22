package osadsakana.utilitiesforprogrammers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

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
        // Feature wiring is added in subsequent commits.
    }
}
