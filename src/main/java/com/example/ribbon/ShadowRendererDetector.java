package com.example.ribbon;

import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;

/**
 * 通过 Iris 公开 API (IrisApi) 检测当前是否在渲染阴影。
 *
 * 这是 Iris 官方为外部模组提供的稳定接口，Oculus 作为 Iris 的 Forge 移植版
 * 完整实现了该 API。
 */
public class ShadowRendererDetector {

    private static boolean initialized = false;
    private static Method isRenderingShadowPassMethod = null;
    private static boolean logged = false;

    public static boolean isShadowPassActive() {
        if (!initialized) {
            init();
        }

        if (isRenderingShadowPassMethod == null) {
            return false;
        }

        try {
            boolean result = (Boolean) isRenderingShadowPassMethod.invoke(null);

            // 只在第一次成功调用时打印一条日志，避免刷屏
            if (!logged) {
                logged = true;
                System.out.println("[Ribbon] Iris API 阴影检测已启用，当前状态: " + result);
            }

            return result;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void init() {
        initialized = true;

        // 优先检查 Oculus/Iris 是否已加载
        if (!ModList.get().isLoaded("oculus") && !ModList.get().isLoaded("iris")) {
            System.out.println("[Ribbon] 未检测到 Oculus 或 Iris，阴影检测已禁用");
            return;
        }

        // Iris 公开 API 的类名（Oculus 的包名可能不同）
        String[] apiClassNames = new String[]{
                "net.irisshaders.iris.api.v0.IrisApi",
                "net.coderbot.iris.api.v0.IrisApi"
        };

        Class<?> apiClass = null;
        for (String name : apiClassNames) {
            try {
                apiClass = Class.forName(name);
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (apiClass == null) {
            System.out.println("[Ribbon] 找到 Oculus/Iris，但未找到 IrisApi 类，阴影检测已禁用");
            return;
        }

        // 获取 IrisApi 的单例实例
        Object apiInstance = null;
        try {
            Method getInstanceMethod = apiClass.getMethod("getInstance");
            apiInstance = getInstanceMethod.invoke(null);
        } catch (Throwable ignored) {
        }

        if (apiInstance == null) {
            System.out.println("[Ribbon] 无法获取 IrisApi 实例，阴影检测已禁用");
            return;
        }

        // 获取 isRenderingShadowPass() 方法
        try {
            isRenderingShadowPassMethod = apiInstance.getClass().getMethod("isRenderingShadowPass");
            System.out.println("[Ribbon] Iris API 阴影检测初始化成功");
        } catch (NoSuchMethodException e) {
            System.out.println("[Ribbon] IrisApi 中未找到 isRenderingShadowPass 方法，阴影检测已禁用");
        }
    }
}