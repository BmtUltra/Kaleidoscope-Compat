package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_tavern;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.BarCabinetBlockEntityRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BarCabinetBlockEntityRender.class)
@SuppressWarnings("all")
public abstract class BarCabinetRenderMixin {

    @Inject(
            method = "render(Lcom/github/ysbbbbbb/kaleidoscopetavern/blockentity/brew/BarCabinetBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            )
    )
    private void kaleidoscopeCompat$applyEffectsToSingleItem(
            BarCabinetBlockEntity barCabinet, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay,
            CallbackInfo ci
    ) {
        ItemStack leftStack = barCabinet.getLeftItem();
        if (!leftStack.isEmpty() && leftStack.getItem() instanceof BlockItem) {
            kaleidoscopeCompat$applyScaleEffect(leftStack, poseStack);
        }
    }

    @Inject(
            method = "render(Lcom/github/ysbbbbbb/kaleidoscopetavern/blockentity/brew/BarCabinetBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
                    ordinal = 1
            )
    )
    private void kaleidoscopeCompat$applyEffectsToLeftItem(
            BarCabinetBlockEntity barCabinet, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay,
            CallbackInfo ci
    ) {
        ItemStack leftStack = barCabinet.getLeftItem();
        if (!leftStack.isEmpty() && leftStack.getItem() instanceof BlockItem) {
            kaleidoscopeCompat$applyScaleEffect(leftStack, poseStack);
        }
    }

    @Inject(
            method = "render(Lcom/github/ysbbbbbb/kaleidoscopetavern/blockentity/brew/BarCabinetBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
                    ordinal = 2
            )
    )
    private void kaleidoscopeCompat$applyEffectsToRightItem(
            BarCabinetBlockEntity barCabinet, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay,
            CallbackInfo ci
    ) {
        ItemStack rightStack = barCabinet.getRightItem();
        if (!rightStack.isEmpty() && rightStack.getItem() instanceof BlockItem) {
            kaleidoscopeCompat$applyScaleEffect(rightStack, poseStack);
        }
    }

    @Unique
    private void kaleidoscopeCompat$applyScaleEffect(ItemStack itemStack, PoseStack poseStack) {
        if (itemStack.is(TagUtil.Items.SCALE_DOWN_IN_CABINET)) {
            float additionalScale = 0.7f;
            poseStack.scale(additionalScale, additionalScale, additionalScale);
        }
    }
}