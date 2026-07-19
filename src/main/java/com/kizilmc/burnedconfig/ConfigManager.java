package com.kizilmc.burnedconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registers, saves, loads and reloads @Config annotated classes as JSON files
 * under the game's config directory. One instance is kept per class; call
 * get(Class) to fetch the live object (edits to its fields + a save() call persist them).
 */
public final class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger("BurnedConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<Class<?>, Object> REGISTRY = new HashMap<>();

    private ConfigManager() {
    }

    /**
     * Registers a config class: creates the JSON file with defaults if missing,
     * otherwise loads existing values on top of a fresh default instance.
     */
    public static synchronized <T> T register(Class<T> configClass) {
        if (REGISTRY.containsKey(configClass)) {
            //noinspection unchecked
            return (T) REGISTRY.get(configClass);
        }

        Config meta = configClass.getAnnotation(Config.class);
        if (meta == null) {
            throw new IllegalArgumentException(configClass.getName() + " is missing @Config");
        }

        T instance = newDefaultInstance(configClass);
        Path path = pathFor(meta.name());

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                T loaded = GSON.fromJson(reader, configClass);
                if (loaded != null) {
                    copyFields(loaded, instance);
                }
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.log(Level.WARNING, "BurnedConfig: could not read " + path + ", regenerating with defaults", e);
            }
        }

        clampAll(instance);
        REGISTRY.put(configClass, instance);
        save(configClass);
        return instance;
    }

    /** Fetches an already-registered config instance. */
    public static synchronized <T> T get(Class<T> configClass) {
        Object instance = REGISTRY.get(configClass);
        if (instance == null) {
            throw new IllegalStateException(configClass.getName() + " was never registered. Call ConfigManager.register() first.");
        }
        //noinspection unchecked
        return (T) instance;
    }

    /** Writes the current in-memory state of the config back to disk. */
    public static synchronized void save(Class<?> configClass) {
        Object instance = REGISTRY.get(configClass);
        if (instance == null) return;
        Config meta = configClass.getAnnotation(Config.class);
        Path path = pathFor(meta.name());
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "BurnedConfig: failed to save " + path, e);
        }
    }

    /** Re-reads the JSON file from disk into the live instance, overwriting in-memory values. */
    public static synchronized void reload(Class<?> configClass) {
        Object instance = REGISTRY.get(configClass);
        if (instance == null) return;
        Config meta = configClass.getAnnotation(Config.class);
        Path path = pathFor(meta.name());
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = GSON.fromJson(reader, configClass);
            if (loaded != null) {
                copyFields(loaded, instance);
                clampAll(instance);
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "BurnedConfig: reload of " + path + " failed", e);
        }
    }

    private static Path pathFor(String name) {
        return FabricLoader.getInstance().getConfigDir().resolve(name + ".json");
    }

    private static <T> T newDefaultInstance(Class<T> configClass) {
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(configClass.getName() + " needs a public no-arg constructor", e);
        }
    }

    private static void copyFields(Object from, Object to) {
        for (Field field : to.getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
            try {
                field.set(to, field.get(from));
            } catch (IllegalAccessException ignored) {
                // field not accessible, skip it
            }
        }
    }

    private static void clampAll(Object instance) {
        for (Field field : instance.getClass().getFields()) {
            ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
            if (entry == null) continue;
            try {
                Object value = field.get(instance);
                if (value instanceof Number) {
                    double d = ((Number) value).doubleValue();
                    double clamped = Math.max(entry.min(), Math.min(entry.max(), d));
                    if (clamped != d) {
                        setNumeric(field, instance, clamped);
                    }
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static void setNumeric(Field field, Object instance, double value) throws IllegalAccessException {
        Class<?> type = field.getType();
        if (type == int.class || type == Integer.class) field.set(instance, (int) value);
        else if (type == long.class || type == Long.class) field.set(instance, (long) value);
        else if (type == float.class || type == Float.class) field.set(instance, (float) value);
        else field.set(instance, value);
    }
}
