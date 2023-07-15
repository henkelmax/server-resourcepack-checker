package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.IFilePackResource;
import de.maxhenkel.resourcepackchecker.ResourcePackChecker;
import de.maxhenkel.resourcepackchecker.ShaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.server.packs.PackResources;
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
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Mixin(DownloadedPackSource.class)
public class DownloadedPackSourceMixin {

    private static final Pattern SERVER_PACK_PATTERN = Pattern.compile("server_.{40}\\.zip");

    @Shadow
    private Pack serverPack;

    @Inject(at = @At("HEAD"), method = "setServerPack", cancellable = true)
    private void setServerPack(File file, PackSource packSource, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        String serverPackSha1 = ShaUtils.getSha1(file);

        if (ResourcePackChecker.CLIENT_CONFIG.saveServerPacks.get()) {
            Path resourcePack = Minecraft.getInstance().getResourcePackDirectory().resolve("server_%s.zip".formatted(file.getName()));
            if (Files.notExists(resourcePack)) {
                try {
                    Files.copy(file.toPath(), resourcePack);
                } catch (IOException e) {
                    ResourcePackChecker.LOGGER.error("Failed to copy server pack", e);
                    return;
                }
            }

            PackRepository resourcePackRepository = Minecraft.getInstance().getResourcePackRepository();
            resourcePackRepository.reload();

            boolean changedPack = false;

            for (Pack pack : resourcePackRepository.getAvailablePacks()) {
                File packFile = getPackFile(pack);
                if (packFile == null) {
                    continue;
                }
                String packSha1 = getPackSha(pack);
                if (serverPackSha1.equals(packSha1)) {
                    if (resourcePackRepository.addPack(pack.getId())) {
                        changedPack = true;
                        ResourcePackChecker.LOGGER.info("Enabled new server pack");
                    }
                    continue;
                }
                if (SERVER_PACK_PATTERN.matcher(packFile.getName()).matches()) {
                    if (resourcePackRepository.removePack(pack.getId())) {
                        changedPack = true;
                        ResourcePackChecker.LOGGER.info("Disabled old server pack");
                    }
                }
            }

            serverPack = null;
            CompletableFuture<Void> future;
            if (changedPack) {
                future = Minecraft.getInstance().delayTextureReload();
            } else {
                future = new CompletableFuture<>();
                future.complete(null);
            }
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
