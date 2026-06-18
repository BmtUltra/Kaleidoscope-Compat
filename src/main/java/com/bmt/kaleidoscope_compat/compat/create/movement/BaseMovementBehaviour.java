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
        if (nbt == null && requiresNbt()) return;

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
     * 是否需要 NBT 才能 tick
     * 返回 true（默认）：nbt 为 null 时跳过 tick
     * 返回 false：即使 nbt 为 null 也会执行 tick
     */
    protected boolean requiresNbt() {
        return true;
    }

    /**
     * 检测是否没有热源
     */
    protected boolean hasNoHeatSource(MovementContext context) {
        return !ContraptionUtil.hasHeatSource(context);
    }

    /**
     * 有热源时的 tick 逻辑
     */
    protected abstract void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt);

    /**
     * 获取全局位置
     */
    protected Vec3 getGlobalPos(MovementContext context) {
        return ContraptionUtil.getGlobalPos(context);
    }

    /**
     * 播放音效
     */
    protected void playSound(MovementContext context, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (context.contraption.entity == null) return;
        ContraptionUtil.playSound(context.contraption.entity, context.localPos, sound, source, volume, pitch);
    }

    /**
     * 更新 NBT
     */
    protected void updateNbt(MovementContext context, CompoundTag newNbt) {
        if (context.contraption.entity == null) return;
        var contraption = context.contraption;
        var existingInfo = contraption.getBlocks().get(context.localPos);
        if (existingInfo == null) return;
        var newInfo = new StructureBlockInfo(context.localPos, existingInfo.state(), newNbt);
        ContraptionUtil.updateContraptionData(context.contraption.entity, context.localPos, newInfo);
    }

    /**
     * 更新完整数据
     */
    protected void updateData(MovementContext context, BlockState newState, CompoundTag newNbt) {
        if (context.contraption.entity == null) return;
        StructureBlockInfo newInfo = new StructureBlockInfo(context.localPos, newState, newNbt);
        ContraptionUtil.updateContraptionData(context.contraption.entity, context.localPos, newInfo);
    }
}
