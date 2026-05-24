package com.bmt.kaleidoscope_compat.mixins.spectrum;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.PotBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import de.dafuqs.spectrum.blocks.pastel_network.nodes.PastelNodeBlockEntity;
import de.dafuqs.spectrum.blocks.pastel_network.nodes.PastelNodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(PastelNodeBlockEntity.class)
public class PastelNodeBlockEntityMixin {

    @Inject(method = "getTransferFilterTo", at = @At("HEAD"), cancellable = true)
    private void onGetTransferFilterTo(PastelNodeBlockEntity other, CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        if (!MainConfig.spectrumPotItemHandlerEnabled) {
            return;
        }

        if (other.getNodeType() != PastelNodeType.GATHER) {
            return;
        }

        BlockPos targetPos = other.getBlockPos();
        Level level = other.getLevel();
        if (level == null) {
            return;
        }

        BlockState targetState = level.getBlockState(targetPos);
        if (!(targetState.getBlock() instanceof PotBlock)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        if (!(blockEntity instanceof PotBlockEntity pot)) {
            return;
        }

        PotBlockEntityAccessor accessor = (PotBlockEntityAccessor) pot;

        int status = pot.getStatus();

        if (status == PotBlockEntity.FINISHED || status == PotBlockEntity.BURNT) {
            Ingredient carrier = accessor.kaleidoscopeCompat$getCarrier();
            if (carrier != null && !carrier.isEmpty()) {
                ItemStack[] carrierItems = carrier.getItems();
                if (carrierItems.length > 0) {
                    cir.setReturnValue(stack -> {
                        for (ItemStack carrierStack : carrierItems) {
                            if (ItemStack.isSameItem(stack, carrierStack)) {
                                return true;
                            }
                        }
                        return false;
                    });
                    return;
                }
            } else {
                cir.setReturnValue(stack -> false);
                return;
            }
        }
        if (!targetState.getValue(PotBlock.HAS_OIL)) {
            return;
        }

        if (status != PotBlockEntity.PUT_INGREDIENT) {
            return;
        }
        List<ItemStack> existingInputs = pot.getInputs();

        SimpleInput container = pot.getContainer();
        var recipeOpt = level.getRecipeManager()
                .getRecipeFor(ModRecipes.POT_RECIPE, container, level);

        if (recipeOpt.isEmpty()) {
            return;
        }

        var recipe = recipeOpt.get().value();
        List<ItemStack> recipeInputs = new ArrayList<>();

        for (var ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                ItemStack[] items = ingredient.getItems();
                if (items.length > 0) {
                    recipeInputs.add(items[0]);
                }
            }
        }

        List<ItemStack> missingItems = new ArrayList<>();
        for (ItemStack recipeItem : recipeInputs) {
            boolean found = false;
            for (ItemStack existing : existingInputs) {
                if (!existing.isEmpty() && ItemStack.isSameItem(recipeItem, existing)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingItems.add(recipeItem);
            }
        }

        if (missingItems.isEmpty()) {
            cir.setReturnValue(stack -> false);
            return;
        }

        if (missingItems.size() > 1) {
            return;
        }
        ItemStack neededItem = missingItems.getFirst();
        cir.setReturnValue(stack -> ItemStack.isSameItem(stack, neededItem));
    }
}