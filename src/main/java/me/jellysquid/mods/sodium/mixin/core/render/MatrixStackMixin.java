package me.jellysquid.mods.sodium.mixin.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.embeddedt.embeddium.render.matrix_stack.CachingPoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value = PoseStack.class, priority = 1500)
public abstract class MatrixStackMixin implements CachingPoseStack {

    @Unique
    private static Field embeddium$poseStackField;
    @Unique
    private static boolean embeddium$fieldLookupDone = false;

    @Unique
    private static Field embeddium$findField() {
        if (embeddium$fieldLookupDone) return embeddium$poseStackField;
        embeddium$fieldLookupDone = true;

        // 1. 先按名字找
        for (String name : new String[]{"poseStack", "f_85811_"}) {
            try {
                Field f = PoseStack.class.getDeclaredField(name);
                if (Deque.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    embeddium$poseStackField = f;
                    return f;
                }
            } catch (NoSuchFieldException ignored) {}
        }

        // 2. 按类型找（Deque 类型的字段）
        for (Field f : PoseStack.class.getDeclaredFields()) {
            if (Deque.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                embeddium$poseStackField = f;
                return f;
            }
        }

        // 3. 找不到就返回 null，功能退化为原版
        return null;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private Deque<PoseStack.Pose> embeddium$getPoseStack() {
        Field f = embeddium$findField();
        if (f == null) return null;
        try {
            return (Deque<PoseStack.Pose>) f.get(this);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Unique
    private final Deque<PoseStack.Pose> embeddium$cache = new ArrayDeque<>();

    @Unique
    private int embeddium$cacheEnabled = 0;

    @Inject(method = "pushPose", at = @At("HEAD"), cancellable = true)
    private void embeddium$pushPose(CallbackInfo ci) {
        try {
            Deque<PoseStack.Pose> stack = this.embeddium$getPoseStack();
            if (stack == null || stack.isEmpty()) return; // 找不到字段 → 走原版

            var prev = stack.getLast();
            PoseStack.Pose entry;

            if (this.embeddium$cacheEnabled > 0 && !this.embeddium$cache.isEmpty()) {
                entry = this.embeddium$cache.removeLast();
                entry.pose().set(prev.pose());
                entry.normal().set(prev.normal());
            } else {
                entry = new PoseStack.Pose(new Matrix4f(prev.pose()), new Matrix3f(prev.normal()));
            }

            stack.addLast(entry);
            ci.cancel();
        } catch (Throwable t) {
            // 出任何异常都放行，走原版逻辑
        }
    }

    @Inject(method = "popPose", at = @At("HEAD"), cancellable = true)
    private void embeddium$popPose(CallbackInfo ci) {
        try {
            Deque<PoseStack.Pose> stack = this.embeddium$getPoseStack();
            if (stack == null || stack.isEmpty()) return; // 找不到字段 → 走原版

            PoseStack.Pose pose = stack.removeLast();
            if (this.embeddium$cacheEnabled > 0) {
                this.embeddium$cache.addLast(pose);
            }
            ci.cancel();
        } catch (Throwable t) {
            // 出任何异常都放行，走原版逻辑
        }
    }

    @Override
    public void embeddium$setCachingEnabled(boolean flag) {
        this.embeddium$cacheEnabled += flag ? 1 : -1;
    }
}