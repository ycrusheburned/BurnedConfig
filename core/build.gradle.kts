plugins {
    id("java-library")
    `maven-publish`
}

java {
    // Java 17: 1.20.5+ hattının minimum ortak paydası.
    // mc1211 modülü ayrıca Java 21 hedefiyle derlenir; core bytecode
    // seviyesi düşük tutulduğu için her iki modülde de sorunsuz çalışır.
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // JSON okuma/yazma için tek dış bağımlılık. Fabric'in kendisi zaten
    // Gson'u runtime'da taşıdığından mod jar'ında çakışma yaratmaz.
    compileOnly("com.google.code.gson:gson:2.11.0")
    testImplementation("com.google.code.gson:gson:2.11.0")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
