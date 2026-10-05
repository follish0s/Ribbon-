package me.jellysquid.mods.sodium.client.world.cloned;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ClonedChunkSectionCache {
    private static final long MAX_CACHE_DURATION = 3000L;

    private final Level world;
    private final ConcurrentLinkedDeque<ClonedChunkSection> cache = new ConcurrentLinkedDeque<>();
    private long time = 0L;

    public ClonedChunkSectionCache(Level world) {
        this.world = world;
    }

    public ClonedChunkSection acquire(int x, int y, int z) {
        this.cleanup();
        this.time += 100L;

        SectionPos pos = SectionPos.of(x, y, z);

        for (ClonedChunkSection section : this.cache) {
            if (section.getPosition().equals(pos)) {
                section.setLastUsedTimestamp(this.time);
                return section;
            }
        }

        LevelChunk chunk = this.world.getChunkAt(new BlockPos(x << 4, 0, z << 4));
        if (chunk == null) {
            return null;
        }

        LevelChunkSection[] sections = chunk.getSections();
        int sectionIndex = this.world.getSectionIndexFromSectionY(y);
        if (sectionIndex < 0 || sectionIndex >= sections.length) {
            return null;
        }

        LevelChunkSection section = sections[sectionIndex];
        if (section == null) {
            return null;
        }

        ClonedChunkSection cloned = new ClonedChunkSection(this.world, chunk, section, pos);
        cloned.setLastUsedTimestamp(this.time);
        this.cache.add(cloned);
        return cloned;
    }

    public void invalidate(int x, int y, int z) {
        SectionPos pos = SectionPos.of(x, y, z);
        Iterator<ClonedChunkSection> it = this.cache.iterator();
        while (it.hasNext()) {
            ClonedChunkSection section = it.next();
            if (section.getPosition().equals(pos)) {
                it.remove();
            }
        }
    }

    public void cleanup() {
        Iterator<ClonedChunkSection> it = this.cache.iterator();
        while (it.hasNext()) {
            ClonedChunkSection section = it.next();
            if (this.time > (section.getLastUsedTimestamp() + MAX_CACHE_DURATION)) {
                it.remove();
            }
        }
    }
}