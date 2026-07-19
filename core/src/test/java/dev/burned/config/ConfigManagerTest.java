package dev.burned.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {

    // NOT: ConfigManager kayıtları JVM ömrü boyunca sınıf başına önbelleğe
    // alır (bu, production'da idempotent register() için istenen davranıştır).
    // Test izolasyonunu bozmamak için her test kendi @Config sınıfını kullanır.

    @Config("example")
    static class ExampleConfigA {
        @Entry
        boolean fly = true;
        @Entry
        int maxHomes = 5;
        @Entry
        String prefix = "&6Server";
    }

    @Config("example")
    static class ExampleConfigB {
        @Entry
        boolean fly = true;
        @Entry
        int maxHomes = 5;
        @Entry
        String prefix = "&6Server";
    }

    @Config("example")
    static class ExampleConfigC {
        @Entry
        boolean fly = true;
        @Entry
        int maxHomes = 5;
        @Entry
        String prefix = "&6Server";
    }

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ConfigManager.setBaseDirectory(tempDir);
    }

    @Test
    void registerCreatesFileWithDefaults() {
        ExampleConfigA cfg = ConfigManager.register(ExampleConfigA.class);

        assertTrue(cfg.fly);
        assertEquals(5, cfg.maxHomes);
        assertEquals("&6Server", cfg.prefix);
        assertTrue(Files.exists(tempDir.resolve("example.json")));
    }

    @Test
    void changedValueSurvivesSaveAndReload() {
        ExampleConfigB cfg = ConfigManager.register(ExampleConfigB.class);

        cfg.maxHomes = 42;
        ConfigManager.save(cfg);

        cfg.maxHomes = 0; // bilinçli olarak boz
        ConfigManager.reload(cfg);

        assertEquals(42, cfg.maxHomes);
    }

    @Test
    void missingKeyInExistingFileGetsDefaultAppended() throws Exception {
        Files.createDirectories(tempDir);
        Files.writeString(tempDir.resolve("example.json"), "{\"fly\": false}");

        ExampleConfigC cfg = ConfigManager.register(ExampleConfigC.class);

        assertEquals(false, cfg.fly);       // dosyadaki değer korunur
        assertEquals(5, cfg.maxHomes);      // eksik alan varsayılanla dolar
        String content = Files.readString(tempDir.resolve("example.json"));
        assertTrue(content.contains("maxHomes"));
    }

    @Test
    void builderApiAddsAndPersistsValues() {
        SimpleConfig cfg = ConfigManager.create("settings");
        cfg.addBoolean("fly", true);
        cfg.addInt("maxHomes", 5);
        cfg.addString("prefix", "&6Server");
        cfg.save();

        SimpleConfig reloaded = ConfigManager.create("settings");
        assertTrue(reloaded.getBoolean("fly"));
        assertEquals(5, reloaded.getInt("maxHomes"));
        assertEquals("&6Server", reloaded.getString("prefix"));
    }
}
