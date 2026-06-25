package com.bmt.kaleidoscope_compat.compat.create.movement;

import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.api.event.MillstoneMatchRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.MillstoneBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.NinePart;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.output.RandomOutput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.MillstoneRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.datamap.MillstoneBindableData;
import com.github.ysbbbbbb.kaleidoscopecookery.datamap.resources.MillstoneBindableDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSounds;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.bmt.kaleidoscope_compat.compat.create.util.ContraptionNbtKeys.*;

/**
 * 石磨在动态结构上的移动行为
 */
public class MillstoneMovementBehaviour extends BaseMovementBehaviour {

    private static final int MAX_INPUT_COUNT = 8;
    private static final float DEFAULT_ROT_SPEED_TICK = 200f;
    private static final double MAX_DISTANCE_SQR = 5 * 5;

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof MillstoneBlock;
    }

    @Override
    protected boolean requiresNbt() {
        return super.requiresNbt();
    }

    @Override
    protected boolean shouldTick(MovementContext context, BlockState state, CompoundTag nbt) {
        return state.getValue(MillstoneBlock.PART) == NinePart.CENTER;
    }

    @Override
    protected void doTick(MovementContext context, BlockState state, CompoundTag nbt) {
        if (context.world.isClientSide) return;
        if (!(context.world instanceof ServerLevel serverLevel)) return;

        if (nbt == null) nbt = new CompoundTag();

        UUID entityId = nbt.hasUUID(MILLSTONE_ENTITY_ID)
                ? nbt.getUUID(MILLSTONE_ENTITY_ID) : null;

        if (entityId != null) {
            Entity entity = serverLevel.getEntity(entityId);
            if (entity instanceof Mob mob && isEntityValid(mob, context, state)) {
                // 更新生物位置
                float rot = updateEntityPosition(mob, context, state, nbt);

                // 自动输入
                tryAutoInput(serverLevel, mob, context, state, nbt);

                // 研磨逻辑
                processGrinding(context, nbt);

                // 粒子效果）
                spawnParticles(serverLevel, nbt, state, context, rot);

                // 音效
                if (serverLevel.getGameTime() % 25 == 0) {
                    float pitch = serverLevel.random.nextFloat() * 0.2f + 0.9f;
                    playSound(context, ModSounds.BLOCK_MILLSTONE.get(), SoundSource.BLOCKS, 0.5f, pitch);
                }

                // 输出自动弹出
                if (serverLevel.getGameTime() % 20 == 9) {
                    ejectOutputs(serverLevel, nbt, state, context);
                }
            } else {
                float currentRot = computeRotation(nbt, serverLevel.getGameTime());
                nbt = nbt.copy();

                Entity boundEntity = serverLevel.getEntity(entityId);
                if (boundEntity instanceof Mob boundMob) {
                    boundMob.noPhysics = false;
                }

                nbt.remove(MILLSTONE_ENTITY_ID);
                nbt.putFloat(MILLSTONE_CACHE_ROT, fixRot(currentRot));
                nbt.putFloat(MILLSTONE_LIFT_ANGLE, 0f);
                updateNbt(context, nbt);
                return;
            }
        } else {
            if (serverLevel.getGameTime() % 5 == 0) {
                Mob mob = findBindableMob(serverLevel, context, state);
                if (mob != null) {
                    nbt = bindEntity(mob, nbt, serverLevel);
                    updateNbt(context, nbt);
                    return;
                }
            }
        }

        updateNbt(context, nbt);
    }

    private boolean isEntityValid(Mob mob, MovementContext context, BlockState state) {
        if (!mob.isAlive()) return false;
        Vec3 center = getCenterGlobalPos(context, state);
        if (center == null) return false;
        if (mob.distanceToSqr(center) >= MAX_DISTANCE_SQR) return false;
        if (mob.fallDistance > 0.5f) return false;
        if (mob.isInWall()) return false;
        return !isSaddleEntityControlling(mob);
    }

    /**
     * 更新绑定实体的世界坐标位置
     * @return 当前旋转角度（度）
     */
    private float updateEntityPosition(Mob mob, MovementContext context, BlockState state, CompoundTag nbt) {
        Vec3 center = getCenterGlobalPos(context, state);
        if (center == null) return 0;

        float rot = computeRotation(nbt, context.world.getGameTime());

        Vec3 offset = Vec3.ZERO;
        if (nbt.contains(MILLSTONE_OFFSET)) {
            net.minecraft.nbt.ListTag offsetList = nbt.getList(MILLSTONE_OFFSET, net.minecraft.nbt.Tag.TAG_DOUBLE);
            if (offsetList.size() >= 3) {
                offset = new Vec3(
                        offsetList.getDouble(0),
                        offsetList.getDouble(1),
                        offsetList.getDouble(2)
                );
            }
        }

        Vec3 orbitalOffset = new Vec3(0, 0, 2)
                .add(offset)
                .yRot(rot * Mth.DEG_TO_RAD);
        Vec3 pos = orbitalOffset.add(center);

        // 对齐原版朝向计算
        mob.moveTo(pos.x, pos.y, pos.z, -rot - 90, 0);

        return rot;
    }

    /**
     * getRotation 公式：
     * Math.abs(cacheRot + gameTime * degPerTick) % 360
     */
    private float computeRotation(CompoundTag nbt, long gameTime) {
        float rotSpeedTick = nbt.contains(MILLSTONE_ROT_SPEED_TICK)
                ? nbt.getFloat(MILLSTONE_ROT_SPEED_TICK) : DEFAULT_ROT_SPEED_TICK;
        float cacheRot = nbt.contains(MILLSTONE_CACHE_ROT)
                ? nbt.getFloat(MILLSTONE_CACHE_ROT) : 0f;
        float degPerTick = 360f / Math.max(rotSpeedTick, 1);
        return Math.abs(cacheRot + gameTime * degPerTick) % 360;
    }

    /**
     * 绑定生物到石磨
     */
    private CompoundTag bindEntity(Mob mob, CompoundTag nbt, ServerLevel serverLevel) {
        CompoundTag newNbt = nbt.copy();
        newNbt.putUUID(MILLSTONE_ENTITY_ID, mob.getUUID());

        MillstoneBindableData data = MillstoneBindableDataReloadListener.INSTANCE.getOrDefault(
                mob.getType(), MillstoneBindableData.DEFAULT);
        newNbt.putFloat(MILLSTONE_ROT_SPEED_TICK, data.rotSpeedTick());
        newNbt.putFloat(MILLSTONE_LIFT_ANGLE, data.liftAngle());

        net.minecraft.nbt.ListTag offsetList = new net.minecraft.nbt.ListTag();
        offsetList.add(net.minecraft.nbt.DoubleTag.valueOf(data.offset().x));
        offsetList.add(net.minecraft.nbt.DoubleTag.valueOf(data.offset().y));
        offsetList.add(net.minecraft.nbt.DoubleTag.valueOf(data.offset().z));
        newNbt.put(MILLSTONE_OFFSET, offsetList);

        float rot = computeRotation(newNbt, serverLevel.getGameTime());
        float cacheRot = newNbt.contains(MILLSTONE_CACHE_ROT)
                ? newNbt.getFloat(MILLSTONE_CACHE_ROT) : 0f;
        newNbt.putFloat(MILLSTONE_CACHE_ROT, fixRot(cacheRot - (rot - cacheRot)));

        if (mob instanceof OwnableEntity ownable && ownable.getOwner() instanceof ServerPlayer player) {
            ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.DRIVE_THE_MILLSTONE);
        }

        return newNbt;
    }

    private Mob findBindableMob(ServerLevel serverLevel, MovementContext context, BlockState state) {
        Vec3 center = getCenterGlobalPos(context, state);
        if (center == null) return null;

        List<Mob> nearbyMobs = serverLevel.getEntitiesOfClass(Mob.class,
                new AABB(center.subtract(5, 2, 5), center.add(5, 2, 5)));

        for (Mob mob : nearbyMobs) {
            if (canBindEntity(mob) && !isSaddleEntityControlling(mob)) {
                return mob;
            }
        }
        return null;
    }

    private void tryAutoInput(ServerLevel serverLevel, Mob mob, MovementContext context,
                              BlockState state, CompoundTag nbt) {
        if (serverLevel.getGameTime() % 10 != 0) return;

        ItemStack input = readInput(nbt, serverLevel.registryAccess());
        int progress = nbt.getInt(MILLSTONE_PROGRESS);

        if (!isOutputEmpty(nbt, serverLevel.registryAccess())) return;
        if (!input.isEmpty() && progress > 0) return;

        // 1. 从绑定实体物品栏提取
        IItemHandler handler = mob.getCapability(Capabilities.ItemHandler.ENTITY);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stackInSlot = handler.getStackInSlot(i);
                if (stackInSlot.isEmpty()) continue;

                ItemStack stack = handler.extractItem(i, MAX_INPUT_COUNT, true);
                if (tryPutItem(stack, nbt, context)) {
                    handler.extractItem(i, MAX_INPUT_COUNT, false);
                    return;
                }
            }
        }

        // 2. 检查上方 ItemEntity
        BlockPos abovePos = getCenterPos(context, state).above();
        Vec3 startPos = new Vec3(abovePos.getX() - 0.3125, abovePos.getY(), abovePos.getZ() - 0.3125);
        Vec3 endPos = new Vec3(abovePos.getX() + 1.3125, abovePos.getY() + 0.5, abovePos.getZ() + 1.3125);
        AABB aabb = new AABB(startPos, endPos);
        List<ItemEntity> entities = serverLevel.getEntitiesOfClass(ItemEntity.class, aabb);

        for (ItemEntity itemEntity : entities) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) continue;

            int countCanInsert = Math.min(stack.getCount(), MAX_INPUT_COUNT);
            ItemStack stackToInsert = stack.copyWithCount(countCanInsert);
            if (tryPutItem(stackToInsert, nbt, context)) {
                stack.shrink(countCanInsert);
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
                break;
            }
        }
    }

    /**
     * 处理研磨进度
     */
    private void processGrinding(MovementContext context, CompoundTag nbt) {
        int progress = nbt.getInt(MILLSTONE_PROGRESS);
        ItemStack input = readInput(nbt, context.world.registryAccess());

        if (progress > 0 && isOutputEmpty(nbt, context.world.registryAccess())) {
            progress--;
            nbt.putInt(MILLSTONE_PROGRESS, progress);
        }

        if (progress <= 0 && !input.isEmpty() && isOutputEmpty(nbt, context.world.registryAccess())) {
            produceOutput(context, nbt, input);
        }
    }

    /**
     * 匹配配方并产出输出物品
     */
    private void produceOutput(MovementContext context, CompoundTag nbt, ItemStack input) {
        SimpleInput simpleInput = new SimpleInput(List.of(input));
        Optional<RecipeHolder<MillstoneRecipe>> recipeOpt = matchRecipe(simpleInput, context);

        recipeOpt.ifPresentOrElse(recipe -> {
            NonNullList<ItemStack> outputs = NonNullList.withSize(4, ItemStack.EMPTY);
            for (int i = 0; i < input.getCount(); i++) {
                for (RandomOutput output : recipe.value().results()) {
                    if (output.isEmpty()) continue;
                    if (Math.random() >= output.chance()) continue;

                    ItemStack out = output.stack().copy();
                    for (int j = 0; j < outputs.size(); j++) {
                        if (outputs.get(j).isEmpty()) {
                            outputs.set(j, out);
                            break;
                        } else if (ItemStack.isSameItemSameComponents(outputs.get(j), out)
                                && outputs.get(j).getCount() + out.getCount() <= out.getMaxStackSize()) {
                            outputs.get(j).grow(out.getCount());
                            break;
                        }
                    }
                }
            }
            saveOutputs(nbt, outputs, context.world.registryAccess());
            nbt.putInt(MILLSTONE_PROGRESS, 0);
            saveInput(nbt, ItemStack.EMPTY, context.world.registryAccess());
        }, () -> {
            NonNullList<ItemStack> outputs = NonNullList.withSize(4, ItemStack.EMPTY);
            outputs.set(0, input.copy());
            saveOutputs(nbt, outputs, context.world.registryAccess());
            saveInput(nbt, ItemStack.EMPTY, context.world.registryAccess());
            nbt.putInt(MILLSTONE_PROGRESS, 0);
        });
    }

    private void spawnParticles(ServerLevel serverLevel, CompoundTag nbt, BlockState state,
                                MovementContext context, float rot) {
        if (serverLevel.getGameTime() % 5 != 2) return;

        ItemStack item;
        if (!isOutputEmpty(nbt, serverLevel.registryAccess())) {
            NonNullList<ItemStack> outputs = readOutputs(nbt, serverLevel.registryAccess());
            item = outputs.getFirst().getItem() == Items.AIR ? ItemStack.EMPTY : outputs.getFirst();
        } else {
            item = readInput(nbt, serverLevel.registryAccess());
        }

        if (item.isEmpty()) return;

        Vec3 center = getCenterGlobalPos(context, state);
        if (center == null) return;

        Vec3 particlePos = new Vec3(0, 1, 1)
                .yRot(rot * Mth.DEG_TO_RAD)
                .add(center);

        if (item.getItem() instanceof BlockItem blockItem) {
            BlockState block = blockItem.getBlock().defaultBlockState();
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, block),
                    particlePos.x, particlePos.y, particlePos.z,
                    5, 0.1, 0.1, 0.1, 0.05);
        } else {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, item),
                    particlePos.x, particlePos.y, particlePos.z,
                    5, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private void ejectOutputs(ServerLevel serverLevel, CompoundTag nbt, BlockState state, MovementContext context) {
        if (isOutputEmpty(nbt, serverLevel.registryAccess())) return;

        NonNullList<ItemStack> outputs = readOutputs(nbt, serverLevel.registryAccess());
        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        Vec3 center = getCenterGlobalPos(context, state);
        if (center == null) return;

        BlockPos outputPos = BlockPos.containing(center).relative(direction);

        for (ItemStack outputStack : outputs) {
            if (outputStack.isEmpty()) continue;
            ItemEntity entity = new ItemEntity(serverLevel,
                    outputPos.getX() + 0.5, outputPos.getY(), outputPos.getZ() + 0.5,
                    outputStack, 0, 0, 0);
            entity.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(entity);
        }

        saveOutputs(nbt, NonNullList.withSize(4, ItemStack.EMPTY), serverLevel.registryAccess());
        nbt.putInt(MILLSTONE_PROGRESS, 0);
    }

    private Optional<RecipeHolder<MillstoneRecipe>> matchRecipe(SimpleInput input, MovementContext context) {
        // 允许其他 mod 覆盖配方
        MillstoneMatchRecipeEvent.Pre preEvent = new MillstoneMatchRecipeEvent.Pre(context.world, null, input);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(preEvent);
        if (preEvent.getOutput() != null) {
            return Optional.of(preEvent.getOutput());
        }

        // 查找原版配方
        Optional<RecipeHolder<MillstoneRecipe>> recipeOpt = context.world.getRecipeManager()
                .getRecipeFor(ModRecipes.MILLSTONE_RECIPE, input, context.world);

        // 获取原始配方结果（如果没有则使用空配方）
        RecipeHolder<MillstoneRecipe> rawOutput = recipeOpt.orElseGet(MillstoneRecipeSerializer::getEmptyRecipe);

        // 允许 Create 兼容模块提供 Create 研磨配方
        MillstoneMatchRecipeEvent.Post postEvent = new MillstoneMatchRecipeEvent.Post(
                context.world, null, input, rawOutput);
        NeoForge.EVENT_BUS.post(postEvent);

        if (postEvent.getOutput() != null) {
            return Optional.of(postEvent.getOutput());
        }

        return recipeOpt;
    }

    private boolean tryPutItem(ItemStack stack, CompoundTag nbt, MovementContext context) {
        if (!isOutputEmpty(nbt, context.world.registryAccess())) return false;

        int progress = nbt.getInt(MILLSTONE_PROGRESS);
        ItemStack input = readInput(nbt, context.world.registryAccess());
        if (progress > 0 && !input.isEmpty()) return false;

        SimpleInput simpleInput = new SimpleInput(List.of(stack));
        if (matchRecipe(simpleInput, context).isEmpty()) return false;

        float rotSpeedTick = nbt.contains(MILLSTONE_ROT_SPEED_TICK)
                ? nbt.getFloat(MILLSTONE_ROT_SPEED_TICK) : DEFAULT_ROT_SPEED_TICK;
        nbt.putInt(MILLSTONE_PROGRESS, Math.max(Math.round(rotSpeedTick), 1));
        saveInput(nbt, stack.split(MAX_INPUT_COUNT), context.world.registryAccess());

        playSound(context, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.8f,
                context.world.random.nextFloat() * 0.2f + 0.9f);
        return true;
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

    private Vec3 getCenterGlobalPos(MovementContext context, BlockState state) {
        BlockPos centerPos = getCenterPos(context, state);
        if (context.contraption.entity == null) {
            return Vec3.atBottomCenterOf(centerPos);
        }
        return context.contraption.entity.toGlobalVector(
                Vec3.atBottomCenterOf(centerPos), 0.0f);
    }

    private BlockPos getCenterPos(MovementContext context, BlockState state) {
        NinePart part = state.getValue(MillstoneBlock.PART);
        return context.localPos.subtract(new Vec3i(part.getPosX(), 0, part.getPosY()));
    }

    private ItemStack readInput(CompoundTag nbt, net.minecraft.core.RegistryAccess ra) {
        return nbt.contains(MILLSTONE_INPUT)
                ? ItemStack.parseOptional(ra, nbt.getCompound(MILLSTONE_INPUT))
                : ItemStack.EMPTY;
    }

    private void saveInput(CompoundTag nbt, ItemStack input, net.minecraft.core.RegistryAccess ra) {
        nbt.put(MILLSTONE_INPUT, input.saveOptional(ra));
    }

    private NonNullList<ItemStack> readOutputs(CompoundTag nbt, net.minecraft.core.RegistryAccess ra) {
        NonNullList<ItemStack> outputs = NonNullList.withSize(4, ItemStack.EMPTY);
        if (nbt.contains(MILLSTONE_OUTPUTS)) {
            net.minecraft.world.ContainerHelper.loadAllItems(
                    nbt.getCompound(MILLSTONE_OUTPUTS), outputs, ra);
        }
        return outputs;
    }

    private void saveOutputs(CompoundTag nbt, NonNullList<ItemStack> outputs,
                             net.minecraft.core.RegistryAccess ra) {
        nbt.put(MILLSTONE_OUTPUTS,
                net.minecraft.world.ContainerHelper.saveAllItems(new CompoundTag(), outputs, ra));
    }

    private boolean isOutputEmpty(CompoundTag nbt, net.minecraft.core.RegistryAccess ra) {
        NonNullList<ItemStack> outputs = readOutputs(nbt, ra);
        for (ItemStack stack : outputs) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    private float fixRot(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0f;
        return Math.abs(value) % 360;
    }
}
