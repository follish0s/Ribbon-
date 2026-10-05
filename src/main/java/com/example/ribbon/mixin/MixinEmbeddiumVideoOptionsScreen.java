package com.example.ribbon.mixin;

import com.example.ribbon.Config;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "org.embeddedt.embeddium.client.gui.options.EmbeddiumVideoOptionsScreen")
public abstract class MixinEmbeddiumVideoOptionsScreen {

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    @Inject(method = "init", at = @At("TAIL"))
    private void ribbon$addRainbowButton(CallbackInfo ci) {
        Button rainbowButton = Button.builder(
                Component.literal("彩虹描边: " + (Config.RAINBOW_BLOCK_OUTLINE.get() ? "开" : "关")),
                button -> {
                    boolean newState = !Config.RAINBOW_BLOCK_OUTLINE.get();
                    Config.RAINBOW_BLOCK_OUTLINE.set(newState);
                    Config.SPEC.save();
                    button.setMessage(Component.literal("彩虹描边: " + (newState ? "开" : "关")));
                }
        ).bounds(10, 10, 200, 20).build();

        this.addRenderableWidget(rainbowButton);
    }
}