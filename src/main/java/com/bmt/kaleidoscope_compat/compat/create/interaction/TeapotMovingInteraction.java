package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.TeapotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.util.FluidUtils;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;
import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.TeapotStatus.*;

/**
 * 茶壶在动态结构上的交互行为
 */
public class TeapotMovingInteraction extends BaseMovingInteraction {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
            AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }

        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof TeapotBlock)) {
            return false;
        }

        CompoundTag nbt = getOrCreateNbt(info);
        ItemStack mainHandItem = player.getMainHandItem();
        var capability = mainHandItem.getCapability(Capabilities.FluidHandler.ITEM);

        // 1. 加入/取出水源
        if (capability != null) {
            if (FluidUtils.hasFluid(mainHandItem)) {
                return addTeaFluid(player, contraptionEntity, localPos, mainHandItem, nbt, info);
            }
            return removeTeaFluid(player, contraptionEntity, localPos, mainHandItem, nbt, info);
        }

        // 2. 加入原料
        if (!mainHandItem.isEmpty()) {
            return addIngredient(player, contraptionEntity, localPos, mainHandItem, nbt, info);
        }

        // 3. 取出原料
        if (player.isSecondaryUseActive()) {
            return removeIngredient(player, contraptionEntity, localPos, nbt, info);
        }

        // 4. 拿起茶壶
        if (mainHandItem.isEmpty() && !player.isSecondaryUseActive()) {
            return takeTeapot(player, contraptionEntity, localPos);
        }

        return false;
    }

    private boolean addTeaFluid(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
            ItemStack itemStack, CompoundTag nbt, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != PUT_INGREDIENT) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.add_tea_fluid.state_incorrect",
                    getStatusText(status));
            return false;
        }

        var cap = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (cap == null) {
            return false;
        }

        String currentTeaFluidId = nbt.getString(TEA_FLUID_ID);
        if (!currentTeaFluidId.isEmpty()
                && !currentTeaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString())) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.add_tea_fluid.has_fluid");
            return false;
        }

        FluidStack fluidInTank = cap.getFluidInTank(0);
        Fluid fluid = fluidInTank.getFluid();
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);

        int amount = fluidInTank.getAmount();
        if (amount < FluidType.BUCKET_VOLUME) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.add_tea_fluid.fluid_not_enough");
            return false;
        }

        FluidTank needFluidHandler = new FluidTank(FluidType.BUCKET_VOLUME,
                stack -> FluidStack.isSameFluidSameComponents(stack, fluidInTank));
        if (!FluidUtils.emptyItem(player, itemStack, needFluidHandler, FluidType.BUCKET_VOLUME)) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putString(TEA_FLUID_ID, id.toString());
            updateData(contraptionEntity, localPos, new StructureBlockInfo(localPos, info.state(), newNbt));
        }

        return true;
    }

    /**
     * 取出茶水流体
     */
    private boolean removeTeaFluid(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
            ItemStack itemStack, CompoundTag nbt, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        String teaFluidId = nbt.getString(TEA_FLUID_ID);

        if (status != PUT_INGREDIENT || teaFluidId.isEmpty()
                || teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString())) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.take_tea_fluid.blocked");
            return false;
        }

        CompoundTag inputTag = nbt.getCompound(TEAPOT_INPUT);
        if (!inputTag.isEmpty()) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.take_tea_fluid.blocked");
            return false;
        }

        var cap = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (cap == null) {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(teaFluidId));
        if (fluid == Fluids.EMPTY) {
            return false;
        }

        FluidTank sourceFluidHandler = new FluidTank(FluidType.BUCKET_VOLUME);
        sourceFluidHandler.setFluid(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
        if (!FluidUtils.fillItem(player, itemStack, sourceFluidHandler, FluidType.BUCKET_VOLUME)) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            CompoundTag newNbt = nbt.copy();
            newNbt.putString(TEA_FLUID_ID, TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString());
            newNbt.putInt(CURRENT_TICK, -1);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(localPos, info.state(), newNbt));
        }

        return true;
    }

    /**
     * 添加原料
     */
    private boolean addIngredient(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
            ItemStack itemStack, CompoundTag nbt, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != PUT_INGREDIENT) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.add_ingredient.state_incorrect",
                    getStatusText(status));
            return false;
        }

        String teaFluidId = nbt.getString(TEA_FLUID_ID);
        if (teaFluidId.isEmpty() || teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID.toString())) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.add_ingredient.no_fluid");
            return false;
        }

        CompoundTag inputTag = nbt.getCompound(TEAPOT_INPUT);
        if (!inputTag.isEmpty()) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.add_ingredient.has_ingredient");
            return false;
        }

        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
        ItemStack input = itemStack.copy();
        TeapotInput container = new TeapotInput(input, ResourceLocation.parse(teaFluidId));
        Optional<RecipeHolder<TeapotRecipe>> recipeOpt = contraptionEntity.level().getRecipeManager()
                .getRecipeFor(ModRecipes.TEAPOT_RECIPE, container, contraptionEntity.level());

        if (recipeOpt.isPresent()) {
            TeapotRecipe recipe = recipeOpt.get().value();
            int count = recipe.ingredientCount();

            if (!contraptionEntity.level().isClientSide) {
                CompoundTag newNbt = nbt.copy();
                newNbt.put(TEAPOT_INPUT, input.copyWithCount(count).saveOptional(registryAccess));
                newNbt.putInt(CURRENT_TICK, 200); // INGREDIENT_TIME
                updateData(contraptionEntity, localPos, new StructureBlockInfo(localPos, info.state(), newNbt));
            }

            itemStack.shrink(count);
            return true;
        }

        sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.add_ingredient.recipe_incorrect");
        return false;
    }

    /**
     * 取出原料
     */
    private boolean removeIngredient(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos,
            CompoundTag nbt, StructureBlockInfo info) {
        int status = nbt.getInt(STATUS);
        if (status != PUT_INGREDIENT) {
            return false;
        }

        CompoundTag inputTag = nbt.getCompound(TEAPOT_INPUT);
        if (inputTag.isEmpty()) {
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            RegistryAccess registryAccess = contraptionEntity.level().registryAccess();
            ItemStack input = ItemStack.parseOptional(registryAccess, inputTag);
            ItemUtils.getItemToLivingEntity(player, input.copyAndClear());

            CompoundTag newNbt = nbt.copy();
            newNbt.remove(TEAPOT_INPUT);
            updateData(contraptionEntity, localPos, new StructureBlockInfo(localPos, info.state(), newNbt));
        }

        return true;
    }

    /**
     * 右键拿走茶壶
     */
    private boolean takeTeapot(Player player, AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        StructureBlockInfo latestInfo = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (latestInfo == null)
            return false;
        CompoundTag latestNbt = latestInfo.nbt();
        if (latestNbt == null) {
            latestNbt = new CompoundTag();
        }

        int status = latestNbt.getInt(STATUS);

        if (status == PROCESSING) {
            sendActionBarMessage(player, "tooltip.kaleidoscope_cookery.teapot.take_teapot.state_incorrect");
            return false;
        }

        if (!contraptionEntity.level().isClientSide) {
            RegistryAccess registryAccess = contraptionEntity.level().registryAccess();

            List<ItemStack> drops = getTeapotDrops(registryAccess, latestNbt);

            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    ContraptionUtil.giveItemToPlayer(player, drop);
                }
            }

            playSound(contraptionEntity, localPos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 0.6f,
                    0.8f + contraptionEntity.level().random.nextFloat() * 0.2F);

            ContraptionUtil.removeBlockFromContraption(contraptionEntity, localPos, true);
        }

        return true;
    }

    private List<ItemStack> getTeapotDrops(RegistryAccess registryAccess, CompoundTag nbt) {
        List<ItemStack> drops = new ArrayList<>();
        if (nbt == null)
            return drops;

        int status = nbt.getInt(STATUS);

        if (status == PUT_INGREDIENT || status == PROCESSING) {
            CompoundTag inputTag = nbt.getCompound(TEAPOT_INPUT);
            if (!inputTag.isEmpty()) {
                ItemStack input = ItemStack.parseOptional(registryAccess, inputTag);
                drops.add(input.copy());
            }

            ItemStack teapot = ModItems.TEAPOT.get().getDefaultInstance();
            CompoundTag tag = new CompoundTag();
            tag.putString(TEA_FLUID_ID, nbt.getString(TEA_FLUID_ID));
            tag.putInt(STATUS, status);
            BlockItem.setBlockEntityData(teapot, ModBlocks.TEAPOT_BE.get(), tag);
            drops.add(teapot);

        } else if (status == FINISHED) {
            ItemStack teapot = ModItems.TEAPOT.get().getDefaultInstance();
            CompoundTag tag = new CompoundTag();
            CompoundTag resultTag = nbt.getCompound(RESULT);
            if (!resultTag.isEmpty()) {
                tag.put(RESULT, resultTag);
            }
            tag.putInt(STATUS, status);
            tag.putString(TEA_FLUID_ID, nbt.getString(TEA_FLUID_ID));
            BlockItem.setBlockEntityData(teapot, ModBlocks.TEAPOT_BE.get(), tag);
            drops.add(teapot);

        } else {
            ItemStack teapot = ModItems.TEAPOT.get().getDefaultInstance();
            CompoundTag tag = new CompoundTag();
            tag.putInt(STATUS, status);
            tag.putString(TEA_FLUID_ID, nbt.getString(TEA_FLUID_ID));
            BlockItem.setBlockEntityData(teapot, ModBlocks.TEAPOT_BE.get(), tag);
            drops.add(teapot);
        }

        return drops;
    }

    private Component getStatusText(int status) {
        if (status == PUT_INGREDIENT) {
            return Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.put_ingredient");
        }
        if (status == PROCESSING) {
            return Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.processing");
        }
        if (status == FINISHED) {
            return Component.translatable("tooltip.kaleidoscope_cookery.teapot.statue.finished");
        }
        return Component.empty();
    }
}
