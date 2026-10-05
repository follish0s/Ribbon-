package me.jellysquid.mods.sodium.client.world;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import me.jellysquid.mods.sodium.client.world.biome.*;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import me.jellysquid.mods.sodium.client.world.cloned.ClonedChunkSection;
import me.jellysquid.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.embeddedt.embeddium.api.ChunkMeshEvent;
import org.embeddedt.embeddium.api.MeshAppender;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class WorldSlice implements BlockAndTintGetter, BiomeColorView {
    private static final LightLayer[] LIGHT_TYPES = LightLayer.values();
    private static final int SECTION_BLOCK_COUNT = 16 * 16 * 16;
    private static final int NEIGHBOR_BLOCK_RADIUS = 2;
    private static final int NEIGHBOR_CHUNK_RADIUS = Mth.roundToward(NEIGHBOR_BLOCK_RADIUS, 16) >> 4;
    private static final int SECTION_ARRAY_LENGTH = 1 + (NEIGHBOR_CHUNK_RADIUS * 2);
    private static final int SECTION_ARRAY_SIZE = SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH;
    private static final int BLOCK_ARRAY_LENGTH = SECTION_ARRAY_LENGTH * 16;
    private static final int LOCAL_XYZ_BITS = 4;
    private static final BlockState EMPTY_BLOCK_STATE = Blocks.AIR.defaultBlockState();

    public final ClientLevel world;
    private final BiomeSlice biomeSlice;
    private final BiomeColorCache biomeColors;
    private final BlockState[][] blockArrays;
    private final @Nullable DataLayer[][] lightArrays;
    private final @Nullable Int2ReferenceMap<BlockEntity>[] blockEntityArrays;

    private int originX, originY, originZ;
    private BoundingBox volume;

    public static ChunkRenderContext prepare(Level world, SectionPos origin, ClonedChunkSectionCache sectionCache) {
        LevelChunk chunk = world.getChunk(origin.getX(), origin.getZ());
        LevelChunkSection section = chunk.getSections()[world.getSectionIndexFromSectionY(origin.getY())];

        List<MeshAppender> meshAppenders = ChunkMeshEvent.post(world, origin);
        boolean isEmpty = (section == null || section.hasOnlyAir()) && meshAppenders.isEmpty();

        if (isEmpty) {
            return null;
        }

        BoundingBox volume = new BoundingBox(origin.minBlockX() - NEIGHBOR_BLOCK_RADIUS,
                origin.minBlockY() - NEIGHBOR_BLOCK_RADIUS,
                origin.minBlockZ() - NEIGHBOR_BLOCK_RADIUS,
                origin.maxBlockX() + NEIGHBOR_BLOCK_RADIUS,
                origin.maxBlockY() + NEIGHBOR_BLOCK_RADIUS,
                origin.maxBlockZ() + NEIGHBOR_BLOCK_RADIUS);

        final int minChunkX = origin.getX() - NEIGHBOR_CHUNK_RADIUS;
        final int minChunkY = origin.getY() - NEIGHBOR_CHUNK_RADIUS;
        final int minChunkZ = origin.getZ() - NEIGHBOR_CHUNK_RADIUS;

        final int maxChunkX = origin.getX() + NEIGHBOR_CHUNK_RADIUS;
        final int maxChunkY = origin.getY() + NEIGHBOR_CHUNK_RADIUS;
        final int maxChunkZ = origin.getZ() + NEIGHBOR_CHUNK_RADIUS;

        ClonedChunkSection[] sections = new ClonedChunkSection[SECTION_ARRAY_SIZE];

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                    sections[getLocalSectionIndex(chunkX - minChunkX, chunkY - minChunkY, chunkZ - minChunkZ)] =
                            sectionCache.acquire(chunkX, chunkY, chunkZ);
                }
            }
        }

        return new ChunkRenderContext(origin, sections, volume).withMeshAppenders(meshAppenders);
    }

    @SuppressWarnings("unchecked")
    public WorldSlice(ClientLevel world) {
        this.world = world;
        this.blockArrays = new BlockState[SECTION_ARRAY_SIZE][SECTION_BLOCK_COUNT];
        this.lightArrays = new DataLayer[SECTION_ARRAY_SIZE][LIGHT_TYPES.length];
        this.blockEntityArrays = new Int2ReferenceMap[SECTION_ARRAY_SIZE];
        this.biomeSlice = new BiomeSlice();
        this.biomeColors = new BiomeColorCache(this.biomeSlice, Minecraft.getInstance().options.biomeBlendRadius().get());

        for (BlockState[] blockArray : this.blockArrays) {
            Arrays.fill(blockArray, EMPTY_BLOCK_STATE);
        }
    }

    public void copyData(ChunkRenderContext context) {
        this.originX = (context.getOrigin().getX() - NEIGHBOR_CHUNK_RADIUS) << 4;
        this.originY = (context.getOrigin().getY() - NEIGHBOR_CHUNK_RADIUS) << 4;
        this.originZ = (context.getOrigin().getZ() - NEIGHBOR_CHUNK_RADIUS) << 4;
        this.volume = context.getVolume();

        for (int x = 0; x < SECTION_ARRAY_LENGTH; x++) {
            for (int y = 0; y < SECTION_ARRAY_LENGTH; y++) {
                for (int z = 0; z < SECTION_ARRAY_LENGTH; z++) {
                    this.copySectionData(context, getLocalSectionIndex(x, y, z));
                }
            }
        }

        this.biomeSlice.update(this.world, context);
        this.biomeColors.update(context);
    }

    private void copySectionData(ChunkRenderContext context, int sectionIndex) {
        var section = context.getSections()[sectionIndex];

        // 🛡️ 邻接区块未加载 → 用空气填充，跳过这一层，不再抛 NPE
        if (section == null) {
            Arrays.fill(this.blockArrays[sectionIndex], EMPTY_BLOCK_STATE);
            this.lightArrays[sectionIndex][LightLayer.BLOCK.ordinal()] = null;
            this.lightArrays[sectionIndex][LightLayer.SKY.ordinal()] = null;
            this.blockEntityArrays[sectionIndex] = null;
            return;
        }

        try {
            this.unpackBlockData(this.blockArrays[sectionIndex], context, section);
        } catch (RuntimeException e) {
            // 单个 section 数据异常不影响整个区块构建
            Arrays.fill(this.blockArrays[sectionIndex], EMPTY_BLOCK_STATE);
            this.lightArrays[sectionIndex][LightLayer.BLOCK.ordinal()] = null;
            this.lightArrays[sectionIndex][LightLayer.SKY.ordinal()] = null;
            this.blockEntityArrays[sectionIndex] = null;
            return;
        }

        this.lightArrays[sectionIndex][LightLayer.BLOCK.ordinal()] = section.getLightArray(LightLayer.BLOCK);
        this.lightArrays[sectionIndex][LightLayer.SKY.ordinal()] = section.getLightArray(LightLayer.SKY);
        this.blockEntityArrays[sectionIndex] = section.getBlockEntityMap();
    }

    private void unpackBlockData(BlockState[] blockArray, ChunkRenderContext context, ClonedChunkSection section) {
        if (section.getBlockData() == null) {
            Arrays.fill(blockArray, EMPTY_BLOCK_STATE);
            return;
        }

        ReadableContainerExtended<BlockState> container = ReadableContainerExtended.of(section.getBlockData());
        SectionPos origin = context.getOrigin();
        SectionPos pos = section.getPosition();

        if (origin.equals(pos)) {
            container.sodium$unpack(blockArray);
        } else {
            var bounds = context.getVolume();
            int minBlockX = Math.max(bounds.minX(), pos.minBlockX());
            int maxBlockX = Math.min(bounds.maxX(), pos.maxBlockX());
            int minBlockY = Math.max(bounds.minY(), pos.minBlockY());
            int maxBlockY = Math.min(bounds.maxY(), pos.maxBlockY());
            int minBlockZ = Math.max(bounds.minZ(), pos.minBlockZ());
            int maxBlockZ = Math.min(bounds.maxZ(), pos.maxBlockZ());

            container.sodium$unpack(blockArray, minBlockX & 15, minBlockY & 15, minBlockZ & 15,
                    maxBlockX & 15, maxBlockY & 15, maxBlockZ & 15);
        }
    }

    public void reset() {
        for (int sectionIndex = 0; sectionIndex < SECTION_ARRAY_LENGTH; sectionIndex++) {
            Arrays.fill(this.lightArrays[sectionIndex], null);
            this.blockEntityArrays[sectionIndex] = null;
        }
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        int relX = x - this.originX;
        int relY = y - this.originY;
        int relZ = z - this.originZ;

        if (!isInside(relX, relY, relZ)) {
            return EMPTY_BLOCK_STATE;
        }

        return this.blockArrays[getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4)]
                [getLocalBlockIndex(relX & 15, relY & 15, relZ & 15)];
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public float getShade(Direction direction, boolean shaded) {
        return this.world.getShade(direction, shaded);
    }

    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return this.world.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBrightness(LightLayer type, BlockPos pos) {
        int relX = pos.getX() - this.originX;
        int relY = pos.getY() - this.originY;
        int relZ = pos.getZ() - this.originZ;

        if (!isInside(relX, relY, relZ)) {
            return 0;
        }

        var lightArray = this.lightArrays[getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4)][type.ordinal()];
        if (lightArray == null) {
            return 0;
        }

        return lightArray.get(relX & 15, relY & 15, relZ & 15);
    }

    @Override
    public int getRawBrightness(BlockPos pos, int ambientDarkness) {
        int relX = pos.getX() - this.originX;
        int relY = pos.getY() - this.originY;
        int relZ = pos.getZ() - this.originZ;

        if (!isInside(relX, relY, relZ)) {
            return 0;
        }

        var lightArrays = this.lightArrays[getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4)];
        var skyLightArray = lightArrays[LightLayer.SKY.ordinal()];
        var blockLightArray = lightArrays[LightLayer.BLOCK.ordinal()];

        int localX = relX & 15;
        int localY = relY & 15;
        int localZ = relZ & 15;

        int skyLight = skyLightArray == null ? 0 : skyLightArray.get(localX, localY, localZ) - ambientDarkness;
        int blockLight = blockLightArray == null ? 0 : blockLightArray.get(localX, localY, localZ);

        return Math.max(blockLight, skyLight);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.getBlockEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockEntity getBlockEntity(int x, int y, int z) {
        int relX = x - this.originX;
        int relY = y - this.originY;
        int relZ = z - this.originZ;

        if (!isInside(relX, relY, relZ)) {
            return null;
        }

        var blockEntities = this.blockEntityArrays[getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4)];
        if (blockEntities == null) {
            return null;
        }

        return blockEntities.get(getLocalBlockIndex(relX & 15, relY & 15, relZ & 15));
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return this.biomeColors.getColor(resolver, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getHeight() {
        return this.world.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return this.world.getMinBuildHeight();
    }

    @Override
    public int getColor(BiomeColorSource source, int x, int y, int z) {
        return this.biomeColors.getColor(source, x, y, z);
    }

    @Override
    public @Nullable ModelDataManager getModelDataManager() {
        return this.world.getModelDataManager();
    }

    public boolean hasBiomes() {
        return true;
    }

    public static int getLocalBlockIndex(int x, int y, int z) {
        return (y << LOCAL_XYZ_BITS << LOCAL_XYZ_BITS) | (z << LOCAL_XYZ_BITS) | x;
    }

    public static int getLocalSectionIndex(int x, int y, int z) {
        return (y * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH) + (z * SECTION_ARRAY_LENGTH) + x;
    }

    private boolean isInside(int relX, int relY, int relZ) {
        return relX >= 0 && relX < BLOCK_ARRAY_LENGTH && relZ >= 0 && relZ < BLOCK_ARRAY_LENGTH && relY >= 0 && relY < BLOCK_ARRAY_LENGTH;
    }
}