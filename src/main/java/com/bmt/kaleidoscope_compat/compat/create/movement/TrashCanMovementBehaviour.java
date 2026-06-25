package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.misc.TrashCanBlock;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TRASH_CAN_ITEMS;

/**
 * 垃圾桶在动态结构上的移动行为
 */
public class TrashCanMovementBehaviour extends BaseMovementBehaviour {

    private static final int SLOT_COUNT = 3;

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof TrashCanBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        return state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        if (context.world.isClientSide) return;
        if (!(context.world instanceof ServerLevel serverLevel)) return;
        if (context.contraption.entity == null) return;

        if (nbt == null) return;

        boolean powered = state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);

        if (powered) {
            trySuckItems(context, state, nbt, serverLevel);
        }
    }

    /**
     * 尝试吸取上方的物品实体
     */
    private void trySuckItems(MovementContext context, BlockState state, CompoundTag nbt, ServerLevel serverLevel) {
        Vec3 globalPos = ContraptionUtil.getGlobalPos(context.contraption.entity, context.localPos);
        BlockPos worldPos = BlockPos.containing(globalPos);

        AABB suckArea = new AABB(worldPos).move(0, 1, 0);
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(ItemEntity.class, suckArea);

        if (items.isEmpty()) return;

        ItemStackHandler handler = readItems(nbt, serverLevel.registryAccess());

        for (ItemEntity itemEntity : items) {
            ItemStack itemStack = itemEntity.getItem();
            if (itemStack.isEmpty()) continue;

            if (canSuckItem(handler, itemStack)) {
                suckItem(handler, itemStack);
                itemEntity.discard();

                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        itemEntity.getX(), itemEntity.getY() + itemEntity.getEyeHeight(), itemEntity.getZ(),
                        1, 0.1, 0.1, 0.1, 0.01);

                CompoundTag newNbt = nbt.copy();
                saveItems(newNbt, handler, serverLevel.registryAccess());
                updateNbt(context, newNbt);

                break;
            }
        }
    }

    private boolean canSuckItem(ItemStackHandler handler, ItemStack stack) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).is(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    private void suckItem(ItemStackHandler handler, ItemStack stack) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).is(stack.getItem())) {
                ItemStack remainder = handler.insertItem(i, stack.copy(), false);
                if (remainder.isEmpty()) {
                    return;
                }
            }
        }
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                handler.setStackInSlot(i, stack.copy());
                return;
            }
        }
        handler.setStackInSlot(0, handler.getStackInSlot(1).copy());
        handler.setStackInSlot(1, handler.getStackInSlot(2).copy());
        handler.setStackInSlot(2, stack.copy());
    }

    private ItemStackHandler readItems(CompoundTag nbt, net.minecraft.core.RegistryAccess registryAccess) {
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT);
        if (nbt.contains(TRASH_CAN_ITEMS)) {
            CompoundTag itemsTag = nbt.getCompound(TRASH_CAN_ITEMS);
            handler.deserializeNBT(registryAccess, itemsTag);
        }
        return handler;
    }

    private void saveItems(CompoundTag nbt, ItemStackHandler handler, net.minecraft.core.RegistryAccess registryAccess) {
        nbt.put(TRASH_CAN_ITEMS, handler.serializeNBT(registryAccess));
    }
}
