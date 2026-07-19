package dev.burned.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.burned.config.internal.JsonIo;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Annotasyon kullanmadan, doğrudan kod ile anahtar/değer tanımlayarak
 * kullanılan config API'si. {@link ConfigManager#create(String)} ile
 * elde edilir.
 * <p>
 * {@code addX(key, default)} metotları zincirlenebilir (fluent) ve
 * "yalnızca eksikse ekle" mantığıyla çalışır: dosyada zaten bir değer
 * varsa dokunulmaz, yoksa verilen varsayılan değer eklenir. Bu, annotation
 * API'sindeki "eksik alanları tamamlama" davranışının builder karşılığıdır.
 * <p>
 * Bu sınıf thread-safe DEĞİLDİR; tek thread'den (genelde sunucu ana
 * thread'i) kullanılması beklenir, aynı Fabric'teki diğer config
 * kütüphaneleri gibi.
 */
public final class SimpleConfig {

    private final Path path;
    private JsonObject data;

    SimpleConfig(Path baseDirectory, String fileName) {
        this.path = baseDirectory.resolve(fileName + ".json");
        this.data = JsonIo.readOrEmpty(path);
    }

    public SimpleConfig addBoolean(String key, boolean defaultValue) {
        return addIfAbsent(key, new JsonPrimitive(defaultValue));
    }

    public SimpleConfig addInt(String key, int defaultValue) {
        return addIfAbsent(key, new JsonPrimitive(defaultValue));
    }

    public SimpleConfig addLong(String key, long defaultValue) {
        return addIfAbsent(key, new JsonPrimitive(defaultValue));
    }

    public SimpleConfig addDouble(String key, double defaultValue) {
        return addIfAbsent(key, new JsonPrimitive(defaultValue));
    }

    public SimpleConfig addString(String key, String defaultValue) {
        return addIfAbsent(key, new JsonPrimitive(defaultValue));
    }

    private SimpleConfig addIfAbsent(String key, JsonPrimitive value) {
        if (!data.has(key)) {
            data.add(key, value);
        }
        return this;
    }

    public boolean getBoolean(String key) {
        return requireValue(key).getAsBoolean();
    }

    public int getInt(String key) {
        return requireValue(key).getAsInt();
    }

    public long getLong(String key) {
        return requireValue(key).getAsLong();
    }

    public double getDouble(String key) {
        return requireValue(key).getAsDouble();
    }

    public String getString(String key) {
        return requireValue(key).getAsString();
    }

    public void set(String key, boolean value) {
        data.add(key, new JsonPrimitive(value));
    }

    public void set(String key, int value) {
        data.add(key, new JsonPrimitive(value));
    }

    public void set(String key, long value) {
        data.add(key, new JsonPrimitive(value));
    }

    public void set(String key, double value) {
        data.add(key, new JsonPrimitive(value));
    }

    public void set(String key, String value) {
        data.add(key, new JsonPrimitive(value));
    }

    public boolean has(String key) {
        return data.has(key);
    }

    /** Mevcut belleği diske yazar. */
    public SimpleConfig save() {
        JsonIo.write(path, data);
        return this;
    }

    /** Diskten yeniden okur (elle yapılan dış değişiklikleri yakalar için). */
    public SimpleConfig load() {
        this.data = JsonIo.readOrEmpty(path);
        return this;
    }

    private com.google.gson.JsonElement requireValue(String key) {
        com.google.gson.JsonElement element = data.get(key);
        if (element == null) {
            throw new IllegalArgumentException(
                    "Config anahtarı bulunamadı (önce addX(...) ile tanımlanmalı): " + key);
        }
        return element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleConfig other)) return false;
        return path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
