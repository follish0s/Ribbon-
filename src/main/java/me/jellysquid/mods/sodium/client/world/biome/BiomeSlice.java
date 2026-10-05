package me.jellysquid.mods.sodium.client.world.biome;

import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import me.jellysquid.mods.sodium.client.world.cloned.ClonedChunkSection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.jetbrains.annotations.Nullable;

public class BiomeSlice {
    private static final int SECTION_ARRAY_LENGTH = 3;
    private static final int SECTION_ARRAY_SIZE = SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH;

    @SuppressWarnings("unchecked")
    private final PalettedContainerRO<Holder<Biome>>[] sections = new PalettedContainerRO[SECTION_ARRAY_SIZE];

    private boolean hasBiomes = false;
    private @Nullable Holder<Biome> fallbackBiome;

    public void update(Level world, ChunkRenderContext context) {
        if (world == null || context == null) {
            return;
        }

        // 缓存一个默认生物群系作为 fallback，防止 getBiome 返回 null
        if (this.fallbackBiome == null) {
            try {
                this.fallbackBiome = world.registryAccess()
                        .registryOrThrow(Registries.BIOME)
                        .getHolderOrThrow(Biomes.PLAINS);
            } catch (RuntimeException e) {
                this.fallbackBiome = null;
            }
        }

        this.copyBiomeData(context);
    }

    public void copyBiomeData(ChunkRenderContext context) {
        this.hasBiomes = false;

        for (int i = 0; i < SECTION_ARRAY_SIZE; i++) {
            ClonedChunkSection section = context.getSections()[i];

            // 邻接区块未加载 → 置空
            if (section == null) {
                this.sections[i] = null;
                continue;
            }

            try {
                PalettedContainerRO<Holder<Biome>> data = section.getBiomeData();
                this.sections[i] = data;
                if (data != null) {
                    this.hasBiomes = true;
                }
            } catch (RuntimeException e) {
                this.sections[i] = null;
            }
        }
    }

    public boolean hasBiomes() {
        return this.hasBiomes;
    }

    public @Nullable Holder<Biome> getBiome(int x, int y, int z) {
        int sectionX = (x >> 4) + 1;
        int sectionY = (y >> 4) + 1;
        int sectionZ = (z >> 4) + 1;
        int sectionIndex = getLocalSectionIndex(sectionX, sectionY, sectionZ);

        if (sectionIndex < 0 || sectionIndex >= SECTION_ARRAY_SIZE) {
            return this.fallbackBiome;
        }

        PalettedContainerRO<Holder<Biome>> data = this.sections[sectionIndex];
        if (data == null) {
            return this.fallbackBiome;
        }

        try {
            Holder<Biome> biome = data.get(x & 15, y & 15, z & 15);
            return biome != null ? biome : this.fallbackBiome;
        } catch (RuntimeException e) {
            return this.fallbackBiome;
        }
    }

    private static int getLocalSectionIndex(int x, int y, int z) {
        return (y * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH) + (z * SECTION_ARRAY_LENGTH) + x;
    }
}