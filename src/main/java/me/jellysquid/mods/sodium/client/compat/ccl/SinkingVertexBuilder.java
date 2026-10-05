package me.jellysquid.mods.sodium.client.compat.ccl;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3fc;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public final class SinkingVertexBuilder implements VertexConsumer {
    private static final int VERTEX_SIZE_BYTES = 32;
    private static final int INITIAL_CAPACITY = 16384;
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private ByteBuffer buffer = EMPTY_BUFFER;

    private final int[] sideCount = new int[ModelQuadFacing.VALUES.length];
    private int currentVertex;

    private float x;
    private float y;
    private float z;
    private float u;
    private float v;
    private int color;
    private int light;

    private int fixedColor;
    private boolean hasFixedColor = false;

    private final ChunkVertexEncoder.Vertex[] sodiumVertexArray = ChunkVertexEncoder.Vertex.uninitializedQuad();
    private final ModelQuadView previousQuad = new ModelQuadView() {

        private int getBaseIndex(int idx, int offset) {
            return (currentVertex - 4 + idx) * VERTEX_SIZE_BYTES + (offset * 4);
        }

        @Override
        public float getX(int idx) {
            return buffer.getFloat(getBaseIndex(idx, 1));
        }

        @Override
        public float getY(int idx) {
            return buffer.getFloat(getBaseIndex(idx, 2));
        }

        @Override
        public float getZ(int idx) {
            return buffer.getFloat(getBaseIndex(idx, 3));
        }

        @Override
        public int getColor(int idx) {
            return buffer.getInt(getBaseIndex(idx, 4));
        }

        @Override
        public float getTexU(int idx) {
            return buffer.getFloat(getBaseIndex(idx, 5));
        }

        @Override
        public float getTexV(int idx) {
            return buffer.getFloat(getBaseIndex(idx, 6));
        }

        @Override
        public int getLight(int idx) {
            return buffer.getInt(getBaseIndex(idx, 7));
        }

        @Override
        public int getFlags() {
            return 0;
        }

        @Override
        public int getColorIndex() {
            return 0;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return null;
        }

        @Override
        public Direction getLightFace() {
            return null;
        }

        @Override
        public int getForgeNormal(int idx) {
            return 0;
        }

        @Override
        public int getComputedFaceNormal() {
            return 0;
        }
    };

    private static ByteBuffer reallocDirect(ByteBuffer old, int capacity) {
        ByteBuffer newBuf = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        int oldPos = old.position();
        old.rewind();
        newBuf.put(old);
        newBuf.position(Math.min(capacity, oldPos));
        old.position(oldPos);
        return newBuf;
    }

    @Nonnull
    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        color = ((a & 255) << 24) | ((b & 255) << 16) | ((g & 255) << 8) | (r & 255);
        return this;
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        fixedColor = ((a & 255) << 24) | ((b & 255) << 16) | ((g & 255) << 8) | (r & 255);
        hasFixedColor = true;
    }

    @Override
    public void unsetDefaultColor() {
        hasFixedColor = false;
    }

    @Nonnull
    @Override
    public VertexConsumer uv(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer uv2(int u, int v) {
        light = (v << 16) | u;
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this;
    }

    @Override
    public void endVertex() {
        if ((this.buffer.capacity() - this.buffer.position()) < VERTEX_SIZE_BYTES) {
            int newCapacity = this.buffer.capacity() * 2;
            if (newCapacity == 0) {
                newCapacity = INITIAL_CAPACITY;
            }
            this.buffer = reallocDirect(this.buffer, newCapacity);
        }

        ByteBuffer buffer = this.buffer;

        buffer.putInt(-1);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(u);
        buffer.putFloat(v);
        buffer.putInt(hasFixedColor ? fixedColor : color);
        buffer.putInt(light);

        resetCurrentVertex();
        currentVertex++;
        if((currentVertex % 4) == 0) {
            recalculateNormals();
        }
    }

    private void recalculateNormals() {
        ModelQuadFacing normal = ModelQuadUtil.findNormalFace(ModelQuadUtil.calculateNormal(this.previousQuad));
        buffer.putInt((currentVertex - 4) * VERTEX_SIZE_BYTES, normal.ordinal());
    }

    public void reset() {
        if(currentVertex != 0) {
            buffer.rewind();
            currentVertex = 0;
            Arrays.fill(sideCount, 0);
        }
        resetCurrentVertex();
    }

    public boolean isEmpty() {
        return currentVertex == 0;
    }

    public boolean flush(@Nonnull ChunkModelBuilder buffers, Material material, Vector3fc origin) {
        return flush(buffers, material, origin.x(), origin.y(), origin.z());
    }

    public boolean flush(@Nonnull ChunkModelBuilder buffers, Material material, float oX, float oY, float oZ) {
        if(currentVertex == 0) {
            return false;
        }

        final int numQuads = currentVertex >> 2;

        for (int quadIdx = 0; quadIdx < numQuads; quadIdx++) {
            final int normal = buffer.getInt((quadIdx << 2) << 5);
            sideCount[normal]++;
        }

        final int byteSize = currentVertex << 5;
        byte sideMask = 0;

        buffer.rewind();

        while (buffer.position() < byteSize) {
            final int normal = buffer.getInt();
            final ModelQuadFacing facing = ModelQuadFacing.VALUES[normal];
            final int facingIdx = facing.ordinal();

            final ChunkMeshBufferBuilder sink = buffers.getVertexBuffer(facing);

            ChunkVertexEncoder.Vertex[] sodiumQuad = sodiumVertexArray;

            float midU = 0, midV = 0;

            for(int i = 0; i < 4; i++) {
                if(i != 0)
                    buffer.getInt();

                ChunkVertexEncoder.Vertex sodiumVertex = sodiumQuad[i];
                sodiumVertex.x = oX + buffer.getFloat();
                sodiumVertex.y = oY + buffer.getFloat();
                sodiumVertex.z = oZ + buffer.getFloat();
                sodiumVertex.u = buffer.getFloat();
                sodiumVertex.v = buffer.getFloat();
                midU += sodiumVertex.u;
                midV += sodiumVertex.v;
                sodiumVertex.color = buffer.getInt();
                sodiumVertex.light = buffer.getInt();
            }

            sink.push(sodiumQuad, material);

            sideMask |= 1 << facingIdx;
        }

        return true;
    }

    private void resetCurrentVertex() {
        x = y = z = 0F;
        u = v = 0F;
        color = 0xFFFF_FFFF;
        light = 0;
    }
}