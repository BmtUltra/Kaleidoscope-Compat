package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.common.BasicItemListing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BasicItemListing.class)
public class BasicItemListingMixin {

    @Unique
    private static final String REPLACEMENT_TYPE = "villager";

    @Shadow @Final
    protected ItemStack price;

    @Shadow @Final
    protected ItemStack price2;

    @Shadow @Final
    protected ItemStack forSale;

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

        boolean modified = false;

        ItemCost newPrice = offer.getItemCostA();
        if (newPrice != null) {
            Item item = newPrice.item().value();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);
            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    newPrice = new ItemCost(newItem, newPrice.count());
                    modified = true;
                }
            }
        }

        ItemCost newPrice2 = offer.getItemCostB().orElse(null);
        if (newPrice2 != null) {
            Item item = newPrice2.item().value();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);
            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    newPrice2 = new ItemCost(newItem, newPrice2.count());
                    modified = true;
                }
            }
        }

        ItemStack newForSale = offer.getResult().copy();
        if (!newForSale.isEmpty()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(newForSale.getItem());
            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);
            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    newForSale = new ItemStack(newItem, newForSale.getCount());
                    modified = true;
                }
            }
        }

        if (modified) {
            MerchantOffer newOffer = new MerchantOffer(
                    newPrice,
                    Optional.ofNullable(newPrice2),
                    newForSale,
                    offer.getMaxUses(),
                    offer.getXp(),
                    offer.getPriceMultiplier()
            );
            cir.setReturnValue(newOffer);
        }
    }
}