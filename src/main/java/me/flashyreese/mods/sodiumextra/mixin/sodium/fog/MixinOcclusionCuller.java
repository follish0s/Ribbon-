package me.flashyreese.mods.sodiumextra.mixin.sodium.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Verified against Embeddium 0.5.8
 * (D:\robbon\src\main\java\me\jellysquid\mods\sodium\client\render\chunk\occlusion\OcclusionCuller.java):
 *   private static boolean isWithinRenderDistance(CameraTransform, RenderSection, float)   [line 163]
 *   private static int nearestToZero(int, int)                                             [line 179]
 *   RenderSection.getOriginX()/getOriginZ(), CameraTransform.intX/fracX/intZ/fracZ all still exist.
 */
@Mixin(value = OcclusionCuller.class, remap = false)
public abstract class MixinOcclusionCuller {
    @Shadow
    private static int nearestToZero(int min, int max) {
        return 0;
    }

    @Inject(method = "isWithinRenderDistance", at = @At(value = "HEAD"), cancellable = true)
    private static void isWithinRenderDistance(CameraTransform camera, RenderSection section, float maxDistance, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();

        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl && client.level != null ?
                SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.getOrDefault(client.level.dimension().location(), 0) :
                SodiumExtraClientMod.options().renderSettings.fogDistance;

        if (fogDistance == 33) {
            int ox = section.getOriginX() - camera.intX;
            int oz = section.getOriginZ() - camera.intZ;
            float dx = nearestToZero(ox, ox + 16) - camera.fracX;
            float dz = nearestToZero(oz, oz + 16) - camera.fracZ;
            boolean result = ((dx * dx) + (dz * dz)) < (maxDistance * maxDistance);
            cir.setReturnValue(result);
        }
    }
}
