package me.jellysquid.mods.sodium.client.world.cloned;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import org.jetbrains.annotations.Nullable;

public class ClonedChunkSection {
    private final Level world;
    private final LevelChunk chunk;
    private final LevelChunkSection section;
    private final SectionPos pos;
    private final @Nullable Int2ReferenceMap<BlockEntity> blockEntityMap;
    private long lastUsedTimestamp;

    public ClonedChunkSection(Level world, LevelChunk chunk, LevelChunkSection section, SectionPos pos) {
        this.world = world;
        this.chunk = chunk;
        this.section = section;
        this.pos = pos;
        this.blockEntityMap = createBlockEntityMap(chunk, pos, section);
    }

    private static @Nullable Int2ReferenceMap<BlockEntity> createBlockEntityMap(LevelChunk chunk, SectionPos pos, LevelChunkSection section) {
        if (section.hasOnlyAir()) {
            return null;
        }

        Int2ReferenceMap<BlockEntity> map = new Int2ReferenceOpenHashMap<>();
        int sectionY = pos.getY();

        for (var entry : chunk.getBlockEntities().entrySet()) {
            BlockPos blockPos = entry.getKey();
            if (SectionPos.blockToSectionCoord(blockPos.getY()) != sectionY) {
                continue;
            }
            int localX = blockPos.getX() & 15;
            int localY = blockPos.getY() & 15;
            int localZ = blockPos.getZ() & 15;
            int key = (localY << 8) | (localZ << 4) | localX;
            map.put(key, entry.getValue());
        }

        return map.isEmpty() ? null : map;
    }

    public BlockState getBlockState(int x, int y, int z) {
        return this.section.getBlockState(x, y, z);
    }

    public PalettedContainer<BlockState> getBlockData() {
        return this.section.getStates();
    }

    public PalettedContainerRO<Holder<Biome>> getBiomeData() {
        return this.section.getBiomes();
    }

    /**
     * 从世界的光照引擎读取当前 section 的光照数据。
     * 天空光和方块光分别存在两个 LayerLightEventListener 里。
     */
    public @Nullable DataLayer getLightArray(LightLayer layer) {
        if (this.world == null || this.world.getLightEngine() == null) {
            return null;
        }

        LayerLightEventListener listener = this.world.getLightEngine().getLayerListener(layer);
        if (listener == null) {
            return null;
        }

        return listener.getDataLayerData(this.pos);
    }

    public @Nullable Int2ReferenceMap<BlockEntity> getBlockEntityMap() {
        return this.blockEntityMap;
    }

    public long getLastUsedTimestamp() {
        return this.lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long timestamp) {
        this.lastUsedTimestamp = timestamp;
    }

    public SectionPos getPosition() {
        return this.pos;
    }

    public LevelChunk getChunk() {
        return this.chunk;
    }

    public Level getWorld() {
        return this.world;
    }
}