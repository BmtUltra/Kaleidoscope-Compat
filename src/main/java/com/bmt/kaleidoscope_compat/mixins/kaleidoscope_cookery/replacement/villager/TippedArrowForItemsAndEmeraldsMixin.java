package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.replacement.villager;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTrades.TippedArrowForItemsAndEmeralds.class)
public class TippedArrowForItemsAndEmeraldsMixin {

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

        ItemCost cost = offer.getItemCostA();
        if (cost == null) return;

        Item item = cost.item().value();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);

        if (replacement != null) {
            Item newItem = BuiltInRegistries.ITEM.get(replacement);
            if (newItem != Items.AIR) {
                ItemCost newCost = new ItemCost(newItem, cost.count());
                MerchantOffer newOffer = new MerchantOffer(
                        newCost,
                        offer.getResult(),
                        offer.getMaxUses(),
                        offer.getXp(),
                        offer.getPriceMultiplier()
                );
                cir.setReturnValue(newOffer);
            }
        }
    }
}