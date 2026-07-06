package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.compat.create.StockpotArmAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(StockpotBlockEntity.class)
public class StockpotBlockEntityGoggleMixin implements IHaveGoggleInformation {

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        StockpotBlockEntity self = (StockpotBlockEntity) (Object) this;
        
        if (self.getLevel() == null) {
            return false;
        }

        if (!(self instanceof StockpotArmAutomation armAutomation)) {
            return false;
        }
        
        RecipeItem.RecipeRecord storedRecipe = armAutomation.kaleidoscopeCompat$getStoredRecipe();
        
        if (storedRecipe == null) {
            return false;
        }

        tooltip.add(Component.literal("    ").append(Component.translatable("gui.goggles.mechanical_arm.stockpot_recipe")
            .withStyle(ChatFormatting.WHITE)));

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

        ItemStack result = storedRecipe.output();
        if (!result.isEmpty()) {
            tooltip.add(Component.literal("     ").append((Component.translatable("gui.goggles.mechanical_arm.result")))
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
        return AllItems.GOGGLES.asStack();
    }
}
