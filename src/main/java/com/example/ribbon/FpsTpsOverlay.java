package com.example.ribbon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ribbon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FpsTpsOverlay {

    private static long lastServerTick = 0L;
    private static double smoothedTps = 20.0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long now = System.nanoTime();
            if (lastServerTick != 0L) {
                double tps = 1_000_000_000.0 / (now - lastServerTick);
                smoothedTps = smoothedTps * 0.7 + tps * 0.3;
            }
            lastServerTick = now;
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.level == null) return;
        if (!Config.SHOW_OVERLAY.get()) return; // 配置：显示开关

        GuiGraphics g = event.getGuiGraphics();
        int fps = mc.getFps();
        String text = String.format("FPS: %d | TPS: %.1f", fps, Math.min(smoothedTps, 20.0));

        int tw = mc.font.width(text);          // 文字宽
        int th = mc.font.lineHeight;           // 文字高
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        float s = Config.OVERLAY_SCALE.get().floatValue(); // 配置：大小
        int m = 4;                             // 距屏幕边缘的像素

        // 根据配置选角，算出文字的左上角该放哪
        float tx, ty;
        switch (Config.OVERLAY_CORNER.get()) {
            case TOP_RIGHT    -> { tx = sw - m - tw * s; ty = m; }
            case BOTTOM_LEFT  -> { tx = m;             ty = sh - m - th * s; }
            case BOTTOM_RIGHT -> { tx = sw - m - tw * s; ty = sh - m - th * s; }
            default           -> { tx = m;             ty = m; } // TOP_LEFT
        }

        var pose = g.pose();
        pose.pushPose();
        pose.translate(tx, ty, 0.0f);
        pose.scale(s, s, 1.0f);
        g.drawString(mc.font, text, 0, 0, 0xFFFFFF);
        pose.popPose();
    }
}