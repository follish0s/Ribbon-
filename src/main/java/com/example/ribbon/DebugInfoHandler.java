package com.example.ribbon;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "ribbon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DebugInfoHandler {

    @SubscribeEvent
    public static void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null) {
            event.getLeft().add("Ribbon: renderer not ready");
            return;
        }

        var lines = renderer.getDebugStrings();;
        event.getLeft().addAll(lines);
    }
}