package me.jellysquid.mods.sodium.client.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteUtil {

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (sprite.contents() instanceof SpriteContentsExtended extended) {
            extended.sodium$setActive(true);
        }
    }

    public static boolean hasAnimation(TextureAtlasSprite sprite) {
        if (sprite.contents() instanceof SpriteContentsExtended extended) {
            return extended.sodium$hasAnimation();
        }
        return false;
    }
}