# BurnedConfig

Annotation-based JSON config library for Fabric mods.

```java
@Config(name = "my_mod")
public class MyConfig {
    @ConfigEntry(comment = "Max players per island", min = 1, max = 64)
    public int maxIslandMembers = 4;
    public boolean pvpEnabled = false;
}

// in your mod's onInitialize():
MyConfig cfg = ConfigManager.register(MyConfig.class);
// edit a field, then persist it:
cfg.maxIslandMembers = 8;
ConfigManager.save(MyConfig.class);
// re-read from disk (e.g. after an in-game /reload command):
ConfigManager.reload(MyConfig.class);
```

- File is created automatically on first `register()` call, at `config/<name>.json`.
- Any public, non-static, non-transient field is persisted — no getters/setters needed.
- `@ConfigEntry(min=, max=)` clamps numeric fields automatically after every load/reload.
- Missing keys in an existing file fall back to defaults instead of crashing.

## Supported Minecraft versions

| Version | Mapping system | Notes |
|---|---|---|
| 1.18.2 | Yarn | legacy track |
| 1.19.4 | Yarn | legacy track |
| 1.20.1 | Yarn | legacy track |
| 1.21.1 | Yarn | legacy track, last obfuscated line |
| 26.1 "Tiny Takeover" | Mojang official (unobfuscated) | modern track, needs Java 25 |
| 26.2 "Chaos Cubed" | Mojang official (unobfuscated) | modern track, needs Java 25 |

Each version is compiled as its own separate jar by the CI matrix in
`.github/workflows/build.yml` — there is no single jar that magically runs on
every version, because 26.1+ dropped Yarn mappings entirely and requires a
different Loom plugin id (`net.fabricmc.fabric-loom`) and Java 25. This is the
same approach real multi-version Fabric mods use.

## Building yourself

```
gradle build -Ptrack=legacy -Pminecraft_version=1.20.1 -Pyarn_mappings=1.20.1+build.10 -Ploader_version=0.16.9
gradle build -Ptrack=modern -Pminecraft_version=26.2 -Ploader_version=0.19.3
```

Or just push to `main` / trigger the workflow manually — GitHub Actions builds
all six versions and bundles every jar that succeeded into
`BurnedConfig-all-versions.zip` under the **package** job's artifacts.

If a given matrix leg fails (e.g. a loader/loom version number goes stale),
the other five still build — check the Actions run's per-job logs for the
one that failed and bump the number in `build.yml`.
