package de.maxhenkel.resourcepackchecker;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

public class ClientConfig {

    public final ConfigEntry<Boolean> ignoreServerPacks;

    public ClientConfig(ConfigBuilder builder) {
        ignoreServerPacks = builder.booleanEntry("ignore_server_packs", false);
    }
}
