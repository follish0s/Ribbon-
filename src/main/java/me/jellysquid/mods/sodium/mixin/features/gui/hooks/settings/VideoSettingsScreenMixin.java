package me.jellysquid.mods.sodium.mixin.features.gui.hooks.settings;

import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends Screen {

    protected VideoSettingsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ribbon$addButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(
                Component.literal("Ribbon 设置"),
                btn -> Minecraft.getInstance().setScreen(new SodiumOptionsGUI(this))
        ).bounds(this.width / 2 - 100, this.height - 50, 200, 20).build());
    }
}