package de.maxhenkel.resourcepackchecker.repository;

import de.maxhenkel.resourcepackchecker.ResourcePackChecker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ServerPacksCacheRepositorySource implements RepositorySource {

    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private static final String CACHE_PREFIX = "cache/";

    protected Path path;

    public ServerPacksCacheRepositorySource(Path path) {
        this.path = path;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        try {
            load(consumer);
        } catch (IOException e) {
            ResourcePackChecker.LOGGER.error("Error loading cached pack", e);
        }
    }

    private void load(Consumer<Pack> consumer) throws IOException {
        List<Path> list = Files.list(path).toList();

        for (Path file : list) {
            if (!Files.isDirectory(file)) {
                continue;
            }
            String folder = file.getFileName().toString();
            UUID id;
            try {
                id = UUID.fromString(folder);
            } catch (IllegalArgumentException e) {
                continue;
            }
            List<Path> packs = Files.list(file).toList();
            for (Path packPath : packs) {
                if (!SHA1.matcher(packPath.getFileName().toString()).matches()) {
                    continue;
                }
                if (!Files.isRegularFile(packPath)) {
                    continue;
                }
                Pack pack = fileToPack(id, packPath.getFileName().toString(), packPath);
                if (pack != null) {
                    consumer.accept(pack);
                }
            }
        }
    }

    @Nullable
    private Pack fileToPack(UUID id, String hash, Path resourcePackFile) {
        return Pack.readMetaAndCreate(new PackLocationInfo(idFromHash(hash), cachedPackName(hash), PackSource.SERVER, Optional.empty()), new FilePackResources.FileResourcesSupplier(resourcePackFile), PackType.CLIENT_RESOURCES, new PackSelectionConfig(false, Pack.Position.TOP, false));
    }

    private Component cachedPackName(String name) {
        return ComponentUtils.wrapInSquareBrackets(Component.translatable("message.resourcepackchecker.cached_pack")).withStyle(ChatFormatting.GRAY).append(" ").append(Component.literal(name).withStyle(ChatFormatting.WHITE));
    }

    public static boolean isCached(Pack pack) {
        return pack.getId().startsWith(CACHE_PREFIX);
    }

    public static String idFromHash(String hash) {
        return CACHE_PREFIX + hash;
    }

}
