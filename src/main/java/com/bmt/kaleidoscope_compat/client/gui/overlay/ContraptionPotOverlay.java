package com.bmt.kaleidoscope_compat.client.gui.overlay;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Optional;

/**
 * 炒锅在动态结构上的状态提示覆盖层
 */
public class ContraptionPotOverlay {
    public static void render(Minecraft minecraft, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        Vec3 eyePosition = player.getEyePosition(deltaTracker.getGameTimeDeltaPartialTick(true));
        Vec3 lookVector = player.getViewVector(deltaTracker.getGameTimeDeltaPartialTick(true));
        double reachDistance = player.blockInteractionRange();
        Vec3 endPosition = eyePosition.add(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance);

        Optional<PotBlockTarget> target = findTargetedPotBlock(minecraft, eyePosition, endPosition);
        if (target.isEmpty()) {
            return;
        }

        PotBlockTarget potTarget = target.get();
        BlockState state = potTarget.blockInfo().state();
        CompoundTag nbt = potTarget.blockInfo().nbt();

        if (nbt == null) {
            return;
        }

        boolean hasOil = state.getValue(PotBlock.HAS_OIL);
        if (!hasOil || !ContraptionUtil.hasHeatSource(potTarget.contraptionEntity(), potTarget.localPos())) {
            return;
        }

        int status = nbt.getInt(ContraptionNbtKeys.STATUS);

        net.minecraft.client.gui.Font font = minecraft.font;
        Window window = minecraft.getWindow();
        int x = window.getGuiScaledWidth() / 2;
        int y = window.getGuiScaledHeight() - 84;

        MutableComponent message = null;
        int color = 0xFFFFFF;

        if (status == ContraptionNbtKeys.PotStatus.PUT_INGREDIENT) {
            message = Component.translatable("tip.kaleidoscope_cookery.pot.add_ingredient");
        } else if (status == ContraptionNbtKeys.PotStatus.COOKING) {
            message = Component.translatable("tip.kaleidoscope_cookery.pot.need_stir_fry");
        } else if (status == ContraptionNbtKeys.PotStatus.FINISHED) {
            message = Component.translatable("tip.kaleidoscope_cookery.pot.done");
            color = ChatFormatting.RED.getColor();
        }

        if (message != null) {
            drawWordWrap(guiGraphics, font, message, x, y, color);
        }
    }

    /**
     * 查找玩家视线范围内的Contraption PotBlock
     */
    private static Optional<PotBlockTarget> findTargetedPotBlock(Minecraft minecraft, Vec3 eyePos, Vec3 endPos) {
        if (minecraft.level == null) {
            return Optional.empty();
        }

        AABB aabb = new AABB(eyePos, endPos).inflate(16);
        Collection<WeakReference<AbstractContraptionEntity>> contraptions =
            ContraptionHandler.loadedContraptions.get(minecraft.level).values();

        PotBlockTarget closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
            AbstractContraptionEntity contraptionEntity = ref.get();
            if (contraptionEntity == null) continue;

            if (!contraptionEntity.getBoundingBox().intersects(aabb)) continue;

            BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(eyePos, endPos, contraptionEntity);
            if (hitResult == null) continue;

            BlockPos hitLocalPos = hitResult.getBlockPos();
            StructureTemplate.StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(hitLocalPos);

            if (info == null || !(info.state().getBlock() instanceof PotBlock)) continue;

            double distance = contraptionEntity.toGlobalVector(hitResult.getLocation(), 1).distanceTo(eyePos);
            if (distance > closestDistance) continue;

            closestDistance = distance;
            closestTarget = new PotBlockTarget(contraptionEntity, hitLocalPos, info);
        }

        return Optional.ofNullable(closestTarget);
    }

    private static void drawWordWrap(GuiGraphics graphics, net.minecraft.client.gui.Font font, MutableComponent text, int pX, int pY, int color) {
        for (FormattedCharSequence sequence : font.split(text, 100)) {
            graphics.drawString(font, sequence, pX - font.width(sequence) / 2, pY, color);
            pY += font.lineHeight;
        }
    }

    private record PotBlockTarget(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureTemplate.StructureBlockInfo blockInfo) {}
}
