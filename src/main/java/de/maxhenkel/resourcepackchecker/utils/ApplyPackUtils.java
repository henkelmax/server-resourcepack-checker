package de.maxhenkel.resourcepackchecker.utils;

import de.maxhenkel.resourcepackchecker.repository.ServerPacksCacheRepositorySource;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplyPackUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void equipPack(List<String> packIds) {
        PackRepository resourcePackRepository = mc.getResourcePackRepository();
        resourcePackRepository.reload();
        List<String> selectedPacks = new ArrayList<>(resourcePackRepository.getSelectedPacks().stream().filter(p -> !ServerPacksCacheRepositorySource.isCached(p)).map(Pack::getId).toList());
        selectedPacks.addAll(packIds);
        resourcePackRepository.setSelected(selectedPacks);
        mc.options.updateResourcePacks(resourcePackRepository);
    }

    public static void equipPack(String packId) {
        equipPack(Collections.singletonList(packId));
    }

    public static void equipPack(Pack pack) {
        equipPack(pack.getId());
    }

}
