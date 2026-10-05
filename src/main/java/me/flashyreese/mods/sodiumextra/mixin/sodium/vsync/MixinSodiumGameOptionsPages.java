package me.flashyreese.mods.sodiumextra.mixin.sodium.vsync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptionPages;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptions;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public class MixinSodiumGameOptionsPages {
    @Shadow @Final private static MinecraftOptionsStorage vanillaOpts;

    @WrapOperation(method = "general",
            at = @At(value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder;add(Lme/jellysquid/mods/sodium/client/gui/options/Option;)Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder;",
                    ordinal = 5),
            remap = false)
    private static OptionGroup.Builder redirectVsyncToggle(OptionGroup.Builder instance, Option<?> option, Operation<OptionGroup.Builder> original) {
        if (!option.getTooltip().getString().equals(Component.translatable("sodium.options.v_sync.tooltip").getString())) {
            return original.call(instance, option);
        }
        return original.call(instance, OptionImpl.createBuilder(SodiumExtraGameOptions.VerticalSyncOption.class, SodiumExtraGameOptionPages.sodiumExtraOpts)
                .setName(Component.translatable("options.vsync"))
                .setTooltip(Component.literal(Component.translatable("sodium.options.v_sync.tooltip").getString() + "\n- "
                        + Component.translatable("sodium-extra.option.use_adaptive_sync.name").getString() + ": "
                        + Component.translatable("sodium-extra.option.use_adaptive_sync.tooltip").getString()))
                .setControl((opt) -> new CyclingControl<>(opt, SodiumExtraGameOptions.VerticalSyncOption.class,
                        SodiumExtraGameOptions.VerticalSyncOption.getAvailableOptions()))
                .setBinding((opts, value) -> {
                    switch (value) {
                        case OFF -> {
                            opts.extraSettings.useAdaptiveSync = false;
                            vanillaOpts.getData().enableVsync().set(false);
                        }
                        case ON -> {
                            opts.extraSettings.useAdaptiveSync = false;
                            vanillaOpts.getData().enableVsync().set(true);
                        }
                        case ADAPTIVE -> {
                            opts.extraSettings.useAdaptiveSync = true;
                            vanillaOpts.getData().enableVsync().set(true);
                        }
                    }
                    vanillaOpts.save();
                }, opts -> {
                    if (vanillaOpts.getData().enableVsync().get() && !opts.extraSettings.useAdaptiveSync) {
                        return SodiumExtraGameOptions.VerticalSyncOption.ON;
                    } else if (!vanillaOpts.getData().enableVsync().get() && !opts.extraSettings.useAdaptiveSync) {
                        return SodiumExtraGameOptions.VerticalSyncOption.OFF;
                    }
                    return SodiumExtraGameOptions.VerticalSyncOption.ADAPTIVE;
                })
                .setImpact(OptionImpact.VARIES)
                .build());
    }
}