package me.jellysquid.mods.sodium.mixin.core.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.world.WorldRendererExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
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

    // ⚠️ 加了 require = 0，Oculus 修改方法后找不到目标也不会崩溃
    @Inject(method = "setBlocksDirty", at = @At("HEAD"), require = 0)
    public void ribbon$setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, CallbackInfo ci) {
        if (this.renderer == null) return;
        this.renderer.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, false);
    }

    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"), require = 0)
    public void ribbon$setSectionDirtyWithNeighbors(int x, int y, int z, CallbackInfo ci) {
        if (this.renderer == null) return;
        this.renderer.scheduleRebuildForChunks(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1, false);
    }

    @Inject(method = "setBlockDirty", at = @At("HEAD"), require = 0)
    private void ribbon$setBlockDirty(BlockPos pos, boolean important, CallbackInfo ci) {
        if (this.renderer == null) return;
        this.renderer.scheduleRebuildForBlockArea(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, important);
    }

    @Inject(method = "setSectionDirty", at = @At("HEAD"), require = 0)
    private void ribbon$setSectionDirty(int x, int y, int z, CallbackInfo ci) {
        if (this.renderer == null) return;
        this.renderer.scheduleRebuildForChunk(x, y, z, false);
    }
}