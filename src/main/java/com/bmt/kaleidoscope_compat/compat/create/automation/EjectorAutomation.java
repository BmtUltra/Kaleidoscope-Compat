package com.bmt.kaleidoscope_compat.compat.create.automation;

import com.bmt.kaleidoscope_compat.config.category.CreateCategory;
import com.bmt.kaleidoscope_compat.config.category.ejector.EjectorConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EjectorAutomation {
    private EjectorAutomation() {
    }

    public static ItemStack insertAtTarget(Level level, BlockPos targetPos, ItemStack stack) {
        if (!CreateCategory.createCompatEnabled || !isTargetEnabled(level, targetPos)) {
            return stack;
        }
        return WorkBlockItemAutomation.insert(level, targetPos, stack, false);
    }

    private static boolean isTargetEnabled(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PotBlockEntity) {
            return EjectorConfig.potEnabled;
        }
        if (blockEntity instanceof StockpotBlockEntity) {
            return EjectorConfig.stockpotEnabled;
        }
        if (blockEntity instanceof SteamerBlockEntity) {
            return EjectorConfig.steamerEnabled;
        }
        if (blockEntity instanceof MillstoneBlockEntity) {
            return EjectorConfig.millstoneEnabled;
        }
        if (blockEntity instanceof TeapotBlockEntity) {
            return EjectorConfig.teapotEnabled;
        }
        return blockEntity instanceof ShawarmaSpitBlockEntity && EjectorConfig.shawarmaSpitEnabled;
    }
}
