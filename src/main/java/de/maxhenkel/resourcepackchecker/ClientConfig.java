package de.maxhenkel.resourcepackchecker;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

public class ClientConfig {

    public final ConfigEntry<Boolean> ignoreServerPacks;
    public final ConfigEntry<Boolean> listCachedServerPacks;
    public final ConfigEntry<Boolean> keepCachedServerPacks;

    public ClientConfig(ConfigBuilder builder) {
        ignoreServerPacks = builder.booleanEntry(
                "ignore_server_packs",
                false,
                "Enabling this option will completely ignore server resource packs"
        );
        listCachedServerPacks = builder.booleanEntry(
                "list_cached_server_packs",
                false,
                "Enabling this option will show cached downloaded server resource packs in your resource pack selection"
        );
        keepCachedServerPacks = builder.booleanEntry(
                "keep_cached_server_packs",
                false,
                "Enabling this option will use the cached server resource packs when connecting to a server that provides a resource pack",
                "This will also leave the resource pack enabled after leaving the server",
                "NOTE: This option only works if 'list_cached_server_packs' is set to 'true'"
        );
    }
}
