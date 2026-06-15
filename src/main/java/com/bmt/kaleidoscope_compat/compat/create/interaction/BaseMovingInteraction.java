package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

/**
 * MovingInteractionBehaviour 抽象基类
 */
public abstract class BaseMovingInteraction extends MovingInteractionBehaviour {

    // ====== 提供的工具方法 ======

    /**
     * 检测热源
     */
    protected boolean hasHeatSource(AbstractContraptionEntity entity, BlockPos localPos) {
        return ContraptionUtil.hasHeatSource(entity, localPos);
    }

    /**
     * 播放音效
     */
    protected void playSound(AbstractContraptionEntity entity, BlockPos localPos, SoundEvent sound, float volume, float pitch) {
        ContraptionUtil.playSound(entity, localPos, sound, volume, pitch);
    }

    /**
     * 播放音效（带 SoundSource）
     */
    protected void playSound(AbstractContraptionEntity entity, BlockPos localPos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        ContraptionUtil.playSound(entity, localPos, sound, source, volume, pitch);
    }

    /**
     * 发送 ActionBar 消息
     */
    protected void sendActionBar(Player player, String translationKey, Object... args) {
        ContraptionUtil.sendActionBar(player, translationKey, args);
    }

    /**
     * 获取全局位置
     */
    protected Vec3 getGlobalPos(AbstractContraptionEntity entity, BlockPos localPos) {
        return ContraptionUtil.getGlobalPos(entity, localPos);
    }

    /**
     * 获取方块信息（带 NBT 初始化）
     */
    protected StructureBlockInfo getInfo(AbstractContraptionEntity entity, BlockPos localPos) {
        return entity.getContraption().getBlocks().get(localPos);
    }

    /**
     * 获取 NBT（如果为空则创建新的）
     */
    protected CompoundTag getOrCreateNbt(StructureBlockInfo info) {
        CompoundTag nbt = info.nbt();
        if (nbt == null) {
            nbt = new CompoundTag();
        }
        return nbt;
    }

    /**
     * 更新方块数据（BlockState + NBT）
     */
    protected void updateData(AbstractContraptionEntity entity, BlockPos localPos, StructureBlockInfo newInfo) {
        ContraptionUtil.updateContraptionData(entity, localPos, newInfo);
    }

    /**
     * 更新方块数据（带 bounds）
     */
    protected void updateData(AbstractContraptionEntity entity, BlockPos localPos, StructureBlockInfo newInfo, net.minecraft.world.phys.AABB bounds) {
        ContraptionUtil.updateContraptionData(entity, localPos, newInfo, bounds);
    }
}
