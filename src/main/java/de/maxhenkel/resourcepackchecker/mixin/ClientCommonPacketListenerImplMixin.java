package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.IFilePackResource;
import de.maxhenkel.resourcepackchecker.ResourcePackChecker;
import de.maxhenkel.resourcepackchecker.ShaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Collection;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private void setServerPack(ClientboundResourcePackPacket clientboundResourcePackPacket, CallbackInfo ci) {
        if (ResourcePackChecker.CLIENT_CONFIG.ignoreServerPacks.get()) {
            ResourcePackChecker.LOGGER.info("Ignored server resource pack '{}', required={}", clientboundResourcePackPacket.getUrl(), clientboundResourcePackPacket.isRequired());
            ci.cancel();
        }
    }

    @Inject(method = "handleResourcePack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/ClientboundResourcePackPacket;isRequired()Z", shift = At.Shift.AFTER), cancellable = true)
    private void handleResourcePack(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        if (packet.getHash().isBlank()) {
            return;
        }
        Collection<Pack> selectedPacks = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks();
        for (Pack pack : selectedPacks) {
            PackResources resources = pack.open();
            if (resources instanceof IFilePackResource) {
                File f = ((IFilePackResource) resources).getFile();
                String packSha1 = ShaUtils.getSha1(f);
                if (packSha1.equals(packet.getHash())) {
                    send(ServerboundResourcePackPacket.Action.ACCEPTED);
                    ci.cancel();
                    break;
                }
            }
        }
    }

    @Shadow
    protected abstract void send(ServerboundResourcePackPacket.Action action);

}
