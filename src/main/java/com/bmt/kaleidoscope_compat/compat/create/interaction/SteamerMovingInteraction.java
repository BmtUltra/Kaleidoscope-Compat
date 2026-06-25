package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.bmt.kaleidoscope_compat.mixins.create.accessor.ContraptionAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.item.SteamerItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Map;
import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.STEAMER_COOKING_PROGRESS;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.STEAMER_COOKING_TIME;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock.HALF;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock.HAS_LID;

/**
 * 蒸笼在动态结构上的交互行为
 */
public class SteamerMovingInteraction extends BaseMovingInteraction {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof SteamerBlock)) {
            return false;
        }

        BlockState state = info.state();
        CompoundTag nbt = getOrCreateNbt(info);
        ItemStack itemInHand = player.getItemInHand(activeHand);

        // 1. 空手 Shift 右击：盖/取盖子
        if (handleLidInteraction(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
            return true;
        }

        // 2. 手持蒸笼堆叠
        if (itemInHand.getItem() instanceof SteamerItem) {
            return handleStackSteamer(player, contraptionEntity, localPos, state, nbt, itemInHand, info);
        }

        // 2.5. 空手右键取下蒸笼
        if (takeSteamer(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
            return true;
        }

        // 3. 尝试放入食材
        if (!itemInHand.isEmpty() && placeFood(contraptionEntity, localPos, state, nbt, itemInHand, info)) {
            return true;
        }

        // 3. 尝试取出食材
        if (takeFood(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
            return true;
        }

        return false;
    }

    private boolean handleLidInteraction(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                         BlockState state, CompoundTag nbt, ItemStack itemInHand, StructureBlockInfo info) {
        boolean hasLid = state.getValue(HAS_LID);

        if (itemInHand.isEmpty() && player.isSecondaryUseActive()) {
            if (isAboveSteamer(contraptionEntity, localPos)) {
                return false;
            }

            if (!contraptionEntity.level().isClientSide) {
                BlockState newState = state.setValue(HAS_LID, !hasLid);
                StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), newState, nbt);
                updateData(contraptionEntity, localPos, newInfo);
            }
            return true;
        }

        return false;
    }

    /**
     * 取下蒸笼
     */
    private boolean takeSteamer(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                BlockState state, CompoundTag nbt, ItemStack itemInHand, StructureBlockInfo info) {
        // 空手才能取下
        if (!itemInHand.isEmpty()) {
            return false;
        }

        // 必须是蒸笼最上方
        if (isAboveSteamer(contraptionEntity, localPos)) {
            return false;
        }

        // 有盖子不能取下
        if (state.getValue(HAS_LID)) {
            return false;
        }

        // 如果有原料，不执行取下，让 takeFood 处理
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        NonNullList<ItemStack> items = readItems(nbt, registryAccess);
        boolean hasItems = items.stream().anyMatch(stack -> !stack.isEmpty());
        if (hasItems) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            boolean half = state.getValue(HALF);

            // 给玩家一个蒸笼物品
            ItemStack drop = ModItems.STEAMER.get().getDefaultInstance();
            ContraptionUtil.giveItemToPlayer(player, drop);

            // 播放音效
            playSound(contraptionEntity, localPos,
                    state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

            if (half) {
                // 半格蒸笼：完全移除
                StructureBlockInfo airInfo = new StructureBlockInfo(info.pos(), Blocks.AIR.defaultBlockState(), null);
                updateData(contraptionEntity, localPos, airInfo);
                ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, true);
            } else {
                // 整格蒸笼：变为半格，NBT 清空
                CompoundTag newNbt = new CompoundTag();
                ContainerHelper.saveAllItems(newNbt, NonNullList.withSize(8, ItemStack.EMPTY), false, registryAccess);
                newNbt.putIntArray(STEAMER_COOKING_PROGRESS, new int[8]);
                newNbt.putIntArray(STEAMER_COOKING_TIME, new int[8]);

                BlockState newState = state.setValue(HALF, true);
                StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), newState, newNbt);
                updateData(contraptionEntity, localPos, newInfo);
            }
        }

        return true;
    }

    /**
     * 处理手持蒸笼右键堆叠
     */
    private boolean handleStackSteamer(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                       BlockState state, CompoundTag nbt, ItemStack itemInHand, StructureBlockInfo info) {
        if (!(itemInHand.getItem() instanceof SteamerItem)) {
            return false;
        }

        Contraption contraption = contraptionEntity.getContraption();
        Map<BlockPos, StructureBlockInfo> blocks = contraption.getBlocks();

        // 向上搜索直到找到可以放置的位置
        BlockPos placePos = localPos;
        BlockState blockState = state;
        while (blockState.is(ModBlocks.STEAMER.get()) && !blockState.getValue(HAS_LID) && !blockState.getValue(HALF)) {
            placePos = placePos.above();
            StructureBlockInfo aboveInfo = blocks.get(placePos);
            blockState = aboveInfo != null ? aboveInfo.state() : Blocks.AIR.defaultBlockState();
        }

        // 判断是否可以放置：空气格或单层未加盖蒸笼
        boolean canPlaceInAir = blockState.isAir();
        boolean canReplaceHalf = blockState.is(ModBlocks.STEAMER.get())
                && blockState.getValue(HALF)
                && !blockState.getValue(HAS_LID);

        if (!canPlaceInAir && !canReplaceHalf) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
            BlockState newState;
            CompoundTag newNbt;

            if (canReplaceHalf) {
                // 情况1：替换单层蒸笼为完整蒸笼
                StructureBlockInfo placeInfo = blocks.get(placePos);
                CompoundTag placeNbt = getOrCreateNbt(placeInfo);

                newState = placeInfo.state().setValue(HALF, false);
                newNbt = placeNbt;

                StructureBlockInfo newInfo = new StructureBlockInfo(placePos, newState, newNbt);
                contraption.getBlocks().put(placePos, newInfo);
                ((ContraptionAccessor) contraption).getUpdateTags().put(placePos, newNbt.copy());

                for (var actor : contraption.getActors()) {
                    if (actor.getLeft().pos().equals(placePos)) {
                        actor.setLeft(newInfo);
                        break;
                    }
                }

                AABB updatedBounds = ContraptionUtil.recalculateBounds(contraption);
                ContraptionUtil.syncBlockChange(contraptionEntity, placePos, newState, newNbt, updatedBounds);
            } else {
                // 情况2：在空位放置新蒸笼
                newState = ModBlocks.STEAMER.get().defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING))
                        .setValue(HALF, true)
                        .setValue(HAS_LID, false)
                        .setValue(SteamerBlock.HAS_BASE, false)
                        .setValue(SteamerBlock.WATERLOGGED, false);

                newNbt = new CompoundTag();
                ContainerHelper.saveAllItems(newNbt, NonNullList.withSize(8, ItemStack.EMPTY), false, registryAccess);
                newNbt.putIntArray(STEAMER_COOKING_PROGRESS, new int[8]);
                newNbt.putIntArray(STEAMER_COOKING_TIME, new int[8]);

                StructureBlockInfo newInfo = new StructureBlockInfo(placePos, newState, newNbt);
                contraption.getBlocks().put(placePos, newInfo);
                ((ContraptionAccessor) contraption).getUpdateTags().put(placePos, newNbt.copy());

                // 注册交互行为
                MovingInteractionBehaviour interactionBehaviour = MovingInteractionBehaviour.REGISTRY.get(newState);
                if (interactionBehaviour != null) {
                    contraption.getInteractors().put(placePos, interactionBehaviour);
                }

                // 注册 MovementBehaviour
                MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(newState);
                if (movementBehaviour != null) {
                    final BlockPos fp = placePos;
                    var actors = contraption.getActors();
                    boolean exists = actors.stream().anyMatch(actor -> actor.getLeft().pos().equals(fp));
                    if (!exists) {
                        MovementContext context = new MovementContext(contraptionEntity.level(), newInfo, contraption);
                        actors.add(MutablePair.of(newInfo, context));
                    }
                }

                for (var actor : contraption.getActors()) {
                    if (actor.getLeft().pos().equals(placePos)) {
                        actor.setLeft(newInfo);
                        break;
                    }
                }

                AABB updatedBounds = ContraptionUtil.recalculateBounds(contraption);
                ContraptionUtil.syncBlockChange(contraptionEntity, placePos, newState, newNbt, updatedBounds);
            }

            contraption.invalidateColliders();

            if (!placePos.equals(localPos)) {
                BlockState currentState = state.setValue(HAS_LID, false);
                CompoundTag currentNbt = nbt.copy();
                StructureBlockInfo currentInfo = new StructureBlockInfo(localPos, currentState, currentNbt);
                updateData(contraptionEntity, localPos, currentInfo);
            }

            // 消耗物品
            if (!player.isCreative()) {
                itemInHand.shrink(1);
            }

            // 播放音效
            playSound(contraptionEntity, placePos,
                    ModBlocks.STEAMER.get().defaultBlockState().getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, 1.0F, 0.8F);
        }

        return true;
    }

    private boolean placeFood(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                              BlockState state, CompoundTag nbt, ItemStack food, StructureBlockInfo info) {
        if (isAboveBlocking(contraptionEntity, localPos)) {
            return false;
        }

        if (contraptionEntity.level().isClientSide) {
            if (food.isEmpty()) return false;
            RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
            NonNullList<ItemStack> items = readItems(nbt, registryAccess);
            boolean half = state.getValue(HALF);
            int endIndex = half ? 4 : 8;
            return hasEmptySlot(items, endIndex);
        }

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        NonNullList<ItemStack> items = readItems(nbt, registryAccess);
        int[] cookingProgress = readIntArray(nbt, STEAMER_COOKING_PROGRESS);
        int[] cookingTime = readIntArray(nbt, STEAMER_COOKING_TIME);
        boolean half = state.getValue(HALF);
        int endIndex = half ? 4 : 8;
        boolean added = false;

        if (!hasEmptySlot(items, endIndex)) {
            return false;
        }

        Optional<RecipeHolder<SteamerRecipe>> recipe = getSteamerRecipe(contraptionEntity, food);
        if (recipe.isEmpty()) {
            return false;
        }

        int cookTime = recipe.get().value().getCookTick();
        if (cookTime <= 0) {
            return false;
        }

        for (int i = 0; i < endIndex && !food.isEmpty(); i++) {
            ItemStack itemstack = items.get(i);
            if (itemstack.isEmpty()) {
                cookingTime[i] = cookTime;
                cookingProgress[i] = 0;
                items.set(i, food.split(1));
                added = true;
            }
        }

        if (added) {
            CompoundTag newNbt = nbt.copy();
            saveItems(newNbt, items, registryAccess);
            saveIntArray(newNbt, STEAMER_COOKING_PROGRESS, cookingProgress);
            saveIntArray(newNbt, STEAMER_COOKING_TIME, cookingTime);

            StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), state, newNbt);
            updateData(contraptionEntity, localPos, newInfo);
        }

        return added;
    }

    private boolean takeFood(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                             BlockState state, CompoundTag nbt, ItemStack itemInHand, StructureBlockInfo info) {
        if (isAboveBlocking(contraptionEntity, localPos)) {
            return false;
        }

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        NonNullList<ItemStack> items = readItems(nbt, registryAccess);
        int[] cookingProgress = readIntArray(nbt, STEAMER_COOKING_PROGRESS);
        int[] cookingTime = readIntArray(nbt, STEAMER_COOKING_TIME);
        boolean half = state.getValue(HALF);
        int endIndex = half ? 4 : 8;
        boolean isAllEmpty = true;

        for (int i = 0; i < endIndex; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            isAllEmpty = false;
            if (!contraptionEntity.level().isClientSide) {
                ItemUtils.getItemToLivingEntity(player, stack, player.getInventory().selected);
                items.set(i, ItemStack.EMPTY);
                cookingTime[i] = 0;
                cookingProgress[i] = 0;
            }
        }

        boolean hasLid = state.getValue(HAS_LID);

        boolean isAboveSteamer = isAboveSteamer(contraptionEntity, localPos);

        // 空手时取完食物后才取下蒸笼，手持物品时只取食物不取蒸笼
        if (itemInHand.isEmpty() && isAllEmpty && !hasLid && !isAboveSteamer && !contraptionEntity.level().isClientSide) {
            ItemUtils.getItemToLivingEntity(player, ModItems.STEAMER.get().getDefaultInstance(), player.getInventory().selected);

            playSound(contraptionEntity, localPos, state.getSoundType(contraptionEntity.level(), localPos, null).getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

            if (half) {
                StructureBlockInfo airInfo = new StructureBlockInfo(info.pos(), Blocks.AIR.defaultBlockState(), null);
                updateData(contraptionEntity, localPos, airInfo);
                ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, false);
            } else {
                // 整格蒸笼：变为半格，NBT 清空
                CompoundTag newNbt = new CompoundTag();
                ContainerHelper.saveAllItems(newNbt, NonNullList.withSize(8, ItemStack.EMPTY), false, registryAccess);
                newNbt.putIntArray(STEAMER_COOKING_PROGRESS, new int[8]);
                newNbt.putIntArray(STEAMER_COOKING_TIME, new int[8]);

                BlockState newState = state.setValue(HALF, true);
                StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), newState, newNbt);
                updateData(contraptionEntity, localPos, newInfo);
            }
            return true;
        }

        if (!contraptionEntity.level().isClientSide && !isAllEmpty) {
            CompoundTag newNbt = nbt.copy();
            saveItems(newNbt, items, registryAccess);
            saveIntArray(newNbt, STEAMER_COOKING_PROGRESS, cookingProgress);
            saveIntArray(newNbt, STEAMER_COOKING_TIME, cookingTime);

            StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), state, newNbt);
            updateData(contraptionEntity, localPos, newInfo);
        }

        return !isAllEmpty;
    }

    private boolean hasEmptySlot(NonNullList<ItemStack> items, int endIndex) {
        for (int i = 0; i < endIndex; i++) {
            if (items.get(i).isEmpty()) return true;
        }
        return false;
    }

    private Optional<RecipeHolder<SteamerRecipe>> getSteamerRecipe(AbstractContraptionEntity contraptionEntity, ItemStack stack) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return contraptionEntity.level().getRecipeManager().getRecipeFor(ModRecipes.STEAMER_RECIPE, input, contraptionEntity.level());
    }

    private boolean isAboveSteamer(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Map<BlockPos, StructureBlockInfo> blocks = contraptionEntity.getContraption().getBlocks();
        StructureBlockInfo aboveInfo = blocks.get(localPos.above());
        return aboveInfo != null && aboveInfo.state().is(ModBlocks.STEAMER.get());
    }

    /**
     * 检查上方是否有方块阻拦
     */
    private boolean isAboveBlocking(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Map<BlockPos, StructureBlockInfo> blocks = contraptionEntity.getContraption().getBlocks();
        StructureBlockInfo aboveInfo = blocks.get(localPos.above());
        if (aboveInfo != null) {
            return aboveInfo.state().isFaceSturdy(contraptionEntity.level(), localPos.above(), Direction.DOWN);
        }
        return false;
    }

    private NonNullList<ItemStack> readItems(CompoundTag nbt, RegistryAccess registryAccess) {
        NonNullList<ItemStack> items = NonNullList.withSize(8, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, items, registryAccess);
        return items;
    }

    private void saveItems(CompoundTag nbt, NonNullList<ItemStack> items, RegistryAccess registryAccess) {
        ContainerHelper.saveAllItems(nbt, items, true, registryAccess);
    }

    private int[] readIntArray(CompoundTag nbt, String key) {
        if (nbt.contains(key, Tag.TAG_INT_ARRAY)) {
            return nbt.getIntArray(key);
        }
        return new int[8];
    }

    private void saveIntArray(CompoundTag nbt, String key, int[] array) {
        nbt.putIntArray(key, array);
    }
}
