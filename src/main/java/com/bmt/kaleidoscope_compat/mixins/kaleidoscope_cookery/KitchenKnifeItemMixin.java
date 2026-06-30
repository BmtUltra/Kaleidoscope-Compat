package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenKnifeItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.neoforged.neoforge.common.ItemAbilities.SWORD_SWEEP;

@Mixin(KitchenKnifeItem.class)
public class KitchenKnifeItemMixin {

    @Inject(method = "canPerformAction", at = @At("HEAD"), cancellable = true)
    private void onCanPerformAction(ItemStack stack, ItemAbility itemAbility, CallbackInfoReturnable<Boolean> cir) {
        if (itemAbility == SWORD_SWEEP) {
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (var entry : enchantments.entrySet()) {
                if (entry.getKey().is(Enchantments.SWEEPING_EDGE)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}