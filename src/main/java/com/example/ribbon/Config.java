package com.example.ribbon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Ribbon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    // FPS/TPS Overlay 配置
    public static final ForgeConfigSpec.BooleanValue SHOW_OVERLAY;
    public static final ForgeConfigSpec.EnumValue<OverlayCorner> OVERLAY_CORNER;
    public static final ForgeConfigSpec.DoubleValue OVERLAY_SCALE;

    // 描边配置
    public static final ForgeConfigSpec.BooleanValue RAINBOW_BLOCK_OUTLINE;
    public static final ForgeConfigSpec.EnumValue<OutlineColorMode> OUTLINE_COLOR_MODE;

    // GUI 配置
    public static final ForgeConfigSpec.BooleanValue RAINBOW_CROSSHAIR;
    public static final ForgeConfigSpec.BooleanValue HOTBAR_HIGHLIGHT;
    public static final ForgeConfigSpec.BooleanValue DAMAGE_NUMBERS;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_NUMBER_SCALE;

    // 语言切换
    public static final ForgeConfigSpec.EnumValue<Language> LANGUAGE;

    // 生物群系标题
    public static final ForgeConfigSpec.BooleanValue BIOME_TITLE;
    public static final ForgeConfigSpec.IntValue BIOME_TITLE_DURATION;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("fps_tps_overlay");
        SHOW_OVERLAY = BUILDER
                .comment("Show the FPS/TPS overlay on screen")
                .define("showOverlay", true);
        OVERLAY_CORNER = BUILDER
                .comment("Which corner to draw the overlay in")
                .defineEnum("overlayCorner", OverlayCorner.TOP_LEFT);
        OVERLAY_SCALE = BUILDER
                .comment("Text size multiplier (0.5 = half, 2.0 = double)")
                .defineInRange("overlayScale", 1.0, 0.5, 3.0);
        BUILDER.pop();

        BUILDER.push("render_settings");
        RAINBOW_BLOCK_OUTLINE = BUILDER
                .comment("Enable custom block outline")
                .define("rainbowBlockOutline", true);
        OUTLINE_COLOR_MODE = BUILDER
                .comment("The color mode of the block outline")
                .defineEnum("outlineColorMode", OutlineColorMode.RAINBOW);
        BUILDER.pop();

        BUILDER.push("gui_settings");
        RAINBOW_CROSSHAIR = BUILDER
                .comment("Enable rainbow crosshair")
                .define("rainbowCrosshair", false);
        HOTBAR_HIGHLIGHT = BUILDER
                .comment("Highlight the selected hotbar slot with a rainbow border")
                .define("hotbarHighlight", false);
        DAMAGE_NUMBERS = BUILDER
                .comment("Show floating damage numbers when hitting entities")
                .define("damageNumbers", false);
        DAMAGE_NUMBER_SCALE = BUILDER
                .comment("Damage number size multiplier")
                .defineInRange("damageNumberScale", 2.0, 0.5, 5.0);
        BIOME_TITLE = BUILDER
                .comment("Show an RPG-style title when entering a new biome")
                .define("biomeTitle", true);
        BIOME_TITLE_DURATION = BUILDER
                .comment("How long the biome title stays on screen (milliseconds)")
                .defineInRange("biomeTitleDuration", 3000, 1000, 10000);
        BUILDER.pop();

        BUILDER.push("misc");
        LANGUAGE = BUILDER
                .comment("Language used by the Ribbon GUI. Restart the video settings screen after switching.")
                .defineEnum("language", Language.ENGLISH);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }

    public enum OverlayCorner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public enum OutlineColorMode {
        RAINBOW,
        BLUE_PURPLE,
        GREEN_PURPLE
    }

    public enum Language {
        ENGLISH,
        CHINESE
    }
}