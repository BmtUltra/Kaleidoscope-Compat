package com.bmt.kaleidoscope_compat.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IChoppingBoard;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

@SuppressWarnings("all")
public class MaidChoppingBoardBehavior extends MaidCheckRateTask {
    private final float movementSpeed;
    private BlockPos currentWorkPos = null;
    private BlockPos standPos = null;
    private int cutTimer = 0;
    private int restTimer = 0;
    private int actionStage = 0;
    private int failCount = 0;
    private boolean hasReached = false;

    public MaidChoppingBoardBehavior(float movementSpeed) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                InitEntities.TARGET_POS.get(), MemoryStatus.REGISTERED
        ));
        this.movementSpeed = movementSpeed;
        this.setMaxCheckRate(20);
    }

    private BlockPos findStandPos(ServerLevel world, BlockPos boardPos) {
        BlockState state = world.getBlockState(boardPos);
        Direction facing = Direction.SOUTH;
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            facing = state.getValue(HorizontalDirectionalBlock.FACING);
        }
        BlockPos standPos = boardPos.relative(facing.getOpposite());
        if (world.getBlockState(standPos).canBeReplaced()) {
            return standPos;
        }
        for (Direction dir : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos sidePos = boardPos.relative(dir);
            if (world.getBlockState(sidePos).canBeReplaced()) {
                return sidePos;
            }
        }
        return boardPos;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
        if (super.checkExtraStartConditions(worldIn, owner)) {
            BlockPos boardPos = this.findChoppingBoard(worldIn, owner);
            if (boardPos == null) {
                return false;
            }
            this.currentWorkPos = boardPos;
            this.standPos = this.findStandPos(worldIn, boardPos);

            double distSq = this.standPos.distToCenterSqr(owner.position());
            if (distSq < 1.44 && Math.abs(owner.getY() - (this.standPos.getY() + 0.5)) < 1.5) {
                this.hasReached = true;
                owner.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                return true;
            }
            BehaviorUtils.setWalkAndLookTargetMemories(owner, this.standPos, this.movementSpeed, 0);
            this.hasReached = false;
            return true;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        this.cutTimer = 0;
        this.restTimer = 0;
        this.actionStage = 0;
        this.failCount = 0;
        maid.getNavigation().stop();
    }

    @Override
    protected void tick(ServerLevel world, EntityMaid maid, long gameTime) {

        double distSq = this.standPos.distToCenterSqr(maid.position());
        if (distSq > 1.44 || Math.abs(maid.getY() - (this.standPos.getY() + 0.5)) >= 1.5) {
            if (!this.hasReached) {
                BehaviorUtils.setWalkAndLookTargetMemories(maid, this.standPos, this.movementSpeed, 0);
            }
            return;
        }
        this.hasReached = true;
        maid.getNavigation().stop();

        BlockEntity blockEntity = world.getBlockEntity(this.currentWorkPos);
        if (!(blockEntity instanceof IChoppingBoard choppingBoard)) {
            return;
        }

        maid.getLookControl().setLookAt(blockEntity.getBlockPos().getCenter());

        switch (this.actionStage) {
            case 0 -> this.tryPutItem(world, maid, choppingBoard);
            case 1 -> this.tryCut(world, maid, choppingBoard);
            case 2 -> this.tryTakeOut(world, maid, choppingBoard);
        }
    }

    private void tryPutItem(ServerLevel world, EntityMaid maid, IChoppingBoard choppingBoard) {
        IItemHandler inventory = maid.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
        if (inventory == null) {
            return;
        }

        if (choppingBoard.onCutItem(world, maid, ItemStack.EMPTY)) {
            this.actionStage = 2;
            this.failCount = 0;
            return;
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack toPut = stack.copyWithCount(1);
            if (choppingBoard.onPutItem(world, maid, toPut)) {
                inventory.extractItem(slot, 1, false);
                maid.swing(InteractionHand.MAIN_HAND);
                this.cutTimer = 0;
                this.restTimer = 0;
                this.failCount = 0;
                this.actionStage = 1;
                return;
            }
        }

        this.failCount++;
        if (this.failCount >= 3) {
            this.actionStage = 2;
            this.failCount = 0;
        }
    }

    private void tryCut(ServerLevel world, EntityMaid maid, IChoppingBoard choppingBoard) {
        ItemStack mainHandItem = maid.getMainHandItem();

        if (!mainHandItem.is(TagMod.KITCHEN_KNIFE)) {
            IItemHandler inventory = maid.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
            if (inventory == null) {
                return;
            }

            int knifeSlot = -1;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.is(TagMod.KITCHEN_KNIFE)) {
                    knifeSlot = slot;
                    break;
                }
            }

            if (knifeSlot == -1) {
                if (this.restTimer < 60) {
                    this.restTimer++;
                } else {
                    this.restTimer = 0;
                    this.failCount++;
                    if (this.failCount >= 3) {
                        this.actionStage = 2;
                        this.failCount = 0;
                    }
                }
                return;
            }

            ItemStack knife = inventory.extractItem(knifeSlot, 1, false);
            ItemStack oldMainHand = maid.getMainHandItem();
            maid.setItemSlot(EquipmentSlot.MAINHAND, knife);
            if (!oldMainHand.isEmpty()) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    oldMainHand = inventory.insertItem(i, oldMainHand, false);
                    if (oldMainHand.isEmpty()) {
                        break;
                    }
                }
                if (!oldMainHand.isEmpty()) {
                    maid.spawnAtLocation(oldMainHand);
                }
            }
        }

        if (this.cutTimer > 0) {
            this.cutTimer--;
            return;
        }

        if (choppingBoard.onCutItem(world, maid, maid.getMainHandItem())) {
            maid.swing(InteractionHand.MAIN_HAND);
            this.cutTimer = 10;
            this.restTimer = 0;
            this.failCount = 0;
        } else {
            this.failCount++;
            if (this.failCount >= 3) {
                this.actionStage = 2;
                this.failCount = 0;
            }
        }
    }

    private void tryTakeOut(ServerLevel world, EntityMaid maid, IChoppingBoard choppingBoard) {
        if (choppingBoard.onTakeOut(world, maid)) {
            maid.swing(InteractionHand.MAIN_HAND);
            this.cutTimer = 0;
            this.restTimer = 0;
            this.failCount = 0;
            this.actionStage = 0;
        } else {
            this.failCount++;
            if (this.failCount >= 3) {
                this.actionStage = 0;
                this.failCount = 0;
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {

        double distSq = entityIn.distanceToSqr(
                this.standPos.getX() + 0.5,
                entityIn.getY(),
                this.standPos.getZ() + 0.5
        );

        if (this.hasReached) {
            return distSq < 9.0;
        }
        return distSq < 2.25;
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        super.stop(worldIn, entityIn, gameTimeIn);
        this.currentWorkPos = null;
        this.standPos = null;
        this.cutTimer = 0;
        this.restTimer = 0;
        this.actionStage = 0;
        this.failCount = 0;
        this.hasReached = false;
    }

    private BlockPos findChoppingBoard(ServerLevel world, EntityMaid maid) {
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
        return blockEntity instanceof IChoppingBoard;
    }
}