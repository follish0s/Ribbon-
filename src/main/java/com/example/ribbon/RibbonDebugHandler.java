package com.example.ribbon;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ribbon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RibbonDebugHandler {

    @SubscribeEvent
    public static void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 左侧调试信息
        event.getLeft().add("");
        event.getLeft().add("[Ribbon] 调试信息");
        event.getLeft().add("坐标: " + (int) mc.player.getX() + ", " + (int) mc.player.getY() + ", " + (int) mc.player.getZ());

        // 右侧调试信息
        event.getRight().add("");
        event.getRight().add("[Ribbon] 运行状态");
        event.getRight().add("帧率: " + mc.getFps());
        event.getRight().add("光影: " + (me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer.instance() != null));
    }
}