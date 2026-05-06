plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.14")
    }
}

dependencies {
    api("org.springframework:spring-web")
    api("org.springframework.security:spring-security-core")
    api("org.springframework.boot:spring-boot-actuator")
    api("io.micrometer:micrometer-core")
    api("jakarta.validation:jakarta.validation-api")

    implementation("org.slf4j:slf4j-api")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
}

