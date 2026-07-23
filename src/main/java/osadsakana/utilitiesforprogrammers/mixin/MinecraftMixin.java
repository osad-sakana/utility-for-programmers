package osadsakana.utilitiesforprogrammers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Suppresses the player's click actions (attack, use, pick) while the
 * external-operation (freeze) mode is active.
 *
 * <p>Freeze mode releases the cursor so other desktop windows can be operated;
 * cancelling these handlers at {@code HEAD} guarantees a stray click on the
 * Minecraft window never leaks a block break, place, attack or pick into the
 * game, complementing the cursor release done by {@code MouseHandlerMixin}.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void ufp$blockStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (ToggleState.isFrozen()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void ufp$blockContinueAttack(boolean leftClick, CallbackInfo ci) {
        if (ToggleState.isFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void ufp$blockStartUseItem(CallbackInfo ci) {
        if (ToggleState.isFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    private void ufp$blockPickBlock(CallbackInfo ci) {
        if (ToggleState.isFrozen()) {
            ci.cancel();
        }
    }
}
