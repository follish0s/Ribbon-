package org.embeddedt.embeddium.util.sodium;

import net.minecraftforge.fml.ModList;

public class FlawlessFrames {
    private static final boolean IS_PRESENT = ModList.get().isLoaded("flawless_frames");
    private static final boolean FORCE_ENABLED = Boolean.getBoolean("embeddium.flawless_frames.enabled");
    private static final boolean FORCE_DISABLED = Boolean.getBoolean("embeddium.flawless_frames.disabled");

    public static void onClientInitialization() {
        // 客户端初始化占位，无需实际逻辑
    }

    public static boolean isActive() {
        if (FORCE_DISABLED) return false;
        if (FORCE_ENABLED) return true;
        return IS_PRESENT;
    }

    public static boolean isEnabled() {
        return isActive();
    }
}