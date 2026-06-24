package osadsakana.utilitiesforprogrammers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MouseHandler;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Keeps the mouse cursor released while the external-operation (freeze) mode is
 * active.
 *
 * <p>Vanilla re-grabs the cursor whenever you click inside a focused window with
 * no screen open ({@code MouseHandler#grabMouse}). Cancelling {@code grabMouse}
 * while frozen lets the cursor stay free so other desktop windows can be
 * operated, even if the Minecraft window is clicked.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void ufp$keepMouseFree(CallbackInfo ci) {
        if (ToggleState.isFrozen()) {
            ci.cancel();
        }
    }
}
