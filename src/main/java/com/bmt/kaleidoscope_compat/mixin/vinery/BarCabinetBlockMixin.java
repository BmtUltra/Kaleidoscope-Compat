package com.bmt.kaleidoscope_compat.mixin.vinery;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarCabinetBlock.class)
public abstract class BarCabinetBlockMixin {

    @Inject(method = "getBottleBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void kc$getVineryBottleBlock(ItemStack stack, CallbackInfoReturnable<BottleBlock> cir) {
        if (stack.getItem() instanceof DrinkBlockItem) {
            cir.setReturnValue((BottleBlock) ModBlocks.EMPTY_BOTTLE.get());
        }
    }
}