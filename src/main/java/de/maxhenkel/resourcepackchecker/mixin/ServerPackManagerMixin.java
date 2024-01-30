package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.interfaces.ClearableServerPackManager;
import net.minecraft.client.resources.server.ServerPackManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ServerPackManager.class)
public class ServerPackManagerMixin implements ClearableServerPackManager {

    @Shadow
    @Final
    List<?> packs;

    @Override
    public void clear() {
        packs.clear();
    }
}
