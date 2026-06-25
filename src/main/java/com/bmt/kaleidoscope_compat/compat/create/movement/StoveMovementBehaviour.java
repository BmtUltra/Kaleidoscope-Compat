package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StoveBlock;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
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
        if (context.world instanceof ServerLevel sl && random.nextFloat() < 0.15F) {
            BlockState blockState = context.contraption.getBlocks().get(context.localPos).state();
            Direction direction = blockState.getValue(HorizontalDirectionalBlock.FACING);
            Direction.Axis axis = direction.getAxis();
            double offsetRandom = random.nextDouble() * 0.6 - 0.3;
            double xOffset = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : offsetRandom;
            double yOffset = 0.25 + random.nextDouble() * 6.0 / 16.0;
            double zOffset = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : offsetRandom;
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ParticleTypes.FLAME,
                    gp.x + xOffset, gp.y + yOffset - 0.5, gp.z + zOffset,
                    1, 0, 0, 0, 0);
        }

        // 烟雾粒子
        if (context.world instanceof ServerLevel sl && random.nextFloat() < 0.1F) {
            double xRand = random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1);
            double yRand = 0.5 + random.nextDouble() / 3;
            double zRand = random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1);
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ParticleTypes.SMOKE,
                    gp.x+ xRand, gp.y + yRand, gp.z + zRand,
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
