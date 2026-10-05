package me.flashyreese.mods.sodiumextra.mixin.sun_moon;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinWorldRenderer {

    // 需要在 LevelRenderer.java 里搜 "textures/environment/sun.png" 确认字段名
    @Mutable
    @Shadow
    @Final
    private static ResourceLocation SUN_LOCATION;

    @Mutable
    @Shadow
    @Final
    private static ResourceLocation MOON_LOCATION;

    // Yarn: DimensionEffects.getFogColorOverride(FF)[F
    // Mojmap: DimensionSpecialEffects.getSunriseColor(FF)[F
    @WrapOperation(
            method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"
            )
    )
    public float[] redirectGetFogColorOverride(DimensionSpecialEffects instance, float skyAngle, float tickDelta, Operation<float[]> original) {
        if (SodiumExtraClientMod.options().detailSettings.sunMoon) {
            return original.call(instance, skyAngle, tickDelta);
        }
        return null;
    }

    @Inject(method = "allChanged()V", at = @At(value = "TAIL"))
    private void postWorldRendererReload(CallbackInfo ci) {
        if (SodiumExtraClientMod.options().detailSettings.sunMoon) {
            MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
            SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
        } else {
            MOON_LOCATION = new ResourceLocation("sodium-extra", "textures/transparent.png");
            SUN_LOCATION = new ResourceLocation("sodium-extra", "textures/transparent.png");
        }
    }
}