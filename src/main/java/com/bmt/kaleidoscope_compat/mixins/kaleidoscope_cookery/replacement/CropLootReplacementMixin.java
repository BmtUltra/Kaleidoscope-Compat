package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.replacement;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class CropLootReplacementMixin {

    @Unique
    private static final String REPLACEMENT_TYPE = "croploot";

    @Inject(
            method = "getDrops",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onGetDrops(BlockState state,
                            net.minecraft.world.level.storage.loot.LootParams.Builder params,
                            CallbackInfoReturnable<java.util.List<ItemStack>> cir) {
        Block block = state.getBlock();
        if (!(block instanceof CropBlock ||
              block instanceof StemBlock ||
              block instanceof SweetBerryBushBlock ||
              block instanceof CocoaBlock ||
              block instanceof NetherWartBlock ||
              block instanceof PitcherCropBlock )) {
            return;
        }

        java.util.List<ItemStack> drops = cir.getReturnValue();
        if (drops == null || drops.isEmpty()) {
            return;
        }

        boolean modified = false;
        java.util.List<ItemStack> newDrops = new java.util.ArrayList<>();

        for (ItemStack stack : drops) {
            if (stack.isEmpty()) {
                newDrops.add(stack);
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);
            
            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    newDrops.add(new ItemStack(newItem, stack.getCount()));
                    modified = true;
                } else {
                    newDrops.add(stack);
                }
            } else {
                newDrops.add(stack);
            }
        }

        if (modified) {
            cir.setReturnValue(newDrops);
        }
    }
}