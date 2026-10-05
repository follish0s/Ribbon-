package me.jellysquid.mods.sodium.mixin.features.render.world;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Embeddium 的 ClientLevel 优化 Mixin
 *
 * 只保留 animateTick 的单线程随机数优化（避免 AtomicLong 开销）。
 * 原本的 doAnimateTick @Overwrite 优化因为 @Shadow 在 SRG 下找不到目标方法，
 * 已被移除。这个取舍不影响游戏功能，只是少了一点点内存分配上的优化。
 */
@Mixin(value = ClientLevel.class, priority = 500)
public abstract class ClientLevelMixin {

    /**
     * @author embeddedt
     * @reason Use singlethreaded random to avoid AtomicLong overhead
     */
    @Redirect(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"
            )
    )
    private RandomSource createLocal() {
        return new SingleThreadedRandomSource(RandomSupport.generateUniqueSeed());
    }
}