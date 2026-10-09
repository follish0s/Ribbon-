package me.jellysquid.mods.sodium.mixin.core.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.world.WorldRendererExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public abstract class WorldRendererMixin implements WorldRendererExtended {

    @Unique private SodiumWorldRenderer renderer;

    @Override
    public SodiumWorldRenderer sodium$getWorldRenderer() {
        return this.renderer;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Minecraft client, EntityRenderDispatcher entityRenderDispatcher,
                      BlockEntityRenderDispatcher blockEntityRenderDispatcher,
                      RenderBuffers bufferBuilderStorage, CallbackInfo ci) {
        this.renderer = new SodiumWorldRenderer(client);
    }

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void onWorldChanged(ClientLevel world, CallbackInfo ci) {
        if (this.renderer == null) return;
        this.renderer.setWorld(world);
    }

    // ==========================================
    // ⚠️ 彻底删除了 setBlocksDirty 等 4 个注入！
    // Embeddium 自己会处理方块更新重建。
    // 我们加的那些，就是导致你移动时阴影抽搐、变亮片、被剔除的罪魁祸首。
    // ==========================================
}