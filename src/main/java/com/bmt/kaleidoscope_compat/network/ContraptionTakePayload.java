package com.bmt.kaleidoscope_compat.network;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.ChairBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.TableBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 从动态结构上取下厨具的网络包
 */
public record ContraptionTakePayload(int contraptionEntityId, BlockPos localPos) implements CustomPacketPayload {

    public static final Type<ContraptionTakePayload> TYPE = new Type<>(
            KaleidoscopeCompat.id("contraption_take")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionTakePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ContraptionTakePayload decode(@NotNull RegistryFriendlyByteBuf buf) {
            int id = buf.readInt();
            BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
            return new ContraptionTakePayload(id, pos);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, ContraptionTakePayload payload) {
            buf.writeInt(payload.contraptionEntityId);
            BlockPos.STREAM_CODEC.encode(buf, payload.localPos);
        }
    };

    public static void sendToServer(ContraptionTakePayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void handle(ContraptionTakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                executeTake(serverPlayer, payload.contraptionEntityId(), payload.localPos());
            }
        });
    }

    private static void executeTake(ServerPlayer player, int entityId, BlockPos localPos) {
        Entity entity = player.level().getEntity(entityId);
        if (!(entity instanceof AbstractContraptionEntity contraptionEntity)) {
            return;
        }

        var contraption = contraptionEntity.getContraption();
        StructureBlockInfo info = contraption.getBlocks().get(localPos);
        if (info == null) {
            return;
        }

        BlockState state = info.state();
        Block block = state.getBlock();
        CompoundTag blockNbt = info.nbt();
        if (blockNbt == null) blockNbt = new CompoundTag();

        if (block instanceof TableBlock || block instanceof ChairBlock) {
            return;
        }

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();

        // 对于厨房方块，使用专门的掉落逻辑
        switch (block) {
            case TeapotBlock ignored -> {
                List<ItemStack> drops = getTeapotDrops(registryAccess, blockNbt);
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        ItemUtils.getItemToLivingEntity(player, drop);
                    }
                }
                ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, true);
                return;
            }
            case SteamerBlock ignored -> {
                handleSteamerTake(player, contraptionEntity, localPos, state, blockNbt, registryAccess);
                return;
            }
            case PotBlock ignored -> {
                ItemUtils.getItemToLivingEntity(player, ModItems.POT.get().getDefaultInstance());
                ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, true);
                return;
            }
            case StockpotBlock ignored -> {
                ItemUtils.getItemToLivingEntity(player, ModItems.STOCKPOT.get().getDefaultInstance());
                // 返还锅盖（如果有）
                ItemStack lid = ItemStack.parseOptional(registryAccess, blockNbt.getCompound(ContraptionNbtKeys.STOCKPOT_LID_ITEM));
                if (!lid.isEmpty()) {
                    ItemUtils.getItemToLivingEntity(player, lid);
                }
                ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, true);
                return;
            }
            default -> {
            }
        }

        // 非厨房方块的默认行为
        ItemStack drop = new ItemStack(block.asItem());
        if (!drop.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    contraptionEntity.level(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    drop
            );
            itemEntity.setDeltaMovement(0, 0.2, 0);
            contraptionEntity.level().addFreshEntity(itemEntity);
        }
        ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, true);
    }

    /**
     * 处理蒸笼取下：
     * - 有原料时不能取下（让普通交互处理取出原料）
     * - 半格蒸笼：完全移除，给 1 个蒸笼物品
     * - 整格蒸笼：变为半格（空 NBT），给 1 个蒸笼物品
     * - 有盖子或上方有蒸笼时不能取下
     */
    private static void handleSteamerTake(ServerPlayer player, AbstractContraptionEntity contraptionEntity,
                                           BlockPos localPos, BlockState state, CompoundTag nbt,
                                           RegistryAccess registryAccess) {
        // 有盖子不能取下
        if (state.getValue(SteamerBlock.HAS_LID)) {
            return;
        }

        // 上方有蒸笼不能取下
        var contraption = contraptionEntity.getContraption();
        StructureBlockInfo aboveInfo = contraption.getBlocks().get(localPos.above());
        if (aboveInfo != null && aboveInfo.state().is(ModBlocks.STEAMER.get())) {
            return;
        }

        // 如果有原料，不执行取下
        NonNullList<ItemStack> items = ContraptionUtil.readItems(nbt, registryAccess, 8);
        boolean hasItems = items.stream().anyMatch(stack -> !stack.isEmpty());
        if (hasItems) {
            return;
        }

        boolean half = state.getValue(SteamerBlock.HALF);

        // 给 1 个蒸笼物品
        ItemUtils.getItemToLivingEntity(player, ModItems.STEAMER.get().getDefaultInstance());

        if (half) {
            // 半格蒸笼：完全移除
            ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, true);
        } else {
            // 整格蒸笼：变为半格，NBT 清空
            CompoundTag newNbt = new CompoundTag();
            ContainerHelper.saveAllItems(newNbt, NonNullList.withSize(8, ItemStack.EMPTY), false, registryAccess);
            newNbt.putIntArray(ContraptionNbtKeys.STEAMER_COOKING_PROGRESS, new int[8]);
            newNbt.putIntArray(ContraptionNbtKeys.STEAMER_COOKING_TIME, new int[8]);

            BlockState newState = state.setValue(SteamerBlock.HALF, true);
            StructureBlockInfo newInfo = new StructureBlockInfo(localPos, newState, newNbt);
            contraption.getBlocks().put(localPos, newInfo);

            AABB updatedBounds = ContraptionUtil.recalculateBounds(contraption);
            ContraptionUtil.syncBlockChange(contraptionEntity, localPos, newState, newNbt, updatedBounds);
            contraption.invalidateColliders();
        }
    }

    private static List<ItemStack> getTeapotDrops(RegistryAccess registryAccess, CompoundTag nbt) {
        List<ItemStack> drops = new ArrayList<>();
        if (nbt == null) return drops;

        int status = nbt.getInt(ContraptionNbtKeys.STATUS);

        if (status == ContraptionNbtKeys.TeapotStatus.PUT_INGREDIENT || status == ContraptionNbtKeys.TeapotStatus.PROCESSING) {
            CompoundTag inputTag = nbt.getCompound(ContraptionNbtKeys.TEAPOT_INPUT);
            if (!inputTag.isEmpty()) {
                ItemStack input = ItemStack.parseOptional(registryAccess, inputTag);
                drops.add(input.copy());
            }

            ItemStack teapot = ModItems.TEAPOT.get().getDefaultInstance();
            CompoundTag tag = new CompoundTag();
            tag.putString(ContraptionNbtKeys.TEA_FLUID_ID, nbt.getString(ContraptionNbtKeys.TEA_FLUID_ID));
            tag.putInt(ContraptionNbtKeys.STATUS, status);
            BlockItem.setBlockEntityData(teapot, ModBlocks.TEAPOT_BE.get(), tag);
            drops.add(teapot);

        } else if (status == ContraptionNbtKeys.TeapotStatus.FINISHED) {
            ItemStack teapot = ModItems.TEAPOT.get().getDefaultInstance();
            CompoundTag tag = new CompoundTag();
            CompoundTag resultTag = nbt.getCompound(ContraptionNbtKeys.RESULT);
            if (!resultTag.isEmpty()) {
                tag.put(ContraptionNbtKeys.RESULT, resultTag);
            }
            tag.putInt(ContraptionNbtKeys.STATUS, status);
            tag.putString(ContraptionNbtKeys.TEA_FLUID_ID, nbt.getString(ContraptionNbtKeys.TEA_FLUID_ID));
            BlockItem.setBlockEntityData(teapot, ModBlocks.TEAPOT_BE.get(), tag);
            drops.add(teapot);

        } else {
            ItemStack teapot = ModItems.TEAPOT.get().getDefaultInstance();
            CompoundTag tag = new CompoundTag();
            tag.putInt(ContraptionNbtKeys.STATUS, status);
            tag.putString(ContraptionNbtKeys.TEA_FLUID_ID, nbt.getString(ContraptionNbtKeys.TEA_FLUID_ID));
            BlockItem.setBlockEntityData(teapot, ModBlocks.TEAPOT_BE.get(), tag);
            drops.add(teapot);
        }

        return drops;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
