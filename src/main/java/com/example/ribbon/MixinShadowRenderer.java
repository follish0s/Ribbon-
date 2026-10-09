package com.example.ribbon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "net.coderbot.iris.pipeline.ShadowRenderer", remap = false)
public class MixinShadowRenderer {

    @ModifyVariable(
            method = "renderShadows",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 0
    )
    private Object ribbon$forceNonCullingFrustum(Object frustum) {
        try {
            Class<?> clazz = Class.forName("net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum");
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            return frustum;
        }
    }
}