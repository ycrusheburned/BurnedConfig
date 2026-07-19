package dev.burned.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bir alanı config dosyasına kaydedilecek bir giriş (entry) olarak işaretler.
 * <p>
 * Desteklenen alan tipleri: {@code boolean}, {@code int}, {@code long},
 * {@code double}, {@code float}, {@code String} ve bunların kutulanmış
 * (boxed) karşılıkları, ayrıca {@code List<String>} gibi Gson'un doğal
 * olarak (de)serialize edebildiği basit koleksiyonlar.
 * <p>
 * Alanın Java kaynak kodundaki ilk atanan değeri, dosyada eksik olduğunda
 * kullanılacak varsayılan değerdir.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Entry {

    /**
     * JSON anahtarı. Boş bırakılırsa alan adı kullanılır.
     */
    String key() default "";

    /**
     * Config dosyasına yazılacak açıklama satırı. BurnedConfig'in JSON
     * çıktısı yorum desteklemediğinden bu değer şu an yalnızca
     * dokümantasyon/GUI entegrasyonları için üretici tarafından
     * (ör. reflection ile) okunabilir; ileride yorumlu format
     * (örn. JSON5) desteği eklendiğinde otomatik yazılacaktır.
     */
    String comment() default "";
}
