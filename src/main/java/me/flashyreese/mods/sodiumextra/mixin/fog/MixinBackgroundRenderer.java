package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class MixinBackgroundRenderer {

    @Inject(
            method = "setupFog",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onFogRender(Lnet/minecraft/client/renderer/FogRenderer$FogMode;Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/client/Camera;FFFFLcom/mojang/blaze3d/shaders/FogShape;)V",
                    shift = At.Shift.BY,
                    by = 1
            )
    )
    private static void applyFogBase(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci,
                                     @Share("rbSetFog") LocalBooleanRef setFog,
                                     @Share("rbFogStart") LocalFloatRef fogStartRef,
                                     @Share("rbFogEnd") LocalFloatRef fogEndRef) {
        Entity entity = camera.getEntity();
        SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.putIfAbsent(entity.level().dimension().location(), 0);
        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl
                ? SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.get(entity.level().dimension().location())
                : SodiumExtraClientMod.options().renderSettings.fogDistance;
        if (fogDistance == 0) {
            return;
        }
        if (camera.getFluidInCamera() == FogType.NONE && (thickFog || fogType == FogRenderer.FogMode.FOG_TERRAIN)) {
            float fogStart = (float) SodiumExtraClientMod.options().renderSettings.fogStart / 100;
            if (fogDistance == 33) {
                RenderSystem.setShaderFogColor(1f, 1f, 1f, 0f);
            } else {
                setFog.set(true);
                fogStartRef.set(fogDistance * 16 * fogStart);
                fogEndRef.set((fogDistance + 1) * 16);
            }
        }
    }

    @ModifyArg(
            method = "setupFog",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onFogRender(Lnet/minecraft/client/renderer/FogRenderer$FogMode;Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/client/Camera;FFFFLcom/mojang/blaze3d/shaders/FogShape;)V"
            ),
            index = 5
    )
    private static float applyFogModCompatStart(float fogStart, @Share("rbSetFog") LocalBooleanRef setFog, @Share("rbFogStart") LocalFloatRef fogStartRef) {
        if (SodiumExtraClientMod.options().renderSettings.fogType == SodiumExtraGameOptions.RenderSettings.FogType.MOD_COMPAT && setFog.get()) {
            return fogStartRef.get();
        }
        return fogStart;
    }

    @ModifyArg(
            method = "setupFog",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onFogRender(Lnet/minecraft/client/renderer/FogRenderer$FogMode;Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/client/Camera;FFFFLcom/mojang/blaze3d/shaders/FogShape;)V"
            ),
            index = 6
    )
    private static float applyFogModCompatEnd(float fogEnd, @Share("rbSetFog") LocalBooleanRef setFog, @Share("rbFogEnd") LocalFloatRef fogEndRef) {
        if (SodiumExtraClientMod.options().renderSettings.fogType == SodiumExtraGameOptions.RenderSettings.FogType.MOD_COMPAT && setFog.get()) {
            return fogEndRef.get();
        }
        return fogEnd;
    }

    @Inject(method = "setupFog", at = @At(value = "TAIL"))
    private static void applyFogDefault(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci,
                                        @Share("rbSetFog") LocalBooleanRef setFog,
                                        @Share("rbFogStart") LocalFloatRef fogStartRef,
                                        @Share("rbFogEnd") LocalFloatRef fogEndRef) {
        if (SodiumExtraClientMod.options().renderSettings.fogType == SodiumExtraGameOptions.RenderSettings.FogType.DEFAULT && setFog.get()) {
            RenderSystem.setShaderFogStart(fogStartRef.get());
            RenderSystem.setShaderFogEnd(fogEndRef.get());
        }
    }
}