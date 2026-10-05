package me.jellysquid.mods.sodium.mixin.features.gui.hooks.debug;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugHudMixin {

    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void onGetGameInformation(CallbackInfoReturnable<List<String>> cir) {
        List<String> strings = cir.getReturnValue();
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer != null) {
            strings.addAll(renderer.getDebugStrings());
        }
    }
}