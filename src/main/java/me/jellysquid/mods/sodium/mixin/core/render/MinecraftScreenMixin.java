package me.jellysquid.mods.sodium.mixin.core.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(Minecraft.class)
public class MinecraftScreenMixin {

    @Unique
    private boolean ribbon$playerReadySeen = false;

    @Unique
    private static Field ribbon$playerField;
    @Unique
    private static Field ribbon$levelField;

    @Unique
    private static Field ribbon$findField(Class<?> type) {
        for (Field f : Minecraft.class.getDeclaredFields()) {
            if (f.getType() == type) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    @Unique
    private Object ribbon$getPlayer(Minecraft mc) {
        try {
            if (ribbon$playerField == null) {
                ribbon$playerField = ribbon$findField(net.minecraft.client.player.LocalPlayer.class);
            }
            return ribbon$playerField == null ? null : ribbon$playerField.get(mc);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Unique
    private Object ribbon$getLevel(Minecraft mc) {
        try {
            if (ribbon$levelField == null) {
                ribbon$levelField = ribbon$findField(net.minecraft.client.multiplayer.ClientLevel.class);
            }
            return ribbon$levelField == null ? null : ribbon$levelField.get(mc);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void ribbon$blockCloseUntilPlayerReady(Screen screen, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;

        if (ribbon$playerReadySeen) return;

        if (ribbon$getPlayer(mc) != null) {
            ribbon$playerReadySeen = true;
            return;
        }

        if (screen == null && ribbon$getLevel(mc) != null) {
            ci.cancel();
        }
    }
}