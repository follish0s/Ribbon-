package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Yarn ParticleManager -> Mojmap ParticleEngine.
 * Method name mapping:
 *   addBlockBreakParticles(BlockPos, BlockState)   -> destroy(BlockPos, BlockState)
 *   addBlockBreakingParticles(BlockPos, Direction) -> crack(BlockPos, Direction)
 *   addParticle(ParticleEffect, D,D,D,D,D,D)       -> createParticle(ParticleOptions, D,D,D,D,D,D)
 */
@Mixin(ParticleEngine.class)
public class MixinParticleManager {

    @Inject(method = "destroy(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(value = "HEAD"), cancellable = true)
    public void destroy(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().particleSettings.particles || !SodiumExtraClientMod.options().particleSettings.blockBreak) {
            ci.cancel();
        }
    }

    @Inject(method = "crack(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V",
            at = @At(value = "HEAD"), cancellable = true)
    public void crack(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().particleSettings.particles || !SodiumExtraClientMod.options().particleSettings.blockBreaking) {
            ci.cancel();
        }
    }

    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At(value = "HEAD"), cancellable = true)
    public void createParticle(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        if (SodiumExtraClientMod.options().particleSettings.particles) {
            ResourceLocation particleTypeId = BuiltInRegistries.PARTICLE_TYPE.getKey(parameters.getType());
            if (particleTypeId != null && !SodiumExtraClientMod.options().particleSettings.otherMap.getOrDefault(particleTypeId, true)) {
                cir.setReturnValue(null);
            }
        } else {
            cir.setReturnValue(null);
        }
    }
}
