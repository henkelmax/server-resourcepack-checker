package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.ResourcePackChecker;
import de.maxhenkel.resourcepackchecker.interfaces.ClearableServerPackManager;
import de.maxhenkel.resourcepackchecker.repository.ServerPacksCacheRepositorySource;
import de.maxhenkel.resourcepackchecker.utils.ApplyPackUtils;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackReloadConfig;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {

    @Shadow
    @Final
    private static RepositorySource EMPTY_SOURCE;

    @Shadow
    private RepositorySource packSource;

    @Shadow
    @Final
    ServerPackManager manager;

    @Inject(method = "startReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/server/PackReloadConfig$Callbacks;packsToLoad()Ljava/util/List;"), cancellable = true)
    private void handleResourcePack(PackReloadConfig.Callbacks callbacks, CallbackInfo ci) {
        if (!(ResourcePackChecker.CLIENT_CONFIG.keepCachedServerPacks.get() && ResourcePackChecker.CLIENT_CONFIG.listCachedServerPacks.get())) {
            return;
        }
        ci.cancel();
        packSource = EMPTY_SOURCE;
        callbacks.onSuccess();
        List<String> ids = callbacks.packsToLoad().stream().map(i -> i.path().getFileName().toString()).map(ServerPacksCacheRepositorySource::idFromHash).toList();
        ApplyPackUtils.equipPack(ids);
        ((ClearableServerPackManager) manager).clear();
    }
}
