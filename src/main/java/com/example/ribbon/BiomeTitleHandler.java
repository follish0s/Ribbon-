package com.example.ribbon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ribbon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BiomeTitleHandler {

    private static ResourceLocation lastBiomeId = null;
    private static Component currentBiomeName = null;
    private static long titleStartTime = 0;
    private static boolean titleVisible = false;

    // ★ 防抖：连续 N 次检测到同一群系，才算真的进入了
    private static ResourceLocation pendingBiomeId = null;
    private static int pendingConfirmCount = 0;
    private static final int CONFIRM_THRESHOLD = 3;

    // ★ 冷却：触发后 X 毫秒内不再触发新标题
    private static long lastTriggerTime = 0;
    private static final long TRIGGER_COOLDOWN_MS = 3000;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!Config.BIOME_TITLE.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.player.tickCount % 10 != 0) return;

        Holder<Biome> biomeHolder = mc.level.getBiome(mc.player.blockPosition());
        ResourceLocation biomeId = biomeHolder.unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        if (biomeId == null) return;

        // ★ 稳定性检测：连续 3 次（约 1.5 秒）检测到同一群系，才认为真的进入了
        if (!biomeId.equals(pendingBiomeId)) {
            pendingBiomeId = biomeId;
            pendingConfirmCount = 1;
            return;
        }
        pendingConfirmCount++;

        if (pendingConfirmCount < CONFIRM_THRESHOLD) {
            return;
        }

        // 已经确认，但不是新群系，不触发
        if (biomeId.equals(lastBiomeId)) {
            return;
        }

        // ★ 冷却检查
        long now = System.currentTimeMillis();
        if (now - lastTriggerTime < TRIGGER_COOLDOWN_MS) {
            return;
        }

        lastBiomeId = biomeId;
        lastTriggerTime = now;

        String translationKey = "biome." + biomeId.getNamespace() + "." + biomeId.getPath();
        currentBiomeName = Component.translatable(translationKey);

        titleStartTime = System.currentTimeMillis();
        titleVisible = true;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!titleVisible || currentBiomeName == null) return;
        if (!Config.BIOME_TITLE.get()) return;

        long elapsed = System.currentTimeMillis() - titleStartTime;
        int duration = Config.BIOME_TITLE_DURATION.get();

        if (elapsed > duration) {
            titleVisible = false;
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 淡入淡出
        float alpha;
        if (elapsed < 500) {
            alpha = elapsed / 500.0f;
        } else if (elapsed > duration - 500) {
            alpha = (duration - elapsed) / 500.0f;
        } else {
            alpha = 1.0f;
        }
        alpha = Mth.clamp(alpha, 0.0f, 1.0f);

        // ★ 彩虹色：用时间计算 RGB
        float time = (System.currentTimeMillis() % 3000) / 3000.0f;
        int r = (int) ((Mth.sin(time * (float) Math.PI * 2) * 0.5f + 0.5f) * 255);
        int g = (int) ((Mth.sin((time + 1f / 3f) * (float) Math.PI * 2) * 0.5f + 0.5f) * 255);
        int b = (int) ((Mth.sin((time + 2f / 3f) * (float) Math.PI * 2) * 0.5f + 0.5f) * 255);

        int alphaInt = (int) (alpha * 255);
        int titleColor = (alphaInt << 24) | (r << 16) | (g << 8) | b;

        String prefixText = LangUtil.tr("biome.prefix");
        Component titleLine = Component.literal(prefixText + " ")
                .append(currentBiomeName);

        int y = screenHeight / 4;

        int textWidth = mc.font.width(titleLine);
        gui.drawString(mc.font, titleLine, (screenWidth - textWidth) / 2, y, titleColor, true);
    }
}