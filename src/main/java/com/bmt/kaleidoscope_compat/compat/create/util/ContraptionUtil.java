package com.bmt.kaleidoscope_compat.compat.create.util;

import com.bmt.kaleidoscope_compat.mixins.create.accessor.ContraptionAccessor;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.MillstoneBlockEntityAccessor;
import com.bmt.kaleidoscope_compat.network.ContraptionBlockChangePayload;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

/**
 * 动态结构工具类
 */
public class ContraptionUtil {
    
    // ==================== 热源检测 ====================
    
    /**
     * 检测动态结构下方是否有热源
     */
    public static boolean hasHeatSource(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        return hasHeatSource(
                contraptionEntity.getContraption(),
                localPos,
                pos -> contraptionEntity.level().getBlockState(pos)
        );
    }

    /**
     * 检测动态结构下方是否有热源（MovementContext版本）
     */
    public static boolean hasHeatSource(MovementContext context) {
        if (context.contraption.entity == null) return false;
        return hasHeatSource(
                context.contraption,
                context.localPos,
                pos -> context.world.getBlockState(pos)
        );
    }

    /**
     * 热源检测核心逻辑
     * @param contraption 动态结构
     * @param localPos 本地坐标
     * @param worldStateGetter 世界方块状态获取器
     */
    private static boolean hasHeatSource(Contraption contraption, BlockPos localPos, 
                                         java.util.function.Function<BlockPos, BlockState> worldStateGetter) {
        // 优先检查动态结构内部的方块
        BlockPos belowPos = localPos.below();
        StructureBlockInfo belowInfo = contraption.getBlocks().get(belowPos);
        if (belowInfo != null) {
            return checkHeatSourceState(belowInfo.state());
        }

        // 检查外部世界方块
        Vec3 globalPos = contraption.entity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0f);
        BlockPos worldPos = BlockPos.containing(globalPos);
        return checkHeatSourceState(worldStateGetter.apply(worldPos.below()));
    }

    private static boolean checkHeatSourceState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.LIT)) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return state.is(TagMod.HEAT_SOURCE_BLOCKS_WITHOUT_LIT);
    }

    /**
     * 更新动态结构中的方块数据
     * @param contraptionEntity 动态结构实体
     * @param localPos 本地坐标
     * @param newInfo 新的方块信息
     */
    public static void updateContraptionData(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                             StructureBlockInfo newInfo) {
        Contraption contraption = contraptionEntity.getContraption();

        // 1. 更新方块数据
        contraption.getBlocks().put(localPos, newInfo);

        // 2. 更新 NBT 标签
        updateNbtTag(contraption, localPos, newInfo.nbt());

        // 3. 更新 Actor 数据
        updateActorData(contraption, localPos, newInfo);

        // 4. 同步到客户端
        syncBlockChange(contraptionEntity, localPos, newInfo.state(), newInfo.nbt(), null);

        // 5. 同步 BlockEntity 数据（用于渲染器）
        syncBlockEntityData(contraptionEntity, localPos, newInfo.nbt());
    }

    /**
     * 更新 NBT 标签
     */
    private static void updateNbtTag(Contraption contraption, BlockPos localPos, @Nullable CompoundTag nbt) {
        if (nbt == null) return;
        ((ContraptionAccessor) contraption).getUpdateTags().put(localPos, nbt.copy());
    }

    /**
     * 更新 Actor 数据
     */
    private static void updateActorData(Contraption contraption, BlockPos localPos, StructureBlockInfo newInfo) {
        for (MutablePair<StructureBlockInfo, ?> actor : contraption.getActors()) {
            if (actor.getLeft().pos().equals(localPos)) {
                actor.setLeft(newInfo);
                break;
            }
        }
    }

    /**
     * 将 StructureBlockInfo 的 NBT 数据同步到世界中的 BlockEntity
     */
    private static void syncBlockEntityData(AbstractContraptionEntity contraptionEntity, BlockPos localPos, @Nullable CompoundTag nbt) {
        if (nbt == null) return;
        if (contraptionEntity.level().isClientSide) return;

        Vec3 globalPos = getGlobalPos(contraptionEntity, localPos);
        BlockPos worldPos = BlockPos.containing(globalPos);

        BlockEntity be = contraptionEntity.level().getBlockEntity(worldPos);
        if (be instanceof MillstoneBlockEntityAccessor accessor) {
            syncMillstoneData(be, nbt, contraptionEntity.level().registryAccess(), accessor);
        }
    }

    /**
     * 同步石磨数据
     */
    private static void syncMillstoneData(BlockEntity be, CompoundTag nbt,
                                          RegistryAccess registryAccess,
                                          MillstoneBlockEntityAccessor accessor) {
        if (nbt.contains("MillstoneInput")) {
            ItemStack input = ItemStack.parseOptional(registryAccess, nbt.getCompound("MillstoneInput"));
            accessor.setInput(input);
        }
        if (nbt.contains("MillstoneProgress")) {
            accessor.setProgress(nbt.getInt("MillstoneProgress"));
        }
        be.setChanged();
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
     * 重新计算 Contraption 的 bounds
     */
    public static AABB recalculateBounds(Contraption contraption) {
        AABB newBounds = new AABB(BlockPos.ZERO);
        for (BlockPos pos : contraption.getBlocks().keySet()) {
            newBounds = newBounds.minmax(new AABB(pos));
        }
        contraption.bounds = newBounds;
        contraption.expandBoundsAroundAxis(Direction.Axis.Y);
        return contraption.bounds;
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
            AABB updatedBounds = recalculateBounds(contraption);
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
     * 从 NBT 中读取 Result
     */
    public static ItemStack readResult(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(ContraptionNbtKeys.RESULT, Tag.TAG_COMPOUND)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(ContraptionNbtKeys.RESULT));
        }
        return ItemStack.EMPTY;
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
