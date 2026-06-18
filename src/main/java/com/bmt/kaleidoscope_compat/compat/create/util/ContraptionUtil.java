package com.bmt.kaleidoscope_compat.compat.create.util;

import com.bmt.kaleidoscope_compat.mixins.create.accessor.ContraptionAccessor;
import com.bmt.kaleidoscope_compat.network.ContraptionBlockChangePayload;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 动态结构工具类
 */
public class ContraptionUtil {
    public static boolean hasHeatSource(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        BlockPos belowPos = localPos.below();
        StructureBlockInfo belowInfo = contraption.getBlocks().get(belowPos);
        if (belowInfo != null) {
            return checkHeatSourceState(belowInfo.state());
        }

        Vec3 globalPos = contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0f);
        BlockPos worldPos = new BlockPos((int) globalPos.x, (int) globalPos.y, (int) globalPos.z);
        BlockState worldBelowState = contraptionEntity.level().getBlockState(worldPos.below());
        return checkHeatSourceState(worldBelowState);
    }

    public static boolean hasHeatSource(MovementContext context) {
        Contraption contraption = context.contraption;
        BlockPos belowLocalPos = context.localPos.below();

        StructureBlockInfo belowInfo = contraption.getBlocks().get(belowLocalPos);
        if (belowInfo != null) {
            return checkHeatSourceState(belowInfo.state());
        }

        if (context.contraption.entity == null) return false;

        Vec3 globalPos = context.contraption.entity.toGlobalVector(Vec3.atCenterOf(context.localPos), 1.0f);
        BlockPos worldPos = new BlockPos((int) globalPos.x, (int) globalPos.y, (int) globalPos.z);
        BlockState worldBelowState = context.world.getBlockState(worldPos.below());
        return checkHeatSourceState(worldBelowState);
    }

    private static boolean checkHeatSourceState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.LIT)) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return state.is(TagMod.HEAT_SOURCE_BLOCKS_WITHOUT_LIT);
    }

    public static void updateContraptionData(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                             StructureBlockInfo newInfo) {
        Contraption contraption = contraptionEntity.getContraption();

        contraption.getBlocks().put(localPos, newInfo);

        if (newInfo.nbt() != null) {
            ((ContraptionAccessor) contraption).getUpdateTags().put(localPos, newInfo.nbt().copy());
        }

        for (MutablePair<StructureBlockInfo, ?> actor : contraption.getActors()) {
            if (actor.getLeft().pos().equals(localPos)) {
                actor.setLeft(newInfo);
                break;
            }
        }

        syncBlockChange(contraptionEntity, localPos, newInfo.state(), newInfo.nbt(), null);
    }

    /**
     * 获取全局位置
     */
    public static Vec3 getGlobalPos(AbstractContraptionEntity entity, BlockPos localPos) {
        if (entity == null) {
            return Vec3.atCenterOf(localPos);
        }
        return entity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0f);
    }

    /**
     * 获取全局位置
     */
    public static Vec3 getGlobalPos(MovementContext context) {
        if (context.contraption.entity == null) {
            return Vec3.atCenterOf(context.localPos);
        }
        return context.contraption.entity.toGlobalVector(Vec3.atCenterOf(context.localPos), 1.0f);
    }

    /**
     * 播放音效
     */
    public static void playSound(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                 SoundEvent sound, SoundSource source, float volume, float pitch) {
        Vec3 soundPos = getGlobalPos(contraptionEntity, localPos);
        contraptionEntity.level().playSound(null, soundPos.x, soundPos.y, soundPos.z, sound, source, volume, pitch);
    }

    /**
     * 从 Contraption 中移除一个方块
     */
    public static void removeBlockFromContraption(AbstractContraptionEntity contraptionEntity, BlockPos localPos, boolean sync) {
        Contraption contraption = contraptionEntity.getContraption();
        contraption.getBlocks().remove(localPos);
        contraption.getInteractors().remove(localPos);
        contraption.getActors().removeIf(actor -> actor.getLeft().pos().equals(localPos));

        if (sync) {
            AABB updatedBounds = ContraptionBoundsUtil.recalculateBounds(contraption);
            syncBlockChange(contraptionEntity, localPos, Blocks.AIR.defaultBlockState(), null, updatedBounds);
            contraption.invalidateColliders();
        }
    }

    /**
     * 同步方块变更到客户端
     */
    public static void syncBlockChange(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                       BlockState state, @Nullable CompoundTag nbt, @Nullable AABB updatedBounds) {
        if (contraptionEntity.level().isClientSide) return;

        ContraptionBlockChangePayload payload = new ContraptionBlockChangePayload(
                contraptionEntity.getId(),
                localPos,
                state,
                nbt,
                updatedBounds
        );

        PacketDistributor.sendToPlayersTrackingEntity(contraptionEntity, payload);
    }

    /**
     * 发送粒子
     */
    public static void spawnParticle(MovementContext context, ParticleOptions particle,
                                      double xOffset, double yOffset, double zOffset,
                                      int count, double speedX, double speedY, double speedZ, double speedSpread) {
        if (!(context.world instanceof ServerLevel sl)) return;

        Vec3 gp = getGlobalPos(context);
        sl.sendParticles(particle,
                gp.x - 0.5 + xOffset,
                gp.y - 0.5 + yOffset,
                gp.z - 0.5 + zOffset,
                count, speedX, speedY, speedZ, speedSpread);
    }

    /**
     * 从 NBT 中读取 Inputs 列表
     */
    public static NonNullList<ItemStack> readInputs(CompoundTag nbt, RegistryAccess registryAccess, int size) {
        NonNullList<ItemStack> inputs = NonNullList.withSize(size, ItemStack.EMPTY);
        if (nbt.contains(ContraptionNbtKeys.INPUTS, Tag.TAG_COMPOUND)) {
            ContainerHelper.loadAllItems(nbt.getCompound(ContraptionNbtKeys.INPUTS), inputs, registryAccess);
        }
        return inputs;
    }

    /**
     * 将 Inputs 列表保存到 NBT
     */
    public static void saveInputs(CompoundTag nbt, NonNullList<ItemStack> inputs, RegistryAccess registryAccess) {
        nbt.put(ContraptionNbtKeys.INPUTS, ContainerHelper.saveAllItems(new CompoundTag(), inputs, registryAccess));
    }

    /**
     * 检查 Inputs 是否为空
     */
    public static boolean areInputsEmpty(CompoundTag nbt, RegistryAccess registryAccess, int size) {
        NonNullList<ItemStack> inputs = readInputs(nbt, registryAccess, size);
        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    /**
     * 保存 Carrier (容器) 到 NBT
     */
    public static void saveCarrier(CompoundTag nbt, Ingredient carrier, String key) {
        nbt.put(key, Ingredient.CODEC.encodeStart(NbtOps.INSTANCE, carrier).getOrThrow());
    }

    /**
     * 从 NBT 中读取 Carrier (容器)
     */
    public static Ingredient readCarrier(CompoundTag nbt, String key, Ingredient defaultCarrier) {
        if (nbt.contains(key, Tag.TAG_COMPOUND)) {
            CompoundTag compound = nbt.getCompound(key);
            return Ingredient.CODEC.decode(NbtOps.INSTANCE, compound).getOrThrow().getFirst();
        }
        return defaultCarrier;
    }

    /**
     * 从 NBT 中读取 ItemStack 列表
     */
    public static NonNullList<ItemStack> readItems(CompoundTag nbt, RegistryAccess registryAccess, int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, items, registryAccess);
        return items;
    }

    /**
     * 将 ItemStack 列表保存到 NBT
     */
    public static void saveItems(CompoundTag nbt, NonNullList<ItemStack> items, RegistryAccess registryAccess) {
        ContainerHelper.saveAllItems(nbt, items, true, registryAccess);
    }

    /**
     * 检查 Items 列表中是否有任何物品
     */
    public static boolean hasAnyItem(CompoundTag nbt, RegistryAccess registryAccess, int size) {
        NonNullList<ItemStack> items = readItems(nbt, registryAccess, size);
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    /**
     * 从 NBT 中读取 int 数组
     */
    public static int[] readIntArray(CompoundTag nbt, String key, int size) {
        if (nbt.contains(key, Tag.TAG_INT_ARRAY)) {
            return nbt.getIntArray(key);
        }
        return new int[size];
    }

    /**
     * 将 int 数组保存到 NBT
     */
    public static void saveIntArray(CompoundTag nbt, String key, int[] array) {
        nbt.putIntArray(key, array);
    }

    /**
     * 从 NBT 中读取 Result
     */
    public static ItemStack readResult(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(ContraptionNbtKeys.RESULT, Tag.TAG_COMPOUND)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(ContraptionNbtKeys.RESULT));
        }
        return ItemStack.EMPTY;
    }

    /**
     * 将物品掉落物添加到世界中
     */
    public static void spawnItemDrops(Level world, Vec3 position, List<ItemStack> items) {
        if (world.isClientSide) return;
        for (ItemStack drop : items) {
            if (!drop.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world, position.x, position.y, position.z, drop);
                itemEntity.setDeltaMovement(0, 0.2, 0);
                world.addFreshEntity(itemEntity);
            }
        }
    }

    /**
     * 将物品给予玩家
     */
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
