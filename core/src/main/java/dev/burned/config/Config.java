package dev.burned.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bir sınıfı BurnedConfig tarafından yönetilen bir konfigürasyon dosyası
 * olarak işaretler.
 * <p>
 * Bu annotasyon eklenen sınıf {@link ConfigManager#register(Class)} ile
 * kaydedildiğinde, sınıf içindeki {@link Entry} alanları JSON dosyasına
 * yazılır/dosyadan okunur. Sınıfın parametresiz (no-arg) bir kurucusu
 * olmalıdır; alanların Java tarafındaki ilk değerleri config'in
 * "varsayılan değerleri" olarak kullanılır.
 *
 * <pre>{@code
 * @Config("example")
 * public class ExampleConfig {
 *     @Entry
 *     public boolean fly = true;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {

    /**
     * Dosya adı (uzantısız). Örn. "example" -> config/example.json
     */
    String value();
}
