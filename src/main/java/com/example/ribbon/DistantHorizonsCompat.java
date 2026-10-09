package com.example.ribbon;

import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;

public class DistantHorizonsCompat {
    private static boolean dhLoaded = false;
    private static Object graphicsConfig = null;
    private static Method setOverdrawPreventionMethod = null;
    private static long lastUpdateTime = 0;
    private static final long UPDATE_COOLDOWN = 50; // 50ms冷却，避免每帧反射开销

    public static void init() {
        if (!ModList.get().isLoaded("distanthorizons")) return;
        try {
            Class<?> dhApiClass = Class.forName("com.seibel.distanthorizons.api.DhApi");
            Method getGraphicsConfigMethod = dhApiClass.getMethod("getGraphicsConfig");
            graphicsConfig = getGraphicsConfigMethod.invoke(null);
            if (graphicsConfig != null) {
                // DH 3.3.3 的方法名可能是 setOverdrawPrevention 或 setLodBias
                setOverdrawPreventionMethod = graphicsConfig.getClass().getMethod("setOverdrawPrevention", double.class);
                dhLoaded = true;
                System.out.println("Ribbon: Distant Horizons 兼容层加载成功！");
            }
        } catch (Throwable t) {
            System.out.println("Ribbon: DH 兼容层初始化失败 (无DH时正常现象): " + t.getMessage());
        }
    }

    public static void updateOverdrawPrevention(double value) {
        if (!dhLoaded || graphicsConfig == null || setOverdrawPreventionMethod == null) return;
        // 冷却机制：避免每帧都触发反射调用
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_COOLDOWN) return;
        lastUpdateTime = now;

        try {
            setOverdrawPreventionMethod.invoke(graphicsConfig, value);
        } catch (Throwable ignored) {}
    }
}