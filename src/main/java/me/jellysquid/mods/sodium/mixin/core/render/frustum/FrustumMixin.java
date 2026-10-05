package me.jellysquid.mods.sodium.mixin.core.render.frustum;

import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Frustum.class)
public class FrustumMixin implements ViewportProvider {

    @Override
    public Viewport sodium$createViewport() {
        // 从 Minecraft 的 Camera 拿世界坐标
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        // 直接复用 Frustum 自己的 isVisible，不再反射任何字段
        Frustum self = (Frustum) (Object) this;

        return new Viewport(
                (minX, minY, minZ, maxX, maxY, maxZ) ->
                        self.isVisible(new AABB(minX, minY, minZ, maxX, maxY, maxZ)),
                new Vector3d(camPos.x, camPos.y, camPos.z));
    }
}