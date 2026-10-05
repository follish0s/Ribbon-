package me.flashyreese.mods.sodiumextra.client.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.sodiumextra.client.ClientTickHandler;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = "ribbon")
public class SodiumExtraHud {

    private static final List<Component> TEXT_LIST = new ObjectArrayList<>();

    private static final Minecraft CLIENT = Minecraft.getInstance();

    @SubscribeEvent
    public static void onStartTick(TickEvent.ClientTickEvent client) {
        if (client.phase == TickEvent.Phase.END) {
            return;
        }

        TEXT_LIST.clear();

        if (SodiumExtraClientMod.options().extraSettings.showFps) {
            int currentFPS = CLIENT.getFps();

            Component text = Component.translatable("sodium-extra.overlay.fps", currentFPS);

            if (SodiumExtraClientMod.options().extraSettings.showFPSExtended)
                text = Component.literal(String.format("%s %s", text.getString(), Component.translatable("sodium-extra.overlay.fps_extended", ClientTickHandler.getHighestFps(), ClientTickHandler.getAverageFps(),
                        ClientTickHandler.getLowestFps()).getString()));

            TEXT_LIST.add(text);
        }

        if (SodiumExtraClientMod.options().extraSettings.showCoords && CLIENT.player != null && !CLIENT.player.isReducedDebugInfo()) {
            Vec3 pos = CLIENT.player.position();

            Component text = Component.translatable("sodium-extra.overlay.coordinates", String.format("%.2f", pos.x), String.format("%.2f", pos.y), String.format("%.2f", pos.z));
            TEXT_LIST.add(text);
        }

        if (!SodiumExtraClientMod.options().renderSettings.lightUpdates) {
            Component text = Component.translatable("sodium-extra.overlay.light_updates");
            TEXT_LIST.add(text);
        }
    }

    @SubscribeEvent
    public static void onHudRender(RenderGuiEvent.Post event) {
        if (!CLIENT.options.renderDebug) {
            SodiumExtraGameOptions.OverlayCorner overlayCorner = SodiumExtraClientMod.options().extraSettings.overlayCorner;
            int x;
            int y = overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT ?
                    CLIENT.getWindow().getGuiScaledHeight() - CLIENT.font.lineHeight - 2 : 2;

            for (Component text : TEXT_LIST) {
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.TOP_RIGHT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    x = CLIENT.getWindow().getGuiScaledWidth() - CLIENT.font.width(text) - 2;
                } else {
                    x = 2;
                }
                drawString(event.getGuiGraphics(), text, x, y);
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    y -= CLIENT.font.lineHeight + 2;
                } else {
                    y += CLIENT.font.lineHeight + 2;
                }
            }
        }
    }

    private static void drawString(GuiGraphics drawContext, Component text, int x, int y) {
        int textColor = 0xffffffff;

        if (SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.BACKGROUND) {
            drawContext.fill(x - 1, y - 1, x + CLIENT.font.width(text) + 1, y + CLIENT.font.lineHeight + 1, -1873784752);
        }

        drawContext.drawString(CLIENT.font, text, x, y, textColor, SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.SHADOW);
    }
}
