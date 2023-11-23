package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.IFilePackResource;
import de.maxhenkel.resourcepackchecker.ResourcePackChecker;
import de.maxhenkel.resourcepackchecker.ShaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Collection;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

    @Shadow
    @Final
    protected Connection connection;

    @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
    private void setServerPack(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (ResourcePackChecker.CLIENT_CONFIG.ignoreServerPacks.get()) {
            ResourcePackChecker.LOGGER.info("Ignored server resource pack '{}', required={}", packet.url(), packet.required());
            ci.cancel();
        }
    }

    @Inject(method = "handleResourcePackPush", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/ClientboundResourcePackPushPacket;required()Z", shift = At.Shift.AFTER), cancellable = true)
    private void handleResourcePack(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (packet.hash().isBlank()) {
            return;
        }
        Collection<Pack> selectedPacks = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks();
        for (Pack pack : selectedPacks) {
            PackResources resources = pack.open();
            if (resources instanceof IFilePackResource) {
                File f = ((IFilePackResource) resources).getFile();
                String packSha1 = ShaUtils.getSha1(f);
                if (packSha1.equals(packet.hash())) {
                    connection.send(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
                    ci.cancel();
                    break;
                }
            }
        }
    }

}
