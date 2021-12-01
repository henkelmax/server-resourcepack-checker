package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.ResourcePackChecker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private void setServerPack(ClientboundResourcePackPacket clientboundResourcePackPacket, CallbackInfo ci) {
        if (ResourcePackChecker.CLIENT_CONFIG.ignoreServerPacks.get()) {
            ResourcePackChecker.LOGGER.info("Ignored server resource pack '{}', required={}", clientboundResourcePackPacket.getUrl(), clientboundResourcePackPacket.isRequired());
            ci.cancel();
        }
    }

}
