package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.item.SickleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SickleItem.class)
public class SickleItemMixin {

    @Inject(
            method = "harvest",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void kc$harvestWithTags(BlockPos pos, int x, int y, int z, Level level, Player player, ItemStack stack,
                                    CallbackInfoReturnable<Boolean> cir) {
        BlockPos newPos = pos.offset(x, y, z);
        BlockState blockState = level.getBlockState(newPos);

        if (blockState.isAir()) {
            cir.setReturnValue(false);
            return;
        }

        if (blockState.is(TagUtil.Blocks.SICKLE_BREAKABLE_WITH_DROPS)) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlock(newPos, true, player);
                cir.setReturnValue(true);
            }
            return;
        }

        if (blockState.is(TagUtil.Blocks.SICKLE_BREAKABLE_NO_DROPS)) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlock(newPos, false);
                cir.setReturnValue(true);
            }
        }
    }
}