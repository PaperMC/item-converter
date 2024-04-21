package io.papermc.converter.service;

import ca.spottedleaf.dataconverter.util.CommandArgumentUpgrader;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public final class MinecraftService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftService.class);

	private final CommandArgumentUpgrader upgrader;

	public MinecraftService() {
		LOGGER.info("Bootstrapping Minecraft...");
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		LOGGER.info("Minecraft bootstrapped!");

		LOGGER.info("Creating CommandArgumentUpgrader...");
		this.upgrader = CommandArgumentUpgrader.upgrader_1_20_4_to_1_20_5(2);
		LOGGER.info("CommandArgumentUpgrader created.");
	}

	public String upgradeCommand(final String input) {
		LOGGER.debug("Upgrading command '{}'", input);
		// We do a startsWith check because we aren't supporting WorldEdit style commands
		// and want to support leading '/' or no leading '/'
		final String upgraded = this.upgrader.upgradeCommandArguments(input, input.startsWith("/"));
		LOGGER.debug("Upgraded command '{}' -> '{}'", input, upgraded);
		return upgraded;
	}
}
