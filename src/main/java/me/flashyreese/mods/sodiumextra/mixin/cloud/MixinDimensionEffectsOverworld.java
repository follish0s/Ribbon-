package me.flashyreese.mods.sodiumextra.mixin.cloud;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DimensionSpecialEffects.OverworldEffects.class)
public abstract class MixinDimensionEffectsOverworld extends DimensionSpecialEffects {
    public MixinDimensionEffectsOverworld(float cloudsHeight, boolean alternateSkyColor, SkyType skyType, boolean brightenLighting, boolean darkened) {
        super(cloudsHeight, alternateSkyColor, skyType, brightenLighting, darkened);
    }

    // Yarn getCloudsHeight -> Mojmap getCloudHeight
    @Override
    public float getCloudHeight() {
        return SodiumExtraClientMod.options().extraSettings.cloudHeight;
    }
}