package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

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

        // 2. 尝试放入食材
        if (!itemInHand.isEmpty() && placeFood(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
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

    private boolean placeFood(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
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

        if (isAllEmpty && !hasLid && !isAboveSteamer && !contraptionEntity.level().isClientSide) {
            ItemUtils.getItemToLivingEntity(player, ModItems.STEAMER.get().getDefaultInstance(), player.getInventory().selected);

            playSound(contraptionEntity, localPos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

            if (half) {
                StructureBlockInfo airInfo = new StructureBlockInfo(info.pos(), Blocks.AIR.defaultBlockState(), null);
                updateData(contraptionEntity, localPos, airInfo);
                ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos);
            } else {
                CompoundTag newNbt = new CompoundTag();
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
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
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
