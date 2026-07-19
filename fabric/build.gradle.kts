plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.5"
    `maven-publish`
}

// 1.18, minimum Java 17 gerektiren en eski hedef sürüm; 26.2 dahil tüm
// üst sürümler JVM geriye dönük uyumluluğu sayesinde Java 17 bytecode'unu
// sorunsuz çalıştırır. Bu yüzden TEK bir hedef yeterlidir.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
}

dependencies {
    // BurnedConfig hiçbir net.minecraft.* sınıfına dokunmaz, dolayısıyla
    // Minecraft jar'ına veya yarn mappings'e ihtiyaç YOKTUR. Yalnızca
    // Fabric Loader'ın (obfuscate edilmemiş, stabil) kendi API'si kullanılır.
    compileOnly("net.fabricmc:fabric-loader:0.16.9")

    // core'un sınıfları shadow jar'a doğrudan gömülür (aşağıya bakın),
    // bu yüzden burada `implementation` yeterlidir; core ayrıca ayrı bir
    // Maven artifact'i olarak da (core/build.gradle.kts üzerinden)
    // yayınlanabilir.
    implementation(project(":core"))
    compileOnly("com.google.code.gson:gson:2.11.0")
}

base {
    // Jar ismi, desteklenen tüm sürüm aralığını gösterir.
    archivesName.set("${rootProject.property("archives_base_name")}-fabric")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveVersion.set("${project.version}-1.18-26.2")
    // core dışında hiçbir şey gömülmez: Gson ve fabric-loader zaten Fabric
    // ortamında runtime'da mevcuttur, tekrar paketlenmeleri jar boyutunu
    // şişirir ve sürüm çakışmasına yol açabilir.
    dependencies {
        exclude(dependency("com.google.code.gson:gson"))
        exclude(dependency("net.fabricmc:fabric-loader"))
    }
}

tasks.jar {
    // Loom'suz kurulumda varsayılan `jar` görevi yerine shadowJar'ı asıl
    // dağıtım artifact'i yapıyoruz.
    enabled = false
    dependsOn(tasks.shadowJar)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            from(components["shadow"])
        }
    }
}
