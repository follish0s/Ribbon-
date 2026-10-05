package me.flashyreese.mods.sodiumextra.mixin.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameRenderer.class)
public class MixinItemFrameEntityRenderer {

    // 先让相框本体画完（super.render），再取消后面画物品的部分
    @Inject(
            method = "render(Lnet/minecraft/world/entity/decoration/ItemFrame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    public void render(ItemFrame itemFrameEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    // Yarn hasLabel -> Mojmap shouldShowName
    @Inject(
            method = "shouldShowName(Lnet/minecraft/world/entity/decoration/ItemFrame;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void hasLabel(ItemFrame entity, CallbackInfoReturnable<Boolean> cir) {
        if (!SodiumExtraClientMod.options().renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }
}