import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.util.jar.JarFile
import kotlin.io.path.absolute

plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    alias(libs.plugins.vanillagradle)
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

val mcVersion = "1.20.6"

minecraft {
    version(mcVersion)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-loader")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("jakarta.validation:jakarta.validation-api:3.1.0-M2")
    implementation(files("dataconverter.jar"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

abstract class ProcessBuildFile : DefaultTask() {
    @get:InputDirectory
    abstract val input: DirectoryProperty

    @get:OutputDirectory
    abstract val out: DirectoryProperty

    @get:Inject
    abstract val fsOps: FileSystemOperations

    @get:Input
    abstract val mcVer: Property<String>

    @get:Input
    abstract val vgVersion: Property<String>

    @TaskAction
    fun run() {
        fsOps.delete {
            delete(out.get())
        }
        fsOps.copy {
            from(input.get()) {
                exclude("mc-jar-path.txt")
                exclude(".gradle/")
            }
            into(out.get())
        }
        val buildFile = out.file("build.gradle.kts.without_version").get().asFile.readText()
            .replace("VG_VERSION", vgVersion.get())
        out.file("build.gradle.kts").get().asFile.writeText(buildFile + "\nminecraft { version(\"${mcVer.get()}\") }\n")
    }
}

val processBuildFile = tasks.register<ProcessBuildFile>("processBuildFile") {
    input.set(layout.projectDirectory.dir("runtime-minecraft-resolver"))
    out.set(layout.buildDirectory.dir("tmp/runtime-minecraft-resolver"))
    mcVer.set(mcVersion)
    vgVersion.set(libs.versions.vanillagradle)
}

val zip = tasks.register<Zip>("zipMcRuntimeResolver") {
    archiveFileName.set("runtime-minecraft-resolver.zip")
    from(processBuildFile.flatMap { it.out })
}

tasks.jar {
    from(zip.flatMap { it.archiveFile })
    from(files("dataconverter.jar"))
}
tasks.bootJar {
    from(zip.flatMap { it.archiveFile })
    from(files("dataconverter.jar"))
    // copied from spring boot plugin
    val cp = objects.fileCollection()
    cp.from(Callable {
        sourceSets.main.get().runtimeClasspath
            .minus(configurations.developmentOnly.get().minus(configurations.productionRuntimeClasspath.get()))
            .minus(configurations.testAndDevelopmentOnly.get().minus(configurations.productionRuntimeClasspath.get()))
            .filter(JarTypeFileSpec())
            .filter {
                val ret = !it.toPath().absolute().toString()
                    .replace(it.toPath().fileSystem.separator, "/").contains("jars/net/minecraft")
                    && it.name != "dataconverter.jar"
                return@filter ret
            }
    })
    classpath = cp
}

project.tasks.register("fatBootJar", BootJar::class) {
    archiveClassifier.set("fat")
    classpath(sourceSets.main.get().runtimeClasspath
        .minus(configurations.developmentOnly.get().minus(configurations.productionRuntimeClasspath.get()))
        .minus(configurations.testAndDevelopmentOnly.get().minus(configurations.productionRuntimeClasspath.get()))
        .filter(JarTypeFileSpec()))
    mainClass.convention(tasks.bootJar.flatMap { it.mainClass })
    targetJavaVersion.convention(tasks.bootJar.flatMap { it.targetJavaVersion })
    resolvedArtifacts(configurations.runtimeClasspath.get().incoming.artifacts.resolvedArtifacts)
}

// copied from spring boot plugin
class JarTypeFileSpec : Spec<File> {
    override fun isSatisfiedBy(file: File): Boolean {
        try {
            JarFile(file).use { jar ->
                val jarType = jar.manifest.mainAttributes.getValue("Spring-Boot-Jar-Type")
                if (jarType != null && EXCLUDED_JAR_TYPES.contains(jarType)) {
                    return false
                }
            }
        } catch (ex: Exception) {
            // Continue
        }
        return true
    }

    companion object {
        private val EXCLUDED_JAR_TYPES = setOf("dependencies-starter")
    }
}

configurations.configureEach {
    exclude(module = "log4j-slf4j2-impl")
    exclude(group = "org.apache.logging.log4j")
    exclude(group = "commons-logging", module = "commons-logging")
}

