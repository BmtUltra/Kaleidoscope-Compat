package com.bmt.kaleidoscope_compat.compat.create.interaction;

import com.bmt.kaleidoscope_compat.compat.create.util.ContraptionUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.api.event.MillstoneMatchRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.MillstoneBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.NinePart;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.MillstoneRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.List;
import java.util.Optional;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;

/**
 * 石磨在动态结构上的交互行为
 */
public class MillstoneMovingInteraction extends BaseMovingInteraction {

    private static final int MAX_INPUT_COUNT = 8;
    private static final float DEFAULT_ROT_SPEED_TICK = 200f;

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) return false;

        StructureBlockInfo centerInfo = findCenterInfo(contraptionEntity, localPos);
        if (centerInfo == null) return false;

        CompoundTag nbt = getOrCreateNbt(centerInfo);
        RegistryAccess registryAccess = contraptionEntity.level().registryAccess();

        ItemStack input = readInput(nbt, registryAccess);
        NonNullList<ItemStack> outputs = readOutputs(nbt, registryAccess);
        int progress = nbt.getInt(MILLSTONE_PROGRESS);
        ItemStack itemInHand = player.getItemInHand(activeHand);

        // 1. 空手 + Shift → 取出未研磨的输入物品
        if (itemInHand.isEmpty() && player.isSecondaryUseActive()) {
            if (isOutputEmpty(outputs) && progress <= 0 && !input.isEmpty()) {
                if (!contraptionEntity.level().isClientSide()) {
                    ContraptionUtil.giveItemToPlayer(player, input.copy());
                    reset(centerInfo, contraptionEntity, registryAccess);
                }
                playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS, 1.0F, 1.2F);
                return true;
            }
            return false;
        }

        // 2. 手持物品 → 放入输入
        return putItem(contraptionEntity, centerInfo, itemInHand, registryAccess);
    }

    /**
     * 放入物品到石磨
     */
    private boolean putItem(AbstractContraptionEntity contraptionEntity,
                            StructureBlockInfo centerInfo, ItemStack itemInHand, RegistryAccess registryAccess) {
        CompoundTag nbt = getOrCreateNbt(centerInfo);

        if (!isOutputEmpty(readOutputs(nbt, registryAccess))) {
            return false;
        }

        int progress = nbt.getInt(MILLSTONE_PROGRESS);
        ItemStack input = readInput(nbt, registryAccess);

        // 正在研磨时不能放入
        if (progress > 0 && !input.isEmpty()) {
            return false;
        }

        // 检查是否有匹配的配方
        int insertCount = Math.min(itemInHand.getCount(), MAX_INPUT_COUNT);
        ItemStack toInsert = itemInHand.copyWithCount(insertCount);
        SimpleInput simpleInput = new SimpleInput(List.of(toInsert));

        // 使用带事件的配方匹配，支持 Create 兼容
        Optional<RecipeHolder<MillstoneRecipe>> recipeOpt = matchRecipeWithEvents(simpleInput, contraptionEntity);

        if (recipeOpt.isEmpty()) {
            return false;
        }

        // 只在服务端处理物品和NBT
        if (!contraptionEntity.level().isClientSide()) {
            // 放入物品
            ItemStack actualInput = itemInHand.split(insertCount);

            CompoundTag newNbt = nbt.copy();
            newNbt.putInt(MILLSTONE_PROGRESS, Math.max(Math.round(
                    newNbt.contains(MILLSTONE_ROT_SPEED_TICK)
                            ? newNbt.getFloat(MILLSTONE_ROT_SPEED_TICK)
                            : DEFAULT_ROT_SPEED_TICK), 1));
            saveInput(newNbt, actualInput, registryAccess);

            BlockPos centerPos = findCenterPos(contraptionEntity, centerInfo);
            updateData(contraptionEntity, centerPos,
                    new StructureBlockInfo(centerInfo.pos(), centerInfo.state(), newNbt));

        }

        playSound(contraptionEntity, centerInfo.pos(), SoundEvents.STONE_HIT,
                SoundSource.BLOCKS, 0.8F,
                contraptionEntity.level().random.nextFloat() * 0.2f + 0.9f);
        return true;
    }

    private Optional<RecipeHolder<MillstoneRecipe>> matchRecipeWithEvents(SimpleInput input, AbstractContraptionEntity contraptionEntity) {
        // 允许其他 mod 覆盖配方
        MillstoneMatchRecipeEvent.Pre preEvent = new MillstoneMatchRecipeEvent.Pre(contraptionEntity.level(), null, input);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(preEvent);
        if (preEvent.getOutput() != null) {
            return Optional.of(preEvent.getOutput());
        }

        // 查找原版配方
        Optional<RecipeHolder<MillstoneRecipe>> recipeOpt = contraptionEntity.level().getRecipeManager()
                .getRecipeFor(ModRecipes.MILLSTONE_RECIPE, input, contraptionEntity.level());

        // 获取原始配方结果（如果没有则使用空配方）
        RecipeHolder<MillstoneRecipe> rawOutput = recipeOpt.orElseGet(MillstoneRecipeSerializer::getEmptyRecipe);

        // 允许 Create 兼容模块提供 Create 研磨配方
        MillstoneMatchRecipeEvent.Post postEvent = new MillstoneMatchRecipeEvent.Post(
                contraptionEntity.level(), null, input, rawOutput);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(postEvent);

        if (postEvent.getOutput() != null) {
            return Optional.of(postEvent.getOutput());
        }

        return recipeOpt;
    }


    /**
     * 处理实体碰撞石磨时的绑定逻辑
     */
    @Override
    public void handleEntityCollision(Entity entity, BlockPos localPos,
                                      AbstractContraptionEntity contraptionEntity) {
        if (!(entity instanceof Mob mob)) return;
        if (contraptionEntity.level().isClientSide()) return;
        if (contraptionEntity.level().getGameTime() % 5 != 4) return;

        StructureBlockInfo centerInfo = findCenterInfo(contraptionEntity, localPos);
        if (centerInfo == null) return;

        CompoundTag nbt = getOrCreateNbt(centerInfo);

        // 已有绑定实体时不处理
        if (nbt.hasUUID(MILLSTONE_ENTITY_ID)) return;

        // 检查实体是否可以绑定
        if (!canBindEntity(mob)) return;

        // 绑定实体
        CompoundTag newNbt = nbt.copy();
        newNbt.putUUID(MILLSTONE_ENTITY_ID, mob.getUUID());
        newNbt.putFloat(MILLSTONE_ROT_SPEED_TICK, DEFAULT_ROT_SPEED_TICK);

        BlockPos centerPos = findCenterPos(contraptionEntity, centerInfo);
        updateData(contraptionEntity, centerPos,
                new StructureBlockInfo(centerInfo.pos(), centerInfo.state(), newNbt));

        // 给予成就
        if (mob instanceof OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer player) {
            ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.DRIVE_THE_MILLSTONE);
        }
    }

    private boolean canBindEntity(Mob mob) {
        if (!mob.getType().is(TagMod.MILLSTONE_BINDABLE)) return false;
        if (mob.getVehicle() != null) return false;
        if (mob.isBaby()) return false;
        if (isSaddleEntityControlling(mob)) return false;

        return switch (mob) {
            case AbstractHorse horse -> horse.isTamed();
            case TamableAnimal tamable -> tamable.isTame();
            case OwnableEntity ownable -> ownable.getOwnerUUID() != null;
            default -> true;
        };

    }

    private boolean isSaddleEntityControlling(Mob mob) {
        if (!(mob instanceof Saddleable saddleable)) return false;
        return saddleable.isSaddled() && mob.getControllingPassenger() != null;
    }

    /**
     * 根据点击的任意 9 格位置找到 CENTER 的 StructureBlockInfo
     */
    private StructureBlockInfo findCenterInfo(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof MillstoneBlock)) return null;

        NinePart part = info.state().getValue(MillstoneBlock.PART);
        if (part == NinePart.CENTER) return info;

        BlockPos centerPos = localPos.subtract(new Vec3i(part.getPosX(), 0, part.getPosY()));
        return contraptionEntity.getContraption().getBlocks().get(centerPos);
    }

    private BlockPos findCenterPos(AbstractContraptionEntity contraptionEntity, StructureBlockInfo centerInfo) {
        NinePart part = centerInfo.state().getValue(MillstoneBlock.PART);
        return centerInfo.pos().subtract(new Vec3i(part.getPosX(), 0, part.getPosY()));
    }

    /**
     * 重置石磨的所有数据
     */
    private void reset(StructureBlockInfo centerInfo, AbstractContraptionEntity contraptionEntity,
                       RegistryAccess registryAccess) {
        BlockPos centerPos = findCenterPos(contraptionEntity, centerInfo);
        CompoundTag newNbt = new CompoundTag();
        saveInput(newNbt, ItemStack.EMPTY, registryAccess);
        saveOutputs(newNbt, NonNullList.withSize(4, ItemStack.EMPTY), registryAccess);
        newNbt.putInt(MILLSTONE_PROGRESS, 0);
        updateData(contraptionEntity, centerPos,
                new StructureBlockInfo(centerInfo.pos(), centerInfo.state(), newNbt));
    }

    private ItemStack readInput(CompoundTag nbt, RegistryAccess registryAccess) {
        if (nbt.contains(MILLSTONE_INPUT)) {
            return ItemStack.parseOptional(registryAccess, nbt.getCompound(MILLSTONE_INPUT));
        }
        return ItemStack.EMPTY;
    }

    private void saveInput(CompoundTag nbt, ItemStack input, RegistryAccess registryAccess) {
        nbt.put(MILLSTONE_INPUT, input.saveOptional(registryAccess));
    }

    private NonNullList<ItemStack> readOutputs(CompoundTag nbt, RegistryAccess registryAccess) {
        NonNullList<ItemStack> outputs = NonNullList.withSize(4, ItemStack.EMPTY);
        if (nbt.contains(MILLSTONE_OUTPUTS)) {
            ContainerHelper.loadAllItems(nbt.getCompound(MILLSTONE_OUTPUTS), outputs, registryAccess);
        }
        return outputs;
    }

    private void saveOutputs(CompoundTag nbt, NonNullList<ItemStack> outputs, RegistryAccess registryAccess) {
        nbt.put(MILLSTONE_OUTPUTS, ContainerHelper.saveAllItems(new CompoundTag(), outputs, registryAccess));
    }

    private boolean isOutputEmpty(NonNullList<ItemStack> outputs) {
        for (ItemStack stack : outputs) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }
}
