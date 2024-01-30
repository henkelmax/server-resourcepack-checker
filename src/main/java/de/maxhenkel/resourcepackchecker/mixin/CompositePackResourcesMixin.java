package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.interfaces.FilePackResource;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

@Mixin(CompositePackResources.class)
public class CompositePackResourcesMixin implements FilePackResource {

    @Shadow
    @Final
    private PackResources primaryPackResources;

    @Shadow
    @Final
    private List<PackResources> packResourcesStack;

    @Nullable
    @Override
    public File getFile() {
        if (primaryPackResources instanceof FilePackResource filePackResource) {
            return filePackResource.getFile();
        }
        for (PackResources resources : packResourcesStack) {
            if (resources instanceof FilePackResource filePackResource) {
                return filePackResource.getFile();
            }
        }
        return null;
    }
}
