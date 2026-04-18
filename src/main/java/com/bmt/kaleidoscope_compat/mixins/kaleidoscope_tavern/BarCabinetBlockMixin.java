package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_tavern;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarCabinetBlock.class)
public abstract class BarCabinetBlockMixin {

    @Inject(method = "getBottleBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void kc$getVineryBottleBlock(ItemStack stack, CallbackInfoReturnable<BottleBlock> cir) {
        if (stack.is(TagUtil.Items.SMALL_BOTTLE)) {
            cir.setReturnValue((BottleBlock) ModBlocks.EMPTY_BOTTLE.get());
        }
        else if (stack.is(TagUtil.Items.LARGE_BOTTLE)) {
            cir.setReturnValue((BottleBlock) ModBlocks.BRANDY.get());
        }
    }
}