package com.bmt.kaleidoscope_compat.client;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 客户端射线检测辅助类，用于获取玩家在动态结构上的精确点击位置。
 */
@OnlyIn(Dist.CLIENT)
public class ClientRayTraceHelper {

    private static final double DEFAULT_REACH_DISTANCE = 5.0;

    /**
     * 通过射线检测判断玩家点击的是厨具架的左侧还是右侧。
     * @return true = 左侧, false = 右侧
     */
    public static boolean determineRacksSideFromRayTrace(StructureBlockInfo info,
                                                          AbstractContraptionEntity contraptionEntity,
                                                          Player player) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.x * DEFAULT_REACH_DISTANCE,
                lookVec.y * DEFAULT_REACH_DISTANCE,
                lookVec.z * DEFAULT_REACH_DISTANCE);

        BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(eyePos, endPos, contraptionEntity);
        if (hitResult == null) {
            return true;
        }

        return isLeftFromHitLocation(info, hitResult.getLocation());
    }

    /**
     * 根据的点击位置判断左右侧。
     */
    private static boolean isLeftFromHitLocation(StructureBlockInfo info,
                                                 Vec3 hitLocation) {
        Direction facing = info.state().getValue(HorizontalDirectionalBlock.FACING);
        Vec3 blockCenter = Vec3.atCenterOf(info.pos());

        double yRotDeg = facing.getOpposite().toYRot();
        float yRotRad = (float) Math.toRadians(yRotDeg);
        Vec3 location = hitLocation.subtract(blockCenter).yRot(yRotRad);

        return location.x > 0;
    }
}
