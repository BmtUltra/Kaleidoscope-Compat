package com.bmt.kaleidoscope_compat.mixins.spectrum;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ChoppingBoardBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import de.dafuqs.spectrum.blocks.pastel_network.nodes.PastelNodeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PastelNodeBlock.class)
public class PastelNodeBlockMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void onCanSurvive(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!MainConfig.spectrumPastelNodeCompatEnabled) {
            return;
        }

        Direction targetDirection = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING).getOpposite();
        BlockPos targetPos = pos.relative(targetDirection);
        BlockState targetState = world.getBlockState(targetPos);

        if (targetState.getBlock() instanceof PotBlock || targetState.getBlock() instanceof ChoppingBoardBlock || targetState.getBlock() instanceof TeapotBlock) {
            cir.setReturnValue(true);
        }
    }
}