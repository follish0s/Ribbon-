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

    // 空壳存储：我们的值在静态 Config 里，getData() 用不到
    private static final OptionStorage<Object> STORAGE = new OptionStorage<Object>() {
        private final Object data = new Object();
        @Override public Object getData() { return data; }
        @Override public void save() { Config.SPEC.save(); } // 点"应用"时写回 ribbon 配置
    };

    // 在 mod 加载早期调用一次
    public static void init() {
        OptionGUIConstructionEvent.BUS.addListener(event -> event.addPage(buildPage()));
    }

    private static OptionPage buildPage() {
        OptionGroup.Builder group = OptionGroup.createBuilder();

        // ① 显示开关
        group.add(OptionImpl.createBuilder(Boolean.class, STORAGE)
                .setName(Component.literal("Show FPS/TPS overlay"))
                .setTooltip(Component.literal("Show the FPS/TPS text on the HUD"))
                .setBinding((data, value) -> Config.SHOW_OVERLAY.set(value),
                        data -> Config.SHOW_OVERLAY.get())
                .setControl(option -> new TickBoxControl(option))
                .setImpact(OptionImpact.LOW)
                .build());

        // ② 显示位置（四个角）
        group.add(OptionImpl.createBuilder(Config.OverlayCorner.class, STORAGE)
                .setName(Component.literal("Overlay position"))
                .setTooltip(Component.literal("Which screen corner the overlay is drawn in"))
                .setBinding((data, value) -> Config.OVERLAY_CORNER.set(value),
                        data -> Config.OVERLAY_CORNER.get())
                .setControl(option -> new CyclingControl<>(option, Config.OverlayCorner.class))
                .setImpact(OptionImpact.LOW)
                .build());

        // ③ 文字大小（0.5x ~ 3.0x，步进 0.1）
        group.add(OptionImpl.createBuilder(Integer.class, STORAGE)
                .setName(Component.literal("Overlay scale"))
                .setTooltip(Component.literal("Text size multiplier for the overlay text"))
                .setBinding((data, value) -> Config.OVERLAY_SCALE.set(value / 10.0),
                        data -> (int) Math.round(Config.OVERLAY_SCALE.get() * 10.0))
                .setControl(option -> new SliderControl(option, 5, 30, 1,
                        v -> Component.literal(String.format("%.1fx", v / 10.0))))
                .setImpact(OptionImpact.LOW)
                .build());

        return new OptionPage(Component.literal("Ribbon"), ImmutableList.of(group.build()));
    }
}