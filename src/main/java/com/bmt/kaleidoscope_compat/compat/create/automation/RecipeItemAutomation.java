package com.bmt.kaleidoscope_compat.compat.create.automation;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class RecipeItemAutomation {
    private RecipeItemAutomation() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown() || !(event.getItemStack().getItem() instanceof RecipeItem)) {
            return;
        }

        ItemStack recipeStack = event.getItemStack();
        RecipeItem.RecipeRecord recipe = RecipeItem.getRecipe(recipeStack);
        if (recipe == null) {
            return;
        }

        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        Component message;
        if (blockEntity instanceof PotBlockEntity && recipe.type().equals(RecipeItem.POT)) {
            message = Component.translatable(
                    "tip.kaleidoscope_compat.pot_arm_recipe_set",
                    recipe.output().getHoverName()
            );
        } else if (blockEntity instanceof StockpotBlockEntity && recipe.type().equals(RecipeItem.STOCKPOT)) {
            message = Component.translatable(
                    "tip.kaleidoscope_compat.stockpot_arm_recipe_set",
                    recipe.output().getHoverName()
            );
        } else {
            return;
        }

        if (!RecipeAutomation.configure(blockEntity, recipe)) {
            return;
        }

        event.getEntity().displayClientMessage(message, true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof RecipeItem)) {
            return;
        }

        RecipeItem.RecipeRecord recipe = RecipeItem.getRecipe(stack);
        if (recipe == null) {
            return;
        }

        if (recipe.type().equals(RecipeItem.POT)) {
            event.getToolTip().add(Component.translatable("tooltip.kaleidoscope_compat.recipe_item.pot_arm_recipe")
                    .withStyle(ChatFormatting.GRAY));
        } else if (recipe.type().equals(RecipeItem.STOCKPOT)) {
            event.getToolTip().add(Component.translatable("tooltip.kaleidoscope_compat.recipe_item.stockpot_arm_recipe")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
