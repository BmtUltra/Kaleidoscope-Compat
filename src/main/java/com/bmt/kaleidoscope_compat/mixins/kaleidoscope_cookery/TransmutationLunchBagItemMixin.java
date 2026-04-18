package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransmutationLunchBagItem.class)
public class TransmutationLunchBagItemMixin {

    @Inject(
            method = "canAdd(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void checkBlacklist(ItemStack food, CallbackInfoReturnable<Boolean> cir) {
        if (MainConfig.isItemBlacklisted(food.getItem())) {
            cir.setReturnValue(false);
        }
    }
}