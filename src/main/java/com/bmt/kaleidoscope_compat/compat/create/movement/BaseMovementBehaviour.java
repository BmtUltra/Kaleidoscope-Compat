package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

/**
 * MovementBehaviour 抽象基类
 */
public abstract class BaseMovementBehaviour implements MovementBehaviour {

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide) {
            tickClient();
            return;
        }
        tickServer(context);
    }

    /**
     * 服务端 tick 逻辑
     */
    protected void tickServer(MovementContext context) {
        StructureBlockInfo info = getInfo(context);
        if (info == null) return;
        if (!isValidBlock(info.state())) return;

        CompoundTag nbt = info.nbt();
        if (nbt == null && requiresNbt()) return;
        if (!shouldTick(context, info.state(), nbt)) return;

        doTick(context, info.state(), nbt);
    }

    /**
     * 客户端 tick 调用
     */
    protected void tickClient() {
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
     * 核心 tick 逻辑
     */
    protected abstract void doTick(MovementContext context, BlockState state, CompoundTag nbt);

    /**
     * 是否需要 NBT 才能 tick
     * 返回 true（默认）：nbt 为 null 时跳过 tick
     * 返回 false：即使 nbt 为 null 也会执行 tick
     */
    protected boolean requiresNbt() {
        return true;
    }

    /**
     * 获取当前方块信息
     */
    protected StructureBlockInfo getInfo(MovementContext context) {
        return context.contraption.getBlocks().get(context.localPos);
    }

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
        Vec3 soundPos = getGlobalPos(context);
        context.world.playSound(null, soundPos.x, soundPos.y, soundPos.z, sound, source, volume, pitch);
    }

    /**
     * 更新 NBT 数据
     */
    protected void updateNbt(MovementContext context, CompoundTag nbt) {
        StructureBlockInfo info = getInfo(context);
        if (info == null) return;
        updateData(context, info.state(), nbt);
    }

    /**
     * 更新完整数据（方块状态 + NBT）
     */
    protected void updateData(MovementContext context, BlockState state, CompoundTag nbt) {
        ContraptionUtil.updateContraptionData(context.contraption.entity, context.localPos,
                new StructureBlockInfo(context.localPos, state, nbt));
    }

    /**
     * 检测方块是否被红石信号激活（POWERED 属性为 true）
     */
    protected boolean hasRedstonePower(BlockState state) {
        return state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);
    }

    /**
     * 检测是否有热源
     */
    protected boolean hasNoHeatSource(MovementContext context) {
        return !ContraptionUtil.hasHeatSource(context);
    }
}