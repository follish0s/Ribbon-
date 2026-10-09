package com.example.ribbon;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.network.chat.Component;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;

public class RibbonOptions {

    private static final OptionStorage<Object> STORAGE = new OptionStorage<Object>() {
        private final Object data = new Object();
        @Override public Object getData() { return data; }
        @Override public void save() { Config.SPEC.save(); }
    };

    public static void init() {
        OptionGUIConstructionEvent.BUS.addListener(event -> event.addPage(buildPage()));
    }

    private static OptionPage buildPage() {
        OptionGroup.Builder group = OptionGroup.createBuilder();

        // ① 显示 FPS/TPS 悬浮窗
        group.add(OptionImpl.createBuilder(Boolean.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.show_overlay")))
                .setTooltip(Component.literal(LangUtil.tr("option.show_overlay.tooltip")))
                .setBinding((data, value) -> Config.SHOW_OVERLAY.set(value),
                        data -> Config.SHOW_OVERLAY.get())
                .setControl(option -> new TickBoxControl(option))
                .setImpact(OptionImpact.LOW)
                .build());

        // ② 悬浮窗位置
        group.add(OptionImpl.createBuilder(Config.OverlayCorner.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.overlay_corner")))
                .setTooltip(Component.literal(LangUtil.tr("option.overlay_corner.tooltip")))
                .setBinding((data, value) -> Config.OVERLAY_CORNER.set(value),
                        data -> Config.OVERLAY_CORNER.get())
                .setControl(option -> new CyclingControl<>(option, Config.OverlayCorner.class))
                .setImpact(OptionImpact.LOW)
                .build());

        // ③ 悬浮窗缩放
        group.add(OptionImpl.createBuilder(Integer.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.overlay_scale")))
                .setTooltip(Component.literal(LangUtil.tr("option.overlay_scale.tooltip")))
                .setBinding((data, value) -> Config.OVERLAY_SCALE.set(value / 10.0),
                        data -> (int) Math.round(Config.OVERLAY_SCALE.get() * 10.0))
                .setControl(option -> new SliderControl(option, 5, 30, 1,
                        v -> Component.literal(String.format("%.1fx", v / 10.0))))
                .setImpact(OptionImpact.LOW)
                .build());

        // ④ 彩虹准星
        group.add(OptionImpl.createBuilder(Boolean.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.rainbow_crosshair")))
                .setTooltip(Component.literal(LangUtil.tr("option.rainbow_crosshair.tooltip")))
                .setBinding((data, value) -> Config.RAINBOW_CROSSHAIR.set(value),
                        data -> Config.RAINBOW_CROSSHAIR.get())
                .setControl(option -> new TickBoxControl(option))
                .setImpact(OptionImpact.LOW)
                .build());

        // ⑤ 物品栏高亮
        group.add(OptionImpl.createBuilder(Boolean.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.hotbar_highlight")))
                .setTooltip(Component.literal(LangUtil.tr("option.hotbar_highlight.tooltip")))
                .setBinding((data, value) -> Config.HOTBAR_HIGHLIGHT.set(value),
                        data -> Config.HOTBAR_HIGHLIGHT.get())
                .setControl(option -> new TickBoxControl(option))
                .setImpact(OptionImpact.LOW)
                .build());

        // ⑥ 伤害数字
        group.add(OptionImpl.createBuilder(Boolean.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.damage_numbers")))
                .setTooltip(Component.literal(LangUtil.tr("option.damage_numbers.tooltip")))
                .setBinding((data, value) -> Config.DAMAGE_NUMBERS.set(value),
                        data -> Config.DAMAGE_NUMBERS.get())
                .setControl(option -> new TickBoxControl(option))
                .setImpact(OptionImpact.LOW)
                .build());

        // ⑦ 伤害数字大小
        group.add(OptionImpl.createBuilder(Integer.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.damage_number_scale")))
                .setTooltip(Component.literal(LangUtil.tr("option.damage_number_scale.tooltip")))
                .setBinding((data, value) -> Config.DAMAGE_NUMBER_SCALE.set(value / 10.0),
                        data -> (int) Math.round(Config.DAMAGE_NUMBER_SCALE.get() * 10.0))
                .setControl(option -> new SliderControl(option, 5, 50, 1,
                        v -> Component.literal(String.format("%.1fx", v / 10.0))))
                .setImpact(OptionImpact.LOW)
                .build());

        // ⑧ 生物群系标题开关
        group.add(OptionImpl.createBuilder(Boolean.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.biome_title")))
                .setTooltip(Component.literal(LangUtil.tr("option.biome_title.tooltip")))
                .setBinding((data, value) -> Config.BIOME_TITLE.set(value),
                        data -> Config.BIOME_TITLE.get())
                .setControl(option -> new TickBoxControl(option))
                .setImpact(OptionImpact.LOW)
                .build());

        // ⑨ 生物群系标题时长
        group.add(OptionImpl.createBuilder(Integer.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.biome_title_duration")))
                .setTooltip(Component.literal(LangUtil.tr("option.biome_title_duration.tooltip")))
                .setBinding((data, value) -> Config.BIOME_TITLE_DURATION.set(value),
                        data -> Config.BIOME_TITLE_DURATION.get())
                .setControl(option -> new SliderControl(option, 1000, 10000, 500,
                        v -> Component.literal(String.format("%.1fs", v / 1000.0))))
                .setImpact(OptionImpact.LOW)
                .build());

        // ⑩ 语言切换
        group.add(OptionImpl.createBuilder(Config.Language.class, STORAGE)
                .setName(Component.literal(LangUtil.tr("option.language")))
                .setTooltip(Component.literal(LangUtil.tr("option.language.tooltip")))
                .setBinding((data, value) -> Config.LANGUAGE.set(value),
                        data -> Config.LANGUAGE.get())
                .setControl(option -> new CyclingControl<>(option, Config.Language.class))
                .setImpact(OptionImpact.LOW)
                .build());

        return new OptionPage(Component.literal(LangUtil.tr("ribbon.page")), ImmutableList.of(group.build()));
    }
}