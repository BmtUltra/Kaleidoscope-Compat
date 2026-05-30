package com.bmt.kaleidoscope_compat.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IPressingTub;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class MaidPressingTubBehavior extends MaidCheckRateTask {
    private final float movementSpeed;
    private BlockPos currentWorkPos = null;
    private int jumpTimer = 0;
    private int actionStage = 0;
    private int failCount = 0;
    private List<PressingTubRecipe> cachedRecipes = null;
    private PressingTubRecipe currentRecipe = null;

    public MaidPressingTubBehavior(float movementSpeed) {
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
            BlockPos tubPos = this.findTub(worldIn, owner);
            if (tubPos == null) {
                return false;
            }
            this.currentWorkPos = tubPos;
            this.cachedRecipes = null;
            this.currentRecipe = null;
            double distSq = tubPos.distToCenterSqr(owner.position());
            if (distSq < 1.44 && Math.abs(owner.getY() - (tubPos.getY() + 0.5)) < 1.5) {
                owner.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                return true;
            }
            BehaviorUtils.setWalkAndLookTargetMemories(owner, tubPos, this.movementSpeed, 0);
            return true;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        this.jumpTimer = 0;
        this.actionStage = 0;
        this.failCount = 0;
        this.cachedRecipes = null;
        this.currentRecipe = null;
        maid.getNavigation().stop();
    }

    @Override
    protected void tick(ServerLevel world, EntityMaid maid, long gameTime) {
        if (this.currentWorkPos == null) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(this.currentWorkPos);
        if (!(blockEntity instanceof IPressingTub tub)) {
            return;
        }

        if (this.cachedRecipes == null) {
            this.cachedRecipes = this.getRecipes(world);
        }

        switch (this.actionStage) {
            case 0 -> this.tryPutItem(world, maid, tub);
            case 1 -> this.tryPress(world, maid, tub);
            case 2 -> this.tryExtractWithBucket(world, maid, tub);
        }
    }

    private void tryPutItem(ServerLevel world, EntityMaid maid, IPressingTub tub) {
        if (tub.getFluidAmount() >= IPressingTub.MAX_FLUID_AMOUNT) {
            this.actionStage = 2;
            this.failCount = 0;
            return;
        }

        if (!tub.getItems().getStackInSlot(0).isEmpty()) {
            this.actionStage = 1;
            this.failCount = 0;
            return;
        }

        IItemHandler inventory = maid.getCapability(Capabilities.ItemHandler.ENTITY);
        if (inventory == null) {
            return;
        }

        FluidStack fluidInTub = tub.getFluid().getFluid();

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(Items.BUCKET)) {
                continue;
            }

            PressingTubRecipe matchedRecipe = this.findRecipeForItem(world, stack, fluidInTub);
            if (matchedRecipe == null) {
                continue;
            }

            ItemStack toPut = stack.copyWithCount(1);
            if (tub.addIngredient(toPut)) {
                inventory.extractItem(slot, 1, false);
                this.currentRecipe = matchedRecipe;
                maid.swing(InteractionHand.MAIN_HAND);
                this.jumpTimer = 0;
                this.failCount = 0;
                this.actionStage = 1;
                return;
            }
        }

        this.failCount++;
        if (this.failCount >= 3) {
            if (tub.getFluidAmount() > 0 && tub.getFluidAmount() < IPressingTub.MAX_FLUID_AMOUNT) {
                for (int slot = 0; slot < inventory.getSlots(); slot++) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    if (stack.is(Items.BUCKET)) continue;

                    PressingTubRecipe recipe = this.findRecipeForItem(world, stack, fluidInTub);
                    if (recipe != null) {
                        this.failCount = 0;
                        return;
                    }
                }
            }
            this.failCount = 0;
        }
    }

    private void tryPress(ServerLevel world, EntityMaid maid, IPressingTub tub) {
        if (tub.getFluidAmount() >= IPressingTub.MAX_FLUID_AMOUNT) {
            this.actionStage = 2;
            this.failCount = 0;
            return;
        }

        if (tub.getItems().getStackInSlot(0).isEmpty()) {
            this.actionStage = 0;
            this.failCount = 0;
            return;
        }

        double targetX = this.currentWorkPos.getX() + 0.5;
        double targetZ = this.currentWorkPos.getZ() + 0.5;
        double dx = targetX - maid.getX();
        double dz = targetZ - maid.getZ();
        double distSq = dx * dx + dz * dz;

        if (distSq > 0.01) {
            double factor = distSq > 0.25 ? 0.1 : 0.05;
            maid.setDeltaMovement(dx * factor, maid.getDeltaMovement().y, dz * factor);
        } else {
            maid.setDeltaMovement(0, maid.getDeltaMovement().y, 0);
        }

        if (this.jumpTimer > 0) {
            this.jumpTimer--;
        } else {
            if (maid.onGround() || Math.abs(maid.getDeltaMovement().y) < 0.05) {
                maid.jumpFromGround();
                maid.swing(InteractionHand.MAIN_HAND);
                this.jumpTimer = 20;
            }
        }
    }

    private void tryExtractWithBucket(ServerLevel world, EntityMaid maid, IPressingTub tub) {
        if (tub.getFluidAmount() < IPressingTub.MAX_FLUID_AMOUNT) {
            if (!tub.getItems().getStackInSlot(0).isEmpty()) {
                this.actionStage = 1;
                this.failCount = 0;
                return;
            }
            this.actionStage = 0;
            this.failCount = 0;
            return;
        }

        IItemHandler inventory = maid.getCapability(Capabilities.ItemHandler.ENTITY);
        if (inventory == null) {
            return;
        }

        int bucketSlot = -1;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(Items.BUCKET)) {
                bucketSlot = slot;
                break;
            }
        }

        if (bucketSlot == -1) {
            if (this.jumpTimer < 60) {
                this.jumpTimer++;
            } else {
                this.jumpTimer = 0;
                this.failCount++;
                if (this.failCount >= 3) {
                    this.actionStage = 0;
                    this.failCount = 0;
                }
            }
            return;
        }

        boolean hasSpace = false;
        for (int slot = 6; slot < inventory.getSlots(); slot++) {
            if (slot != bucketSlot) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    hasSpace = true;
                    break;
                }
            }
        }

        if (!hasSpace) {
            if (this.jumpTimer < 60) {
                this.jumpTimer++;
            } else {
                this.jumpTimer = 0;
                this.failCount++;
                if (this.failCount >= 3) {
                    this.actionStage = 0;
                    this.failCount = 0;
                }
            }
            return;
        }

        ItemStack filledBucketTemplate = new ItemStack(Items.BUCKET);
        boolean success = tub.getResult(maid, filledBucketTemplate);
        if (success) {
            inventory.extractItem(bucketSlot, 1, false);
            inventory.insertItem(bucketSlot, filledBucketTemplate, false);
            maid.swing(InteractionHand.MAIN_HAND);
            this.jumpTimer = 0;
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
        if (this.currentWorkPos == null) {
            return false;
        }
        double distSq = entityIn.distanceToSqr(
                this.currentWorkPos.getX() + 0.5,
                entityIn.getY(),
                this.currentWorkPos.getZ() + 0.5
        );
        if (distSq >= 2.25) {
            return false;
        }
        return this.isWorkable(worldIn, this.currentWorkPos);
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        super.stop(worldIn, entityIn, gameTimeIn);
        this.currentWorkPos = null;
        this.jumpTimer = 0;
        this.actionStage = 0;
        this.failCount = 0;
        this.cachedRecipes = null;
        this.currentRecipe = null;
    }

    private List<PressingTubRecipe> getRecipes(ServerLevel world) {
        List<PressingTubRecipe> recipes = new ArrayList<>();
        world.getRecipeManager().getAllRecipesFor(ModRecipes.PRESSING_TUB_RECIPE)
                .forEach(holder -> recipes.add((PressingTubRecipe) holder.value()));
        return recipes;
    }

    private PressingTubRecipe findRecipeForItem(ServerLevel world, ItemStack stack, FluidStack fluidInTub) {
        if (this.cachedRecipes == null) {
            this.cachedRecipes = this.getRecipes(world);
        }

        for (PressingTubRecipe recipe : this.cachedRecipes) {
            if (recipe.getIngredient().test(stack)) {
                if (!fluidInTub.isEmpty()) {
                    if (recipe.getFluid() == fluidInTub.getFluid()) {
                        return recipe;
                    }
                } else {
                    return recipe;
                }
            }
        }
        return null;
    }

    private BlockPos findTub(ServerLevel world, EntityMaid maid) {
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
        if (!(blockEntity instanceof IPressingTub tub)) {
            return false;
        }
        if (!tub.getItems().getStackInSlot(0).isEmpty()) {
            return true;
        }
        if (tub.getFluidAmount() < IPressingTub.MAX_FLUID_AMOUNT) {
            return true;
        }
        return true;
    }
}