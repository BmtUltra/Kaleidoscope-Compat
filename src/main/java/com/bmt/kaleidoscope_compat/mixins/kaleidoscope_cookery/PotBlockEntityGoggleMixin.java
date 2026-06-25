package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.compat.create.PotArmAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(PotBlockEntity.class)
public class PotBlockEntityGoggleMixin implements IHaveGoggleInformation {

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        PotBlockEntity self = (PotBlockEntity) (Object) this;
        
        if (self.getLevel() == null) {
            return false;
        }

        // 获取动力臂设置的菜谱
        if (!(self instanceof PotArmAutomation armAutomation)) {
            return false;
        }
        
        RecipeItem.RecipeRecord storedRecipe = armAutomation.kaleidoscopeCompat$getStoredRecipe();
        
        if (storedRecipe == null) {
            return false;
        }

        // 显示标题
        tooltip.add(Component.literal("    ").append(Component.translatable("gui.goggles.mechanical_arm.pot_recipe")
            .withStyle(ChatFormatting.WHITE)));
        
        // 显示食材需求
        tooltip.add(Component.literal("     ").append(Component.translatable("gui.goggles.mechanical_arm.ingredients"))
            .withStyle(ChatFormatting.GOLD));
        
        List<ItemStack> inputs = storedRecipe.input();
        for (ItemStack input : inputs) {
            if (!input.isEmpty()) {
                Component itemName = input.getHoverName().copy()
                    .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(input))));
                tooltip.add(Component.literal("      ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("- "))
                    .append(itemName));
            }
        }
        
        // 显示产出物
        ItemStack result = storedRecipe.output();
        if (!result.isEmpty()) {
            tooltip.add(Component.literal("     ").append(Component.translatable("gui.goggles.mechanical_arm.result"))
                .withStyle(ChatFormatting.GOLD));
            Component resultName = result.getHoverName().copy()
                .withStyle(Style.EMPTY
                    .withColor(ChatFormatting.GREEN)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(result))));
            tooltip.add(Component.literal("      ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("- "))
                .append(resultName)
                .append(Component.literal(" x" + result.getCount()).withStyle(ChatFormatting.GRAY)));
        }
        
        return true;
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return kaleidoscopeCompat$getRecipeStack();
    }

    @Unique
    private ItemStack kaleidoscopeCompat$getRecipeStack() {
        PotBlockEntity self = (PotBlockEntity) (Object) this;
        if (!(self instanceof PotArmAutomation armAutomation)) {
            return ItemStack.EMPTY;
        }
        RecipeItem.RecipeRecord recipe = armAutomation.kaleidoscopeCompat$getStoredRecipe();
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModItems.RECIPE_ITEM.get());
        RecipeItem.setRecipe(stack, recipe);
        return stack;
    }
}
