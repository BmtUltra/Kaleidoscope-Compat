package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_tavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.BarrelRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

@Mixin(value = BarrelBlockEntity.class, remap = false)
public abstract class BarrelBlockEntityMixin {

    @Shadow @Final private ItemStackHandler output;
    @Shadow private int brewLevel;
    @Shadow private int brewTime;
    @Shadow private ResourceLocation recipeId;

    @Shadow public abstract boolean isBrewing();
    @Shadow public abstract void clearItemsAndFluid();

    @Inject(method = "doTapExtract", at = @At("HEAD"), cancellable = true)
    private void kc$doTapExtract(Level level, BlockPos tapPos, CallbackInfo ci) {
        if (!this.isBrewing()) {
            return;
        }
        if (this.output.getStackInSlot(0).isEmpty()) {
            return;
        }
        if (this.recipeId == null || this.recipeId.equals(BarrelRecipeSerializer.EMPTY_RECIPE_ID)) {
            return;
        }

        level.getRecipeManager().byKey(this.recipeId).ifPresent(holder -> {
            if (!(holder.value() instanceof BarrelRecipe barrelRecipe)) {
                return;
            }

            ItemStack recipeResult = barrelRecipe.result();
            Item resultItem = recipeResult.getItem();

            if (!kc$isDrinkBlockItem(resultItem)) {
                return;
            }

            BlockPos below = tapPos.below();
            BlockState belowState = level.getBlockState(below);
            ItemStack belowStack = belowState.getBlock().asItem().getDefaultInstance();
            if (!barrelRecipe.carrier().test(belowStack)) {
                ci.cancel();
                return;
            }

            BottleBlockItem placeholder = (BottleBlockItem) ModItems.WINE.get();

            this.output.extractItem(0, 1, false);

            BlockState placed = placeholder.getBlock().defaultBlockState();
            if (placed.hasProperty(HORIZONTAL_FACING) && belowState.hasProperty(HORIZONTAL_FACING)) {
                placed = placed.setValue(HORIZONTAL_FACING, belowState.getValue(HORIZONTAL_FACING));
            }
            level.setBlockAndUpdate(below, placed);

            if (level.getBlockEntity(below) instanceof DrinkBlockEntity drinkBlock) {
                drinkBlock.addItem(recipeResult.copyWithCount(1));
                drinkBlock.refresh();
            }

            if (this.output.getStackInSlot(0).isEmpty()) {
                this.clearItemsAndFluid();
                this.recipeId = null;
                this.brewLevel = 0;
                this.brewTime = -1;
                kc$refresh();
            }
            ci.cancel();
        });
    }

    @Unique
    private boolean kc$isDrinkBlockItem(Item item) {
        try {
            Class<?> itemClass = item.getClass();
            String className = itemClass.getName();

            if (className.equals("net.satisfy.vinery.core.item.DrinkBlockItem")) {
                return true;
            }

            Class<?> superClass = itemClass.getSuperclass();
            while (superClass != null) {
                if (superClass.getName().equals("net.satisfy.vinery.core.item.DrinkBlockItem")) {
                    return true;
                }
                superClass = superClass.getSuperclass();
            }

            for (Class<?> interfaceClass : itemClass.getInterfaces()) {
                if (interfaceClass.getName().equals("net.satisfy.vinery.core.item.DrinkBlockItem")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Unique
    private void kc$refresh() {
        BarrelBlockEntity self = (BarrelBlockEntity) (Object) this;
        self.setChanged();
        Level level = self.getLevel();
        if (level != null) {
            BlockPos pos = self.getBlockPos();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }
}