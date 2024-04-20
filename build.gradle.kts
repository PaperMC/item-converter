plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("io.papermc.paperweight.userdev") version "1.5.15"
}

group = "io.papermc"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configurations.runtimeOnly {
    extendsFrom(configurations.mojangMappedServerRuntime.get())
}
configurations.configureEach {
    exclude(module = "log4j-slf4j2-impl")
    exclude(group = "org.apache.logging.log4j")
}
