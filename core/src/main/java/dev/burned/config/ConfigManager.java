package dev.burned.config;

import dev.burned.config.internal.ConfigRegistry;

import java.nio.file.Path;

/**
 * BurnedConfig'in giriş noktası. Kütüphaneyi kullanan bir mod tipik olarak
 * yalnızca bu sınıfla etkileşir.
 *
 * <pre>{@code
 * @Config("example")
 * public class ExampleConfig {
 *     @Entry
 *     public boolean fly = true;
 *     @Entry
 *     public int maxHomes = 5;
 * }
 *
 * ExampleConfig cfg = ConfigManager.register(ExampleConfig.class);
 * // cfg artık ya diskteki değerlerle ya da (dosya yoksa) varsayılan
 * // değerlerle doludur; dosya otomatik oluşturulmuş/tamamlanmıştır.
 *
 * cfg.maxHomes = 10;
 * ConfigManager.save(cfg);   // değişikliği diske yaz
 * ConfigManager.reload(cfg); // diskteki güncel hali belleğe al
 * }</pre>
 */
public final class ConfigManager {

    private static final ConfigRegistry REGISTRY = new ConfigRegistry();

    private ConfigManager() {
    }

    /**
     * Config dosyalarının yazılacağı kök klasörü ayarlar. Fabric mod'ları
     * bunu init aşamasında bir kez çağırmalıdır:
     * {@code ConfigManager.setBaseDirectory(FabricLoader.getInstance().getConfigDir())}.
     * Çağrılmazsa varsayılan olarak çalışma dizinindeki {@code ./config}
     * kullanılır.
     */
    public static void setBaseDirectory(Path directory) {
        REGISTRY.setBaseDirectory(directory);
    }

    /**
     * Bir @Config sınıfını kaydeder: dosyayı okur (yoksa oluşturur),
     * eksik alanları varsayılan değerlerle tamamlar ve doldurulmuş bir
     * örnek döner. Aynı sınıf için ikinci çağrı, ilk register'da oluşan
     * örneği (ve reflection önbelleğini) yeniden kullanır.
     */
    public static <T> T register(Class<T> configClass) {
        return REGISTRY.register(configClass);
    }

    /**
     * Instance'taki güncel alan değerlerini diske yazar. Reflection
     * yalnızca register sırasında yapıldığından bu çağrı ucuzdur ve
     * sık aralıklarla (örn. komut sonrası) çağrılabilir.
     */
    public static void save(Object configInstance) {
        REGISTRY.save(configInstance);
    }

    /**
     * Diskteki güncel içeriği instance'a geri yükler. Elle düzenlenmiş
     * bir config dosyasını oyun içinden "/reload" gibi bir komutla tekrar
     * okutmak için kullanılır.
     */
    public static void reload(Object configInstance) {
        REGISTRY.reload(configInstance);
    }

    /**
     * Annotasyon kullanmadan, doğrudan anahtar/değer ekleyerek bir config
     * dosyası oluşturmak için akıcı (fluent) builder API'si döner.
     *
     * <pre>{@code
     * SimpleConfig cfg = ConfigManager.create("settings");
     * cfg.addBoolean("fly", true);
     * cfg.addInt("maxHomes", 5);
     * cfg.save();
     * }</pre>
     */
    public static SimpleConfig create(String fileName) {
        return new SimpleConfig(REGISTRY.baseDirectory(), fileName);
    }
}
