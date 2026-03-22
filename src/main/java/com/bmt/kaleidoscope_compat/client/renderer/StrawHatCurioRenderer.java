//package com.bmt.kaleidoscope_compat.client.renderer;
//
//import com.github.ysbbbbbb.kaleidoscopecookery.KaleidoscopeCookery;
//import com.github.ysbbbbbb.kaleidoscopecookery.client.model.StrawHatModel;
//import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
//import top.theillusivec4.curios.api.client.ICurioRenderer;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import net.minecraft.client.model.EntityModel;
//import net.minecraft.client.model.HumanoidModel;
//import net.minecraft.client.model.geom.ModelPart;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.RenderLayerParent;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemStack;
//import top.theillusivec4.curios.api.SlotContext;
//
//public class StrawHatCurioRenderer extends StrawHatModel implements ICurioRenderer {
//
//    private static final ResourceLocation STRAW_HAT_TEXTURE =
//            ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,
//                    "textures/models/armor/straw_hat.png");
//
//    private static final ResourceLocation STRAW_HAT_FLOWER_TEXTURE =
//            ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,
//                    "textures/models/armor/straw_hat_flower.png");
//
//    public StrawHatCurioRenderer(ModelPart root) {
//        super(root);
//    }
//
//    @Override
//    public <T extends LivingEntity, M extends EntityModel<T>>
//    void render(ItemStack stack, SlotContext slotContext,
//                PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent,
//                MultiBufferSource buffer, int packedLight,
//                float limbSwing, float limbSwingAmount, float partialTicks,
//                float ageInTicks, float netHeadYaw, float headPitch) {
//
//        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel))
//            return;
//
//        poseStack.pushPose();
//
//        ICurioRenderer.followHeadRotations(slotContext.entity(), humanoidModel.head);
//
//        this.getHead().copyFrom(humanoidModel.head);
//
//        boolean hasFlower = stack.getItem() == ModItems.STRAW_HAT_FLOWER.get();
//
//        ResourceLocation tex = hasFlower ? STRAW_HAT_FLOWER_TEXTURE : STRAW_HAT_TEXTURE;
//
//        VertexConsumer vertexConsumer =
//                buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
//
//        this.renderToBuffer(
//                poseStack,
//                vertexConsumer,
//                packedLight,
//                OverlayTexture.NO_OVERLAY
//        );
//
//        poseStack.popPose();
//    }
//}
