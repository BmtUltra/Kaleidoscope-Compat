package com.bmt.kaleidoscope_compat.mixins.vinery;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import net.satisfy.vinery.core.block.entity.FermentationBarrelBlockEntity;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FermentationBarrelRecipe.class)
public class FermentationBarrelRecipeMixin {

    @Inject(
            method = "matches(Lnet/satisfy/vinery/core/block/entity/FermentationBarrelBlockEntity;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void kc$interceptFermentationBarrelRecipes(FermentationBarrelBlockEntity blockEntity, Level world, CallbackInfoReturnable<Boolean> cir) {
        if (ForgeConfig.VINERY_BARREL_RECIPES_DISABLED.get()) {
            cir.setReturnValue(false);
        }
    }
}