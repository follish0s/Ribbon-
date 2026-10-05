package me.jellysquid.mods.sodium.mixin.features.textures.animations.tracking;

import me.jellysquid.mods.sodium.client.render.texture.SpriteContentsExtended;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin implements SpriteContentsExtended {

    @Unique
    private boolean active;

    // 反射缓存
    @Unique
    private static Field embeddium$animatedTextureField;
    @Unique
    private static boolean embeddium$lookupDone = false;

    @Unique
    private static Field embeddium$findAnimatedTexture() {
        if (embeddium$lookupDone) return embeddium$animatedTextureField;
        embeddium$lookupDone = true;

        // 先按名字找
        for (String name : new String[]{"animatedTexture", "f_243732_"}) {
            try {
                Field f = SpriteContents.class.getDeclaredField(name);
                f.setAccessible(true);
                embeddium$animatedTextureField = f;
                return f;
            } catch (NoSuchFieldException ignored) {}
        }

        // 按类型找：SpriteContents.AnimatedTexture
        for (Field f : SpriteContents.class.getDeclaredFields()) {
            if (f.getType() == SpriteContents.AnimatedTexture.class) {
                f.setAccessible(true);
                embeddium$animatedTextureField = f;
                return f;
            }
        }

        return null;
    }

    @Unique
    private SpriteContents.AnimatedTexture embeddium$getAnimatedTexture() {
        Field f = embeddium$findAnimatedTexture();
        if (f == null) return null;
        try {
            return (SpriteContents.AnimatedTexture) f.get(this);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public void sodium$setActive(boolean value) {
        this.active = value;
    }

    @Override
    public boolean sodium$hasAnimation() {
        return this.embeddium$getAnimatedTexture() != null;
    }

    @Override
    public boolean sodium$isActive() {
        return this.active;
    }
}