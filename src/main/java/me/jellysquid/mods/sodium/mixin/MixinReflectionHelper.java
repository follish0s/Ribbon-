package me.jellysquid.mods.sodium.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于替换 Mixin @Shadow 的通用反射工具。
 * 一次创建，所有 Mixin 通用。
 */
public final class MixinReflectionHelper {
    private static final Map<String, Field> FIELD_CACHE = new HashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new HashMap<>();

    private MixinReflectionHelper() {}

    public static Field findField(Class<?> clazz, String... names) {
        String key = clazz.getName() + ":field:" + String.join("|", names);
        Field cached = FIELD_CACHE.get(key);
        if (cached != null) return cached;

        for (String name : names) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                FIELD_CACHE.put(key, f);
                return f;
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    public static Method findMethod(Class<?> clazz, Class<?>[] paramTypes, Class<?> returnType, String... names) {
        for (String name : names) {
            try {
                Method m = clazz.getDeclaredMethod(name, paramTypes);
                if (returnType == null || m.getReturnType() == returnType) {
                    m.setAccessible(true);
                    return m;
                }
            } catch (NoSuchMethodException ignored) {}
        }
        for (Method m : clazz.getDeclaredMethods()) {
            if (Arrays.equals(m.getParameterTypes(), paramTypes) &&
                    (returnType == null || m.getReturnType() == returnType)) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object instance, Class<?> clazz, String... names) {
        try {
            Field f = findField(clazz, names);
            if (f == null) throw new RuntimeException("Field not found in " + clazz.getName() + ": " + String.join("|", names));
            return (T) f.get(instance);
        } catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static void set(Object instance, Class<?> clazz, Object value, String... names) {
        try {
            Field f = findField(clazz, names);
            if (f == null) throw new RuntimeException("Field not found in " + clazz.getName() + ": " + String.join("|", names));
            f.set(instance, value);
        } catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static Object call(Object instance, Class<?> clazz, Class<?>[] paramTypes, Class<?> returnType, Object[] args, String... names) {
        try {
            Method m = findMethod(clazz, paramTypes, returnType, names);
            if (m == null) throw new RuntimeException("Method not found in " + clazz.getName() + ": " + String.join("|", names));
            return m.invoke(instance, args);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}