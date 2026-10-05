package me.jellysquid.mods.sodium.mixin.features.gui.hooks.settings;

import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class MixinVideoSettingsScreen extends OptionsSubScreen {

    // 1.20.1 原版构造器需要这三个参数，继承父类必须提供
    protected MixinVideoSettingsScreen(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ribbon$replaceWithEmbeddium(CallbackInfo ci) {
        // 在 init 末尾执行，此时 Oculus 的光影包按钮已经注入完毕，不会崩
        Minecraft mc = Minecraft.getInstance();

        // 直接获取父类 OptionsSubScreen 里的 lastScreen 字段
        Screen parent = this.lastScreen;

        // 延迟一帧替换，防止在 init 执行中途切换界面导致崩溃
        mc.execute(() -> {
            if (mc.screen == this) { // 确保当前还是这个界面，防止意外
                mc.setScreen(new SodiumOptionsGUI(parent));
            }
        });
    }
}