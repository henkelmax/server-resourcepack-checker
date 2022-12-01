package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.IFilePackResource;
import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(FilePackResources.class)
public class FilePackResourcesMixin implements IFilePackResource {

    @Shadow
    @Final
    private File file;

    @Override
    public File getFile() {
        return file;
    }
}
