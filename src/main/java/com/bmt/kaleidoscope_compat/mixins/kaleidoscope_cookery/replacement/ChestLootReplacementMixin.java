package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.replacement;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class ChestLootReplacementMixin {

    @Unique
    private static final String REPLACEMENT_TYPE = "chestloot";

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onGetRandomItems(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> stacks = cir.getReturnValue();
        if (stacks == null || stacks.isEmpty()) {
            return;
        }

        boolean modified = false;
        ObjectArrayList<ItemStack> newStacks = new ObjectArrayList<>();

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                newStacks.add(stack);
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);

            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    newStacks.add(new ItemStack(newItem, stack.getCount()));
                    modified = true;
                } else {
                    newStacks.add(stack);
                }
            } else {
                newStacks.add(stack);
            }
        }

        if (modified) {
            cir.setReturnValue(newStacks);
        }
    }
}