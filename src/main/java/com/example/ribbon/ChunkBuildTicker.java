package com.example.ribbon;

import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ribbon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkBuildTicker {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();

        if (renderer == null) return;
        if (renderer.getRenderSectionManager() == null) return;

        if (mc.player != null && mc.screen == null) {
            // 🛡️ 关键：必须进入 managed code 才能访问 OpenGL 设备
            RenderDevice.enterManagedCode();
            try {
                renderer.updateChunks(false);
                renderer.uploadChunks();
            } finally {
                RenderDevice.exitManagedCode();
            }
        }
    }
}