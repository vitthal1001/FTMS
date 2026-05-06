plugins {
    id("org.springframework.boot") version "3.5.14" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

extra["springCloudVersion"] = "2025.0.2"

allprojects {
    group = "com.neobankx"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    plugins.withType<JavaPlugin> {
        dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
