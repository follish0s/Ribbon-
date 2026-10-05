package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptions;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
    @ModifyExpressionValue(
            method = "onFogRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"
            )
    )
    private static boolean ribbonExtra$cancel(boolean original) {
        return original || SodiumExtraClientMod.options().renderSettings.fogType == SodiumExtraGameOptions.RenderSettings.FogType.MOD_COMPAT;
    }
}