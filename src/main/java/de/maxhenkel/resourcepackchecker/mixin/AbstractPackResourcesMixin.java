package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.IFilePackResource;
import net.minecraft.server.packs.AbstractPackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(AbstractPackResources.class)
public class AbstractPackResourcesMixin implements IFilePackResource {

    @Shadow
    @Final
    protected File file;

    @Override
    public File getFile() {
        return file;
    }
}
