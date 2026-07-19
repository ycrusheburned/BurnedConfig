rootProject.name = "BurnedConfig"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// BurnedConfig hiçbir net.minecraft.* sınıfına dokunmaz (yalnızca Fabric
// Loader'ın stabil ModInitializer/FabricLoader API'sini kullanır). Bu
// yüzden Loom/yarn mappings GEREKMEZ ve tek bir "fabric" modülü,
// 1.18'den güncel sürüme kadar TÜM Minecraft sürümlerini kapsar.
// "Aynı API çalışan sürümler tek modülde toplanır" kuralının en sade hali:
// - core:   saf Java, tüm config mantığı (tek kopya)
// - fabric: ince Fabric Loader entrypoint'i, core'u jar-in-jar gömer
include("core")
include("fabric")
