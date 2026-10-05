package me.jellysquid.mods.sodium.mixin.core.model.colors;

import me.jellysquid.mods.sodium.client.model.color.interop.ItemColorsExtended;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemColors.class)
public class ItemColorsMixin implements ItemColorsExtended {

    @Override
    public ItemColor sodium$getColorProvider(ItemStack stack) {
        return null;
    }
}