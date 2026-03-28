package com.bmt.kaleidoscope_compat.mixin.solcarrot;

import com.bmt.kaleidoscope_compat.compat.solcarrot.FoodListFilter;
import com.cazsius.solcarrot.tracking.FoodList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoodList.class)
public class FoodListMixin {

    @Inject(
        method = "addFood(Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void kaleidoscopeCompat$filterFoodOnAdd(ItemStack food, CallbackInfoReturnable<Boolean> cir) {
        if (FoodListFilter.shouldFilter(food)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "hasEaten(Lnet/minecraft/world/item/Item;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void kaleidoscopeCompat$filterFoodOnCheck(Item food, CallbackInfoReturnable<Boolean> cir) {
        if (FoodListFilter.shouldFilter(food)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "hasEaten(Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void kaleidoscopeCompat$filterFoodStackOnCheck(ItemStack food, CallbackInfoReturnable<Boolean> cir) {
        if (FoodListFilter.shouldFilter(food)) {
            cir.setReturnValue(false);
        }
    }
}