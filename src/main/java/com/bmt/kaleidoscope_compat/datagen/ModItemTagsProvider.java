package com.bmt.kaleidoscope_compat.datagen;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider,
                CompletableFuture.completedFuture(TagLookup.empty()),
                "kaleidoscope_compat", existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        TagAppender<Item> strawHatsTag = tag(TagUtil.Items.STRAW_HATS);
        strawHatsTag.addOptional(ResourceLocation.fromNamespaceAndPath("vinery", "straw_hat"));
        strawHatsTag.addOptional(ResourceLocation.fromNamespaceAndPath("sakura", "strawhat"));
        strawHatsTag.addOptional(ResourceLocation.fromNamespaceAndPath("artifacts", "villager_hat"));
        strawHatsTag.addOptional(ResourceLocation.fromNamespaceAndPath("overweight_farming", "straw_hat"));
    }
}