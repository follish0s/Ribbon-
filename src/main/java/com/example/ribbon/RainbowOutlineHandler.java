package com.example.ribbon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "ribbon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RainbowOutlineHandler {

    @SubscribeEvent
    public static void onRenderHighlight(RenderHighlightEvent.Block event) {
        // 如果配置关闭，直接返回，让原版黑框渲染
        if (!Config.RAINBOW_BLOCK_OUTLINE.get()) {
            return;
        }

        // 取消原版黑框
        event.setCanceled(true);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        Camera camera = event.getCamera();
        BlockHitResult target = event.getTarget();

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockState state = level.getBlockState(target.getBlockPos());
        VoxelShape shape = state.getShape(level, target.getBlockPos());
        if (shape.isEmpty()) return;

        // 计算颜色，支持三种模式
        float time = (System.currentTimeMillis() % 3000) / 3000.0F;
        float r, g, b;

        Config.OutlineColorMode mode = Config.OUTLINE_COLOR_MODE.get();
        switch (mode) {
            case BLUE_PURPLE:
                // 蓝紫渐变
                r = Mth.sin(time * (float) Math.PI * 2) * 0.5F + 0.5F;
                g = 0.0F;
                b = 1.0F;
                break;
            case GREEN_PURPLE:
                // 绿紫渐变
                r = Mth.sin(time * (float) Math.PI * 2) * 0.5F + 0.5F;
                g = Mth.sin((time + 1.0F / 3.0F) * (float) Math.PI * 2) * 0.5F + 0.5F;
                b = 1.0F - g;
                break;
            case RAINBOW:
            default:
                // 彩虹模式
                r = Mth.sin(time * (float) Math.PI * 2) * 0.5F + 0.5F;
                g = Mth.sin((time + 1.0F / 3.0F) * (float) Math.PI * 2) * 0.5F + 0.5F;
                b = Mth.sin((time + 2.0F / 3.0F) * (float) Math.PI * 2) * 0.5F + 0.5F;
                break;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableCull();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // 使用自己实现的 renderShape，替代不存在的 ShapeRenderer
        renderShape(poseStack, consumer, shape,
                target.getBlockPos().getX(), target.getBlockPos().getY(), target.getBlockPos().getZ(),
                r, g, b, 1.0F);

        poseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * 手动实现原版 LevelRenderer.renderShape 的逻辑。
     * 关键点：为每个顶点补上 .normal() 调用，匹配 RenderType.lines() 的 POSITION_COLOR_NORMAL 格式。
     */
    private static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                    double x, double y, double z, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float f = (float) (x1 + x);
            float f1 = (float) (y1 + y);
            float f2 = (float) (z1 + z);
            float f3 = (float) (x2 + x);
            float f4 = (float) (y2 + y);
            float f5 = (float) (z2 + z);

            // 第一个顶点：位置 + 颜色 + 法线
            VertexConsumer vc1 = consumer.vertex(matrix, f, f1, f2);
            vc1.color(r, g, b, a);
            vc1.normal(1, 0, 0);
            vc1.endVertex();

            // 第二个顶点：位置 + 颜色 + 法线
            VertexConsumer vc2 = consumer.vertex(matrix, f3, f4, f5);
            vc2.color(r, g, b, a);
            vc2.normal(1, 0, 0);
            vc2.endVertex();
        });
    }
}