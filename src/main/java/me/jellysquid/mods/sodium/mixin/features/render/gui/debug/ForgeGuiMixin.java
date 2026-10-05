package me.jellysquid.mods.sodium.mixin.features.render.gui.debug;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Mixin(DebugScreenOverlay.class)
public class ForgeGuiMixin {
    @Inject(method = "renderLines", at = @At("HEAD"), cancellable = true)
    private void renderLinesVanilla(GuiGraphics guiGraphics, CallbackInfo ci) {
        try {
            Field leftField = DebugScreenOverlay.class.getDeclaredField("renderLinesLeft");
            Field rightField = DebugScreenOverlay.class.getDeclaredField("renderLinesRight");
            leftField.setAccessible(true);
            rightField.setAccessible(true);
            ArrayList<String> listL = (ArrayList<String>) leftField.get(this);
            ArrayList<String> listR = (ArrayList<String>) rightField.get(this);
            // 这里放你原本需要的逻辑，或者留空
        } catch (Exception e) {
            // 反射失败就不处理，直接走原版逻辑
        }
    }
}