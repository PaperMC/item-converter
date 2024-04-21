plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
    id("com.google.cloud.tools.jib") version "3.4.2"
}

group = "io.papermc"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

minecraft {
    version("1.20.5-rc2")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(files("dataconverter.jar"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configurations.configureEach {
    exclude(module = "log4j-slf4j2-impl")
    exclude(group = "org.apache.logging.log4j")
    exclude(group = "commons-logging", module = "commons-logging")
}

jib {
    to {
        image = "ghcr.io/papermc/item-converter"
    }
}
