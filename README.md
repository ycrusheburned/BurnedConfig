# BurnedConfig

Fabric modları için hafif, **allocation-dostu** config kütüphanesi.
İki farklı kullanım şekli sunar:

- **Annotation API** — `@Config` / `@Entry` ile POJO tabanlı, en az kod.
- **Builder API** (`SimpleConfig`) — annotasyonsuz, akıcı (fluent) kullanım.

Her iki API de aynı motoru (`ConfigRegistry`) paylaşır; kod tekrarı yoktur.

## Neden BurnedConfig?

- Dosyayı otomatik oluşturur, eksik alanları varsayılan değerlerle tamamlar.
- Reflection **yalnızca `register()` çağrısında** çalışır; sonrası tamamen
  önbelleğe alınmış alan listesi üzerinden gider. Tick döngüsünde,
  save/reload çağrılarında yeni reflection taraması ya da gereksiz obje
  allocation'ı yoktur.
- Atomik dosya yazımı (geçici dosya + rename) — çökme/eşzamanlı yazma
  config dosyasını bozmaz.
- `core` modülü saf Java'dır, Minecraft/Fabric'e bağımlı değildir; Fabric
  wrapper modülleri yalnızca config klasörünü core'a bildirir.

## Kurulum

`build.gradle` (Groovy):

```groovy
repositories {
    maven { url "https://your-maven-repo/" } // veya jar-in-jar ile dağıtım
}

dependencies {
    modImplementation include("dev.burned:BurnedConfig-fabric:1.0.0-1.18-26.2")
}
```

Desteklenen sürüm aralığı için aşağıdaki **Modüller** bölümüne bakın.

## Kullanım: Annotation API

```java
@Config("example")          // -> config/example.json
public class ExampleConfig {

    @Entry
    public boolean fly = true;

    @Entry
    public int maxHomes = 5;

    @Entry(key = "prefix", comment = "Sohbet öneki")
    public String prefix = "&6Server";
}
```

```java
// Mod init sırasında bir kez:
ExampleConfig cfg = ConfigManager.register(ExampleConfig.class);

// Dosya yoksa oluşturulur; varsa eksik alanlar tamamlanır.
// cfg artık kullanılabilir durumda.

cfg.maxHomes = 10;
ConfigManager.save(cfg);     // değişikliği diske yaz

ConfigManager.reload(cfg);   // diskteki güncel hali belleğe al (örn. /reload komutu)
```

Desteklenen alan tipleri: `boolean`, `int`, `long`, `double`, `float`,
`String` ve bunların kutulanmış (boxed) karşılıkları, ayrıca Gson'un
doğal olarak (de)serialize edebildiği `List<String>` gibi basit
koleksiyonlar.

## Kullanım: Builder API

```java
SimpleConfig cfg = ConfigManager.create("settings"); // -> config/settings.json

cfg.addBoolean("fly", true);
cfg.addInt("maxHomes", 5);
cfg.addString("prefix", "&6Server");
cfg.save();

// Daha sonra:
boolean fly = cfg.getBoolean("fly");
```

`addX(key, default)` metotları **"yalnızca eksikse ekle"** mantığıyla
çalışır: dosyada zaten değer varsa dokunulmaz.

## Config dizinini ayarlama

Fabric wrapper modülleri bunu otomatik yapar
(`FabricLoader.getInstance().getConfigDir()`). Kütüphaneyi Fabric dışında
(örn. unit testte) kullanıyorsanız:

```java
ConfigManager.setBaseDirectory(Path.of("config"));
```

## Modüller

BurnedConfig hiçbir `net.minecraft.*` sınıfına dokunmaz (yalnızca Fabric
Loader'ın stabil `ModInitializer` / `FabricLoader` API'sini kullanır).
Bu yüzden Loom/yarn mappings'e ihtiyaç yoktur ve **tek bir modül**,
**1.18'den güncel sürüme (26.2) kadar tüm Minecraft sürümlerini** kapsar —
"aynı API çalışan sürümler tek modülde toplanır" kuralının en sade hali.

| Modül    | Kapsam        | Jar                                  |
|----------|---------------|---------------------------------------|
| `fabric` | 1.18 – 26.2   | `BurnedConfig-fabric-1.0.0-1.18-26.2.jar` |

Minecraft'ın kendi sürümleme şeması ileride tekrar değişir ya da
Fabric Loader'ın entrypoint API'si kırılırsa (son ~6 yıldır olmadı),
o zaman ve ancak o zaman yeni bir modül açılır.

Tüm jar `BurnedConfig-all.zip` içinde her sürümde otomatik olarak
GitHub Actions tarafından üretilir (bkz. Releases sekmesi).

## Proje yapısı

```
BurnedConfig/
├── core/     # Saf Java: tüm config mantığı burada (tek kopya)
├── fabric/   # İnce Fabric entrypoint (core'u shadow ile gömer)
└── .github/workflows/build.yml
```

Yeni bir MC sürümü Fabric Loader'ın entrypoint API'sini kırmadığı sürece
(pratikte yıllardır kırmıyor), `fabric.mod.json` içindeki `minecraft`
bağımlılık aralığı güncellenir — yeni modül açılmaz.

## Derleme

```bash
./gradlew build
```

> **Not:** Bu repo, `gradle/wrapper/gradle-wrapper.jar` binary dosyasını
> içermez (üretim ortamında ağ erişimi olmayan bir asistan tarafından
> hazırlandığı için indirilemedi). İlk klonlamadan sonra bir kere
> `gradle wrapper --gradle-version 8.10` çalıştırarak wrapper'ı
> oluşturun, ardından commit'leyin. GitHub Actions bu adıma ihtiyaç
> duymadan `gradle` komutunu doğrudan kurup kullanır.

Derlenmiş jar'lar her modülün `build/libs/` klasöründe oluşur.

## Katkı

Issue ve PR'lar memnuniyetle karşılanır. Lütfen:
- Yeni bir MC sürümü mevcut modülün API'sini kırmıyorsa yeni modül
  açmayın, sadece sürüm aralığını güncelleyin.
- `core` modülüne Minecraft/Fabric bağımlılığı eklemeyin.
- Tick döngüsünde çalışacak kod için allocation'dan kaçının.

## Lisans

[MIT](LICENSE)
