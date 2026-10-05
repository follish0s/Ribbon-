package me.flashyreese.mods.sodiumextra.client;

import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptionPages;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;

/**
 * Route B: replaces Embeddium Extra's mixin.compat.MixinSodiumOptionsGUI.
 * The five pages are appended through Embeddium's public extension API instead of
 * being injected into SodiumOptionsGUI's constructor.
 */
public final class SodiumExtraOptions {

    private static boolean registered = false;

    private SodiumExtraOptions() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        registered = true;

        OptionGUIConstructionEvent.BUS.addListener(event -> {
            event.addPage(SodiumExtraGameOptionPages.animation());
            event.addPage(SodiumExtraGameOptionPages.particle());
            event.addPage(SodiumExtraGameOptionPages.detail());
            event.addPage(SodiumExtraGameOptionPages.render());
            event.addPage(SodiumExtraGameOptionPages.extra());
        });
    }
}
