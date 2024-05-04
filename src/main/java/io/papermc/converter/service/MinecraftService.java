package io.papermc.converter.service;

public interface MinecraftService {
    String upgradeCommand(String input);

    String upgradeItemArgument(String input);

    String upgradeComponentArgument(String input);

    String upgradeEntity(String entityType, String nbt);
}
