package com.bmt.kaleidoscope_compat.compat.create.util;

import com.bmt.kaleidoscope_compat.mixins.create.accessor.ContraptionAccessor;
import com.bmt.kaleidoscope_compat.network.ContraptionBlockChangePayload;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 动态结构工具类
 */
public class ContraptionUtil {

    /**
     * 检测动态结构上某位置下方是否有热源
     */
    public static boolean hasHeatSource(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        Map<BlockPos, StructureBlockInfo> blocks = contraption.getBlocks();
        BlockPos belowPos = localPos.below();
        StructureBlockInfo belowInfo = blocks.get(belowPos);
        if (belowInfo == null) {
            return false;
        }
        BlockState belowState = belowInfo.state();
        if (belowState.hasProperty(BlockStateProperties.LIT)) {
            return belowState.getValue(BlockStateProperties.LIT);
        }
        return belowState.is(TagMod.HEAT_SOURCE_BLOCKS_WITHOUT_LIT);
    }

    /**
     * 更新动态结构上方块的数据（BlockState + NBT）
     * 同时更新 blocks map、updateTags 和 actors，并同步到客户端
     */
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

        if (!contraptionEntity.level().isClientSide) {
            syncBlockChange(contraptionEntity, localPos, newInfo.state(), newInfo.nbt(), null);
        }
    }

    /**
     * 更新数据并指定 bounds
     */
    public static void updateContraptionData(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                             StructureBlockInfo newInfo, AABB updatedBounds) {
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

        if (!contraptionEntity.level().isClientSide) {
            syncBlockChange(contraptionEntity, localPos, newInfo.state(), newInfo.nbt(), updatedBounds);
        }
    }

    /**
     * 仅更新 NBT（不改变 BlockState）
     */
    public static void updateContraptionNbt(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                            CompoundTag newNbt, boolean needSync) {
        Contraption contraption = contraptionEntity.getContraption();
        StructureBlockInfo existingInfo = contraption.getBlocks().get(localPos);
        if (existingInfo == null) return;

        BlockState state = existingInfo.state();

        StructureBlockInfo newInfo = new StructureBlockInfo(localPos, state, newNbt);
        contraption.getBlocks().put(localPos, newInfo);

        if (newNbt != null) {
            ((ContraptionAccessor) contraption).getUpdateTags().put(localPos, newNbt.copy());
        }

        for (MutablePair<StructureBlockInfo, ?> actor : contraption.getActors()) {
            if (actor.getLeft().pos().equals(localPos)) {
                actor.setLeft(newInfo);
                break;
            }
        }

        if (needSync && !contraptionEntity.level().isClientSide) {
            syncBlockChange(contraptionEntity, localPos, state, newNbt, null);
        }
    }

    /**
     * 发送 ActionBar 消息
     */
    public static void sendActionBar(net.minecraft.world.entity.player.Player player, String key, Object... args) {
        if (player instanceof ServerPlayer serverPlayer) {
            Component message = Component.translatable(key, args);
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
        }
    }

    /**
     * 将全局坐标转换为 Vec3（用于音效播放，保留 .5 精度）
     */
    public static Vec3 getGlobalSoundVec(MovementContext context) {
        return context.contraption.entity.toGlobalVector(Vec3.atCenterOf(context.localPos), 1.0f);
    }

    /**
     * 将全局坐标转换为 Vec3（用于音效播放，保留 .5 精度）
     */
    public static Vec3 getGlobalSoundVec(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        return contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0f);
    }

    /**
     * 播放音效（考虑动态结构位置，保留精确坐标）
     */
    public static void playSound(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                 SoundEvent sound, float volume, float pitch) {
        Vec3 soundPos = getGlobalSoundVec(contraptionEntity, localPos);
        contraptionEntity.level().playSound(null, soundPos.x, soundPos.y, soundPos.z, sound, SoundSource.BLOCKS, volume, pitch);
    }

    /**
     * 播放音效（使用 SoundSource 参数，保留精确坐标）
     */
    public static void playSound(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                 SoundEvent sound, SoundSource source, float volume, float pitch) {
        Vec3 soundPos = getGlobalSoundVec(contraptionEntity, localPos);
        contraptionEntity.level().playSound(null, soundPos.x, soundPos.y, soundPos.z, sound, source, volume, pitch);
    }

    /**
     * 从 Contraption 中移除一个方块（从 blocks、interactors、actors 中移除）
     */
    public static void removeBlockFromContraption(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        contraption.getBlocks().remove(localPos);
        contraption.getInteractors().remove(localPos);
        contraption.getActors().removeIf(actor -> actor.getLeft().pos().equals(localPos));
    }

    /**
     * 同步方块移除到客户端（发送空气状态包 + 更新 bounds）
     */
    public static void syncBlockRemoval(AbstractContraptionEntity contraptionEntity, BlockPos localPos, AABB updatedBounds) {
        if (contraptionEntity.level().isClientSide) return;

        PacketDistributor.sendToPlayersTrackingEntity(
                contraptionEntity,
                new ContraptionBlockChangePayload(
                        contraptionEntity.getId(),
                        localPos,
                        Blocks.AIR.defaultBlockState(),
                        null,
                        updatedBounds
                )
        );
    }

    /**
     * 同步方块变更到客户端（BlockState + NBT + 可选 bounds）
     */
    public static void syncBlockChange(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                       BlockState state, @Nullable CompoundTag nbt, @Nullable AABB updatedBounds) {
        if (contraptionEntity.level().isClientSide) return;

        PacketDistributor.sendToPlayersTrackingEntity(
                contraptionEntity,
                new ContraptionBlockChangePayload(
                        contraptionEntity.getId(),
                        localPos,
                        state,
                        nbt,
                        updatedBounds
                )
        );
    }

    /**
     * 播放方块的破坏音效
     */
    public static void playBreakSound(AbstractContraptionEntity contraptionEntity, BlockPos localPos, BlockState state) {
        playSound(contraptionEntity, localPos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
    }

    /**
     * 检测热源
     */
    public static boolean hasHeatSource(MovementContext context) {
        Contraption contraption = context.contraption;
        BlockPos belowLocalPos = context.localPos.below();

        StructureBlockInfo belowInfo = contraption.getBlocks().get(belowLocalPos);
        if (belowInfo != null) {
            BlockState belowState = belowInfo.state();
            if (belowState.hasProperty(BlockStateProperties.LIT)) {
                return belowState.getValue(BlockStateProperties.LIT);
            }
            return belowState.is(TagMod.HEAT_SOURCE_BLOCKS_WITHOUT_LIT);
        }

        if (context.contraption.entity == null) return false;

        Vec3 globalPos = context.contraption.entity.toGlobalVector(Vec3.atCenterOf(context.localPos), 1.0f);
        BlockPos worldPos = new BlockPos((int) globalPos.x, (int) globalPos.y, (int) globalPos.z);
        BlockPos worldBelowPos = worldPos.below();

        BlockState worldBelowState = context.world.getBlockState(worldBelowPos);
        if (worldBelowState.hasProperty(BlockStateProperties.LIT)) {
            return worldBelowState.getValue(BlockStateProperties.LIT);
        }
        return worldBelowState.is(TagMod.HEAT_SOURCE_BLOCKS_WITHOUT_LIT);
    }

    /**
     * 更新 MovementContext 的 NBT 数据
     * @param context MovementContext
     * @param newNbt 新的 NBT 数据
     * @param needSync 是否需要同步到客户端
     */
    public static void updateContraptionNbt(MovementContext context, CompoundTag newNbt, boolean needSync) {
        StructureBlockInfo existingInfo = context.contraption.getBlocks().get(context.localPos);
        if (existingInfo == null) return;

        BlockState state = existingInfo.state();
        StructureBlockInfo newInfo = new StructureBlockInfo(context.localPos, state, newNbt);

        context.contraption.getBlocks().put(context.localPos, newInfo);

        if (newNbt != null) {
            ((ContraptionAccessor) context.contraption).getUpdateTags().put(context.localPos, newNbt.copy());
        }

        for (MutablePair<StructureBlockInfo, ?> actor : context.contraption.getActors()) {
            if (actor.getLeft().pos().equals(context.localPos)) {
                actor.setLeft(newInfo);
                break;
            }
        }

        if (needSync && !context.world.isClientSide && context.contraption.entity != null) {
            syncBlockChange(context.contraption.entity, context.localPos, state, newNbt, null);
        }
    }

    /**
     * 更新 MovementContext 的完整数据
     * @param context MovementContext
     * @param newState 新的 BlockState
     * @param newNbt 新的 NBT 数据
     * @param needSync 是否需要同步到客户端
     */
    public static void updateContraptionData(MovementContext context, BlockState newState, CompoundTag newNbt, boolean needSync) {
        StructureBlockInfo newInfo = new StructureBlockInfo(context.localPos, newState, newNbt);

        context.contraption.getBlocks().put(context.localPos, newInfo);

        if (newNbt != null) {
            ((ContraptionAccessor) context.contraption).getUpdateTags().put(context.localPos, newNbt.copy());
        }

        for (MutablePair<StructureBlockInfo, ?> actor : context.contraption.getActors()) {
            if (actor.getLeft().pos().equals(context.localPos)) {
                actor.setLeft(newInfo);
                break;
            }
        }

        if (needSync && !context.world.isClientSide && context.contraption.entity != null) {
            syncBlockChange(context.contraption.entity, context.localPos, newState, newNbt, null);
        }
    }

    /**
     * 获取全局位置（用于粒子效果、音效）
     */
    public static Vec3 getGlobalPos(MovementContext context) {
        if (context.contraption.entity == null) {
            return Vec3.atCenterOf(context.localPos);
        }
        return context.contraption.entity.toGlobalVector(Vec3.atCenterOf(context.localPos), 1.0f);
    }

    /**
     * 获取全局位置（AbstractContraptionEntity 版本）
     */
    public static Vec3 getGlobalPos(AbstractContraptionEntity entity, BlockPos localPos) {
        if (entity == null) {
            return Vec3.atCenterOf(localPos);
        }
        return entity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0f);
    }

    /**
     * 播放音效（MovementContext 版本，保留精确坐标）
     */
    public static void playSound(MovementContext context, SoundEvent sound, float volume, float pitch) {
        if (context.contraption.entity == null) return;
        Vec3 soundPos = getGlobalSoundVec(context);
        context.world.playSound(null, soundPos.x, soundPos.y, soundPos.z, sound, SoundSource.BLOCKS, volume, pitch);
    }

    /**
     * 播放音效（MovementContext 版本，带 SoundSource，保留精确坐标）
     */
    public static void playSound(MovementContext context, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (context.contraption.entity == null) return;
        Vec3 soundPos = getGlobalSoundVec(context);
        context.world.playSound(null, soundPos.x, soundPos.y, soundPos.z, sound, source, volume, pitch);
    }
}
