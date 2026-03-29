package com.bmt.kaleidoscope_compat.mixin.solcarrot;

import com.bmt.kaleidoscope_compat.compat.solcarrot.FoodListFilter;
import com.cazsius.solcarrot.client.FoodItems;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FoodItems.class)
public class FoodItemsMixin {

    @Inject(
            method = "getAllFoodsIgnoringBlacklist()Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void kaleidoscopeCompat$filterFoodsIgnoringBlacklist(CallbackInfoReturnable<List<Item>> cir) {
        List<Item> originalList = cir.getReturnValue();
        if (originalList == null) {
            return;
        }

        List<Item> filteredList = originalList.stream()
                .filter(item -> !FoodListFilter.shouldFilter(item))
                .toList();

        cir.setReturnValue(filteredList);
    }

    @Inject(
            method = "getAllFoods()Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void kaleidoscopeCompat$filterFoods(CallbackInfoReturnable<List<Item>> cir) {
        List<Item> originalList = cir.getReturnValue();
        if (originalList == null) {
            return;
        }

        List<Item> filteredList = originalList.stream()
                .filter(item -> !FoodListFilter.shouldFilter(item))
                .toList();

        cir.setReturnValue(filteredList);
    }

    @Inject(
            method = "setUp(Lnet/minecraftforge/fml/event/lifecycle/FMLLoadCompleteEvent;)V",
            at = @At("TAIL"),
            remap = false
    )
    private static void kaleidoscopeCompat$filterOnSetUp(net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent event, CallbackInfo ci) {
        FoodListFilter.initialize();
    }
}