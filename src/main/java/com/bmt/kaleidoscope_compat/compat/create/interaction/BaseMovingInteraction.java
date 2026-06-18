package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class BaseMovingInteraction extends MovingInteractionBehaviour {

    /**
     * 检测是否有热源
     */
    protected boolean hasHeatSource(AbstractContraptionEntity entity, BlockPos localPos) {
        return ContraptionUtil.hasHeatSource(entity, localPos);
    }

    /**
     * 播放音效
     */
    protected void playSound(AbstractContraptionEntity entity, BlockPos localPos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        ContraptionUtil.playSound(entity, localPos, sound, source, volume, pitch);
    }

    /**
     * 发送 ActionBar 消息
     */
    @OnlyIn(Dist.CLIENT)
    protected void sendActionBarMessage(Player player, String translationKey, Object... args) {
        Component message = Component.translatable(translationKey, args);
        if (player.level().isClientSide()) {
            Minecraft.getInstance().gui.setOverlayMessage(message, false);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
        }
    }

    /**
     * 获取全局位置
     */
    protected Vec3 getGlobalPos(AbstractContraptionEntity entity, BlockPos localPos) {
        return ContraptionUtil.getGlobalPos(entity, localPos);
    }

    /**
     * 获取 NBT
     */
    protected CompoundTag getOrCreateNbt(StructureBlockInfo info) {
        CompoundTag nbt = info.nbt();
        if (nbt == null) {
            nbt = new CompoundTag();
        }
        return nbt;
    }

    /**
     * 更新方块数据
     */
    protected void updateData(AbstractContraptionEntity entity, BlockPos localPos, StructureBlockInfo newInfo) {
        ContraptionUtil.updateContraptionData(entity, localPos, newInfo);
    }

}
