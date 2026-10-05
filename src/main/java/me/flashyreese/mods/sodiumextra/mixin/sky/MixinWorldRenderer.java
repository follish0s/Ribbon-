package me.flashyreese.mods.sodiumextra.mixin.sky;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinWorldRenderer {

    // Yarn: WorldRenderer.renderSky(MatrixStack, Matrix4f, F, Camera, Z, Runnable)
    // Mojmap: LevelRenderer.renderSky(PoseStack, Matrix4f, F, Camera, Z, Runnable)
    @WrapWithCondition(
            method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V",
                    ordinal = 0
            )
    )
    public boolean redirectSetSkyShader(VertexBuffer instance, Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderInstance program) {
        return SodiumExtraClientMod.options().detailSettings.sky;
    }

    @Inject(method = "renderEndSky", at = @At(value = "HEAD"), cancellable = true)
    public void preRenderEndSky(PoseStack matrices, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().detailSettings.sky) {
            ci.cancel();
        }
    }
}