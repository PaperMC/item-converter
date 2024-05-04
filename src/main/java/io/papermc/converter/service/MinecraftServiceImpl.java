package io.papermc.converter.service;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry;
import ca.spottedleaf.dataconverter.util.CommandArgumentUpgrader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
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

    @Override
    public String upgradeEntity(final String entityType, final String nbt) {
        LOGGER.debug("Upgrading entity; type: '{}', nbt: '{}'", entityType, nbt);
        final ResourceLocation id;
        final CompoundTag tag;
        try {
            id = new ResourceLocation(entityType);
            tag = TagParser.parseTag(nbt);
        } catch (final CommandSyntaxException | ResourceLocationException e) {
            LOGGER.debug("Exception upgrading entity; type: '{}', nbt: '{}'", entityType, nbt, e);
            return e.getMessage();
        }

        tag.putString("id", id.toString());
        final CompoundTag convertedTag = MCDataConverter.convertTag(
            MCTypeRegistry.ENTITY,
            tag,
            3700, SharedConstants.getCurrentVersion().getDataVersion().getVersion()
        );
        convertedTag.remove("id");

        final SnbtPrinterTagVisitor visitor = new SnbtPrinterTagVisitor("", 0, new ArrayList<>());
        final String upgradedNbt = visitor.visit(convertedTag);
        LOGGER.debug("Upgraded entity; type: '{}', nbt: '{}' -> '{}'", entityType, nbt, upgradedNbt);
        return upgradedNbt;
    }
}
