package de.maxhenkel.resourcepackchecker.mixin;

import de.maxhenkel.resourcepackchecker.interfaces.FilePackResource;
import net.minecraft.server.packs.OverlayedPackResources;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

@Mixin(OverlayedPackResources.class)
public class OverlayedPackResourcesMixin implements FilePackResource {

    @Shadow
    @Final
    private PackMetadataResources primaryPackMetadataResources;

    @Shadow
    @Final
    private List<PackResources> packResourcesStack;

    @Nullable
    @Override
    public File resourcepack_checker$getFile() {
        if (primaryPackMetadataResources instanceof FilePackResource filePackResource) {
            return filePackResource.resourcepack_checker$getFile();
        }
        for (PackResources resources : packResourcesStack) {
            if (resources instanceof FilePackResource filePackResource) {
                return filePackResource.resourcepack_checker$getFile();
            }
        }
        return null;
    }
}
