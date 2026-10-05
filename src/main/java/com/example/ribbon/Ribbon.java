package com.example.ribbon;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("ribbon")
public class Ribbon {

    public static final String MODID = "ribbon";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Ribbon(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        // 注册 Ribbon 的配置文件（FPS/TPS 悬浮窗的设置）
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Embeddium 的选项界面是纯客户端代码，服务端不能加载这些类
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            // 初始化 Embeddium UI 的扩展页面（Ribbon 自己的 FPS/TPS 页面）
            RibbonOptions.init();

            // 初始化从 Embeddium Extra 移植过来的 animation/particle/detail/render/extra 5 个页面
            me.flashyreese.mods.sodiumextra.client.SodiumExtraOptions.init();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}