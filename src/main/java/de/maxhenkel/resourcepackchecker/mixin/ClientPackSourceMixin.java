package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.IFilePackResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(ClientPackSource.class)
public class ClientPackSourceMixin {

    @Shadow
    private Pack serverPack;

    @Inject(at = @At("HEAD"), method = "setServerPack", cancellable = true)
    private void setServerPack(File file, PackSource packSource, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        String serverPackSha1 = getSha1(file);
        Collection<Pack> selectedPacks = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks();
        for (Pack pack : selectedPacks) {
            PackResources resources = pack.open();
            if (resources instanceof IFilePackResource) {
                File f = ((IFilePackResource) resources).getFile();
                String packSha1 = getSha1(f);
                if (serverPackSha1.equals(packSha1)) {
                    serverPack = null;
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    future.complete(null);
                    cir.setReturnValue(future);
                    break;
                }
            }
        }
    }

    private String getSha1(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            String sha1 = DigestUtils.sha1Hex(fis);
            fis.close();
            return sha1;
        } catch (Exception e) {
            return "";
        }
    }

}
