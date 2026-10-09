package com.example.ribbon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

    @Inject(method = "getBlockModel", at = @At("RETURN"), cancellable = true)
    private void ribbon$fallbackToMissingModel(BlockState state, CallbackInfoReturnable<BakedModel> cir) {
        if (cir.getReturnValue() == null) {
            BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
            if (missing != null) {
                cir.setReturnValue(missing);
            }
        }
    }
}