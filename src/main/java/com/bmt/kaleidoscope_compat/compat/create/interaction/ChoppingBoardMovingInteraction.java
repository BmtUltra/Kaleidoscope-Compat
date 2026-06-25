package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ChoppingBoardBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.ChoppingBoardRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;

/**
 * 菜板在动态结构上的交互行为
 */
public class ChoppingBoardMovingInteraction extends BaseMovingInteraction {

    private static final double DURABILITY_COST_PROBABILITY = 0.25;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (activeHand == InteractionHand.OFF_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof ChoppingBoardBlock)) {
            return false;
        }

        CompoundTag nbt = getOrCreateNbt(info);
        ItemStack itemInHand = player.getItemInHand(activeHand);
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();

        ItemStack currentCutStack = readCurrentCutStack(nbt, registryAccess);
        ItemStack resultItem = readResultItem(nbt, registryAccess);
        int currentCutCount = nbt.getInt(CHOPPING_BOARD_CURRENT_CUT_COUNT);
        int maxCutCount = nbt.getInt(CHOPPING_BOARD_MAX_CUT_COUNT);

        if (resultItem.isEmpty() && !itemInHand.isEmpty() && !itemInHand.is(TagMod.KITCHEN_KNIFE)) {
            return putItem(contraptionEntity, localPos, info, itemInHand, registryAccess);
        }

        if (itemInHand.is(TagMod.KITCHEN_KNIFE)) {
            return cutItem(player, contraptionEntity, localPos, info, itemInHand,
                    resultItem, currentCutCount, maxCutCount);
        }

        if (player.isSecondaryUseActive() && currentCutCount == 0 && !currentCutStack.isEmpty()) {
            return takeOutItem(player, contraptionEntity, localPos, info, currentCutStack);
        }

        return false;
    }

    /**
     * 放入食材到砧板
     */
    private boolean putItem(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                            StructureBlockInfo info, ItemStack itemInHand, RegistryAccess registryAccess) {
        SingleRecipeInput container = new SingleRecipeInput(itemInHand);
        Optional<RecipeHolder<ChoppingBoardRecipe>> recipeOptional = contraptionEntity.level().getRecipeManager()
                .getRecipeFor(ModRecipes.CHOPPING_BOARD_RECIPE, container, contraptionEntity.level());

        if (recipeOptional.isEmpty()) {
            return false;
        }

        ChoppingBoardRecipe recipe = recipeOptional.get().value();
        ItemStack cutStack = itemInHand.split(1);
        ItemStack result = recipe.assemble(container, registryAccess);

        if (!contraptionEntity.level().isClientSide()) {
            CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
            newNbt.putString(CHOPPING_BOARD_MODEL_ID, recipe.getModelId().toString());
            newNbt.putInt(CHOPPING_BOARD_MAX_CUT_COUNT, recipe.getCutCount());
            newNbt.putInt(CHOPPING_BOARD_CURRENT_CUT_COUNT, 0);
            newNbt.put(CHOPPING_BOARD_CURRENT_CUT_STACK, cutStack.saveOptional(registryAccess));
            newNbt.put(CHOPPING_BOARD_RESULT_ITEM, result.saveOptional(registryAccess));
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
        }

        playSound(contraptionEntity, localPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        return true;
    }

    /**
     * 使用刀具切菜
     */
    private boolean cutItem(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                         StructureBlockInfo info, ItemStack knife,
                         ItemStack resultItem,
                         int currentCutCount, int maxCutCount) {
        if (resultItem.isEmpty()) {
            if (!contraptionEntity.level().isClientSide()) {
                spawnCutParticles(contraptionEntity, localPos);
            }
            playSound(contraptionEntity, localPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F,
                    1.5F + contraptionEntity.level().random.nextFloat() * 0.4F);
        }

        if (currentCutCount >= maxCutCount) {
            if (!contraptionEntity.level().isClientSide()) {
                ContraptionUtil.giveItemToPlayer(player, resultItem.copy());

                CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                clearBoardData(newNbt);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
            }

            playSound(contraptionEntity, localPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F,
                    2.0F + contraptionEntity.level().random.nextFloat() * 0.2F);
        }

        if (!contraptionEntity.level().isClientSide()) {
            CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
            newNbt.putInt(CHOPPING_BOARD_CURRENT_CUT_COUNT, currentCutCount + 1);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
            spawnCutParticles(contraptionEntity, localPos);
        }

        playSound(contraptionEntity, localPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F,
                1.5F + contraptionEntity.level().random.nextFloat() * 0.4F);

        if (contraptionEntity.level().random.nextDouble() < DURABILITY_COST_PROBABILITY) {
            knife.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }

        return true;
    }

    /**
     * 取回未切制的食材
     */
    private boolean takeOutItem(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                StructureBlockInfo info, ItemStack currentCutStack) {
        if (!contraptionEntity.level().isClientSide()) {
            ContraptionUtil.giveItemToPlayer(player, currentCutStack);

            CompoundTag newNbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
            clearBoardData(newNbt);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), newNbt));
        }

        playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.2F);
        return true;
    }

    /**
     * 清空砧板数据
     */
    private void clearBoardData(CompoundTag nbt) {
        nbt.remove(CHOPPING_BOARD_MODEL_ID);
        nbt.remove(CHOPPING_BOARD_CURRENT_CUT_STACK);
        nbt.remove(CHOPPING_BOARD_RESULT_ITEM);
        nbt.putInt(CHOPPING_BOARD_MAX_CUT_COUNT, 0);
        nbt.putInt(CHOPPING_BOARD_CURRENT_CUT_COUNT, 0);
    }

    /**
     * 读取当前切制的物品
     */
    private ItemStack readCurrentCutStack(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(CHOPPING_BOARD_CURRENT_CUT_STACK)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(CHOPPING_BOARD_CURRENT_CUT_STACK));
        }
        return ItemStack.EMPTY;
    }

    /**
     * 读取切制结果
     */
    private ItemStack readResultItem(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(CHOPPING_BOARD_RESULT_ITEM)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(CHOPPING_BOARD_RESULT_ITEM));
        }
        return ItemStack.EMPTY;
    }

    /**
     * 生成粒子效果
     */
    private void spawnCutParticles(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        if (!(contraptionEntity.level() instanceof ServerLevel serverLevel)) return;

        Vec3 globalPos = ContraptionUtil.getGlobalPos(contraptionEntity, localPos);
        RandomSource random = serverLevel.getRandom();

        serverLevel.sendParticles(ParticleTypes.CRIT,
                globalPos.x - 0.25 + random.nextDouble() / 2,
                globalPos.y - 0.25,
                globalPos.z - 0.25 + random.nextDouble() / 2,
                2, 0, 0, 0, 0.1);
    }
}
