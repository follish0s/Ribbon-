package me.jellysquid.mods.sodium.mixin.core.world.map;

import me.jellysquid.mods.sodium.client.render.chunk.map.ChunkStatus;
import me.jellysquid.mods.sodium.client.render.chunk.map.ChunkTrackerHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    // ============================================================
    // 反射缓存：按名字或类型查找 level 字段
    // ============================================================
    @Unique
    private static Field embeddium$levelField;

    @Unique
    private ClientLevel embeddium$getLevel() {
        try {
            if (embeddium$levelField == null) {
                // 先按 Mojang / SRG 名字查找
                for (String name : new String[]{"level", "f_104988_"}) {
                    try {
                        Field f = ClientPacketListener.class.getDeclaredField(name);
                        f.setAccessible(true);
                        embeddium$levelField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {
                    }
                }
                // 名字都不对时，按类型匹配 ClientLevel 字段
                if (embeddium$levelField == null) {
                    for (Field f : ClientPacketListener.class.getDeclaredFields()) {
                        if (f.getType() == ClientLevel.class) {
                            f.setAccessible(true);
                            embeddium$levelField = f;
                            break;
                        }
                    }
                }
                if (embeddium$levelField == null) {
                    throw new RuntimeException("无法在 ClientPacketListener 中找到 ClientLevel 字段");
                }
            }
            return (ClientLevel) embeddium$levelField.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "applyLightData", at = @At("RETURN"))
    private void onLightDataReceived(int x, int z, ClientboundLightUpdatePacketData data, CallbackInfo ci) {
        ChunkTrackerHolder.get(this.embeddium$getLevel())
                .onChunkStatusAdded(x, z, ChunkStatus.FLAG_HAS_LIGHT_DATA);
    }

    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void onChunkUnloadPacket(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        ChunkTrackerHolder.get(this.embeddium$getLevel())
                .onChunkStatusRemoved(packet.getX(), packet.getZ(), ChunkStatus.FLAG_ALL);
    }
}