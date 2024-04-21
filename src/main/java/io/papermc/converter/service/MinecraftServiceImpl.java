package io.papermc.converter.service;

import ca.spottedleaf.dataconverter.util.CommandArgumentUpgrader;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecraftServiceImpl implements MinecraftService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftServiceImpl.class);

    private final CommandArgumentUpgrader upgrader;

    public MinecraftServiceImpl() {
        LOGGER.info("Bootstrapping Minecraft...");
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        LOGGER.info("Minecraft bootstrapped!");

        LOGGER.info("Creating CommandArgumentUpgrader...");
        this.upgrader = CommandArgumentUpgrader.upgrader_1_20_4_to_1_20_5(2);
        LOGGER.info("CommandArgumentUpgrader created.");
    }

    @Override
    public String upgradeCommand(final String input) {
        LOGGER.debug("Upgrading command '{}'", input);
        // We do a startsWith check because we aren't supporting WorldEdit style commands
        // and want to support leading '/' or no leading '/'
        final String upgraded = this.upgrader.upgradeCommandArguments(input, input.startsWith("/"));
        LOGGER.debug("Upgraded command '{}' -> '{}'", input, upgraded);
        return upgraded;
    }

    @Override
    public String upgradeItemArgument(final String input) {
        LOGGER.debug("Upgrading item argument '{}'", input);
        final String upgraded = this.upgrader.upgradeSingleArgument(ItemArgument::item, input);
        LOGGER.debug("Upgraded item argument '{}' -> '{}'", input, upgraded);
        return upgraded;
    }

    @Override
    public String upgradeComponentArgument(final String input) {
        LOGGER.debug("Upgrading component argument '{}'", input);
        final String upgraded = this.upgrader.upgradeSingleArgument(ComponentArgument::textComponent, input);
        LOGGER.debug("Upgraded component argument '{}' -> '{}'", input, upgraded);
        return upgraded;
    }
}