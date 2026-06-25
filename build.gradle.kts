plugins {
    id("org.springframework.boot") version "4.1.0"
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.3.21"
    id("org.jetbrains.kotlin.plugin.lombok") version "2.3.21"
    id("io.spring.dependency-management") version "1.1.7"
    java
}

group = "no.novari"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.jar {
    enabled = false
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("no.fint:fint-event-model:3.0.1")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
}

tasks.test {
    useJUnitPlatform()
}
