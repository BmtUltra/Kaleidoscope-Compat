package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.FlexPotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.Quality;
import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.QualityEvaluator;
import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.QualityUtils;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.PotStatus.*;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.HAS_OIL;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.SHOW_OIL;
import static com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteRegistry.*;

/**
 * 炒锅在动态结构上的交互行为
 */
public class PotMovingInteraction extends BaseMovingInteraction {

    private static final int PUT_INGREDIENT_TIME = 60 * 20;
    private static final int TAKEOUT_TIME = 40 * 20;
    private static final int BURNT_TIME = 20 * 20;
    private static final double DURABILITY_COST_PROBABILITY = 0.25;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof PotBlock)) {
            return false;
        }

        BlockState state = info.state();
        ItemStack itemInHand = player.getItemInHand(activeHand);
        CompoundTag nbt = getOrCreateNbt(info);
        int status = nbt.getInt(STATUS);

        // 1. 取出配菜（空手或 INGREDIENT_CONTAINER）
        if ((itemInHand.isEmpty() || itemInHand.is(TagMod.INGREDIENT_CONTAINER)) &&
            removeIngredient(player, contraptionEntity, localPos, state, nbt, info)) {
            return true;
        }

        // 2. 取出成品
        if (takeOutProduct(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
            return true;
        }

        // 3. 检查热源
        if (!hasHeatSource(contraptionEntity, localPos)) {
            sendActionBar(player, "tip.kaleidoscope_cookery.pot.need_lit_stove");
            return true;
        }

        // 4. 检查油
        if (!state.getValue(HAS_OIL)) {
            if (onPlaceOil(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
                return true;
            } else {
                sendActionBar(player, "tip.kaleidoscope_cookery.pot.need_oil");
                return true;
            }
        }

        // 4.5 锅铲翻炒
        if (itemInHand.is(TagMod.KITCHEN_SHOVEL)) {
            if (contraptionEntity.level().random.nextDouble() < DURABILITY_COST_PROBABILITY) {
                itemInHand.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
            onShovelHit(player, contraptionEntity, localPos, state, nbt, info);
            playSound(contraptionEntity, localPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    1F + (contraptionEntity.level().random.nextFloat() - contraptionEntity.level().random.nextFloat()) * 0.8F);
            return true;
        }

        // 4.6-4.9 状态处理
        if (status == PUT_INGREDIENT) {
            if (addIngredient(player, contraptionEntity, localPos, state, nbt, itemInHand, info)) {
                return true;
            }
            return true;
        }
        if (status == COOKING || status == FINISHED || status == BURNT) {
            return true;
        }

        // 5. 放入食材
        return addIngredient(player, contraptionEntity, localPos, state, nbt, itemInHand, info);
    }

    private boolean onPlaceOil(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                               BlockState state, CompoundTag nbt, ItemStack stack, StructureBlockInfo info) {
        if (stack.is(TagMod.OIL)) {
            placeOil(contraptionEntity, localPos, state, nbt, player, info);
            if (!player.isCreative()) stack.shrink(1);
            ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.PUT_OIL_IN_POT);
            return true;
        } else if (stack.is(ModItems.KITCHEN_SHOVEL.get()) && KitchenShovelItem.hasOil(stack)) {
            placeOil(contraptionEntity, localPos, state, nbt, player, info);
            KitchenShovelItem.setHasOil(stack, false);
            ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.PUT_OIL_IN_POT);
            return true;
        } else if (stack.is(ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack)) {
            placeOil(contraptionEntity, localPos, state, nbt, player, info);
            OilPotItem.shrinkOilCount(stack);
            ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.PUT_OIL_IN_POT);
            return true;
        }
        return false;
    }

    private void placeOil(AbstractContraptionEntity contraptionEntity, BlockPos localPos, BlockState state,
                          CompoundTag nbt, Player player, StructureBlockInfo info) {
        if (contraptionEntity.level().isClientSide) return;

        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(CURRENT_TICK, PUT_INGREDIENT_TIME);
        newNbt.putInt(STATUS, PUT_INGREDIENT);

        BlockState newState = state.setValue(HAS_OIL, true).setValue(SHOW_OIL, true);
        StructureBlockInfo newInfo = new StructureBlockInfo(info.pos(), newState, newNbt);
        updateData(contraptionEntity, localPos, newInfo);

        playSound(contraptionEntity, localPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1F,
                (contraptionEntity.level().random.nextFloat() - contraptionEntity.level().random.nextFloat()) * 0.8F);
    }

    private boolean addIngredient(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                  BlockState state, CompoundTag nbt, ItemStack itemStack, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != PUT_INGREDIENT) return false;
        if (itemStack.is(TagMod.INGREDIENT_BLOCKLIST)) return false;

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        NonNullList<ItemStack> inputs = readInputs(nbt, registryAccess);

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
                    saveInputs(newNbt, inputs, registryAccess);
                    updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));
                }

                playSound(contraptionEntity, localPos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0F, 0.5F);
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
        NonNullList<ItemStack> inputs = readInputs(nbt, registryAccess);

        for (int i = inputs.size() - 1; i >= 0; i--) {
            ItemStack stack = inputs.get(i);
            if (stack.isEmpty()) continue;

            if (!containerIsMatch(player, stack)) return false;
            inputs.set(i, ItemStack.EMPTY);

            if (!contraptionEntity.level().isClientSide) {
                ItemUtils.getItemToLivingEntity(player, stack);

                CompoundTag newNbt = nbt.copy();
                saveInputs(newNbt, inputs, registryAccess);
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));
            }

            if (hasHeatSource(contraptionEntity, localPos)) {
                player.hurt(contraptionEntity.level().damageSources().inFire(), 1);
                ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.HURT_WHEN_TAKEOUT_FROM_POT);
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
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "tip.kaleidoscope_cookery.kitchen.remove_ingredient.need_container",
                    containerItem.getDefaultInstance().getHoverName()));
        }
        return false;
    }

    private void onShovelHit(Player user, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                             BlockState state, CompoundTag nbt, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);

        if (!contraptionEntity.level().isClientSide) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putLong(SEED, System.currentTimeMillis());

            if (status == PUT_INGREDIENT && !isEmpty(newNbt, contraptionEntity.level().registryAccess())) {
                startCooking(contraptionEntity, localPos, state, newNbt, info);
                ModTrigger.EVENT.get().trigger(user, ModEventTriggerType.STIR_FRY_IN_POT);
            }

            if (status == COOKING) {
                int stirFryCount = newNbt.getInt(POT_STIR_FRY_COUNT);
                if (stirFryCount > 0) {
                    newNbt.putInt(POT_STIR_FRY_COUNT, stirFryCount - 1);
                }
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));
                ModTrigger.EVENT.get().trigger(user, ModEventTriggerType.STIR_FRY_IN_POT);
            }

            if (status == FINISHED || status == BURNT) {
                updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));
            }
        }
    }

    private void startCooking(AbstractContraptionEntity contraptionEntity, BlockPos localPos, BlockState state,
                              CompoundTag nbt, StructureBlockInfo info) {
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        NonNullList<ItemStack> inputs = readInputs(nbt, registryAccess);
        SimpleInput simpleInput = new SimpleInput(inputs);

        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(STATUS, COOKING);

        var potRecipeOpt = contraptionEntity.level().getRecipeManager().getRecipeFor(ModRecipes.POT_RECIPE, simpleInput, contraptionEntity.level());
        if (potRecipeOpt.isPresent()) {
            RecipeHolder<PotRecipe> holder = potRecipeOpt.get();
            applyPotRecipe(contraptionEntity, localPos, state, newNbt, simpleInput, holder.value(), holder.id());
            return;
        }

        var flexRecipeOpt = contraptionEntity.level().getRecipeManager().getRecipeFor(ModRecipes.FLEX_POT_RECIPE, simpleInput, contraptionEntity.level());
        if (flexRecipeOpt.isPresent()) {
            RecipeHolder<FlexPotRecipe> holder = flexRecipeOpt.get();
            applyFlexRecipe(contraptionEntity, localPos, state, newNbt, simpleInput, holder.value(), inputs, holder.id());
            return;
        }

        applySuspiciousRecipe(contraptionEntity, localPos, state, newNbt, registryAccess, info);
    }

    private void applyPotRecipe(AbstractContraptionEntity contraptionEntity, BlockPos localPos, BlockState state,
                                CompoundTag newNbt, SimpleInput input, PotRecipe recipe, ResourceLocation recipeId) {
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        saveCarrier(newNbt, recipe.carrier(), registryAccess);
        newNbt.put(RESULT, recipe.assemble(input, registryAccess).saveOptional(registryAccess));
        newNbt.putInt(CURRENT_TICK, recipe.time());
        newNbt.putInt(POT_STIR_FRY_COUNT, recipe.stirFryCount());
        updateData(contraptionEntity, localPos, new StructureBlockInfo(localPos, state, newNbt));
    }

    private void applyFlexRecipe(AbstractContraptionEntity contraptionEntity, BlockPos localPos, BlockState state,
                                 CompoundTag newNbt, SimpleInput input, FlexPotRecipe recipe, NonNullList<ItemStack> inputs, ResourceLocation recipeId) {
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        saveCarrier(newNbt, recipe.carrier(), registryAccess);
        ItemStack result = recipe.assemble(input, registryAccess);

        if (contraptionEntity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Quality quality = QualityEvaluator.evaluate(inputs, recipe.ingredients(), recipeId, serverLevel.getSeed());
            QualityUtils.setQuality(result, quality);
        }

        newNbt.put(RESULT, result.saveOptional(registryAccess));
        newNbt.putInt(CURRENT_TICK, recipe.time());
        newNbt.putInt(POT_STIR_FRY_COUNT, recipe.stirFryCount());
        updateData(contraptionEntity, localPos, new StructureBlockInfo(localPos, state, newNbt));
    }

    private void applySuspiciousRecipe(AbstractContraptionEntity contraptionEntity, BlockPos localPos, BlockState state,
                                       CompoundTag newNbt, RegistryAccess registryAccess, StructureBlockInfo info) {
        saveCarrier(newNbt, Ingredient.of(Items.BOWL), registryAccess);
        newNbt.put(RESULT, new ItemStack(getItem(SUSPICIOUS_STIR_FRY)).saveOptional(registryAccess));
        newNbt.putInt(CURRENT_TICK, 10 * 20);
        newNbt.putInt(POT_STIR_FRY_COUNT, 0);
        updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), state, newNbt));
    }

    private boolean takeOutProduct(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                   BlockState state, CompoundTag nbt, ItemStack stack, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != FINISHED && status != BURNT) return false;

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        ItemStack finallyResult = status == FINISHED ? readResult(nbt, registryAccess) : new ItemStack(getItem(DARK_CUISINE));

        Ingredient carrier = readCarrier(nbt, registryAccess);
        if (!carrier.isEmpty()) {
            return takeOutWithCarrier(player, contraptionEntity, localPos, state, nbt, stack, finallyResult, carrier, info);
        } else {
            return takeOutWithoutCarrier(player, contraptionEntity, localPos, state, nbt, stack, finallyResult, info);
        }
    }

    private boolean takeOutWithoutCarrier(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                          BlockState state, CompoundTag nbt, ItemStack stack, ItemStack finallyResult, StructureBlockInfo info) {
        if (stack.is(TagMod.KITCHEN_SHOVEL)) {
            if (!player.isSecondaryUseActive()) return false;
            if (!contraptionEntity.level().isClientSide) {
                ItemUtils.getItemToLivingEntity(player, finallyResult);
                reset(contraptionEntity, localPos, state, nbt, info);
            }
            return true;
        } else {
            if (hasHeatSource(contraptionEntity, localPos)) {
                player.hurt(contraptionEntity.level().damageSources().inFire(), 1);
                ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.HURT_WHEN_TAKEOUT_FROM_POT);
            }
            sendActionBar(player, "tip.kaleidoscope_cookery.pot.need_kitchen_shovel");
            return true;
        }
    }

    private boolean takeOutWithCarrier(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                       BlockState state, CompoundTag nbt, ItemStack mainHandItem,
                                       ItemStack finallyResult, Ingredient carrier, StructureBlockInfo info) {
        net.minecraft.network.chat.Component carrierName = carrier.getItems()[0].getHoverName();
        if (carrier.test(mainHandItem)) {
            if (mainHandItem.getCount() < finallyResult.getCount()) {
                sendActionBar(player, "tip.kaleidoscope_cookery.pot.carrier_count_not_enough", finallyResult.getCount(), carrierName);
                return false;
            } else {
                if (!contraptionEntity.level().isClientSide) {
                    mainHandItem.shrink(finallyResult.getCount());
                    ItemUtils.getItemToLivingEntity(player, finallyResult);
                    reset(contraptionEntity, localPos, state, nbt, info);
                }
                return true;
            }
        }
        if (!mainHandItem.is(TagMod.KITCHEN_SHOVEL)) {
            if (hasHeatSource(contraptionEntity, localPos)) {
                player.hurt(contraptionEntity.level().damageSources().inFire(), 1);
                ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.HURT_WHEN_TAKEOUT_FROM_POT);
            }
            sendActionBar(player, "tip.kaleidoscope_cookery.pot.need_carrier", carrierName);
            return true;
        }
        return false;
    }

    private void reset(AbstractContraptionEntity contraptionEntity, BlockPos localPos, BlockState state,
                       CompoundTag nbt, StructureBlockInfo info) {
        if (contraptionEntity.level().isClientSide) return;

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        CompoundTag newNbt = new CompoundTag();
        newNbt.put(INPUTS, ContainerHelper.saveAllItems(new CompoundTag(), NonNullList.withSize(PotRecipe.RECIPES_SIZE, ItemStack.EMPTY), registryAccess));
        saveCarrier(newNbt, Ingredient.EMPTY, registryAccess);
        newNbt.put(RESULT, ItemStack.EMPTY.saveOptional(registryAccess));
        newNbt.putInt(STATUS, PUT_INGREDIENT);
        newNbt.putInt(CURRENT_TICK, 0);
        newNbt.putInt(POT_STIR_FRY_COUNT, 0);
        newNbt.putLong(SEED, System.currentTimeMillis());

        BlockState newState = state.setValue(HAS_OIL, false);
        updateData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), newState, newNbt));
    }


    private boolean isEmpty(CompoundTag nbt, RegistryAccess registryAccess) {
        NonNullList<ItemStack> inputs = readInputs(nbt, registryAccess);
        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    NonNullList<ItemStack> readInputs(CompoundTag nbt, RegistryAccess registryAccess) {
        NonNullList<ItemStack> inputs = NonNullList.withSize(PotRecipe.RECIPES_SIZE, ItemStack.EMPTY);
        if (nbt.contains(INPUTS, Tag.TAG_COMPOUND)) {
            ContainerHelper.loadAllItems(nbt.getCompound(INPUTS), inputs, registryAccess);
        }
        return inputs;
    }

    private void saveInputs(CompoundTag nbt, NonNullList<ItemStack> inputs, RegistryAccess registryAccess) {
        nbt.put(INPUTS, ContainerHelper.saveAllItems(new CompoundTag(), inputs, registryAccess));
    }

    private Ingredient readCarrier(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(CARRIER, Tag.TAG_COMPOUND)) {
            CompoundTag compound = nbt.getCompound(CARRIER);
            return Ingredient.CODEC.decode(NbtOps.INSTANCE, compound).getOrThrow().getFirst();
        }
        return Ingredient.EMPTY;
    }

    private void saveCarrier(CompoundTag nbt, Ingredient carrier, RegistryAccess registryAccess) {
        nbt.put(CARRIER, Ingredient.CODEC.encodeStart(NbtOps.INSTANCE, carrier).getOrThrow());
    }

    private ItemStack readResult(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(RESULT, Tag.TAG_COMPOUND)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(RESULT));
        }
        return ItemStack.EMPTY;
    }
}
