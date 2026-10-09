package com.example.ribbon;

import java.util.HashMap;
import java.util.Map;

public class LangUtil {

    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> ZH = new HashMap<>();

    static {
        // ===== English =====
        EN.put("ribbon.page", "Ribbon");
        EN.put("option.show_overlay", "Show FPS/TPS overlay");
        EN.put("option.show_overlay.tooltip", "Show the FPS/TPS text on the HUD");
        EN.put("option.overlay_corner", "Overlay position");
        EN.put("option.overlay_corner.tooltip", "Which screen corner the overlay is drawn in");
        EN.put("option.overlay_scale", "Overlay scale");
        EN.put("option.overlay_scale.tooltip", "Text size multiplier for the overlay text");
        EN.put("option.rainbow_crosshair", "Rainbow crosshair");
        EN.put("option.rainbow_crosshair.tooltip", "Replace the vanilla crosshair with a rainbow gradient crosshair");
        EN.put("option.hotbar_highlight", "Hotbar highlight");
        EN.put("option.hotbar_highlight.tooltip", "Draw a rainbow border around the currently selected hotbar slot");
        EN.put("option.damage_numbers", "Damage numbers");
        EN.put("option.damage_numbers.tooltip", "Show floating damage numbers when you hit an entity. "
                + "Fire = red, Freeze = cyan, Lightning = yellow, Magic = purple, "
                + "Special mods = rainbow, Normal = white.");
        EN.put("option.damage_number_scale", "Damage number scale");
        EN.put("option.damage_number_scale.tooltip", "Size multiplier for the damage numbers");
        EN.put("option.language", "Language");
        EN.put("option.language.tooltip", "Language used by the Ribbon GUI. Restart the video settings screen after switching.");
        EN.put("option.biome_title", "Biome title");
        EN.put("option.biome_title.tooltip", "Show an RPG-style title when entering a new biome");
        EN.put("option.biome_title_duration", "Biome title duration");
        EN.put("option.biome_title_duration.tooltip", "How long the biome title stays on screen");

        EN.put("biome.prefix", "Biome");

        // 枚举值
        EN.put("enum.TOP_LEFT", "Top left");
        EN.put("enum.TOP_RIGHT", "Top right");
        EN.put("enum.BOTTOM_LEFT", "Bottom left");
        EN.put("enum.BOTTOM_RIGHT", "Bottom right");
        EN.put("enum.ENGLISH", "English");
        EN.put("enum.CHINESE", "Chinese");

        // ===== 中文 =====
        ZH.put("ribbon.page", "Ribbon");
        ZH.put("option.show_overlay", "显示 FPS/TPS 悬浮窗");
        ZH.put("option.show_overlay.tooltip", "在 HUD 上显示 FPS/TPS 文本");
        ZH.put("option.overlay_corner", "悬浮窗位置");
        ZH.put("option.overlay_corner.tooltip", "悬浮窗显示在屏幕的哪个角落");
        ZH.put("option.overlay_scale", "悬浮窗缩放");
        ZH.put("option.overlay_scale.tooltip", "悬浮窗文字大小的倍数");
        ZH.put("option.rainbow_crosshair", "彩虹准星");
        ZH.put("option.rainbow_crosshair.tooltip", "用彩虹渐变准星替换原版准星");
        ZH.put("option.hotbar_highlight", "物品栏高亮");
        ZH.put("option.hotbar_highlight.tooltip", "在当前选中的物品栏槽位周围绘制彩虹边框");
        ZH.put("option.damage_numbers", "伤害数字");
        ZH.put("option.damage_numbers.tooltip", "攻击实体时显示漂浮的伤害数字。"
                + "火焰=红色，冰冻=青色，闪电=黄色，魔法=紫色，"
                + "特殊模组=彩虹色，普通=白色。");
        ZH.put("option.damage_number_scale", "伤害数字大小");
        ZH.put("option.damage_number_scale.tooltip", "伤害数字大小的倍数");
        ZH.put("option.language", "语言");
        ZH.put("option.language.tooltip", "Ribbon 界面使用的语言。切换后请重新打开视频设置界面。");
        ZH.put("option.biome_title", "生物群系标题");
        ZH.put("option.biome_title.tooltip", "进入新生物群系时显示RPG风格标题");
        ZH.put("option.biome_title_duration", "标题显示时长");
        ZH.put("option.biome_title_duration.tooltip", "生物群系标题在屏幕上停留的时间");

        ZH.put("biome.prefix", "生物群系");

        // 枚举值
        ZH.put("enum.TOP_LEFT", "左上角");
        ZH.put("enum.TOP_RIGHT", "右上角");
        ZH.put("enum.BOTTOM_LEFT", "左下角");
        ZH.put("enum.BOTTOM_RIGHT", "右下角");
        ZH.put("enum.ENGLISH", "英文");
        ZH.put("enum.CHINESE", "中文");
    }

    public static String tr(String key) {
        boolean chinese = Config.LANGUAGE.get() == Config.Language.CHINESE;
        Map<String, String> map = chinese ? ZH : EN;
        return map.getOrDefault(key, key);
    }
}