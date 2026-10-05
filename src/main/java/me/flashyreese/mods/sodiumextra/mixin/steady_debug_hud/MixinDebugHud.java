package me.flashyreese.mods.sodiumextra.mixin.steady_debug_hud;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraftforge.client.gui.overlay.ForgeGui$ForgeDebugScreenOverlay")
public abstract class MixinDebugHud {

    @Unique private final List<String> leftTextCache = new ArrayList<>();
    @Unique private final List<String> rightTextCache = new ArrayList<>();
    @Unique private long nextTime = 0L;
    @Unique private boolean rebuild = true;

    @Inject(method = "update", remap = false, at = @At(value = "HEAD"), cancellable = true)
    public void preRender(CallbackInfo ci) {
        if (SodiumExtraClientMod.options().extraSettings.steadyDebugHud) {
            final long currentTime = Util.getMillis();
            if (currentTime > this.nextTime) {
                this.rebuild = true;
                this.nextTime = currentTime + (SodiumExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval * 50L);
            } else {
                this.rebuild = false;
                ci.cancel();
            }
        } else {
            this.rebuild = true;
        }
    }

    @Inject(method = "getLeft", remap = false, at = @At(value = "HEAD"), cancellable = true)
    public void ribbonExtra$getLeftText0(CallbackInfoReturnable<List<String>> cir) {
        if (!this.rebuild) {
            cir.setReturnValue(this.leftTextCache);
        }
    }

    @Inject(method = "getLeft", remap = false, at = @At(value = "RETURN"))
    public void ribbonExtra$getLeftText1(CallbackInfoReturnable<List<String>> cir) {
        if (this.rebuild) {
            this.leftTextCache.clear();
            this.leftTextCache.addAll(cir.getReturnValue());
        }
    }

    @Inject(method = "getRight", remap = false, at = @At(value = "HEAD"), cancellable = true)
    public void ribbonExtra$getRightText0(CallbackInfoReturnable<List<String>> cir) {
        if (!this.rebuild) {
            cir.setReturnValue(this.rightTextCache);
        }
    }

    @Inject(method = "getRight", remap = false, at = @At(value = "RETURN"))
    public void ribbonExtra$getRightText1(CallbackInfoReturnable<List<String>> cir) {
        if (this.rebuild) {
            this.rightTextCache.clear();
            this.rightTextCache.addAll(cir.getReturnValue());
        }
    }
}