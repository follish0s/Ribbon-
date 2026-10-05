package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V", ordinal = 0),
            method = "<init>", remap = false)
    private void onDefaultWindowHints(int hint, int value, Operation<Void> original) {
        if (Util.getPlatform() == Util.OS.OSX && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac) {
            original.call(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        }
        original.call(hint, value);
    }

    @Inject(at = @At(value = "RETURN"), method = "refreshFramebufferSize")
    private void afterUpdateFrameBufferSize(CallbackInfo ci) {
        if (Util.getPlatform() == Util.OS.OSX && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac) {
            framebufferWidth /= 2;
            framebufferHeight /= 2;
        }
    }
}