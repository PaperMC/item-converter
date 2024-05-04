package io.papermc.converter.service;

import io.papermc.converter.MinecraftRuntime;
import org.springframework.stereotype.Service;

@Service
public final class MinecraftServiceWrapper implements MinecraftService {
    private final MinecraftService impl;

    public MinecraftServiceWrapper() {
        this.impl = MinecraftRuntime.createMinecraftService();
    }

    @Override
    public String upgradeCommand(final String input) {
        return this.impl.upgradeCommand(input);
    }

    @Override
    public String upgradeItemArgument(final String input) {
        return this.impl.upgradeItemArgument(input);
    }

    @Override
    public String upgradeComponentArgument(final String input) {
        return this.impl.upgradeComponentArgument(input);
    }

    @Override
    public String upgradeEntity(final String entityType, final String nbt) {
        return this.impl.upgradeEntity(entityType, nbt);
    }
}
