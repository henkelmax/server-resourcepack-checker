package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.IFilePackResource;
import de.maxhenkel.resourcepackchecker.ResourcePackChecker;
import de.maxhenkel.resourcepackchecker.ShaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Mixin(DownloadedPackSource.class)
public class DownloadedPackSourceMixin {

    private static final Pattern SERVER_PACK_PATTERN = Pattern.compile("^server_.{40}.*\\.zip$");

    @Shadow
    private Pack serverPack;

    @Inject(at = @At("HEAD"), method = "setServerPack", cancellable = true)
    private void setServerPack(File file, PackSource packSource, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        String serverPackSha1 = ShaUtils.getSha1(file);

        if (ResourcePackChecker.CLIENT_CONFIG.saveServerPacks.get()) {
            PackRepository resourcePackRepository = Minecraft.getInstance().getResourcePackRepository();
            Path resourcePackDirectory = Minecraft.getInstance().getResourcePackDirectory();
            serverPack = null;

            String packName = "";
            try (FilePackResources packResources = new FilePackResources(file.getName(), new FilePackResources.SharedZipFileAccess(file), false, "")) {
                PackMetadataSection metadataSection = packResources.getMetadataSection(PackMetadataSection.TYPE);
                if (metadataSection != null) {
                    packName = metadataSection.description().getString().replaceAll("§[0-9a-zA-Z]", "").replace("\n", " ").replaceAll("[^a-zA-Z0-9-_. ]", "");
                } else {
                    ResourcePackChecker.LOGGER.error("Could not find metadata section in server pack");
                }
            } catch (IOException e) {
                ResourcePackChecker.LOGGER.error("Failed to read server pack metadata", e);
            }

            Path resourcePack = resourcePackDirectory.resolve("server_%s%s.zip".formatted(serverPackSha1, "_%s".formatted(packName)));
            if (Files.notExists(resourcePack)) {
                try {
                    Files.copy(file.toPath(), resourcePack);
                } catch (IOException e) {
                    ResourcePackChecker.LOGGER.error("Failed to copy server pack", e);
                    return;
                }
                resourcePackRepository.reload();
            }

            Pack newPack = findPack(serverPackSha1, resourcePack);

            if (newPack == null) {
                ResourcePackChecker.LOGGER.error("Could not find resource pack with sha1 {}", serverPackSha1);
                return;
            }

            List<Pack> selectedPacks = new ArrayList<>(resourcePackRepository.getSelectedPacks());

            List<Pack> toRemove = new ArrayList<>();

            int replaceIndex = -1;
            for (Pack pack : selectedPacks) {
                File packFile = getPackFile(pack);
                if (packFile == null) {
                    continue;
                }
                String packSha1 = getPackSha(pack);
                if (SERVER_PACK_PATTERN.matcher(packFile.getName()).matches()) {
                    if (serverPackSha1.equals(packSha1) && packFile.getName().equals(resourcePack.getFileName().toString())) {
                        if (replaceIndex >= 0) {
                            toRemove.add(selectedPacks.get(replaceIndex));
                        }
                        replaceIndex = selectedPacks.indexOf(pack);
                    } else if (replaceIndex < 0) {
                        replaceIndex = selectedPacks.indexOf(pack);
                    } else {
                        toRemove.add(pack);
                    }
                }
            }
            CompletableFuture<Void> future;
            if (replaceIndex < 0) {
                selectedPacks.add(newPack);
                future = Minecraft.getInstance().delayTextureReload();
            } else if (selectedPacks.get(replaceIndex).getId().equals(newPack.getId())) {
                future = new CompletableFuture<>();
                future.complete(null);
            } else {
                selectedPacks.set(replaceIndex, newPack);
                future = Minecraft.getInstance().delayTextureReload();
            }
            if (selectedPacks.removeAll(toRemove)) {
                future = Minecraft.getInstance().delayTextureReload();
            }
            resourcePackRepository.setSelected(selectedPacks.stream().map(Pack::getId).toList());

            cir.setReturnValue(future);
            return;
        }

        Collection<Pack> selectedPacks = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks();
        for (Pack pack : selectedPacks) {
            String packSha1 = getPackSha(pack);
            if (serverPackSha1.equals(packSha1)) {
                serverPack = null;
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.complete(null);
                cir.setReturnValue(future);
                break;
            }
        }
    }

    @Unique
    @Nullable
    private Pack findPack(String sha1, Path path) {
        for (Pack pack : Minecraft.getInstance().getResourcePackRepository().getAvailablePacks()) {
            File packFile = getPackFile(pack);
            if (packFile == null) {
                continue;
            }
            if (!packFile.getName().equals(path.getFileName().toString())) {
                continue;
            }
            String packSha1 = getPackSha(pack);
            if (sha1.equals(packSha1)) {
                return pack;
            }
        }
        return null;
    }

    @Unique
    @Nullable
    private String getPackSha(Pack pack) {
        File packFile = getPackFile(pack);
        if (packFile != null) {
            return ShaUtils.getSha1(packFile);
        }
        return null;
    }

    @Unique
    @Nullable
    private File getPackFile(Pack pack) {
        PackResources resources = pack.open();
        if (resources instanceof IFilePackResource resource) {
            return resource.getFile();
        }
        return null;
    }

}
