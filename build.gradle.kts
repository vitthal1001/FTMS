plugins {
    id("org.springframework.boot") version "3.5.14" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("java-library") apply false
}

allprojects {
    group = "com.neobankx"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

