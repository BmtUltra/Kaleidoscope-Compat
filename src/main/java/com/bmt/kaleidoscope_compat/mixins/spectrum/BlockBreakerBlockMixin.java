package com.bmt.kaleidoscope_compat.mixins.spectrum;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import de.dafuqs.spectrum.blocks.redstone.BlockBreakerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBreakerBlock.class)
public class BlockBreakerBlockMixin {
    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void onDestroy(ServerLevel world, BlockPos breakerPos, Direction direction, CallbackInfo ci) {
        if (!MainConfig.spectrumBlockBreakerCompatEnabled) {
            return;
        }

        BlockPos breakingPos = breakerPos.relative(direction);
        BlockState blockState = world.getBlockState(breakingPos);

        if (!(blockState.getBlock() instanceof StockpotBlock)) {
            return;
        }

        if (!blockState.getValue(StockpotBlock.HAS_LID)) {
            return;
        }
        ci.cancel();

        BlockEntity blockEntity = world.getBlockEntity(breakingPos);
        if (blockEntity instanceof StockpotBlockEntity stockpot) {
            ItemStack lidItem = stockpot.getLidItem().isEmpty() 
                    ? new ItemStack(ModItems.STOCKPOT_LID.get()) 
                    : stockpot.getLidItem().copy();
            
            Block.popResource(world, breakingPos, lidItem);

            stockpot.setLidItem(ItemStack.EMPTY);
            stockpot.setChanged();

            world.setBlockAndUpdate(breakingPos, blockState.setValue(StockpotBlock.HAS_LID, false));
        }
    }
}