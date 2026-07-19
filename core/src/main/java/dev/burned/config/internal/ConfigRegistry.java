package dev.burned.config.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BurnedConfig'in çekirdek motoru. {@code ConfigManager} bu sınıfa ince bir
 * cephe (facade) sağlar; asıl iş mantığı burada yaşar.
 * <p>
 * Reflection SADECE {@link #register} çağrısında, sınıf başına bir kez
 * çalışır ({@link ConfigSchema#scan}). Elde edilen şema {@code schemaCache}
 * içinde saklanır; {@link #save} ve {@link #reload} bu önbelleği kullanır,
 * bir daha alan/annotasyon taraması yapmaz.
 */
public final class ConfigRegistry {

    private final Map<Class<?>, ConfigSchema> schemaCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    private volatile Path baseDirectory = Path.of("config");

    /**
     * Config dosyalarının yazılacağı kök klasörü belirler. Fabric ortamında
     * bu genelde {@code FabricLoader.getInstance().getConfigDir()} olur;
     * platform-bağımsız kalabilmesi için core modülü bunu doğrudan
     * bilmez, wrapper modül init sırasında bir kez ayarlar.
     */
    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public Path baseDirectory() {
        return baseDirectory;
    }

    @SuppressWarnings("unchecked")
    public <T> T register(Class<T> clazz) {
        // computeIfAbsent: aynı sınıf iki kez register edilirse reflection
        // tekrar çalışmaz, önceki örnek geri döner.
        Object existing = instances.get(clazz);
        if (existing != null) {
            return (T) existing;
        }

        ConfigSchema schema = schemaCache.computeIfAbsent(clazz, ConfigSchema::scan);
        Object instance = schema.newInstance();
        Path file = resolvePath(schema);

        boolean fileExisted = java.nio.file.Files.isRegularFile(file);
        JsonObject json = JsonIo.readOrEmpty(file);
        boolean changed = applyJsonToInstance(schema, instance, json);

        // Dosya hiç yoktu ya da eksik alan vardı: varsayılanlarla tamamlanmış
        // hali diske yazılır. Böylece kullanıcı ilk açılışta dolu bir
        // config dosyası bulur ve eksik anahtarlar otomatik eklenir.
        if (changed || !fileExisted) {
            JsonIo.write(file, json);
        }

        instances.put(clazz, instance);
        return (T) instance;
    }

    /**
     * Bellekteki mevcut alan değerlerini diske yazar. Reflection kullanmaz;
     * yalnızca register sırasında önbelleğe alınmış {@link ConfigSchema}
     * üzerinden alan değerlerini okur.
     */
    public void save(Object instance) {
        ConfigSchema schema = schemaCache.get(instance.getClass());
        if (schema == null) {
            throw new IllegalStateException(
                    "Bu config önce register() ile kaydedilmeli: " + instance.getClass());
        }
        JsonObject json = new JsonObject();
        for (ConfigSchema.FieldEntry entry : schema.fields()) {
            writeFieldToJson(entry.field(), instance, entry.key(), json);
        }
        JsonIo.write(resolvePath(schema), json);
    }

    /**
     * Diskteki güncel değerleri belleğe geri yükler (harici düzenleme
     * sonrası "reload" komutları için).
     */
    public void reload(Object instance) {
        ConfigSchema schema = schemaCache.get(instance.getClass());
        if (schema == null) {
            throw new IllegalStateException(
                    "Bu config önce register() ile kaydedilmeli: " + instance.getClass());
        }
        JsonObject json = JsonIo.readOrEmpty(resolvePath(schema));
        applyJsonToInstance(schema, instance, json);
    }

    private Path resolvePath(ConfigSchema schema) {
        return baseDirectory.resolve(schema.fileName() + ".json");
    }

    /**
     * JSON içeriğini instance alanlarına uygular; JSON'da olmayan alanlar
     * için mevcut (varsayılan) değer JSON'a eklenir.
     *
     * @return en az bir alan JSON'a yeni eklendiyse true (diske yazmayı tetikler)
     */
    private boolean applyJsonToInstance(ConfigSchema schema, Object instance, JsonObject json) {
        boolean changed = false;
        for (ConfigSchema.FieldEntry entry : schema.fields()) {
            Field field = entry.field();
            String key = entry.key();
            if (json.has(key)) {
                JsonElement element = json.get(key);
                try {
                    Object value = JsonIo.gson().fromJson(element, field.getGenericType());
                    field.set(instance, value);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    throw new IllegalStateException(
                            "Alan okunamadı: " + field.getName() + " (" + schema.fileName() + ")", e);
                }
            } else {
                writeFieldToJson(field, instance, key, json);
                changed = true;
            }
        }
        return changed;
    }

    private void writeFieldToJson(Field field, Object instance, String key, JsonObject json) {
        try {
            Object value = field.get(instance);
            json.add(key, JsonIo.gson().toJsonTree(value, field.getGenericType()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Alan okunamadı: " + field.getName(), e);
        }
    }
}
