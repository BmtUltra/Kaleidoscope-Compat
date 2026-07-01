package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.replacement.villager;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTrades.ItemsForEmeralds.class)
public class ItemsForEmeraldsMixin {

    @Unique
    private static final String REPLACEMENT_TYPE = "villager";

    @Inject(
            method = "getOffer",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onGetOffer(net.minecraft.world.entity.Entity trader,
                            net.minecraft.util.RandomSource random,
                            CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOffer offer = cir.getReturnValue();
        if (offer == null) return;

        ItemStack result = offer.getResult();
        if (result.isEmpty()) return;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
        ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);

        if (replacement != null) {
            Item newItem = BuiltInRegistries.ITEM.get(replacement);
            if (newItem != Items.AIR) {
                ItemStack newResult = new ItemStack(newItem, result.getCount());
                MerchantOffer newOffer = new MerchantOffer(
                        offer.getItemCostA(),
                        newResult,
                        offer.getMaxUses(),
                        offer.getXp(),
                        offer.getPriceMultiplier()
                );
                cir.setReturnValue(newOffer);
            }
        }
    }
}