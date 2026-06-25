package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复炒锅在动态结构中翻炒动画缺失的问题
 */
@Mixin(PotBlockEntity.class)
public abstract class PotBlockEntityAnimationMixin {

    @Shadow(remap = false)
    public long seed;

    @Shadow(remap = false)
    private int status;

    @Shadow(remap = false)
    public PotBlockEntity.StirFryAnimationData animationData;

    /**
     * 在加载 NBT 数据后，如果 animationData 未初始化且处于烹饪状态，初始化 timestamp
     */
    @Inject(method = "loadAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V", at = @At("RETURN"))
    private void kaleidoscopeCompat$onLoadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        // 只在 animationData 未初始化时处理
        if (this.animationData.timestamp != -1L) {
            return;
        }
        
        // 如果处于烹饪状态（seed != -1 表示已经开始烹饪）
        if (this.seed != -1L && this.status == 1) { // status == 1 表示 COOKING
            // 设置 timestamp 为较早的时间（2秒前），确保 time > 1000
            // 这样渲染器在首次渲染时会正确初始化动画数据（preSeed、randomHeights）
            // 不设置 preSeed，让渲染器自己管理，确保翻炒时 seed 变化能触发动画重新开始
            this.animationData.timestamp = System.currentTimeMillis() - 2000L;
        }
    }
}
