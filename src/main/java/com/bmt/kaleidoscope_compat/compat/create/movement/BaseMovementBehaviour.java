package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

/**
 * MovementBehaviour 抽象基类
 */
public abstract class BaseMovementBehaviour implements MovementBehaviour {

    @Override
    public final void tick(MovementContext context) {
        if (context.world.isClientSide) return;

        StructureBlockInfo info = context.contraption.getBlocks().get(context.localPos);
        if (info == null) return;
        if (!isValidBlock(info.state())) return;

        CompoundTag nbt = info.nbt();
        if (nbt == null) return;

        if (!shouldTick(context, info.state(), nbt)) return;

        tickWithHeat(context, info.state(), nbt);
    }

    /**
     * 检查方块类型是否匹配
     */
    protected abstract boolean isValidBlock(BlockState state);

    /**
     * 决定是否应该执行 tick
     */
    protected abstract boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt);

    /**
     * 检测热源
     */
    protected boolean hasHeatSource(MovementContext context) {
        return !ContraptionUtil.hasHeatSource(context);
    }

    /**
     * 有热源时的 tick 逻辑
     */
    protected abstract void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt);

    // ====== 提供的工具方法 ======

    /**
     * 获取全局位置
     */
    protected Vec3 getGlobalPos(MovementContext context) {
        return ContraptionUtil.getGlobalPos(context);
    }

    /**
     * 播放音效
     */
    protected void playSound(MovementContext context, SoundEvent sound, float volume, float pitch) {
        ContraptionUtil.playSound(context, sound, volume, pitch);
    }

    /**
     * 播放音效（带 SoundSource）
     */
    protected void playSound(MovementContext context, SoundEvent sound, SoundSource source, float volume, float pitch) {
        ContraptionUtil.playSound(context, sound, source, volume, pitch);
    }

    /**
     * 更新 NBT
     */
    protected void updateNbt(MovementContext context, CompoundTag newNbt, boolean needSync) {
        ContraptionUtil.updateContraptionNbt(context, newNbt, needSync);
    }

    /**
     * 更新完整数据
     */
    protected void updateData(MovementContext context, BlockState newState, CompoundTag newNbt, boolean needSync) {
        ContraptionUtil.updateContraptionData(context, newState, newNbt, needSync);
    }
}
