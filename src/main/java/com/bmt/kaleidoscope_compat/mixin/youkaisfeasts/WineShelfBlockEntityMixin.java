package com.bmt.kaleidoscope_compat.mixin.youkaisfeasts;

import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import dev.xkmc.youkaishomecoming.content.item.fluid.BucketBottleItem;
import dev.xkmc.youkaishomecoming.content.item.fluid.SlipBottleItem;
import dev.xkmc.youkaishomecoming.content.pot.storage.shelf.WineShelfBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WineShelfBlockEntity.class)
public class WineShelfBlockEntityMixin {

    @Inject(
            method = "isFlask",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void kaleidoscopeCompat$isFlask(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof SlipBottleItem || stack.getItem() instanceof BucketBottleItem) {
            cir.setReturnValue(true);
            return;
        }

        if (stack.getItem() instanceof BottleBlockItem || stack.getItem() instanceof DrinkBlockItem) {
            cir.setReturnValue(true);
        }
    }
}