package dev.burned.config.internal;

import dev.burned.config.Config;
import dev.burned.config.Entry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bir @Config sınıfının reflection ile çıkarılmış "şeması".
 * <p>
 * Bu sınıf {@link ConfigRegistry} tarafından SADECE {@code register()}
 * çağrısı sırasında bir kere oluşturulur ve önbelleğe alınır. Sonraki
 * tüm save/reload işlemleri bu önbellekten faydalanır; tekrar reflection
 * taraması yapılmaz. Bu sayede runtime'da (özellikle tick döngüsünde
 * kullanılan değerlerde) reflection maliyeti sıfırdır.
 */
public final class ConfigSchema {

    private final String fileName;
    private final Constructor<?> constructor;
    private final List<FieldEntry> fields;

    private ConfigSchema(String fileName, Constructor<?> constructor, List<FieldEntry> fields) {
        this.fileName = fileName;
        this.constructor = constructor;
        this.fields = fields;
    }

    /**
     * Verilen sınıfı tarayıp bir {@link ConfigSchema} çıkarır.
     *
     * @throws IllegalArgumentException sınıf @Config ile işaretli değilse
     *                                   ya da uygun bir no-arg kurucusu yoksa
     */
    public static ConfigSchema scan(Class<?> clazz) {
        Config configAnnotation = clazz.getAnnotation(Config.class);
        if (configAnnotation == null) {
            throw new IllegalArgumentException(
                    "Sınıf @Config ile işaretlenmemiş: " + clazz.getName());
        }

        Constructor<?> ctor;
        try {
            ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "@Config sınıfının parametresiz bir kurucusu olmalı: " + clazz.getName(), e);
        }

        List<FieldEntry> fieldEntries = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            Entry entryAnnotation = field.getAnnotation(Entry.class);
            if (entryAnnotation == null) {
                continue;
            }
            field.setAccessible(true);
            String key = entryAnnotation.key().isEmpty() ? field.getName() : entryAnnotation.key();
            fieldEntries.add(new FieldEntry(field, key));
        }

        return new ConfigSchema(configAnnotation.value(), ctor, List.copyOf(fieldEntries));
    }

    public String fileName() {
        return fileName;
    }

    public Object newInstance() {
        try {
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Config örneği oluşturulamadı: " + constructor.getDeclaringClass(), e);
        }
    }

    public List<FieldEntry> fields() {
        return fields;
    }

    /**
     * Tek bir @Entry alanının önbelleğe alınmış reflection tanıtıcısı.
     */
    public static final class FieldEntry {
        private final Field field;
        private final String key;

        FieldEntry(Field field, String key) {
            this.field = Objects.requireNonNull(field);
            this.key = Objects.requireNonNull(key);
        }

        public Field field() {
            return field;
        }

        public String key() {
            return key;
        }
    }
}
