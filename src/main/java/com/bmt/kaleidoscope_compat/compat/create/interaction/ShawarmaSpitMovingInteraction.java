package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ShawarmaSpitBlock;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;

/**
 * 烤肉架在动态结构上的交互行为
 */
public class ShawarmaSpitMovingInteraction extends BaseMovingInteraction {

    private static final int MAX_ITEMS = 8;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (player.isSecondaryUseActive()) {
            return false;
        }
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof ShawarmaSpitBlock)) {
            return false;
        }

        BlockState state = info.state();

        CompoundTag nbt = getOrCreateNbt(info);
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();

        ItemStack cookingItem = readCookingItem(nbt, registryAccess);
        ItemStack cookedItem = readCookedItem(nbt, registryAccess);
        int cookTime = nbt.getInt(SHAWARMA_SPIT_COOK_TIME);

        ItemStack mainHandItem = player.getMainHandItem();

        // 1. 尝试放入食材
        if (cookingItem.isEmpty() && cookedItem.isEmpty() && !mainHandItem.isEmpty()) {
            return putCookingItem(player, contraptionEntity, localPos, info, mainHandItem, registryAccess);
        }

        // 2. 取出物品
        return takeItem(player, contraptionEntity, localPos, info, state, cookingItem, cookedItem, cookTime, registryAccess);
    }

    /**
     * 放入食材进行烹饪
     */
    private boolean putCookingItem(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                   StructureBlockInfo info, ItemStack itemInHand, RegistryAccess registryAccess) {
        // 查找营火烹饪配方
        SingleRecipeInput input = new SingleRecipeInput(itemInHand);
        Optional<RecipeHolder<CampfireCookingRecipe>> recipeOpt = contraptionEntity.level().getRecipeManager()
                .getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, contraptionEntity.level());

        if (!recipeOpt.isPresent()) {
            return false;
        }

        CampfireCookingRecipe recipe = recipeOpt.get().value();
        ItemStack cookingStack = itemInHand.split(MAX_ITEMS);
        ItemStack cookedStack = recipe.assemble(input, registryAccess);
        cookedStack.setCount(cookingStack.getCount());
        int cookTime = recipe.getCookingTime();

        if (!contraptionEntity.level().isClientSide()) {
            CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
            newNbt.put(SHAWARMA_SPIT_COOKING_ITEM, cookingStack.saveOptional(registryAccess));
            newNbt.put(SHAWARMA_SPIT_COOKED_ITEM, cookedStack.saveOptional(registryAccess));
            newNbt.putInt(SHAWARMA_SPIT_COOK_TIME, cookTime);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
        }

        playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS,
                0.5F + contraptionEntity.level().random.nextFloat(),
                0.6F + contraptionEntity.level().random.nextFloat() * 0.7F);
        return true;
    }

    /**
     * 取出物品
     */
    private boolean takeItem(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                             StructureBlockInfo info, BlockState state,
                             ItemStack cookingItem, ItemStack cookedItem, int cookTime,
                             RegistryAccess registryAccess) {
        ItemStack toGive = ItemStack.EMPTY;

        // 烹饪完成，取出成品
        if (cookTime <= 0 && !cookedItem.isEmpty()) {
            toGive = cookedItem.copy();
        }
        // 还在烹饪中，取出原料
        else if (cookTime > 0 && !cookingItem.isEmpty()) {
            toGive = cookingItem.copy();
        }

        if (toGive.isEmpty()) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide()) {
            ContraptionUtil.giveItemToPlayer(player, toGive);

            CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
            clearCookingData(newNbt, registryAccess);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
        }

        // 烫伤判断
        if (state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED)) {
            player.hurt(contraptionEntity.level().damageSources().inFire(), 1);
        }

        playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS,
                0.5F + contraptionEntity.level().random.nextFloat(),
                0.6F + contraptionEntity.level().random.nextFloat() * 0.7F);
        return true;
    }

    /**
     * 清空烹饪数据
     */
    private void clearCookingData(CompoundTag nbt, RegistryAccess registryAccess) {
        nbt.put(SHAWARMA_SPIT_COOKING_ITEM, ItemStack.EMPTY.saveOptional(registryAccess));
        nbt.put(SHAWARMA_SPIT_COOKED_ITEM, ItemStack.EMPTY.saveOptional(registryAccess));
        nbt.putInt(SHAWARMA_SPIT_COOK_TIME, 0);
    }

    private ItemStack readCookingItem(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(SHAWARMA_SPIT_COOKING_ITEM)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(SHAWARMA_SPIT_COOKING_ITEM));
        }
        return ItemStack.EMPTY;
    }

    private ItemStack readCookedItem(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(SHAWARMA_SPIT_COOKED_ITEM)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(SHAWARMA_SPIT_COOKED_ITEM));
        }
        return ItemStack.EMPTY;
    }
}
