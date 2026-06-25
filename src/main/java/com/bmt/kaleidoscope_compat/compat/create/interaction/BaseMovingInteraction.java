package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

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
        Vec3 soundPos = getGlobalPos(entity, localPos);
        entity.level().playSound(null, soundPos.x, soundPos.y, soundPos.z, sound, source, volume, pitch);
    }

    /**
     * 发送 ActionBar 消息
     */
    protected void sendActionBarMessage(Player player, String translationKey, Object... args) {
        Component message = Component.translatable(translationKey, args);
        if (player.level().isClientSide()) {
            Minecraft.getInstance().gui.setOverlayMessage(message, false);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
        }
    }

    /**
     * 检查容器是否匹配
     */
    protected boolean containerIsMatch(Player player, ItemStack stack) {
        Item containerItem = ItemUtils.getContainerItem(stack);
        if (containerItem == Items.AIR) return false;
        if (player.getMainHandItem().is(containerItem)) {
            player.getMainHandItem().shrink(1);
            return false;
        }
        sendActionBarMessage(player, "tip.kaleidoscope_cookery.kitchen.remove_ingredient.need_container", containerItem.getDefaultInstance().getHoverName());
        return true;
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