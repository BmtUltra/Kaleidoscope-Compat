package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.replacement;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.StockpotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(StockpotRecipe.class)
public class StockpotMatchesMixin {

    @Unique
    private static final String REPLACEMENT_TYPE = "stockpot";

    @Unique
    private static final ThreadLocal<Boolean> IS_REPLACING = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "matches(Lcom/github/ysbbbbbb/kaleidoscopecookery/crafting/container/StockpotInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onMatches(StockpotInput container, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (IS_REPLACING.get()) {
            return;
        }

        List<ItemStack> originalInputs = container.getInputs();
        List<ItemStack> replacedInputs = new ArrayList<>();
        boolean modified = false;

        for (ItemStack stack : originalInputs) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, itemId);

            if (replacement != null) {
                Item newItem = BuiltInRegistries.ITEM.get(replacement);
                if (newItem != Items.AIR) {
                    replacedInputs.add(new ItemStack(newItem, stack.getCount()));
                    modified = true;
                } else {
                    replacedInputs.add(stack);
                }
            } else {
                replacedInputs.add(stack);
            }
        }

        if (modified) {
            IS_REPLACING.set(true);
            try {
                StockpotInput replacedInput = new StockpotInput(replacedInputs, container.getSoupBase());
                StockpotRecipe recipe = (StockpotRecipe) (Object) this;
                boolean matches = recipe.matches(replacedInput, level);
                cir.setReturnValue(matches);
            } finally {
                IS_REPLACING.set(false);
            }
        }
    }
}