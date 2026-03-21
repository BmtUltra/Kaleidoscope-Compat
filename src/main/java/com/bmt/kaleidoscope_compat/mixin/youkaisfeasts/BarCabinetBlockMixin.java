package com.bmt.kaleidoscope_compat.mixin.youkaisfeasts;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import dev.xkmc.youkaishomecoming.content.item.fluid.BucketBottleItem;
import dev.xkmc.youkaishomecoming.content.item.fluid.SlipBottleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarCabinetBlock.class)
public abstract class BarCabinetBlockMixin {

    @Inject(method = "getBottleBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void kc$getYoukaisFeastsBottleBlock(ItemStack stack, CallbackInfoReturnable<BottleBlock> cir) {
        if (stack.getItem() instanceof SlipBottleItem || stack.getItem() instanceof BucketBottleItem) {
            cir.setReturnValue((BottleBlock) ModBlocks.EMPTY_BOTTLE.get());
        }
    }
}