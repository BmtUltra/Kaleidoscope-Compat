package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_tavern;

import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.ITapBehavior;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.BarrelRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BarrelBlockEntity.class, remap = false)
public abstract class BarrelBlockEntityMixin {

    @Shadow @Final private ItemStackHandler output;
    @Shadow private int brewLevel;
    @Shadow private int brewTime;
    @Shadow private ResourceLocation recipeId;

    @Shadow public abstract boolean isBrewing();
    @Shadow public abstract void clearItemsAndFluid();
    @Shadow public abstract int getBrewLevel();
    @Shadow public abstract void tip(@Nullable LivingEntity entity, String key);

    @Inject(method = "canTapExtract", at = @At("HEAD"), cancellable = true)
    private void kc$canTapExtractDepot(Level level, BlockPos tapPos, @Nullable LivingEntity user,
                                        CallbackInfoReturnable<Boolean> cir) {
        BlockPos belowPos = tapPos.below();
        if (!(level.getBlockEntity(belowPos) instanceof DepotBlockEntity depot)) {
            return;
        }

        if (!this.isBrewing()) {
            this.tip(user, "tap_extract_not_brewing");
            cir.setReturnValue(false);
            return;
        }
        if (this.output.getStackInSlot(0).isEmpty()) {
            this.tip(user, "tap_extract_empty");
            cir.setReturnValue(false);
            return;
        }

        Ingredient carrier = kc$getCurrentCarrier(level);
        if (carrier == null) {
            this.tip(user, "tap_extract_invalid_container");
            cir.setReturnValue(false);
            return;
        }

        ItemStack heldItem = depot.getHeldItem();
        if (!heldItem.isEmpty() && carrier.test(heldItem)) {
            cir.setReturnValue(true);
            return;
        }

        this.tip(user, "tap_extract_empty_container");
        cir.setReturnValue(false);
    }

    @Inject(method = "doTapExtract", at = @At("HEAD"), cancellable = true)
    private void kc$doTapExtract(Level level, BlockPos tapPos, CallbackInfo ci) {
        if (!this.isBrewing()) {
            return;
        }
        if (this.output.getStackInSlot(0).isEmpty()) {
            return;
        }

        BlockPos below = tapPos.below();
        if (!(level.getBlockEntity(below) instanceof DepotBlockEntity depot)) {
            return;
        }

        Ingredient carrier;
        ItemStack recipeResult;

        if (this.recipeId == null || this.recipeId.equals(BarrelRecipeSerializer.EMPTY_RECIPE_ID)) {
            carrier = Ingredient.of(ModItems.EMPTY_BOTTLE.get());
            recipeResult = ModItems.VINEGAR.get().getDefaultInstance();
        } else {
            var recipeOpt = level.getRecipeManager().byKey(this.recipeId);
            if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof BarrelRecipe barrelRecipe)) {
                return;
            }
            carrier = barrelRecipe.carrier();
            recipeResult = barrelRecipe.result();
        }

        kc$handleCreateDepot(level, depot, below, carrier, recipeResult, ci);
    }

    @Unique
    private void kc$handleCreateDepot(Level level, DepotBlockEntity depot, BlockPos depotPos,
                                       Ingredient carrier, ItemStack recipeResult, CallbackInfo ci) {
        TransportedItemStackHandlerBehaviour handler =
                depot.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) {
            return;
        }

        ItemStack filledBottle = recipeResult.copy();
        filledBottle.setCount(1);
        if (filledBottle.getItem() instanceof BottleBlockItem bottleBlockItem) {
            filledBottle = bottleBlockItem.getFilledStack(this.getBrewLevel());
        }

        final Ingredient finalCarrier = carrier;
        final ItemStack finalFilledBottle = filledBottle;
        final boolean[] processed = {false};

        handler.handleProcessingOnAllItems(item -> {
            if (processed[0]) {
                return TransportedResult.doNothing();
            }
            if (!finalCarrier.test(item.stack)) {
                return TransportedResult.doNothing();
            }

            ItemStack remaining = item.stack.copy();
            remaining.shrink(1);

            TransportedItemStack output = item.copy();
            output.stack = finalFilledBottle.copy();

            processed[0] = true;

            if (!remaining.isEmpty()) {
                TransportedItemStack heldRemainder = item.copy();
                heldRemainder.stack = remaining;
                return TransportedResult.convertToAndLeaveHeld(List.of(output), heldRemainder);
            }

            return TransportedResult.convertTo(output);
        });

        if (!processed[0]) {
            return;
        }

        this.output.extractItem(0, 1, false);

        level.playSound(null, depotPos, SoundEvents.BREWING_STAND_BREW,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        ITapBehavior.sendParticles(level,
                depotPos.above());

        if (this.output.getStackInSlot(0).isEmpty()) {
            this.clearItemsAndFluid();
            this.recipeId = null;
            this.brewLevel = 0;
            this.brewTime = -1;
            kc$refresh();
        }

        ci.cancel();
    }

    @Unique
    private Ingredient kc$getCurrentCarrier(Level level) {
        if (this.recipeId == null || this.recipeId.equals(BarrelRecipeSerializer.EMPTY_RECIPE_ID)) {
            return Ingredient.of(ModItems.EMPTY_BOTTLE.get());
        }
        return level.getRecipeManager().byKey(this.recipeId).map(recipe -> {
            if (recipe.value() instanceof BarrelRecipe barrelRecipe) {
                return barrelRecipe.carrier();
            }
            return null;
        }).orElse(null);
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