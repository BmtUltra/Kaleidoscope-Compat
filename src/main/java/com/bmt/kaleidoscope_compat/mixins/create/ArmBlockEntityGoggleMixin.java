package com.bmt.kaleidoscope_compat.mixins.create;

import com.bmt.kaleidoscope_compat.compat.create.PotArmAutomation;
import com.bmt.kaleidoscope_compat.compat.create.StockpotArmAutomation;
import com.bmt.kaleidoscope_compat.mixins.create.accessor.ArmBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ArmBlockEntity.class)
public abstract class ArmBlockEntityGoggleMixin {

    @Inject(method = "addToTooltip", at = @At("RETURN"), remap = false, cancellable = true)
    private void kaleidoscopeCompat$addArmRecipeInfo(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (isPlayerSneaking) return;
        
        ArmBlockEntity self = (ArmBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) return;

        ArmBlockEntityAccessor accessor = (ArmBlockEntityAccessor) this;

        RecipeItem.RecipeRecord foundRecipe = kaleidoscopeCompat$findRecipe(accessor, level);
        if (foundRecipe != null) {
            kaleidoscope_Compat_Dev_1$addRecipeToTooltip(tooltip, foundRecipe);
            // 强制返回 true，让 GoggleOverlayRenderer 显示 tooltip
            cir.setReturnValue(true);
        }
    }
    
    @Unique
    private RecipeItem.RecipeRecord kaleidoscopeCompat$findRecipe(ArmBlockEntityAccessor accessor, Level level) {
        // 检查输入
        List<ArmInteractionPoint> inputs = accessor.getInputs();
        if (inputs != null) {
            for (ArmInteractionPoint point : inputs) {
                if (point == null || !point.isValid()) continue;
                RecipeItem.RecipeRecord recipe = kaleidoscopeCompat$getRecipeAt(level, point.getPos());
                if (recipe != null) return recipe;
            }
        }

        // 检查输出
        List<ArmInteractionPoint> outputs = accessor.getOutputs();
        if (outputs != null) {
            for (ArmInteractionPoint point : outputs) {
                if (point == null || !point.isValid()) continue;
                RecipeItem.RecipeRecord recipe = kaleidoscopeCompat$getRecipeAt(level, point.getPos());
                if (recipe != null) return recipe;
            }
        }
        return null;
    }
    
    @Unique
    private RecipeItem.RecipeRecord kaleidoscopeCompat$getRecipeAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PotArmAutomation armAutomation) {
            return armAutomation.kaleidoscopeCompat$getStoredRecipe();
        }
        if (be instanceof StockpotArmAutomation stockpotAutomation) {
            return stockpotAutomation.kaleidoscopeCompat$getStoredRecipe();
        }
        return null;
    }
    
    @Unique
    private void kaleidoscope_Compat_Dev_1$addRecipeToTooltip(List<Component> tooltip, RecipeItem.RecipeRecord recipe) {
        // 显示配方类型标题
        String typePath = recipe.type().getPath();
        String typeKey = typePath.equals("pot") 
            ? "gui.goggles.mechanical_arm.pot_recipe" 
            : "gui.goggles.mechanical_arm.stockpot_recipe";
        
        tooltip.add(Component.literal("    ").append(Component.translatable(typeKey)
            .withStyle(ChatFormatting.WHITE)));
        
        // 显示食材需求
        tooltip.add(Component.literal("     ").append(Component.translatable("gui.goggles.mechanical_arm.ingredients")
            .withStyle(ChatFormatting.GOLD)));
        
        List<ItemStack> inputs = recipe.input();
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
        ItemStack result = recipe.output();
        if (!result.isEmpty()) {
            tooltip.add(Component.literal("     ").append(Component.translatable("gui.goggles.mechanical_arm.result")
                .withStyle(ChatFormatting.GOLD)));
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
    }
}
