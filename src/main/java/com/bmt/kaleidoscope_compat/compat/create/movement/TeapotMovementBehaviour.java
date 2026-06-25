package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TeapotStatus.*;

/**
 * 茶壶在动态结构上的移动行为
 */
public class TeapotMovementBehaviour extends BaseMovementBehaviour {

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof TeapotBlock;
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        String teaFluidId = nbt.getString(TEA_FLUID_ID);
        return !teaFluidId.isEmpty() && !teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString());
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        int status = nbt.getInt(STATUS);

        if (status == PUT_INGREDIENT) {
            tickPutIngredient(context, nbt);
            return;
        }

        if (status == PROCESSING) {
            tickProcessing(context, nbt);
            return;
        }

        if (status == FINISHED) {
            tickFinished(context);
        }
    }

    /**
     * 准备阶段 tick
     */
    private void tickPutIngredient(MovementContext context, CompoundTag nbt) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 23) != 0) {
            return;
        }

        if (hasNoHeatSource(context)) {
            return;
        }

        onProcessingEffects(context);

        CompoundTag inputTag = nbt.getCompound(TEAPOT_INPUT);
        if (inputTag.isEmpty()) {
            return;
        }

        int currentTick = nbt.getInt(CURRENT_TICK);
        if (currentTick > 0) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, Math.max(-1, currentTick - 23));
            updateNbt(context, newNbt);
            return;
        }

        ItemStack input = ItemStack.parseOptional(context.world.registryAccess(), inputTag);
        String teaFluidId = nbt.getString(TEA_FLUID_ID);

        TeapotInput container = new TeapotInput(input, ResourceLocation.parse(teaFluidId));
        Optional<RecipeHolder<TeapotRecipe>> recipeOpt = context.world.getRecipeManager()
                .getRecipeFor(ModRecipes.TEAPOT_RECIPE, container, context.world);

        if (recipeOpt.isPresent()) {
            TeapotRecipe teapotRecipe = recipeOpt.get().value();
            ItemStack result = teapotRecipe.assemble(container, context.world.registryAccess());

            CompoundTag newNbt = nbt.copy();
            newNbt.put(RESULT, result.saveOptional(context.world.registryAccess()));
            newNbt.putInt(CURRENT_TICK, teapotRecipe.time());
            newNbt.putInt(STATUS, PROCESSING);
            updateNbt(context, newNbt);
            return;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.remove(TEAPOT_INPUT);
        newNbt.putInt(CURRENT_TICK, -1);
        updateNbt(context, newNbt);
    }

    /**
     * 烹饪阶段 tick
     */
    private void tickProcessing(MovementContext context, CompoundTag nbt) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 23) != 0) {
            return;
        }

        if (hasNoHeatSource(context)) {
            return;
        }

        onProcessingEffects(context);

        int currentTick = nbt.getInt(CURRENT_TICK);
        if (currentTick > 0) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(CURRENT_TICK, Math.max(-1, currentTick - 23));
            updateNbt(context, newNbt);
            return;
        }

        CompoundTag newNbt = nbt.copy();
        newNbt.putInt(STATUS, FINISHED);
        newNbt.putInt(CURRENT_TICK, -1);
        updateNbt(context, newNbt);
    }

    /**
     * 完成阶段 tick
     */
    private void tickFinished(MovementContext context) {
        long offset = context.world.getGameTime() + context.localPos.hashCode();
        if (Math.floorMod(offset, 11) != 0) {
            return;
        }

        if (ContraptionUtil.hasHeatSource(context)) {
            onBoilingEffects(context);
        }
    }

    /**
     * 烹饪中
     */
    private void onProcessingEffects(MovementContext context) {
        playSound(context, ModSounds.BLOCK_TEAPOT_PROCESSING.get(), SoundSource.BLOCKS, 0.6f,
                0.8f + context.world.random.nextFloat() * 0.2F);
        onFinishEffects(context);
    }

    /**
     * 沸腾时
     */
    private void onBoilingEffects(MovementContext context) {
        RandomSource random = context.world.random;
        playSound(context, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.4f,
                0.8f + random.nextFloat() * 0.2F);

        if (context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x + (random.nextFloat() - 0.5F),
                    gp.y + 0.1 + random.nextDouble() / 5,
                    gp.z + (random.nextFloat() - 0.5F),
                    3,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.1,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.02);
        }
    }

    private void onFinishEffects(MovementContext context) {
        RandomSource random = context.world.random;
        if (context.world instanceof ServerLevel sl) {
            Vec3 gp = getGlobalPos(context);
            sl.sendParticles(ModParticles.COOKING.get(),
                    gp.x + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                    gp.y + 0.3 + random.nextDouble() / 3,
                    gp.z + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                    1,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.1,
                    (random.nextFloat() - 0.5) * 0.05F,
                    0.02);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
            ContraptionMatrices matrices, MultiBufferSource buffer) {
        CompoundTag nbt = context.blockEntityData;
        if (nbt == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        int status = nbt.getInt(STATUS);
        PoseStack viewProjection = matrices.getViewProjection();
        Camera camera = mc.gameRenderer.getMainCamera();

        // 渲染浮动文本
        Vec3 globalPos = context.contraption.entity.toGlobalVector(
                Vec3.atLowerCornerOf(context.localPos).add(0.5, 0.5, 0.5), 1.0f);

        Font font = mc.font;

        viewProjection.pushPose();
        viewProjection.translate(globalPos.x - camera.getPosition().x,
                globalPos.y - camera.getPosition().y + 2.0,
                globalPos.z - camera.getPosition().z);
        viewProjection.mulPose(camera.rotation());

        float scale = 0.015625F;
        viewProjection.scale(scale, -scale, scale);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int light = 15728880;

        Component statusText = getStatusText(status);
        if (statusText != null) {
            float width = (float) (-font.width(statusText) / 2) + 0.5f;
            font.drawInBatch(statusText, width, -5, 0xFFFFFF, false,
                    viewProjection.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        if (status == PUT_INGREDIENT) {
            Component fluidText = getFluidName(nbt.getString(TEA_FLUID_ID));
            ItemStack input = ItemStack.parseOptional(mc.level.registryAccess(), nbt.getCompound(TEAPOT_INPUT));
            int count = input.getCount();
            Component itemText = input.isEmpty() ? Component.translatable("mco.configure.world.slot.empty")
                    : ComponentUtils.formatList(Arrays.asList(
                            input.getHoverName(),
                            Component.literal("x%d".formatted(count))), CommonComponents.space());

            Component infoText = Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.fluid_ingredient",
                    fluidText, itemText, count);
            float infoWidth = (float) (-font.width(infoText) / 2) + 0.5f;
            font.drawInBatch(infoText, infoWidth, 5, 0xFFFFFF, false,
                    viewProjection.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        // 完成状态：显示产物
        if (status == FINISHED) {
            ItemStack result = ItemStack.parseOptional(mc.level.registryAccess(), nbt.getCompound(RESULT));
            int count = result.getCount();
            Component itemText = result.isEmpty() ? Component.translatable("mco.configure.world.slot.empty")
                    : ComponentUtils.formatList(Arrays.asList(
                            result.getHoverName(),
                            Component.literal("x%d".formatted(count))), CommonComponents.space());

            Component infoText = Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.result", itemText,
                    count);
            float infoWidth = (float) (-font.width(infoText) / 2) + 0.5f;
            font.drawInBatch(infoText, infoWidth, 5, 0x00FF00, false,
                    viewProjection.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }

        viewProjection.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private Component getStatusText(int status) {
        return switch (status) {
            case PUT_INGREDIENT -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.put_ingredient");
            case PROCESSING -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.processing");
            case FINISHED -> Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.finished");
            default -> null;
        };
    }

    private Component getFluidName(String fluidId) {
        if (fluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString())) {
            return Component.translatable("mco.configure.world.slot.empty");
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidId));
        return Component.translatable(fluid.getFluidType().getDescriptionId());
    }

}
