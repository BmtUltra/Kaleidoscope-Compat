package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.FluidSoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.util.BlockDrop;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.StockpotStatus.*;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock.HAS_LID;

/**
 * 汤锅在动态结构上的交互行为
 */
public class StockpotMovingInteraction extends BaseMovingInteraction {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof StockpotBlock)) {
            return false;
        }

        BlockState state = info.state();
        CompoundTag nbt = getOrCreateNbt(info);
        ItemStack itemInHand = player.getItemInHand(activeHand);

        if (itemInHand.isEmpty()) {
            return false;
        }

        int status = nbt.getInt(STATUS);
        boolean hasLid = state.getValue(HAS_LID);

        // 1. 放上/取下盖子
        if (handleLidInteraction(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
            return true;
        }

        // 2. 有盖子时，禁止其他交互
        if (hasLid) {
            return true;
        }

        // 3. 取出成品
        if (takeOutProduct(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
            return true;
        }

        // 4. 放入汤底
        if (status == PUT_SOUP_BASE) {
            if (addSoupBase(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
                return true;
            }
            return true;
        }

        // 6. 取出汤底
        if (status == PUT_INGREDIENT && isEmpty(nbt, contraptionEntity.level().registryAccess())) {
            if (removeSoupBase(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
                return true;
            }
        }

        // 7. 放入/取出食材
        if (status == PUT_INGREDIENT) {
            if ((itemInHand.isEmpty() || itemInHand.is(TagMod.INGREDIENT_CONTAINER)) &&
                removeIngredient(player, contraptionEntity, localPos, state, nbt, info)) {
                return true;
            }
            if (addIngredient(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
                return true;
            }
            return true;
        }

        // 8. 烹饪中/已完成
        return status == COOKING || status == FINISHED;
    }

    private boolean handleLidInteraction(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                          BlockState state, CompoundTag nbt, ItemStack stack, StructureBlockInfo info) {
        boolean hasLid = state.getValue(HAS_LID);

        if (!hasLid && stack.is(ModItems.STOCKPOT_LID.get())) {
            if (!contraptionEntity.level().isClientSide) {
                CompoundTag newNbt = nbt.copy();
                newNbt.put(STOCKPOT_LID_ITEM, stack.split(1).saveOptional(contraptionEntity.level().registryAccess()));

                BlockState newState = state.setValue(HAS_LID, true);
                StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), newState, newNbt);
                updateData(contraptionEntity, localPos, newInfo);

                playSound(contraptionEntity, localPos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.5F, 0.5F);
                ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.USE_LID_ON_STOCKPOT);
            }
            return true;
        }

        if (hasLid) {
            if (!contraptionEntity.level().isClientSide) {
                ItemStack lid = ItemStack.parseOptional(contraptionEntity.level().registryAccess(),
                        nbt.getCompound(STOCKPOT_LID_ITEM));
                if (lid.isEmpty()) {
                    lid = ModItems.STOCKPOT_LID.get().getDefaultInstance();
                }

                CompoundTag newNbt = nbt.copy();
                newNbt.remove(STOCKPOT_LID_ITEM);

                if (stack.isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, lid);
                } else {
                    Vec3 globalPos = getGlobalPos(contraptionEntity, localPos);
                    BlockDrop.popResource(contraptionEntity.level(), BlockPos.containing(globalPos), 0.5, lid);
                }

                BlockState newState = state.setValue(HAS_LID, false);
                StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), newState, newNbt);
                updateData(contraptionEntity, localPos, newInfo);

                playSound(contraptionEntity, localPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 0.5F, 0.5F);
            }
            return true;
        }

        return false;
    }

    private boolean addSoupBase(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                BlockState state, CompoundTag nbt, ItemStack stack, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != PUT_SOUP_BASE) return false;

        for (var entry : SoupBaseManager.getAllSoupBases().entrySet()) {
            ResourceLocation key = entry.getKey();
            ISoupBase soupBase = entry.getValue();
            if (soupBase.isSoupBase(stack)) {
                if (!contraptionEntity.level().isClientSide) {
                    CompoundTag newNbt = nbt.copy();
                    newNbt.putString(STOCKPOT_SOUP_BASE_ID, key.toString());
                    newNbt.putInt(STATUS, PUT_INGREDIENT);
                    updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));

                    ItemStack container = soupBase.getReturnContainer(contraptionEntity.level(), player, stack);
                    stack.shrink(1);
                    ItemUtils.getItemToLivingEntity(player, container);
                }
                ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.PUT_SOUP_BASE_IN_STOCKPOT);
                return true;
            }
        }
        return false;
    }

    private boolean removeSoupBase(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                   BlockState state, CompoundTag nbt, ItemStack stack, StructureBlockInfo info) {
        ResourceLocation soupBaseId = ResourceLocation.tryParse(nbt.getString(STOCKPOT_SOUP_BASE_ID));
        if (soupBaseId == null) return false;

        ISoupBase soupBase = SoupBaseManager.getSoupBase(soupBaseId);
        if (soupBase == null || !soupBase.isContainer(stack)) return false;

        if (!contraptionEntity.level().isClientSide) {
            CompoundTag newNbt = new CompoundTag();
            newNbt.putString(STOCKPOT_SOUP_BASE_ID, ModSoupBases.WATER.toString());
            newNbt.putInt(STATUS, PUT_SOUP_BASE);
            newNbt.put(INPUTS, ContainerHelper.saveAllItems(new CompoundTag(),
                    NonNullList.withSize(StockpotRecipe.RECIPES_SIZE, ItemStack.EMPTY),
                    contraptionEntity.level().registryAccess()));
            newNbt.put(RESULT, ItemStack.EMPTY.saveOptional(contraptionEntity.level().registryAccess()));
            newNbt.putInt(CURRENT_TICK, -1);

            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));

            ItemStack container = soupBase.getReturnSoupBase(contraptionEntity.level(), player, stack);
            stack.shrink(1);
            ItemUtils.getItemToLivingEntity(player, container);
        }
        return true;
    }

    private boolean addIngredient(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                  BlockState state, CompoundTag nbt, ItemStack itemStack, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != PUT_INGREDIENT) return false;
        if (itemStack.is(TagMod.INGREDIENT_BLOCKLIST)) return false;

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        NonNullList<ItemStack> inputs = ContraptionUtil.readInputs(nbt, registryAccess, StockpotRecipe.RECIPES_SIZE);

        for (int i = 0; i < inputs.size(); i++) {
            ItemStack item = inputs.get(i);
            if (item.isEmpty()) {
                Item containerItem = ItemUtils.getContainerItem(itemStack);
                if (containerItem != Items.AIR) {
                    ItemUtils.getItemToLivingEntity(player, containerItem.getDefaultInstance());
                }
                inputs.set(i, itemStack.split(1));

                if (!contraptionEntity.level().isClientSide) {
                    CompoundTag newNbt = nbt.copy();
                    ContraptionUtil.saveInputs(newNbt, inputs, registryAccess);
                    updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));
                }

                playSound(contraptionEntity, localPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                        ((contraptionEntity.level().random.nextFloat() - contraptionEntity.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                return true;
            }
        }
        return false;
    }

    private boolean removeIngredient(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                     BlockState state, CompoundTag nbt, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != PUT_INGREDIENT) return false;

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        NonNullList<ItemStack> inputs = ContraptionUtil.readInputs(nbt, registryAccess, StockpotRecipe.RECIPES_SIZE);

        for (int i = inputs.size() - 1; i >= 0; i--) {
            ItemStack stack = inputs.get(i);
            if (stack.isEmpty()) continue;

            if (!containerIsMatch(player, stack)) return false;
            inputs.set(i, ItemStack.EMPTY);

            if (!contraptionEntity.level().isClientSide) {
                ItemUtils.getItemToLivingEntity(player, stack.copy());

                CompoundTag newNbt = nbt.copy();
                ContraptionUtil.saveInputs(newNbt, inputs, registryAccess);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));
            }

            if (hasHeatSource(contraptionEntity, localPos)) {
                ResourceLocation soupBaseId = ResourceLocation.tryParse(nbt.getString(STOCKPOT_SOUP_BASE_ID));
                if (soupBaseId != null) {
                    ISoupBase soupBase = SoupBaseManager.getSoupBase(soupBaseId);
                    if (soupBase instanceof FluidSoupBase fluidSoupBase
                            && fluidSoupBase.getFluid().getFluidType().getTemperature() > 500) {
                        player.hurt(contraptionEntity.level().damageSources().inFire(), 1);
                        ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.HURT_WHEN_TAKEOUT_FROM_STOCKPOT);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean containerIsMatch(Player player, ItemStack stack) {
        Item containerItem = ItemUtils.getContainerItem(stack);
        if (containerItem == Items.AIR) return true;
        if (player.getMainHandItem().is(containerItem)) {
            player.getMainHandItem().shrink(1);
            return true;
        }
        sendActionBarMessage(player, "tip.kaleidoscope_cookery.kitchen.remove_ingredient.need_container",
                containerItem.getDefaultInstance().getHoverName());
        return false;
    }

    private boolean takeOutProduct(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                   BlockState state, CompoundTag nbt, ItemStack stack, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        int takeoutCount = nbt.getInt(STOCKPOT_TAKEOUT_COUNT);
        if (status != FINISHED || takeoutCount <= 0) return false;

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        ItemStack result = readResult(nbt, registryAccess);
        if (result.isEmpty()) return false;

        Ingredient carrier = readCarrier(nbt);

        if (!carrier.isEmpty() && !carrier.test(stack)) {
            Component carrierName = carrier.getItems()[0].getHoverName();
            sendActionBarMessage(player, "tip.kaleidoscope_cookery.pot.need_carrier", carrierName);
            return true;
        }

        if (!contraptionEntity.level().isClientSide) {
            if (!carrier.isEmpty()) {
                stack.shrink(1);
            }

            ItemStack resultCopy = result.copyWithCount(1);
            ItemUtils.getItemToLivingEntity(player, resultCopy);

            int newCount = takeoutCount - 1;
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(STOCKPOT_TAKEOUT_COUNT, newCount);

            if (newCount <= 0) {
                newNbt = new CompoundTag();
                newNbt.putInt(STATUS, PUT_SOUP_BASE);
                newNbt.put(INPUTS, ContainerHelper.saveAllItems(new CompoundTag(),
                        NonNullList.withSize(StockpotRecipe.RECIPES_SIZE, ItemStack.EMPTY), registryAccess));
                newNbt.putString(STOCKPOT_SOUP_BASE_ID, ModSoupBases.WATER.toString());
                newNbt.put(RESULT, ItemStack.EMPTY.saveOptional(registryAccess));
                newNbt.putInt(CURRENT_TICK, -1);
            }

            updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));

            playSound(contraptionEntity, localPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((contraptionEntity.level().random.nextFloat() - contraptionEntity.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
        return true;
    }

    private boolean isEmpty(CompoundTag nbt, RegistryAccess registryAccess) {
        return ContraptionUtil.areInputsEmpty(nbt, registryAccess, StockpotRecipe.RECIPES_SIZE);
    }

    private ItemStack readResult(CompoundTag nbt, RegistryAccess registryAccess) {
        return ContraptionUtil.readResult(nbt, registryAccess);
    }

    private Ingredient readCarrier(CompoundTag nbt) {
        return ContraptionUtil.readCarrier(nbt, STOCKPOT_CARRIER, Ingredient.of(Items.BOWL));
    }

}
