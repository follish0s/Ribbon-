package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Yarn WorldRenderer -> Mojmap LevelRenderer.
 * Method name mapping (Yarn -> Mojmap):
 *   tickRainSplashing(Camera)                                -> tickRain(Camera)
 *   renderWeather(LightmapTextureManager, F, D, D, D)        -> renderSnowAndRain(LightTexture, F, D, D, D)
 */
@Mixin(LevelRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "tickRain(Lnet/minecraft/client/Camera;)V", at = @At(value = "HEAD"), cancellable = true)
    public void tickRain(Camera camera, CallbackInfo callbackInfo) {
        if (!(SodiumExtraClientMod.options().particleSettings.particles && SodiumExtraClientMod.options().particleSettings.rainSplash)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderSnowAndRain(LightTexture manager, float f, double d, double e, double g, CallbackInfo callbackInfo) {
        if (!(SodiumExtraClientMod.options().detailSettings.rainSnow)) {
            callbackInfo.cancel();
        }
    }
}
