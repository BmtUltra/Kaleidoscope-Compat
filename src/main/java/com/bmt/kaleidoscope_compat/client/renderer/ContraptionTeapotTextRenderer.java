package com.bmt.kaleidoscope_compat.client.renderer;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * 在动态结构上的茶壶上方渲染状态文字
 */
@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, value = Dist.CLIENT)
public class ContraptionTeapotTextRenderer {

    private static final String TEA_FLUID_ID = "TeaFluidId";
    private static final String RESULT = "Result";
    private static final String STATUS = "Status";
    private static final String INPUT = "Input";

    private static final int PUT_INGREDIENT = 0;
    private static final int PROCESSING = 1;
    private static final int FINISHED = 2;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        // 获取玩家视线
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Vec3 eyePos = mc.player.getEyePosition(partialTick);
        Vec3 lookVec = mc.player.getViewVector(partialTick);
        double reach = mc.player.blockInteractionRange();
        Vec3 endPos = eyePos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

        // 找到玩家对准的茶壶
        TeapotTarget target = findTargetedTeapot(mc, eyePos, endPos);
        if (target == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Camera camera = mc.gameRenderer.getMainCamera();

        renderTeapotText(target.globalPos(), target.nbt(), poseStack, bufferSource, camera);
    }

    /**
     * 使用射线检测找到玩家对准的茶壶
     */
    private static TeapotTarget findTargetedTeapot(Minecraft mc, Vec3 eyePos, Vec3 endPos) {
        Map<Integer, WeakReference<AbstractContraptionEntity>> contraptionMap = null;
        if (mc.level != null) {
            contraptionMap = ContraptionHandler.loadedContraptions.get(mc.level);
        }

        Collection<WeakReference<AbstractContraptionEntity>> contraptions = null;
        if (contraptionMap != null) {
            contraptions = contraptionMap.values();
        }

        TeapotTarget closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
            AbstractContraptionEntity contraptionEntity = ref.get();
            if (contraptionEntity == null) {
                continue;
            }

            // 射线检测
            BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(eyePos, endPos, contraptionEntity);
            if (hitResult == null) {
                continue;
            }

            BlockPos hitLocalPos = hitResult.getBlockPos();
            StructureTemplate.StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(hitLocalPos);

            if (info == null) {
                continue;
            }

            if (!(info.state().getBlock() instanceof TeapotBlock)) {
                continue;
            }

            CompoundTag nbt = info.nbt();
            if (nbt == null) {
                continue;
            }

            // 获取世界坐标
            Vec3 globalPos = contraptionEntity.toGlobalVector(Vec3.atCenterOf(hitLocalPos), 1.0f);
            double distance = globalPos.distanceTo(eyePos);

            if (distance >= closestDistance) continue;

            closestDistance = distance;
            closestTarget = new TeapotTarget(globalPos, nbt);
        }

        return closestTarget;
    }

    /**
     * 渲染茶壶状态文字
     */
    private static void renderTeapotText(Vec3 globalPos, CompoundTag nbt, PoseStack poseStack,
                                         MultiBufferSource.BufferSource bufferSource, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int status = nbt.getInt(STATUS);

        // 计算相机到茶壶的距离
        double distanceToCamera = globalPos.distanceTo(camera.getPosition());
        if (distanceToCamera > 32) {
            return; // 超过32格不渲染
        }

        poseStack.pushPose();

        // 平移到茶壶中心上方
        poseStack.translate(globalPos.x - camera.getPosition().x,
                           globalPos.y - camera.getPosition().y + 0.5,
                           globalPos.z - camera.getPosition().z);

        // 面向相机
        poseStack.mulPose(camera.rotation());

        // 使用与原模组相同的固定缩放
        float scale = 0.015625F;
        poseStack.scale(scale, -scale, scale);

        // 禁用深度测试，启用混合
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int light = 15728880; // 最大亮度

        // 渲染状态标题
        Component statusText = getStatusText(status);
        if (statusText != null) {
            float width = (float) (-font.width(statusText) / 2) + 0.5f;
            font.drawInBatch(statusText, width, -5, 0xFFFFFF, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        // 准备状态：显示流体和原料
        if (status == PUT_INGREDIENT) {
            Component fluidText = getFluidName(nbt.getString(TEA_FLUID_ID));
            ItemStack input = null;
            if (mc.level != null) {
                input = ItemStack.parseOptional(mc.level.registryAccess(), nbt.getCompound(INPUT));
            }
            int count = 0;
            if (input != null) {
                count = input.getCount();
            }
            Component itemText = null;
            if (input != null) {
                itemText = input.isEmpty() ?
                        Component.translatable("mco.configure.world.slot.empty") :
                        ComponentUtils.formatList(Arrays.asList(
                                input.getHoverName(),
                                Component.literal("x%d".formatted(count))
                        ), CommonComponents.space());
            }

            Component infoText = Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.fluid_ingredient", fluidText, itemText, count);
            float infoWidth = (float) (-font.width(infoText) / 2) + 0.5f;
            font.drawInBatch(infoText, infoWidth, 5, 0xFFFFFF, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        // 完成状态：显示产物
        if (status == FINISHED) {
            ItemStack result = null;
            if (mc.level != null) {
                result = ItemStack.parseOptional(mc.level.registryAccess(), nbt.getCompound(RESULT));
            }
            int count = result.getCount();
            Component itemText = result.isEmpty() ?
                    Component.translatable("mco.configure.world.slot.empty") :
                    ComponentUtils.formatList(Arrays.asList(
                            result.getHoverName(),
                            Component.literal("x%d".formatted(count))
                    ), CommonComponents.space());

            Component infoText = Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.result", itemText, count);
            float infoWidth = (float) (-font.width(infoText) / 2) + 0.5f;
            font.drawInBatch(infoText, infoWidth, 5, 0xFFFFFF, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        poseStack.popPose();

        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static Component getStatusText(int status) {
        return switch (status) {
            case PUT_INGREDIENT -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.put_ingredient");
            case PROCESSING -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.processing");
            case FINISHED -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.finished");
            default -> null;
        };
    }

    private static Component getFluidName(String fluidId) {
        if (fluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString())) {
            return Component.translatable("mco.configure.world.slot.empty");
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidId));
        return Component.translatable(fluid.getFluidType().getDescriptionId());
    }

    private record TeapotTarget(Vec3 globalPos, CompoundTag nbt) {}
}
