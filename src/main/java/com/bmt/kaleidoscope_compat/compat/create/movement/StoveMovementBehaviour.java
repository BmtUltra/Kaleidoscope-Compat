package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StoveBlock;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * 炉灶在动态结构上的移动行为
 */
public class StoveMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof StoveBlock;
    }

    @Override
    protected boolean requiresNbt() {
        return false;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        return state.getValue(BlockStateProperties.LIT);
    }

    @Override
    protected void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt) {
        RandomSource random = context.world.random;

        // 熄灭检测：雨水
        if (context.world.getGameTime() % 20 == 0) {
            Vec3 gp = getGlobalPos(context);
            BlockPos globalPos = BlockPos.containing(gp);
            if (context.world.isRainingAt(globalPos.above())) {
                extinguish(context, state);
                return;
            }
        }

        // 熄灭检测：上方水方块
        if (context.world.getGameTime() % 10 == 0) {
            Vec3 gp = getGlobalPos(context);
            BlockPos globalPos = BlockPos.containing(gp);
            if (context.world.isWaterAt(globalPos.above())) {
                extinguish(context, state);
                return;
            }
        }

        // 火焰粒子
        if (context.world instanceof ServerLevel && random.nextFloat() < 0.15F) {
            BlockState blockState = context.contraption.getBlocks().get(context.localPos).state();
            net.minecraft.core.Direction direction = blockState.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
            net.minecraft.core.Direction.Axis axis = direction.getAxis();
            double offsetRandom = random.nextDouble() * 0.6 - 0.3;
            double xOffset = axis == net.minecraft.core.Direction.Axis.X ? (double) direction.getStepX() * 0.52 : offsetRandom;
            double yOffset = 0.25 + random.nextDouble() * 6.0 / 16.0;
            double zOffset = axis == net.minecraft.core.Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : offsetRandom;

            ContraptionUtil.spawnParticle(context, ParticleTypes.FLAME,
                    0.5 + xOffset, yOffset, 0.5 + zOffset,
                    1, 0, 0, 0, 0);
        }

        // 烟雾粒子
        if (context.world instanceof ServerLevel && random.nextFloat() < 0.1F) {
            double xRand = random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1);
            double yRand = 0.5 + random.nextDouble() / 3;
            double zRand = random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1);

            ContraptionUtil.spawnParticle(context, ParticleTypes.SMOKE,
                    0.5 + xRand, yRand, 0.5 + zRand,
                    1, 0, 0.02, 0, 0);
        }

        // 噼啪音效
        if (context.world.getGameTime() % 40 == 0 && random.nextInt(4) == 0) {
            playSound(context, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.6F);
        }
    }

    private void extinguish(MovementContext context, BlockState state) {
        if (!(context.world instanceof ServerLevel)) return;

        BlockState newState = state.setValue(BlockStateProperties.LIT, false);
        updateData(context, newState, context.contraption.getBlocks().get(context.localPos).nbt());

        playSound(context, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
