package com.bmt.kaleidoscope_compat.mixins.spectrum;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import de.dafuqs.spectrum.blocks.redstone.BlockPlacerBlock;
import de.dafuqs.spectrum.blocks.redstone.BlockPlacerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockPlacerBlock.class)
public class BlockPlacerBlockMixin {
    @Inject(method = "tryPlace", at = @At("TAIL"))
    private void onTryPlace(ItemStack stack, BlockSource pointer, net.minecraft.world.entity.player.Player owner, CallbackInfo ci) {
        if (!MainConfig.spectrumBlockPlacerCompatEnabled) {
            return;
        }

        ServerLevel level = pointer.level();
        BlockPos placerPos = pointer.pos();

        BlockState placerState = level.getBlockState(placerPos);
        if (!(placerState.getBlock() instanceof BlockPlacerBlock)) {
            return;
        }

        FrontAndTop orientation = placerState.getValue(BlockStateProperties.ORIENTATION);
        Direction facing = orientation.front();
        BlockPos targetPos = placerPos.relative(facing);

        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.getBlock() instanceof StockpotBlock) {
            kaleidoscope_Compat_1_21_1_NeoForge$handleStockpotLid(level, placerPos, targetPos, targetState);
            return;
        }

        if (targetState.getBlock() instanceof PotBlock) {
            kaleidoscope_Compat_1_21_1_NeoForge$handlePotStirFry(level, placerPos, targetPos, targetState);
        }
    }

    @Unique
    private static void kaleidoscope_Compat_1_21_1_NeoForge$handleStockpotLid(ServerLevel level, BlockPos placerPos, BlockPos targetPos, BlockState targetState) {
        if (targetState.getValue(StockpotBlock.HAS_LID)) {
            return;
        }

        if (level.getBlockEntity(placerPos) instanceof BlockPlacerBlockEntity placer) {
            for (int i = 0; i < placer.getContainerSize(); i++) {
                ItemStack lidStack = placer.getItem(i);
                if (lidStack.is(ModItems.STOCKPOT_LID.get())) {
                    lidStack.shrink(1);
                    if (level.getBlockEntity(targetPos) instanceof StockpotBlockEntity stockpot) {
                        stockpot.setLidItem(new ItemStack(ModItems.STOCKPOT_LID.get()));
                        stockpot.setChanged();
                        level.setBlockAndUpdate(targetPos, targetState.setValue(StockpotBlock.HAS_LID, true));
                    }
                    return;
                }
            }
        }
    }

    @Unique
    @SuppressWarnings("all")
    private static void kaleidoscope_Compat_1_21_1_NeoForge$handlePotStirFry(ServerLevel level, BlockPos placerPos, BlockPos targetPos, BlockState targetState) {
        if (level.getBlockEntity(placerPos) instanceof BlockPlacerBlockEntity placer) {
            for (int i = 0; i < placer.getContainerSize(); i++) {
                ItemStack shovelStack = placer.getItem(i);
                if (shovelStack.isEmpty()) {
                    continue;
                }

                if (!shovelStack.is(TagMod.KITCHEN_SHOVEL)) {
                    continue;
                }

                if (level.getBlockEntity(targetPos) instanceof PotBlockEntity pot) {
                    pot.onShovelHit(level, null, shovelStack);
                    return;
                }
            }
        }
    }
}