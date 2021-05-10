package com.minelatino.pixelbuy.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtils {

    private static final Map<String, Class<?>> classCache = new HashMap<>();
    private static final Map<String, Method> methodCache = new HashMap<>();
    private static final Map<String, Field> fieldCache = new HashMap<>();
    private static final Map<String, Constructor<?>> constructorCache = new HashMap<>();

    public static void addClass(String name, Class<?> clazz) {
        classCache.put(name, clazz);
    }

    public static Class<?> getClass(String name) {
        return classCache.get(name);
    }

    public static void addMethod(String name, Method method) {
        methodCache.put(name, method);
    }

    public static Method getMethod(String name) {
        return methodCache.get(name);
    }

    public static void addField(String name, Field field) {
        fieldCache.put(name, field);
    }

    public static Field getField(String name) {
        return fieldCache.get(name);
    }

    public static void addConstructor(String name, Constructor<?> constructor) {
        constructorCache.put(name, constructor);
    }

    public static Constructor<?> getConstructor(String name) {
        return constructorCache.get(name);
    }
}
