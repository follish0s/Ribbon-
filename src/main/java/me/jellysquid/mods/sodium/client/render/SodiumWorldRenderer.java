package me.jellysquid.mods.sodium.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

public class SodiumWorldRenderer {

    private static volatile SodiumWorldRenderer instance;

    private final Minecraft client;
    private ClientLevel world;
    private RenderSectionManager renderSectionManager;

    public SodiumWorldRenderer(Minecraft client) {
        this.client = client;
        instance = this;
    }

    public static SodiumWorldRenderer instanceNullable() {
        return instance;
    }

    public static SodiumWorldRenderer instance() {
        return instance;
    }

    public void setWorld(ClientLevel world) {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.destroy();
            this.renderSectionManager = null;
        }
        this.world = world;
    }

    private void ensureManager(ClientLevel level) {
        if (this.renderSectionManager != null) return;
        try {
            int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
            try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
                this.renderSectionManager = new RenderSectionManager(level, renderDistance, commandList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ClientLevel getWorld() { return this.world; }
    public RenderSectionManager getRenderSectionManager() { return this.renderSectionManager; }

    public void updateChunks(boolean updateImmediately) {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.runAsyncTasks();
            this.renderSectionManager.updateChunks(updateImmediately);
        }
    }

    public void uploadChunks() {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.uploadChunks();
        }
    }

    public void onChunkAdded(int x, int z) {
        if (this.renderSectionManager == null) return;
        this.renderSectionManager.onChunkAdded(x, z);
    }

    public void onChunkUnload(int x, int z) {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.onChunkRemoved(x, z);
        }
    }

    public void setupTerrain(Camera camera, Viewport viewport, int frame,
                             boolean spectator, boolean updateChunksImmediately) {
        if (this.world != null && this.renderSectionManager == null) {
            ensureManager(this.world);
        }
        if (this.renderSectionManager == null) {
            return;
        }

        // ★ Ribbon: 完全按原版 Embeddium 的逻辑
        //   Oculus 通过自己的 MixinRenderSectionManager 隔离两份列表，
        //   这里任何 shadow 判断都会破坏它的状态机。
        this.renderSectionManager.needsUpdate();
        this.renderSectionManager.update(camera, viewport, frame, spectator);
    }

    public void scheduleTerrainUpdate() {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.markGraphDirty();
        }
    }

    public void scheduleRebuildForBlockArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        this.scheduleRebuildForChunks(minX >> 4, minY >> 4, minZ >> 4, maxX >> 4, maxY >> 4, maxZ >> 4, important);
    }

    public void scheduleRebuildForChunks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        if (this.renderSectionManager == null) return;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    this.renderSectionManager.scheduleRebuild(x, y, z, important);
                }
            }
        }
    }

    public void scheduleRebuildForChunk(int x, int y, int z, boolean important) {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.scheduleRebuild(x, y, z, important);
        }
    }

    public boolean isSectionReady(int x, int y, int z) {
        if (this.renderSectionManager != null) return this.renderSectionManager.isSectionBuilt(x, y, z);
        return true;
    }

    public void reload() {
        ClientLevel w = this.world;
        this.setWorld(null);
        this.setWorld(w);
    }

    public List<String> getDebugStrings() {
        if (this.renderSectionManager != null) return new ArrayList<>(this.renderSectionManager.getDebugStrings());
        return new ArrayList<>();
    }

    public String getChunksDebugString() {
        if (this.renderSectionManager != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : this.renderSectionManager.getDebugStrings()) sb.append(s).append('\n');
            return sb.toString();
        }
        return "";
    }

    public int getVisibleChunkCount() {
        if (this.renderSectionManager != null) return this.renderSectionManager.getVisibleChunkCount();
        return 0;
    }

    public boolean isTerrainRenderComplete() {
        if (this.renderSectionManager == null) return true;
        return !this.renderSectionManager.needsUpdate();
    }

    public boolean didBlockEntityRequestOutline() { return false; }

    public void drawChunkLayer(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z) {
        if (this.renderSectionManager == null) return;
        ChunkRenderMatrices matrices = ChunkRenderMatrices.from(matrixStack);
        TerrainRenderPass pass;
        if (renderLayer == RenderType.solid()) pass = DefaultTerrainRenderPasses.SOLID;
        else if (renderLayer == RenderType.cutout()) pass = DefaultTerrainRenderPasses.CUTOUT;
        else if (renderLayer == RenderType.translucent()) pass = DefaultTerrainRenderPasses.TRANSLUCENT;
        else return;
        this.renderSectionManager.renderLayer(matrices, pass, x, y, z);
    }

    public void tickVisibleRenders() {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.tickVisibleRenders();
        }
    }

    public void renderBlockEntities(PoseStack matrices, RenderBuffers bufferBuilders,
                                    Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
                                    Camera camera, float tickDelta) {
    }

    @SuppressWarnings("unused")
    private static void renderBlockEntity(PoseStack matrices, RenderBuffers bufferBuilders,
                                          Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
                                          float tickDelta, MultiBufferSource.BufferSource immediate,
                                          double x, double y, double z, BlockEntityRenderDispatcher dispatcher,
                                          BlockEntity blockEntity) {
    }

    @SuppressWarnings("unused")
    public void renderBlockEntities(PoseStack matrices, RenderBuffers bufferBuilders,
                                    Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
                                    float tickDelta, MultiBufferSource.BufferSource immediate,
                                    double x, double y, double z, BlockEntityRenderDispatcher dispatcher) {
        List<BlockEntity> empty = Collections.emptyList();
        for (BlockEntity entity : empty) {
            renderBlockEntity(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, dispatcher, entity);
        }
    }

    @SuppressWarnings("unused")
    public void renderGlobalBlockEntities(PoseStack matrices, RenderBuffers bufferBuilders,
                                          Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
                                          float tickDelta, MultiBufferSource.BufferSource immediate,
                                          double x, double y, double z, BlockEntityRenderDispatcher dispatcher) {
        List<BlockEntity> empty = Collections.emptyList();
        for (BlockEntity entity : empty) {
            renderBlockEntity(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, dispatcher, entity);
        }
    }

    @SuppressWarnings("unused")
    public boolean isEntityVisible(Entity entity) { return true; }
}