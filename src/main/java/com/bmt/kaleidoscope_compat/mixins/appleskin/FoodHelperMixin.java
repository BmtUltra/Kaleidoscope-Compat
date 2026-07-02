package com.bmt.kaleidoscope_compat.mixins.appleskin;

import com.bmt.kaleidoscope_compat.compat.appleskin.AppleSkinCompat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import squeek.appleskin.api.food.FoodValues;
import squeek.appleskin.helpers.FoodHelper;

import java.util.Optional;

@Mixin(value = FoodHelper.class, remap = false)
public class FoodHelperMixin {

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    private static void kaleidoscopeCompat$isFood(ItemStack itemStack, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (AppleSkinCompat.shouldTreatAsFood(itemStack, player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private static void kaleidoscopeCompat$canConsume(ItemStack itemStack, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (AppleSkinCompat.shouldTreatAsFood(itemStack, player)) {
            cir.setReturnValue(player.canEat(true));
        }
    }

    @Inject(method = "getDefaultFoodValues", at = @At("HEAD"), cancellable = true)
    private static void kaleidoscopeCompat$getDefaultFoodValues(ItemStack itemStack, Player player, CallbackInfoReturnable<FoodValues> cir) {
        Optional<FoodProperties> previewFood = AppleSkinCompat.findPreviewFoodProperties(itemStack, player);
        if (previewFood.isEmpty()) {
            return;
        }

        FoodProperties foodProperties = previewFood.get();
        cir.setReturnValue(new FoodValues(foodProperties.getNutrition(), foodProperties.getSaturationModifier()));
    }

    @Inject(method = "isRotten", at = @At("HEAD"), cancellable = true)
    private static void kaleidoscopeCompat$isRotten(ItemStack itemStack, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (AppleSkinCompat.shouldTreatAsFood(itemStack, player)) {
            Optional<FoodProperties> previewFood = AppleSkinCompat.findPreviewFoodProperties(itemStack, player);
            if (previewFood.isPresent()) {
                FoodProperties foodProperties = previewFood.get();
                for (var effect : foodProperties.getEffects()) {
                    if (effect.getFirst() != null) {
                        effect.getFirst().getEffect();
                        if (effect.getFirst().getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
                cir.setReturnValue(false);
            }
        }
    }
}