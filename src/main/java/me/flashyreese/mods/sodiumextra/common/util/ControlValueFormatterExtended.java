package me.flashyreese.mods.sodiumextra.common.util;

import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.minecraft.network.chat.Component;

public class ControlValueFormatterExtended {

    public static ControlValueFormatter fogDistance() {
        return value -> {
            if (value == 0) {
                return Component.translatable("sodium-extra.option.fog.disabled");
            }
            if (value == 33) {
                return Component.translatable("sodium-extra.option.fog.default");
            }
            return Component.literal(value + " chunks");
        };
    }

    public static ControlValueFormatter ticks() {
        return value -> Component.literal(value + " ticks");
    }
}