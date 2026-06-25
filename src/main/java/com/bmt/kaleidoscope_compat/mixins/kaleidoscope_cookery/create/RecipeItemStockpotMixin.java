package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.create;

import com.bmt.kaleidoscope_compat.compat.create.PotArmAutomation;
import com.bmt.kaleidoscope_compat.compat.create.StockpotArmAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeItem.class)
public class RecipeItemStockpotMixin {
    @Inject(method = "onPutRecipe", at = @At("HEAD"), cancellable = true)
    private void kaleidoscopeCompat$storeStockpotRecipe(BlockEntity blockEntity, Player player, ItemStack itemInHand,
                                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.isShiftKeyDown()) {
            return;
        }
        RecipeItem.RecipeRecord record = RecipeItem.getRecipe(itemInHand);
        if (record == null) {
            return;
        }

        if (blockEntity instanceof PotBlockEntity pot && record.type().equals(RecipeItem.POT)) {
            if (pot instanceof PotArmAutomation automation) {
                automation.kaleidoscopeCompat$setStoredRecipe(record);
            }
            player.displayClientMessage(
                    Component.translatable("tip.kaleidoscope_compat.pot_arm_recipe_set", record.output().getHoverName()),
                    true);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (!(blockEntity instanceof StockpotBlockEntity stockpot) || !record.type().equals(RecipeItem.STOCKPOT)) {
            return;
        }
        if (stockpot instanceof StockpotArmAutomation automation) {
            automation.kaleidoscopeCompat$setStoredRecipe(record);
        }
        player.displayClientMessage(
                Component.translatable("tip.kaleidoscope_compat.stockpot_arm_recipe_set", record.output().getHoverName()),
                true);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void kaleidoscopeCompat$appendArmRecipeHint(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                                        TooltipFlag flag, CallbackInfo ci) {
        RecipeItem.RecipeRecord record = RecipeItem.getRecipe(stack);
        if (record == null) {
            return;
        }

        if (record.type().equals(RecipeItem.POT)) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_compat.recipe_item.pot_arm_recipe")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        if (record.type().equals(RecipeItem.STOCKPOT)) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_compat.recipe_item.stockpot_arm_recipe")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}