package de.maxhenkel.resourcepackchecker;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.resourcepackchecker.interfaces.PackRepository;
import de.maxhenkel.resourcepackchecker.repository.ServerPacksCacheRepositorySource;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class ResourcePackChecker implements ClientModInitializer {

    public static final String MODID = "resourcepackchecker";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ClientConfig CLIENT_CONFIG;

    @Override
    public void onInitializeClient() {
        Minecraft mc = Minecraft.getInstance();
        CLIENT_CONFIG = ConfigBuilder
                .builder(ClientConfig::new)
                .path(mc.gameDirectory.toPath().resolve("config").resolve(MODID).resolve("resourcepackchecker.properties"))
                .build();

        if (CLIENT_CONFIG.listCachedServerPacks.get()) {
            Path downloads = mc.gameDirectory.toPath().resolve("downloads");
            ((PackRepository) mc.getResourcePackRepository()).addSource(new ServerPacksCacheRepositorySource(downloads));
        }
    }
}
