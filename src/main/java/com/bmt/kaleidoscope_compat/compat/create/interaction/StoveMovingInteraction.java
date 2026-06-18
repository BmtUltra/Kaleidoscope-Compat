package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionBoundsUtil;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.bmt.kaleidoscope_compat.mixins.create.accessor.ContraptionAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.*;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

import static com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem.hasOil;
import static com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem.setHasOil;

/**
 * 灶台在动态结构上的交互行为
 */
public class StoveMovingInteraction extends BaseMovingInteraction {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        StructureTemplate.StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof StoveBlock)) {
            return false;
        }

        BlockState state = info.state();
        ItemStack itemInHand = player.getItemInHand(activeHand);

        // 处理放置 PotBlock
        if (handleKitchenBlockPlacement(player, localPos, contraptionEntity, itemInHand, PotBlock.class,
                () -> ModBlocks.POT.get().defaultBlockState(), this::createPotNbt,
                () -> { if (state.getValue(BlockStateProperties.LIT)) ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.PLACE_POT_ON_HEAT_SOURCE); })) {
            return true;
        }

        // 处理放置 StockpotBlock
        if (handleKitchenBlockPlacement(player, localPos, contraptionEntity, itemInHand, StockpotBlock.class,
                () -> ModBlocks.STOCKPOT.get().defaultBlockState(), this::createStockpotNbt,
                () -> { if (state.getValue(BlockStateProperties.LIT)) ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.PLACE_STOCKPOT_ON_HEAT_SOURCE); })) {
            return true;
        }

        // 处理放置 SteamerBlock
        if (handleKitchenBlockPlacement(player, localPos, contraptionEntity, itemInHand, SteamerBlock.class,
                () -> ModBlocks.STEAMER.get().defaultBlockState(), this::createSteamerNbt, null)) {
            return true;
        }

        // 处理放置 TeapotBlock
        if (player.isSecondaryUseActive() && handleTeapotPlacement(player, localPos, contraptionEntity, itemInHand,
                () -> ModBlocks.TEAPOT.get().defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, player.getDirection().getOpposite())
                        .setValue(TeapotBlock.VARIANT, TeapotBlock.COMMON),
                () -> { if (state.getValue(BlockStateProperties.LIT)) ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.LIT_THE_STOVE); })) {
            return true;
        }

        // 点燃炉灶
        if (!state.getValue(BlockStateProperties.LIT) && itemInHand.is(TagMod.LIT_STOVE)) {
            if (!contraptionEntity.level().isClientSide) {
                BlockState newState = state.setValue(BlockStateProperties.LIT, true);
                updateData(contraptionEntity, localPos, new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));

                Vec3 globalPos = getGlobalPos(contraptionEntity, localPos);
                BlockPos soundPos = new BlockPos((int) globalPos.x, (int) globalPos.y, (int) globalPos.z);

                if (itemInHand.is(Items.FIRE_CHARGE)) {
                    contraptionEntity.level().playSound(null, soundPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, contraptionEntity.level().getRandom().nextFloat() * 0.4F + 0.8F);
                    itemInHand.shrink(1);
                } else {
                    contraptionEntity.level().playSound(null, soundPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, contraptionEntity.level().getRandom().nextFloat() * 0.4F + 0.8F);
                    itemInHand.hurtAndBreak(1, player, LivingEntity.getSlotForHand(activeHand));
                }

                ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.LIT_THE_STOVE);
            }
            return true;
        }

        // 熄灭炉灶
        if (state.getValue(BlockStateProperties.LIT) && itemInHand.is(TagMod.EXTINGUISH_STOVE)) {
            if (!contraptionEntity.level().isClientSide) {
                if (itemInHand.is(ModItems.KITCHEN_SHOVEL.get()) && hasOil(itemInHand)) {
                    setHasOil(itemInHand, false);
                }

                BlockState newState = state.setValue(BlockStateProperties.LIT, false);
                updateData(contraptionEntity, localPos, new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));

                playSound(contraptionEntity, localPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                        2.6F + (contraptionEntity.level().random.nextFloat() - contraptionEntity.level().random.nextFloat()) * 0.8F);

                itemInHand.hurtAndBreak(1, player, LivingEntity.getSlotForHand(activeHand));
            }
            return true;
        }

        return false;
    }

    /**
     * 茶壶放置方法
     */
    private boolean handleTeapotPlacement(Player player, BlockPos stoveLocalPos,
                                          AbstractContraptionEntity contraptionEntity,
                                          ItemStack itemInHand,
                                          java.util.function.Supplier<BlockState> defaultStateSupplier,
                                          Runnable onSuccess) {
        Block heldBlock = Block.byItem(itemInHand.getItem());
        if (!(heldBlock instanceof TeapotBlock)) {
            return false;
        }

        BlockPos abovePos = stoveLocalPos.above();
        Contraption contraption = contraptionEntity.getContraption();

        StructureTemplate.StructureBlockInfo aboveInfo = contraption.getBlocks().get(abovePos);
        if (aboveInfo != null && !aboveInfo.state().isAir()) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            BlockState newState = defaultStateSupplier.get();

            CompoundTag nbt;
            CustomData customData = itemInHand.get(DataComponents.BLOCK_ENTITY_DATA);
            if (customData != null && !customData.isEmpty()) {
                nbt = customData.copyTag();
            } else {
                nbt = createTeapotNbt();
            }

            StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(abovePos, newState, nbt);

            contraption.getBlocks().put(abovePos, newInfo);
            ((ContraptionAccessor) contraption).getUpdateTags().put(abovePos, nbt.copy());

            MovingInteractionBehaviour interactionBehaviour = MovingInteractionBehaviour.REGISTRY.get(newState);
            if (interactionBehaviour != null) {
                contraption.getInteractors().put(abovePos, interactionBehaviour);
            }

            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(newState);
            if (movementBehaviour != null) {
                var actors = contraption.getActors();
                boolean exists = actors.stream().anyMatch(actor -> actor.getLeft().pos().equals(abovePos));
                if (!exists) {
                    MovementContext context = new MovementContext(contraptionEntity.level(), newInfo, contraption);
                    actors.add(MutablePair.of(newInfo, context));
                }
            }

            AABB newBounds = ContraptionBoundsUtil.recalculateBounds(contraption);
            ContraptionUtil.syncBlockChange(contraptionEntity, abovePos, newState, nbt, newBounds);

            contraption.invalidateColliders();

            if (!player.isCreative()) {
                itemInHand.shrink(1);
            }

            playSound(contraptionEntity, abovePos, newState.getSoundType(contraptionEntity.level(), abovePos, null).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);

            if (onSuccess != null) onSuccess.run();
        }

        return true;
    }

    /**
     * 通用的厨房方块放置方法
     */
    private <T extends Block> boolean handleKitchenBlockPlacement(Player player, BlockPos stoveLocalPos,
                                                                   AbstractContraptionEntity contraptionEntity,
                                                                   ItemStack itemInHand,
                                                                   Class<T> blockClass,
                                                                   java.util.function.Supplier<BlockState> defaultStateSupplier,
                                                                   java.util.function.Function<AbstractContraptionEntity, CompoundTag> nbtFactory,
                                                                   Runnable onSuccess) {
        Block heldBlock = Block.byItem(itemInHand.getItem());
        if (!blockClass.isInstance(heldBlock)) {
            return false;
        }

        BlockPos abovePos = stoveLocalPos.above();
        Contraption contraption = contraptionEntity.getContraption();

        StructureTemplate.StructureBlockInfo aboveInfo = contraption.getBlocks().get(abovePos);
        if (aboveInfo != null && !aboveInfo.state().isAir()) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            BlockState newState = defaultStateSupplier.get()
                    .setValue(HorizontalDirectionalBlock.FACING, player.getDirection().getOpposite());

            if (newState.hasProperty(PotBlock.HAS_BASE)) {
                newState = newState.setValue(PotBlock.HAS_BASE, false);
            }

            CompoundTag nbt = nbtFactory.apply(contraptionEntity);
            StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(abovePos, newState, nbt);

            contraption.getBlocks().put(abovePos, newInfo);
            ((ContraptionAccessor) contraption).getUpdateTags().put(abovePos, nbt.copy());

            MovingInteractionBehaviour interactionBehaviour = MovingInteractionBehaviour.REGISTRY.get(newState);
            if (interactionBehaviour != null) {
                contraption.getInteractors().put(abovePos, interactionBehaviour);
            }

            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(newState);
            if (movementBehaviour != null) {
                var actors = contraption.getActors();
                boolean exists = actors.stream().anyMatch(actor -> actor.getLeft().pos().equals(abovePos));
                if (!exists) {
                    MovementContext context = new MovementContext(contraptionEntity.level(), newInfo, contraption);
                    actors.add(MutablePair.of(newInfo, context));
                }
            }

            AABB newBounds = ContraptionBoundsUtil.recalculateBounds(contraption);
            ContraptionUtil.syncBlockChange(contraptionEntity, abovePos, newState, nbt, newBounds);

            // 更新碰撞体
            contraption.invalidateColliders();

            if (!player.isCreative()) {
                itemInHand.shrink(1);
            }

            playSound(contraptionEntity, abovePos, newState.getSoundType(contraptionEntity.level(), abovePos, null).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);

            if (onSuccess != null) onSuccess.run();
        }

        return true;
    }

    private CompoundTag createPotNbt(AbstractContraptionEntity entity) {
        var registryAccess = entity.level().registryAccess();
        CompoundTag nbt = new CompoundTag();
        nbt.put(ContraptionNbtKeys.INPUTS, ContainerHelper.saveAllItems(new CompoundTag(), NonNullList.withSize(9, ItemStack.EMPTY), registryAccess));
        nbt.putString(ContraptionNbtKeys.CARRIER, "");
        nbt.put(ContraptionNbtKeys.RESULT, ItemStack.EMPTY.saveOptional(registryAccess));
        nbt.putInt(ContraptionNbtKeys.STATUS, ContraptionNbtKeys.PotStatus.PUT_INGREDIENT);
        nbt.putInt(ContraptionNbtKeys.CURRENT_TICK, 0);
        nbt.putInt(ContraptionNbtKeys.POT_STIR_FRY_COUNT, 0);
        nbt.putLong(ContraptionNbtKeys.SEED, System.currentTimeMillis());
        return nbt;
    }

    private CompoundTag createStockpotNbt(AbstractContraptionEntity entity) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(ContraptionNbtKeys.STATUS, ContraptionNbtKeys.StockpotStatus.PUT_SOUP_BASE);
        nbt.putInt(ContraptionNbtKeys.CURRENT_TICK, 0);
        nbt.putLong(ContraptionNbtKeys.SEED, System.currentTimeMillis());
        return nbt;
    }

    private CompoundTag createSteamerNbt(AbstractContraptionEntity entity) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(ContraptionNbtKeys.STATUS, 0);
        nbt.putInt(ContraptionNbtKeys.CURRENT_TICK, 0);
        nbt.putLong(ContraptionNbtKeys.SEED, System.currentTimeMillis());
        return nbt;
    }

    private CompoundTag createTeapotNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("TeaFluidId", "minecraft:empty");
        nbt.putInt("Status", 0);
        nbt.putInt("CurrentTick", -1);
        nbt.putLong(ContraptionNbtKeys.SEED, System.currentTimeMillis());
        return nbt;
    }
}
