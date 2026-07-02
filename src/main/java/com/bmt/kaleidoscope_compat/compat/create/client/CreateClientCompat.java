package com.bmt.kaleidoscope_compat.compat.create.client;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.client.KeyMappings;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys;
import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.bmt.kaleidoscope_compat.network.ContraptionTakePayload;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class CreateClientCompat {

    private static final double DEFAULT_REACH_DISTANCE = 5.0;

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(KaleidoscopeCompat.id("contraption_pot_overlay"),
                CreateClientCompat::renderPotOverlay);
        event.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR,
                KaleidoscopeCompat.id("contraption_trash_can_overlay"),
                CreateClientCompat::renderTrashCanOverlay);
    }

    public static void onKeyInput(InputEvent.Key event) {
        if (KeyMappings.TAKE_KITCHEN_ITEM.consumeClick()) {
            handleTakeKitchenItem();
        }
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        renderTeapotText(event);
    }

    private static void renderPotOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.SPECTATOR) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) return;

        Vec3 eyePosition = player.getEyePosition(deltaTracker.getGameTimeDeltaPartialTick(true));
        Vec3 lookVector = player.getViewVector(deltaTracker.getGameTimeDeltaPartialTick(true));
        double reachDistance = player.blockInteractionRange();
        Vec3 endPosition = eyePosition.add(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance);

        PotBlockTarget target = findTargetedPotBlock(minecraft, eyePosition, endPosition);
        if (target == null) return;

        BlockState state = target.blockInfo().state();
        CompoundTag nbt = target.blockInfo().nbt();
        if (nbt == null) return;

        boolean hasOil = state.getValue(PotBlock.HAS_OIL);
        if (!hasOil || !ContraptionUtil.hasHeatSource(target.contraptionEntity(), target.localPos())) return;

        int status = nbt.getInt(ContraptionNbtKeys.STATUS);
        renderPotStatus(guiGraphics, minecraft, status);
    }

    private static PotBlockTarget findTargetedPotBlock(Minecraft minecraft, Vec3 eyePos, Vec3 endPos) {
        if (minecraft.level == null) return null;

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
        return closestTarget;
    }

    private static void renderPotStatus(GuiGraphics guiGraphics, Minecraft minecraft, int status) {
        Component message = null;
        int color = 0xFFFFFF;

        if (status == ContraptionNbtKeys.PotStatus.PUT_INGREDIENT) {
            message = Component.translatable("tip.kaleidoscope_cookery.pot.add_ingredient");
        } else if (status == ContraptionNbtKeys.PotStatus.COOKING) {
            message = Component.translatable("tip.kaleidoscope_cookery.pot.need_stir_fry");
        } else if (status == ContraptionNbtKeys.PotStatus.FINISHED) {
            message = Component.translatable("tip.kaleidoscope_cookery.pot.done");
            color = net.minecraft.ChatFormatting.RED.getColor();
        }

        if (message == null) return;

        Font font = minecraft.font;
        int x = minecraft.getWindow().getGuiScaledWidth() / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() - 84;

        for (net.minecraft.util.FormattedCharSequence sequence : font.split(message, 100)) {
            guiGraphics.drawString(font, sequence, x - font.width(sequence) / 2, y, color);
            y += font.lineHeight;
        }
    }

    private record PotBlockTarget(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureTemplate.StructureBlockInfo blockInfo) {}

    private static void renderTrashCanOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ContraptionTrashCanOverlay.render(Minecraft.getInstance(), guiGraphics, deltaTracker);
    }

    private static void handleTakeKitchenItem() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        TargetResult target = findTargetKitchenBlock(mc, player);
        if (target == null) return;

        PacketDistributor.sendToServer(new ContraptionTakePayload(target.contraptionId(), target.targetPos()));
    }

    private static TargetResult findTargetKitchenBlock(Minecraft mc, LocalPlayer player) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.x * DEFAULT_REACH_DISTANCE, lookVec.y * DEFAULT_REACH_DISTANCE, lookVec.z * DEFAULT_REACH_DISTANCE);

        AABB aabb = new AABB(eyePos, endPos).inflate(16);
        Collection<WeakReference<AbstractContraptionEntity>> contraptions =
                null;
        if (mc.level != null) {
            contraptions = ContraptionHandler.loadedContraptions.get(mc.level).values();
        }

        BlockPos targetPos = null;
        AbstractContraptionEntity targetContraption = null;
        double bestDistance = Double.MAX_VALUE;

        if (contraptions != null) {
            for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
                AbstractContraptionEntity contraptionEntity = ref.get();
                if (contraptionEntity == null) continue;
                if (!contraptionEntity.getBoundingBox().intersects(aabb)) continue;

                BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(eyePos, endPos, contraptionEntity);
                if (hitResult == null) continue;

                BlockPos hitLocalPos = hitResult.getBlockPos();
                StructureTemplate.StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(hitLocalPos);
                if (info == null || !isKitchenBlock(info.state())) continue;

                double distance = contraptionEntity.toGlobalVector(hitResult.getLocation(), 1).distanceTo(eyePos);
                if (distance > bestDistance) continue;

                bestDistance = distance;
                targetPos = hitLocalPos;
                targetContraption = contraptionEntity;
            }
        }

        if (targetPos == null) return null;
        return new TargetResult(targetContraption.getId(), targetPos);
    }

    private static boolean isKitchenBlock(BlockState state) {
        Block block = state.getBlock();
        return block instanceof PotBlock
                || block instanceof StockpotBlock
                || block instanceof SteamerBlock
                || block instanceof TeapotBlock;
    }

    private record TargetResult(int contraptionId, BlockPos targetPos) {}

    private static void renderTeapotText(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Vec3 eyePos = mc.player.getEyePosition(partialTick);
        Vec3 lookVec = mc.player.getViewVector(partialTick);
        double reach = mc.player.blockInteractionRange();
        Vec3 endPos = eyePos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

        TeapotTarget target = findTargetedTeapot(mc, eyePos, endPos);
        if (target == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Camera camera = mc.gameRenderer.getMainCamera();

        renderTeapotText(target.globalPos(), target.nbt(), poseStack, bufferSource, camera);
    }

    private static TeapotTarget findTargetedTeapot(Minecraft mc, Vec3 eyePos, Vec3 endPos) {
        Map<Integer, WeakReference<AbstractContraptionEntity>> contraptionMap =
                null;
        if (mc.level != null) {
            contraptionMap = ContraptionHandler.loadedContraptions.get(mc.level);
        }

        Collection<WeakReference<AbstractContraptionEntity>> contraptions = null;
        if (contraptionMap != null) {
            contraptions = contraptionMap.values();
        }
        TeapotTarget closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        if (contraptions != null) {
            for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
                AbstractContraptionEntity contraptionEntity = ref.get();
                if (contraptionEntity == null) continue;

                BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(eyePos, endPos, contraptionEntity);
                if (hitResult == null || hitResult.getType() != BlockHitResult.Type.BLOCK) continue;

                BlockPos hitLocalPos = hitResult.getBlockPos();
                StructureTemplate.StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(hitLocalPos);
                if (info == null || !(info.state().getBlock() instanceof TeapotBlock)) continue;

                CompoundTag nbt = info.nbt();
                if (nbt == null) continue;

                Vec3 globalPos = contraptionEntity.toGlobalVector(Vec3.atCenterOf(hitLocalPos), 1.0f);
                double distance = globalPos.distanceTo(eyePos);
                if (distance >= closestDistance) continue;

                closestDistance = distance;
                closestTarget = new TeapotTarget(globalPos, nbt);
            }
        }
        return closestTarget;
    }

    private static void renderTeapotText(Vec3 globalPos, CompoundTag nbt, PoseStack poseStack,
                                         MultiBufferSource.BufferSource bufferSource, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int status = nbt.getInt(ContraptionNbtKeys.STATUS);

        if (camera == null) return;

        double distanceToCamera = globalPos.distanceTo(camera.getPosition());
        if (distanceToCamera > 32) return;

        poseStack.pushPose();

        poseStack.translate(globalPos.x - camera.getPosition().x,
                globalPos.y - camera.getPosition().y + 0.5,
                globalPos.z - camera.getPosition().z);
        poseStack.mulPose(camera.rotation());

        float scale = 0.015625F;
        poseStack.scale(scale, -scale, scale);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int light = 15728880;
        Component statusText = getStatusText(status);
        if (statusText != null) {
            float width = (float) (-font.width(statusText) / 2) + 0.5f;
            font.drawInBatch(statusText, width, -5, 0xFFFFFF, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        if (status == ContraptionNbtKeys.TeapotStatus.PUT_INGREDIENT) {
            Component fluidText = getFluidName(nbt.getString(ContraptionNbtKeys.TEA_FLUID_ID));
            ItemStack input = mc.level != null ? ItemStack.parseOptional(mc.level.registryAccess(), nbt.getCompound(ContraptionNbtKeys.TEAPOT_INPUT)) : ItemStack.EMPTY;
            Component itemText = input.isEmpty() ?
                    Component.translatable("mco.configure.world.slot.empty") :
                    ComponentUtils.formatList(Arrays.asList(input.getHoverName(), Component.literal("x%d".formatted(input.getCount()))), CommonComponents.space());
            Component infoText = Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.fluid_ingredient", fluidText, itemText, input.getCount());
            float infoWidth = (float) (-font.width(infoText) / 2) + 0.5f;
            font.drawInBatch(infoText, infoWidth, 5, 0xFFFFFF, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        if (status == ContraptionNbtKeys.TeapotStatus.FINISHED) {
            ItemStack result = mc.level != null ? ItemStack.parseOptional(mc.level.registryAccess(), nbt.getCompound(ContraptionNbtKeys.RESULT)) : ItemStack.EMPTY;
            Component itemText = result.isEmpty() ?
                    Component.translatable("mco.configure.world.slot.empty") :
                    ComponentUtils.formatList(Arrays.asList(result.getHoverName(), Component.literal("x%d".formatted(result.getCount()))), CommonComponents.space());
            Component infoText = Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.result", itemText, result.getCount());
            float infoWidth = (float) (-font.width(infoText) / 2) + 0.5f;
            font.drawInBatch(infoText, infoWidth, 5, 0xFFFFFF, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }
    }

    private static Component getStatusText(int status) {
        return switch (status) {
            case ContraptionNbtKeys.TeapotStatus.PUT_INGREDIENT -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.put_ingredient");
            case ContraptionNbtKeys.TeapotStatus.PROCESSING -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.processing");
            case ContraptionNbtKeys.TeapotStatus.FINISHED -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.finished");
            default -> null;
        };
    }

    private static Component getFluidName(String fluidId) {
        if (fluidId == null || fluidId.isEmpty() || fluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString())) {
            return Component.translatable("mco.configure.world.slot.empty");
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidId));
        fluid.getFluidType();
        return Component.translatable(fluid.getFluidType().getDescriptionId());
    }

    private record TeapotTarget(Vec3 globalPos, CompoundTag nbt) {}
}