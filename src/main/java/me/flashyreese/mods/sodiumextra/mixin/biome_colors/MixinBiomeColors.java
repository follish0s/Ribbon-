package me.flashyreese.mods.sodiumextra.mixin.biome_colors;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {

    // Yarn: net.minecraft.client.color.world.BiomeColors#getGrassColor(BlockRenderView, BlockPos)
    // Mojmap: net.minecraft.client.renderer.BiomeColors#getAverageGrassColor(BlockAndTintGetter, BlockPos)
    @Inject(
            method = "getAverageGrassColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void grassColor(BlockAndTintGetter view, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!SodiumExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(9551193);
        }
    }

    @Inject(
            method = "getAverageWaterColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void waterColor(BlockAndTintGetter view, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!SodiumExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(4159204);
        }
    }

    @Inject(
            method = "getAverageFoliageColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void foliageColor(BlockAndTintGetter view, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!SodiumExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(5877296);
        }
    }
}