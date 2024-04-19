package io.papermc.converter.service;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public final class MinecraftService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftService.class);

	public MinecraftService() {
		LOGGER.info("Bootstrapping Minecraft...");
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		LOGGER.info("Minecraft bootstrapped!");
	}
}
