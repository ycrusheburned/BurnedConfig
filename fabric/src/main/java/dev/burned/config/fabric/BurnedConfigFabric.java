package dev.burned.config.fabric;

import dev.burned.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BurnedConfig'in Fabric giriş noktası.
 * <p>
 * Bu sınıfın TEK görevi, platform-bağımsız {@code core} modülüne
 * Minecraft'ın gerçek config klasörünü bildirmektir. Bütün config mantığı
 * core modülünde yaşar; burada tekrar yazılmış hiçbir kod yoktur.
 * <p>
 * Bu sınıf yalnızca Fabric Loader'ın (obfuscate edilmemiş, stabil) kendi
 * API'sini kullanır -- hiçbir net.minecraft.* sınıfına dokunmaz. Bu
 * sayede tek bir derleme, 1.18'den güncel sürüme kadar TÜM Minecraft
 * sürümlerinde çalışır; sürüme özel derleme/yarn mappings GEREKMEZ.
 */
public final class BurnedConfigFabric implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("BurnedConfig");

    @Override
    public void onInitialize() {
        ConfigManager.setBaseDirectory(FabricLoader.getInstance().getConfigDir());
        LOGGER.info("BurnedConfig hazır ({} config dizini)", FabricLoader.getInstance().getConfigDir());
    }
}
