package com.bmt.kaleidoscope_compat.mixins.youkaisfeasts;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import dev.xkmc.youkaishomecoming.content.pot.steamer.RackItemData;
import dev.xkmc.youkaishomecoming.content.pot.steamer.SteamerBlockEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RackItemData.class)
public class RackItemDataMixin {

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kc$interceptRackItemTick(SteamerBlockEntity be, Level level, double heat, CallbackInfoReturnable<Boolean> cir) {
        if (MainConfig.steamingRecipesDisabled) {
            cir.setReturnValue(false);
        }
    }
}