package me.flashyreese.mods.sodiumextra.mixin.stars;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class MixinWorldRenderer {

    // Yarn: ClientWorld.method_23787(F)F  ==  getStarBrightness
    // Mojmap: ClientLevel.getStarBrightness(F)F
    @WrapOperation(
            method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F"
            )
    )
    public float redirectGetStarBrightness(ClientLevel instance, float f, Operation<Float> original) {
        if (SodiumExtraClientMod.options().detailSettings.stars) {
            return original.call(instance, f);
        }
        return 0.0f;
    }
}