package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeapotBlockEntity.class)
@SuppressWarnings("all")
public class TeapotBlockEntityMixin {

    @Inject(method = "addTeaFluid", at = @At("HEAD"), cancellable = true)
    private void onAddTeaFluid(Level level, LivingEntity user, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (user == null) {
            TeapotBlockEntity self = (TeapotBlockEntity) (Object) this;

            if (self.getStatus() != 0) {
                cir.setReturnValue(false);
                return;
            }

            if (!self.getTeaFluidId().equals(
                    com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                cir.setReturnValue(false);
                return;
            }

            var cap = itemStack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
            if (cap == null) {
                cir.setReturnValue(false);
                return;
            }

            net.neoforged.neoforge.fluids.FluidStack fluidInTank = cap.getFluidInTank(0);
            if (fluidInTank.isEmpty() || fluidInTank.getAmount() < net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME) {
                cir.setReturnValue(false);
                return;
            }

            net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluidInTank.getFluid());
            net.neoforged.neoforge.fluids.capability.templates.FluidTank needFluidHandler = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(
                    net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME,
                    stack -> net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(stack, fluidInTank));

            if (!com.github.ysbbbbbb.kaleidoscopecookery.util.FluidUtils.emptyItem(null, itemStack, needFluidHandler, net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME)) {
                cir.setReturnValue(false);
                return;
            }

            ((TeapotBlockEntityAccessor) self).kaleidoscopeCompat$setTeaFluidId(id);
            self.setChanged();

            if (level != null && !level.isClientSide) {
                ((net.minecraft.server.level.ServerLevel) level).getChunkSource().blockChanged(self.getBlockPos());
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "addIngredient", at = @At("HEAD"), cancellable = true)
    private void onAddIngredient(Level level, LivingEntity user, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (user == null) {
            TeapotBlockEntity self = (TeapotBlockEntity) (Object) this;

            if (self.getStatus() != 0) {
                cir.setReturnValue(false);
                return;
            }

            if (self.getTeaFluidId().equals(
                    com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                cir.setReturnValue(false);
                return;
            }

            if (!self.getInput().isEmpty()) {
                cir.setReturnValue(false);
                return;
            }

            com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput container =
                    new com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput(itemStack, self.getTeaFluidId());
            var recipeOpt = self.getLevel().getRecipeManager()
                    .getRecipeFor(com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes.TEAPOT_RECIPE, container, level);

            if (recipeOpt.isPresent()) {
                var recipe = recipeOpt.get().value();
                int count = recipe.ingredientCount();

                ((TeapotBlockEntityAccessor) self).kaleidoscopeCompat$setInput(itemStack.copyWithCount(count));
                ((TeapotBlockEntityAccessor) self).kaleidoscopeCompat$setCurrentTick(com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity.INGREDIENT_TIME);
                self.setChanged();

                if (level != null && !level.isClientSide) {
                    ((net.minecraft.server.level.ServerLevel) level).getChunkSource().blockChanged(self.getBlockPos());
                }
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}