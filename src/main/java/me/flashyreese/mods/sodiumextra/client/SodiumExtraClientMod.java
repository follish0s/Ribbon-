package me.flashyreese.mods.sodiumextra.client;

import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptions;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Ported from Embeddium Extra (LGPL-3.0-only, (c) dima_dencep / FlashyReese).
 * The upstream version used CaffeineConfig (bundled by Embeddium 0.3.x). Embeddium 0.5.8
 * dropped CaffeineConfig, so mixin gating is done here with a plain properties file.
 */
public class SodiumExtraClientMod {
    public static final String MOD_ID = "embeddium_extra";
    public static final Logger LOGGER = LogManager.getLogger("Ribbon Extra");

    private static SodiumExtraGameOptions CONFIG;

    /** Mixin option names that the user turned off in config/sodium-extra-mixins.properties. */
    private static final Set<String> DISABLED_MIXINS = new HashSet<>();

    private static final String[] KNOWN_MIXINS = {
            "adaptive_sync", "animation", "biome_colors", "cloud", "fog", "fog_falloff",
            "instant_sneak", "light_updates", "particle", "prevent_shaders", "profiler",
            "reduce_resolution_on_mac", "render.block.entity", "render.entity", "sky",
            "sky_colors", "stars", "steady_debug_hud", "sun_moon", "toasts",
            "sodium.accessibility", "sodium.cloud", "sodium.fog", "sodium.resolution", "sodium.vsync",
            "gui"
    };

    public static SodiumExtraGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }
        return CONFIG;
    }

    /** True when the given behaviour mixin should be applied. */
    public static boolean mixinEnabled(String name) {
        ensureMixinConfigLoaded();
        return !DISABLED_MIXINS.contains(name);
    }

    public static Set<String> disabledMixins() {
        ensureMixinConfigLoaded();
        return Collections.unmodifiableSet(DISABLED_MIXINS);
    }

    private static boolean MIXIN_CONFIG_LOADED = false;

    private static synchronized void ensureMixinConfigLoaded() {
        if (MIXIN_CONFIG_LOADED) {
            return;
        }
        MIXIN_CONFIG_LOADED = true;

        File file = FMLPaths.CONFIGDIR.get().resolve("sodium-extra-mixins.properties").toFile();

        if (!file.exists()) {
            Properties props = new Properties();
            for (String name : KNOWN_MIXINS) {
                props.setProperty("mixin." + name, "true");
            }
            try (OutputStream out = new FileOutputStream(file)) {
                props.store(out, "Ribbon Extra mixin toggles (port of sodium-extra.properties)");
            } catch (IOException e) {
                LOGGER.warn("Could not write default mixin configuration", e);
            }
            return;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            LOGGER.warn("Could not read mixin configuration", e);
            return;
        }

        for (String key : props.stringPropertyNames()) {
            if (!key.startsWith("mixin.")) {
                continue;
            }
            String name = key.substring("mixin.".length());
            if (!"true".equalsIgnoreCase(props.getProperty(key))) {
                DISABLED_MIXINS.add(name);
            }
        }
    }

    private static SodiumExtraGameOptions loadConfig() {
        return SodiumExtraGameOptions.load(
                FMLPaths.CONFIGDIR.get().resolve("sodium-extra-options.json").toFile());
    }
}
