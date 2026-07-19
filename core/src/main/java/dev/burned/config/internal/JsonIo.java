package dev.burned.config.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Dosya sistemi ile JSON arasındaki tüm okuma/yazma işlemlerini tek yerde
 * toplar. Atomik yazma (geçici dosya + rename) kullanır; bu sayede sunucu
 * çökmesi ya da eşzamanlı yazma config dosyasını bozamaz.
 */
public final class JsonIo {

    // Tek bir Gson örneği yeniden kullanılır; her save/load çağrısında
    // yeni Gson örneği oluşturmak gereksiz allocation'dır.
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private JsonIo() {
    }

    public static Gson gson() {
        return GSON;
    }

    /**
     * Dosyayı okuyup {@link JsonObject} olarak döner. Dosya yoksa ya da
     * geçersizse boş bir JsonObject döner (dosya bu durumda register
     * sırasında varsayılan değerlerle yeniden oluşturulur).
     */
    public static JsonObject readOrEmpty(Path path) {
        if (!Files.isRegularFile(path)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            }
        } catch (IOException | JsonSyntaxException ignored) {
            // Bozuk/okunamayan dosya -> boş şema ile devam edilir, üzerine
            // varsayılan değerlerle yeniden yazılır. Kullanıcı verisini
            // sessizce silmemek için burada dosyayı .bak olarak yedekleriz.
            backupCorruptFile(path);
        }
        return new JsonObject();
    }

    private static void backupCorruptFile(Path path) {
        try {
            Path backup = path.resolveSibling(path.getFileName() + ".bak");
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Yedekleme başarısız olsa dahi config yükleme akışını durdurmayız.
        }
    }

    /**
     * JsonObject'i diske atomik olarak yazar.
     */
    public static void write(Path path, JsonObject object) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = Files.createTempFile(parent, path.getFileName().toString(), ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(object, writer);
            }
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Config dosyası yazılamadı: " + path, e);
        }
    }
}
