package com.bmt.kaleidoscope_compat.mixin.youkaisfeasts;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
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
        if (stack.is(TagUtil.Items.WINE)) {
            cir.setReturnValue(true);
        }
    }
}