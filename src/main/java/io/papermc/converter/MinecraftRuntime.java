package io.papermc.converter;

import io.papermc.converter.service.MinecraftService;
import io.papermc.converter.service.MinecraftServiceImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.net.protocol.jar.JarUrlClassLoader;

public final class MinecraftRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftRuntime.class);

    public static MinecraftService createMinecraftService() {
        final Path mcJarPath = Path.of("mc-runtime/mc.jar");
        final Path dcJarPath = Path.of("mc-runtime/dataconverter.jar");
        if (!setupMinecraft(mcJarPath, dcJarPath)) {
            LOGGER.info("Minecraft already on classpath.");
            return new MinecraftServiceImpl();
        }
        try {
            final var loader = new JarUrlClassLoader(
                new URL[]{
                    mcJarPath.toUri().toURL(),
                    dcJarPath.toUri().toURL(),
                    MinecraftRuntime.class.getProtectionDomain().getCodeSource().getLocation()
                },
                MinecraftRuntime.class.getClassLoader()
            ) {
                Class<?> load(final String name) throws ClassNotFoundException {
                    return this.findClass(name);
                }
            };

            try {
                return loader.load("io.papermc.converter.service.MinecraftServiceImpl")
                    .asSubclass(MinecraftService.class)
                    .getDeclaredConstructor()
                    .newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean setupMinecraft(final Path mcJarPath, final Path dataconverterPath) {
        try {
            final Path workDir = Files.createTempDirectory("mc-runtime");
            final Path unpackedZip = workDir.resolve("resolver.zip");
            final URL resolverZipUrl = MinecraftRuntime.class.getClassLoader()
                .getResource("runtime-minecraft-resolver.zip");
            if (resolverZipUrl == null) {
                try {
                    final ClassLoader bootstrapLoader = net.minecraft.server.Bootstrap.class.getClassLoader();
                    return false;
                } catch (final NoClassDefFoundError ignore) {
                }
                throw new IOException("Could not locate runtime-minecraft-resolver.zip");
            }
            LOGGER.info("Resolving Minecraft runtime...");
            try (final InputStream in = resolverZipUrl.openConnection().getInputStream()) {
                Files.copy(in, unpackedZip);
            }
            final URL dataconverterUrl = MinecraftRuntime.class.getClassLoader().getResource("dataconverter.jar");
            if (dataconverterUrl == null) {
                throw new IOException("Could not locate dataconverter.jar");
            }
            Files.createDirectories(dataconverterPath.getParent());
            try (final InputStream in = dataconverterUrl.openConnection().getInputStream()) {
                Files.copy(in, dataconverterPath);
            }
            try (final FileSystem zipFs = FileSystems.newFileSystem(URI.create("jar:" + unpackedZip.toUri()), Map.of())) {
                final Path root = zipFs.getPath("/");
                try (final Stream<Path> stream = Files.walk(root)) {
                    stream.forEach(path -> {
                        final String rel = root.relativize(path).toString();
                        try {
                            if (Files.isDirectory(path)) {
                                Files.createDirectories(workDir.resolve(rel));
                            } else {
                                try (final InputStream in = Files.newInputStream(path)) {
                                    Files.copy(in, workDir.resolve(rel));
                                }
                            }
                        } catch (final IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                }
            } catch (final UncheckedIOException e) {
                throw e.getCause();
            }
            final Path gradlew = workDir.resolve("gradlew");
            gradlew.toFile().setExecutable(true);
            final Process proc = new ProcessBuilder("./gradlew", "writeMcPath", "--no-daemon")
                .directory(workDir.toFile())
                .redirectErrorStream(true)
                .start();
            try {
                proc.waitFor(20, TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                throw new IOException(e);
            }
            if (proc.exitValue() != 0) {
                throw new IOException("Gradle exited with code " + proc.exitValue());
            }
            final String mcJarOrigin = Files.readString(workDir.resolve("mc-jar-path.txt"));
            LOGGER.info("Minecraft resolved to {}", mcJarOrigin);
            Files.createDirectories(mcJarPath.getParent());
            Files.copy(Path.of(mcJarOrigin), mcJarPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to setup Minecraft runtime", e);
        }
        return true;
    }
}
