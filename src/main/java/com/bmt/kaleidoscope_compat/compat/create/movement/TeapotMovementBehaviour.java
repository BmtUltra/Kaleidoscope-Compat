package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.KaleidoscopeCookery;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.client.model.TeapotModel;
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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TeapotStatus.*;

/**
 * 茶壶在动态结构上的移动行为
 */
public class TeapotMovementBehaviour extends BaseMovementBehaviour {

    private static final ResourceLocation TEAPOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeCookery.MOD_ID, "textures/block/teapot.png");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    @OnlyIn(Dist.CLIENT)
    private static TeapotModel teapotModel;

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
    public void stopMoving(MovementContext context) {
        if (context.world.isClientSide)
            return;

        StructureBlockInfo info = context.contraption.getBlocks().get(context.localPos);
        if (info == null || !(info.state().getBlock() instanceof TeapotBlock)) {
            return;
        }

        CompoundTag nbt = info.nbt();
        if (nbt == null)
            return;

        Vec3 globalPos = context.contraption.entity.toGlobalVector(Vec3.atCenterOf(context.localPos), 1.0f);
        RegistryAccess registryAccess = context.world.registryAccess();

        // 只掉落内容物（原料/成品），不掉落茶壶本身（茶壶方块会被动态结构系统重新放置）
        List<ItemStack> drops = getContentDrops(registryAccess, nbt);

        if (!drops.isEmpty()) {
            Player nearestPlayer = null;
            double closestDist = Double.MAX_VALUE;
            for (Player player : context.world.players()) {
                double dist = player.position().distanceTo(globalPos);
                if (dist < closestDist) {
                    closestDist = dist;
                    nearestPlayer = player;
                }
            }

            if (nearestPlayer != null && closestDist < 10.0) {
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        ContraptionUtil.giveItemToPlayer(nearestPlayer, drop);
                    }
                }
            } else {
                ContraptionUtil.spawnItemDrops(context.world, globalPos, drops);
            }
        }

        // 重置茶壶状态（与 PotMovementBehaviour.resetPot 同理）
        resetTeapot(context, info.state());
    }

    /**
     * 获取茶壶内的内容物掉落
     */
    private List<ItemStack> getContentDrops(RegistryAccess registryAccess, CompoundTag nbt) {
        List<ItemStack> drops = new ArrayList<>();
        if (nbt == null)
            return drops;

        int status = nbt.getInt(STATUS);

        // PUT_INGREDIENT 或 PROCESSING 状态：掉落原料
        if (status == PUT_INGREDIENT || status == PROCESSING) {
            CompoundTag inputTag = nbt.getCompound(TEAPOT_INPUT);
            if (!inputTag.isEmpty()) {
                ItemStack input = ItemStack.parseOptional(registryAccess, inputTag);
                drops.add(input.copy());
            }
        } else if (status == FINISHED) {
            // FINISHED 状态：掉落成品
            CompoundTag resultTag = nbt.getCompound(RESULT);
            if (!resultTag.isEmpty()) {
                ItemStack result = ItemStack.parseOptional(registryAccess, resultTag);
                drops.add(result.copy());
            }
        }

        return drops;
    }

    /**
     * 重置茶壶状态
     */
    private void resetTeapot(MovementContext context, BlockState state) {
        CompoundTag newNbt = new CompoundTag();
        newNbt.putInt(STATUS, PUT_INGREDIENT);
        newNbt.putInt(CURRENT_TICK, -1);
        newNbt.putString(TEA_FLUID_ID, TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString());
        updateData(context, state, newNbt);
    }

    @Override
    protected void tickWithHeat(MovementContext context, BlockState state, CompoundTag nbt) {
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
        this.onFinishEffects(context);

        ContraptionUtil.spawnParticle(context, ModParticles.COOKING.get(),
                (random.nextFloat() - 0.5F),
                1.1 + random.nextDouble() / 5,
                (random.nextFloat() - 0.5F),
                3,
                (random.nextFloat() - 0.5) * 0.05F,
                0.1,
                (random.nextFloat() - 0.5) * 0.05F,
                0.02);
    }

    private void onFinishEffects(MovementContext context) {
        RandomSource random = context.world.random;
        ContraptionUtil.spawnParticle(context, ModParticles.COOKING.get(),
                0.5 + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                0.8 + random.nextDouble() / 3,
                0.5 +random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
                1,
                (random.nextFloat() - 0.5) * 0.05F,
                0.1,
                (random.nextFloat() - 0.5) * 0.05F,
                0.02);
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
        BlockState state = context.state;
        PoseStack viewProjection = matrices.getViewProjection();
        PoseStack modelMatrix = matrices.getModel();
        Camera camera = mc.gameRenderer.getMainCamera();

        // 渲染茶壶模型动画（沸腾效果）
        renderTeapotAnimation(context, state, nbt, mc, viewProjection, modelMatrix, buffer);

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

    /**
     * 渲染茶壶沸腾动画
     */
    @OnlyIn(Dist.CLIENT)
    private void renderTeapotAnimation(MovementContext context, BlockState state, CompoundTag nbt,
            Minecraft mc, PoseStack viewProjection, PoseStack modelMatrix, MultiBufferSource buffer) {
            //TODO: 实现沸腾动画
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
