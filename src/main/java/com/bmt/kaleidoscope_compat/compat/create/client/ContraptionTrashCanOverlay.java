package com.bmt.kaleidoscope_compat.compat.create.client;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys;
import com.github.ysbbbbbb.kaleidoscopecookery.block.misc.TrashCanBlock;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Optional;

/**
 * 垃圾桶在动态结构上的物品渲染覆盖层
 * 当玩家看向动态结构上的垃圾桶时，显示其中存储的物品
 */
public class ContraptionTrashCanOverlay {

    private static final int SLOT_COUNT = 3;

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

        Optional<TrashCanTarget> target = findTargetedTrashCanBlock(minecraft, eyePosition, endPosition);
        if (target.isEmpty()) {
            return;
        }

        TrashCanTarget trashCanTarget = target.get();
        StructureBlockInfo info = trashCanTarget.blockInfo();
        CompoundTag nbt = info.nbt();

        if (nbt == null) {
            return;
        }

        RegistryAccess registryAccess = minecraft.level.registryAccess();
        ItemStackHandler handler = readItems(nbt, registryAccess);

        // 检查是否有物品
        boolean hasItems = false;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            return;
        }

        // 渲染物品
        renderTrashCanTip(guiGraphics, minecraft, player, handler);
    }

    /**
     * 渲染垃圾桶物品提示
     */
    private static void renderTrashCanTip(GuiGraphics guiGraphics, Minecraft minecraft, LocalPlayer player, ItemStackHandler storage) {
        Font font = minecraft.font;
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 - 28;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + 4;

        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                guiGraphics.renderFakeItem(stack, x, y);
                guiGraphics.renderItemDecorations(font, stack, x, y);
                x = x + 20;
            }
        }
    }

    /**
     * 查找玩家视线范围内的 Contraption TrashCanBlock
     */
    private static Optional<TrashCanTarget> findTargetedTrashCanBlock(Minecraft minecraft, Vec3 eyePos, Vec3 endPos) {
        if (minecraft.level == null) {
            return Optional.empty();
        }

        AABB aabb = new AABB(eyePos, endPos).inflate(16);
        Collection<WeakReference<AbstractContraptionEntity>> contraptions =
                ContraptionHandler.loadedContraptions.get(minecraft.level).values();

        TrashCanTarget closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
            AbstractContraptionEntity contraptionEntity = ref.get();
            if (contraptionEntity == null) continue;

            if (!contraptionEntity.getBoundingBox().intersects(aabb)) continue;

            BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(eyePos, endPos, contraptionEntity);
            if (hitResult == null) continue;

            BlockPos hitLocalPos = hitResult.getBlockPos();
            StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(hitLocalPos);

            if (info == null || !(info.state().getBlock() instanceof TrashCanBlock)) continue;

            double distance = contraptionEntity.toGlobalVector(hitResult.getLocation(), 1).distanceTo(eyePos);
            if (distance > closestDistance) continue;

            closestDistance = distance;
            closestTarget = new TrashCanTarget(contraptionEntity, hitLocalPos, info);
        }

        return Optional.ofNullable(closestTarget);
    }

    private static ItemStackHandler readItems(CompoundTag nbt, RegistryAccess registryAccess) {
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT);
        if (nbt.contains(ContraptionNbtKeys.TRASH_CAN_ITEMS)) {
            CompoundTag itemsTag = nbt.getCompound(ContraptionNbtKeys.TRASH_CAN_ITEMS);
            handler.deserializeNBT(registryAccess, itemsTag);
        }
        return handler;
    }

    private record TrashCanTarget(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureBlockInfo blockInfo) {}
}