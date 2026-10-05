package me.jellysquid.mods.sodium.mixin.features.render.immediate.buffer_builder;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import me.jellysquid.mods.sodium.client.buffer.ExtendedVertexFormat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin extends DefaultedVertexConsumer implements BufferVertexConsumer {
    @Shadow
    private VertexFormatElement currentElement;

    @Shadow
    private int nextElementByte;

    @Shadow
    private int elementIndex;

    // 明确告诉编译器这些字段是浮点数，防止它当成 Object
    @Shadow
    protected float defaultR;

    @Shadow
    protected float defaultG;

    @Shadow
    protected float defaultB;

    @Shadow
    protected float defaultA;

    @Shadow
    protected boolean defaultColorSet;

    // 这些字段用于存储 Embeddium 的扩展顶点格式数据
    private ExtendedVertexFormat.Element[] embeddium$vertexFormatExtendedElements;
    private ExtendedVertexFormat.Element embeddium$currentExtendedElement;

    /**
     * 当 BufferBuilder 切换顶点格式时触发
     * 我们在这里安全地获取扩展数据，如果是原版格式就不处理，防止崩溃
     */
    @Inject(method = "switchFormat",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;format:Lcom/mojang/blaze3d/vertex/VertexFormat;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void onFormatChanged(VertexFormat format, CallbackInfo ci) {
        // 核心修复：判断是不是 Embeddium 的扩展格式
        if (format instanceof ExtendedVertexFormat extendedFormat) {
            // 如果是，就安全地读取扩展数据
            embeddium$vertexFormatExtendedElements = extendedFormat.embeddium$getExtendedElements();
            embeddium$currentExtendedElement = embeddium$vertexFormatExtendedElements[0];
        } else {
            // 如果是原版格式（比如游戏初始化时），将其置空，后续走原版逻辑
            embeddium$vertexFormatExtendedElements = null;
            embeddium$currentExtendedElement = null;
        }
    }

    /**
     * 核心优化：移除原版的取模运算、递归和列表查找
     * 修改前是 @Overwrite（强制霸占），会导致原版格式崩溃
     * 修改后是 @Inject + 拦截，只有遇到扩展格式才执行这个优化，否则原版逻辑正常运行
     */
    @Inject(method = "nextElement", at = @At("HEAD"), cancellable = true)
    public void nextElement(CallbackInfo ci) {
        // 只有当扩展数据确实存在时，才执行 Embeddium 的极速逻辑
        if (embeddium$vertexFormatExtendedElements != null && embeddium$currentExtendedElement != null) {
            // 这是 Embeddium 的优化算法，只对扩展格式生效，用来替代原版较慢的代码
            if ((elementIndex += embeddium$currentExtendedElement.increment) >= embeddium$vertexFormatExtendedElements.length) {
                elementIndex -= embeddium$vertexFormatExtendedElements.length;
            }
            nextElementByte += embeddium$currentExtendedElement.byteLength;
            embeddium$currentExtendedElement = embeddium$vertexFormatExtendedElements[elementIndex];
            currentElement = embeddium$currentExtendedElement.actual;

            if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
                // 这里加入了 (int) 强制转换，告诉编译器把浮点数值转成整数，彻底解决报错
                BufferVertexConsumer.super.color((int) this.defaultR, (int) this.defaultG, (int) this.defaultB, (int) this.defaultA);
            }

            // 重要：执行完优化逻辑后，取消原版逻辑，防止重复执行
            ci.cancel();
        }
        // 如果扩展数据为空（说明是原版格式），这里什么都不做，原版游戏的 nextElement() 会自然执行
    }
}