package me.jellysquid.mods.sodium.mixin.features.render.gui.font;

import net.caffeinemc.mods.sodium.api.vertex.format.common.GlyphVertex;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mixin(BakedGlyph.class)
public class GlyphRendererMixin {

    // ============================================================
    // 反射缓存：一次性找到 8 个 float 字段，之后复用
    // ============================================================
    @Unique
    private static Field[] embeddium$fields;

    @Unique
    private static Field[] embeddium$getFields() {
        if (embeddium$fields != null) return embeddium$fields;

        String[] names = {"left", "right", "up", "down", "u0", "v0", "u1", "v1"};
        Field[] result = new Field[8];

        // 先尝试按原版字段名查找
        boolean byName = true;
        for (int i = 0; i < 8; i++) {
            try {
                Field f = BakedGlyph.class.getDeclaredField(names[i]);
                f.setAccessible(true);
                result[i] = f;
            } catch (NoSuchFieldException e) {
                byName = false;
                break;
            }
        }

        if (byName) {
            embeddium$fields = result;
            return result;
        }

        // 名字对不上（SRG 环境），按「float 类型 + 声明顺序」匹配
        List<Field> floats = new ArrayList<>();
        for (Field f : BakedGlyph.class.getDeclaredFields()) {
            if (f.getType() == float.class) {
                f.setAccessible(true);
                floats.add(f);
            }
        }
        if (floats.size() < 8) {
            throw new RuntimeException("BakedGlyph 中 float 字段不足 8 个，实际 " + floats.size());
        }
        for (int i = 0; i < 8; i++) {
            result[i] = floats.get(i);
        }

        embeddium$fields = result;
        return result;
    }

    @Unique
    private float embeddium$get(int index) {
        try {
            return embeddium$getFields()[index].getFloat(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ============================================================
    // 注入逻辑保持不变，只把字段访问换成反射
    // ============================================================

    /**
     * @reason Use intrinsics
     * @author JellySquid
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderFast(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, CallbackInfo ci) {
        if (drawFast(italic, x, y, matrix, vertexConsumer, red, green, blue, alpha, light)) {
            ci.cancel();
        }
    }

    private boolean drawFast(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
        var writer = VertexBufferWriter.tryOf(vertexConsumer);

        if (writer == null)
            return false;

        // 索引映射：0=left, 1=right, 2=up, 3=down, 4=u0, 5=v0, 6=u1, 7=v1
        float left = embeddium$get(0);
        float right = embeddium$get(1);
        float up = embeddium$get(2);
        float down = embeddium$get(3);
        float u0 = embeddium$get(4);
        float v0 = embeddium$get(5);
        float u1 = embeddium$get(6);
        float v1 = embeddium$get(7);

        float x1 = x + left;
        float x2 = x + right;
        float y1 = up - 3.0F;
        float y2 = down - 3.0F;
        float h1 = y + y1;
        float h2 = y + y2;
        float w1 = italic ? 1.0F - 0.25F * y1 : 0.0F;
        float w2 = italic ? 1.0F - 0.25F * y2 : 0.0F;

        int color = ColorABGR.pack(red, green, blue, alpha);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long buffer = stack.nmalloc(4 * GlyphVertex.STRIDE);
            long ptr = buffer;

            write(ptr, matrix, x1 + w1, h1, 0.0F, color, u0, v0, light);
            ptr += GlyphVertex.STRIDE;

            write(ptr, matrix, x1 + w2, h2, 0.0F, color, u0, v1, light);
            ptr += GlyphVertex.STRIDE;

            write(ptr, matrix, x2 + w2, h2, 0.0F, color, u1, v1, light);
            ptr += GlyphVertex.STRIDE;

            write(ptr, matrix, x2 + w1, h1, 0.0F, color, u1, v0, light);
            ptr += GlyphVertex.STRIDE;

            writer.push(stack, buffer, 4, GlyphVertex.FORMAT);
        }

        return true;
    }

    @Unique
    private static void write(long buffer,
                              Matrix4f matrix, float x, float y, float z, int color, float u, float v, int light) {
        float x2 = MatrixHelper.transformPositionX(matrix, x, y, z);
        float y2 = MatrixHelper.transformPositionY(matrix, x, y, z);
        float z2 = MatrixHelper.transformPositionZ(matrix, x, y, z);

        GlyphVertex.put(buffer, x2, y2, z2, color, u, v, light);
    }
}