package com.bmt.kaleidoscope_compat.client;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.network.ContraptionTakePayload;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import java.lang.ref.WeakReference;
import java.util.Collection;

/**
 * 客户端按键输入处理器
 */
@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    private static final double DEFAULT_REACH_DISTANCE = 4.5;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyMappings.TAKE_KITCHEN_ITEM.consumeClick()) {
            handleTakeKitchenItem();
        }
    }

    private static void handleTakeKitchenItem() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        TargetResult target = findTargetKitchenBlock(mc, player);
        if (target == null) {
            return;
        }

        // 发送网络包到服务端，包含 contraptionId 和目标方块位置
        ContraptionTakePayload payload = new ContraptionTakePayload(target.contraptionId(), target.targetPos());
        ContraptionTakePayload.sendToServer(payload);
    }

    /**
     * 在客户端遍历所有动态结构，找到玩家指向的厨房方块
     */
    private static TargetResult findTargetKitchenBlock(Minecraft mc, LocalPlayer player) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.x * DEFAULT_REACH_DISTANCE, lookVec.y * DEFAULT_REACH_DISTANCE, lookVec.z * DEFAULT_REACH_DISTANCE);

        AABB aabb = new AABB(eyePos, endPos).inflate(16);

        // 使用 ContraptionHandler.loadedContraptions 获取已加载的动态结构
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
                if (contraptionEntity == null) {
                    continue;
                }

                // 检查包围盒是否相交
                if (!contraptionEntity.getBoundingBox().intersects(aabb)) {
                    continue;
                }

                // 使用 Create 的 rayTraceContraption 进行精确射线检测
                BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(eyePos, endPos, contraptionEntity);
                if (hitResult == null) {
                    continue;
                }

                BlockPos hitLocalPos = hitResult.getBlockPos();

                // 检查是否为厨房方块
                StructureTemplate.StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(hitLocalPos);
                if (info == null || !isKitchenBlock(info.state())) {
                    continue;
                }

                // 计算距离，找到最近的
                double distance = contraptionEntity.toGlobalVector(hitResult.getLocation(), 1).distanceTo(eyePos);
                if (distance > bestDistance) {
                    continue;
                }

                bestDistance = distance;
                targetPos = hitLocalPos;
                targetContraption = contraptionEntity;
            }
        }

        if (targetPos == null) {
            return null;
        }

        return new TargetResult(targetContraption.getId(), targetPos);
    }

    /**
     * 检查方块是否为厨房方块
     */
    private static boolean isKitchenBlock(BlockState state) {
        Block block = state.getBlock();
        return block instanceof PotBlock
                || block instanceof StockpotBlock
                || block instanceof SteamerBlock
                || block instanceof TeapotBlock;
    }

    /**
     * 记录目标动态结构的信息
     */
    private record TargetResult(int contraptionId, BlockPos targetPos) {}
}
