package com.bmt.kaleidoscope_compat.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.entity.BlockEntity;

@SuppressWarnings("all")
public class MaidMillstoneBehavior extends MaidCheckRateTask {
    private final float movementSpeed;
    private BlockPos currentWorkPos = null;
    private boolean hasReached = false;
    private boolean isBound = false;

    public MaidMillstoneBehavior(float movementSpeed) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                InitEntities.TARGET_POS.get(), MemoryStatus.REGISTERED
        ));
        this.movementSpeed = movementSpeed;
        this.setMaxCheckRate(20);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
        if (super.checkExtraStartConditions(worldIn, owner)) {
            BlockPos millstonePos = this.findMillstone(worldIn, owner);
            if (millstonePos == null) {
                return false;
            }
            this.currentWorkPos = millstonePos;
            double distSq = millstonePos.distToCenterSqr(owner.position());
            if (distSq < 1.44 && Math.abs(owner.getY() - (millstonePos.getY() + 0.5)) < 1.5) {
                this.hasReached = true;
                owner.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                return true;
            }
            BehaviorUtils.setWalkAndLookTargetMemories(owner, millstonePos, this.movementSpeed, 0);
            this.hasReached = false;
            return true;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        this.isBound = false;
        maid.getNavigation().stop();
    }

    @Override
    protected void tick(ServerLevel world, EntityMaid maid, long gameTime) {

        double distSq = this.currentWorkPos.distToCenterSqr(maid.position());
        if (distSq > 1.44 || Math.abs(maid.getY() - (this.currentWorkPos.getY() + 0.5)) >= 1.5) {
            if (!this.hasReached) {
                BehaviorUtils.setWalkAndLookTargetMemories(maid, this.currentWorkPos, this.movementSpeed, 0);
            }
            return;
        }
        this.hasReached = true;
        maid.getNavigation().stop();

        BlockEntity blockEntity = world.getBlockEntity(this.currentWorkPos);
        if (!(blockEntity instanceof MillstoneBlockEntity)) {
            return;
        }

        if (!this.isBound) {
            double targetX = this.currentWorkPos.getX() + 0.5;
            double targetZ = this.currentWorkPos.getZ() + 0.5;
            double dx = targetX - maid.getX();
            double dz = targetZ - maid.getZ();
            double distToCenter = Math.sqrt(dx * dx + dz * dz);

            if (distToCenter > 0.3) {
                double factor = 0.1;
                maid.setDeltaMovement(dx * factor, maid.getDeltaMovement().y, dz * factor);
            } else {
                this.isBound = true;
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        double distSq = entityIn.distanceToSqr(
                this.currentWorkPos.getX() + 0.5,
                entityIn.getY(),
                this.currentWorkPos.getZ() + 0.5
        );

        if (this.hasReached) {
            return distSq < 16.0;
        }
        return distSq < 2.25;
    }

    private BlockPos findMillstone(ServerLevel world, EntityMaid maid) {
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(
                maid.getNavigation().getNodeEvaluator(), world, maid
        );
        BlockPos centrePos = maid.hasRestriction() ? maid.getRestrictCenter() : maid.blockPosition();
        int searchRange = maid.hasRestriction() ? (int) maid.getRestrictRadius() : 16;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int yOffset = 0; yOffset <= 4; yOffset = yOffset > 0 ? -yOffset : 1 - yOffset) {
            for (int r = 0; r <= searchRange; r++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        if (Math.abs(x) == r || Math.abs(z) == r) {
                            mutableBlockPos.setWithOffset(centrePos, x, yOffset, z);
                            if (maid.isWithinRestriction(mutableBlockPos)
                                    && this.isWorkable(world, mutableBlockPos)
                                    && pathFinding.canPathReach(mutableBlockPos)) {
                                BlockPos result = mutableBlockPos.immutable();
                                pathFinding.finish();
                                return result;
                            }
                        }
                    }
                }
            }
        }
        pathFinding.finish();
        return null;
    }

    private boolean isWorkable(ServerLevel world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof MillstoneBlockEntity;
    }
}