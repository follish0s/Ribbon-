package me.jellysquid.mods.sodium.mixin.core.model.colors;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import me.jellysquid.mods.sodium.client.model.color.interop.BlockColorsExtended;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockColors.class)
public class BlockColorsMixin implements BlockColorsExtended {

    @Override
    public Reference2ReferenceMap<Block, BlockColor> sodium$getProviders() {
        return new Reference2ReferenceOpenHashMap<>();
    }

    @Override
    public ReferenceSet<Block> embeddium$getOverridenVanillaBlocks() {
        return new ReferenceOpenHashSet<>();
    }
}