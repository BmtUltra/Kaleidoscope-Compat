package com.bmt.kaleidoscope_compat.network;


import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.mixins.create.accessor.ContraptionAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 动态结构方块变更包
 */
public record ContraptionBlockChangePayload(
        int contraptionEntityId,
        BlockPos localPos,
        BlockState state,
        @Nullable CompoundTag nbt,
        @Nullable AABB updatedBounds
) implements CustomPacketPayload {

    public static final Type<ContraptionBlockChangePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(KaleidoscopeCompat.MOD_ID, "contraption_block_change")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionBlockChangePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ContraptionBlockChangePayload decode(@NotNull RegistryFriendlyByteBuf buf) {
            int id = ByteBufCodecs.VAR_INT.decode(buf);
            BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
            BlockState state = blockStateCodec().decode(buf);
            CompoundTag nbt = buf.readBoolean() ? nbtCodec().decode(buf) : null;
            AABB bounds = buf.readBoolean() ? aabbCodec().decode(buf) : null;
            return new ContraptionBlockChangePayload(id, pos, state, nbt, bounds);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, ContraptionBlockChangePayload payload) {
            ByteBufCodecs.VAR_INT.encode(buf, payload.contraptionEntityId);
            BlockPos.STREAM_CODEC.encode(buf, payload.localPos);
            blockStateCodec().encode(buf, payload.state);
            if (payload.nbt != null) {
                buf.writeBoolean(true);
                nbtCodec().encode(buf, payload.nbt);
            } else {
                buf.writeBoolean(false);
            }
            if (payload.updatedBounds != null) {
                buf.writeBoolean(true);
                aabbCodec().encode(buf, payload.updatedBounds);
            } else {
                buf.writeBoolean(false);
            }
        }
    };

    private static StreamCodec<RegistryFriendlyByteBuf, CompoundTag> nbtCodec() {
        return new StreamCodec<>() {
            @Override
            public CompoundTag decode(@NotNull RegistryFriendlyByteBuf buf) {
                return buf.readNbt();
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull CompoundTag tag) {
                buf.writeNbt(tag);
            }
        };
    }

    private static StreamCodec<RegistryFriendlyByteBuf, BlockState> blockStateCodec() {
        return new StreamCodec<>() {
            @Override
            public @NotNull BlockState decode(@NotNull RegistryFriendlyByteBuf buf) {
                CompoundTag tag = buf.readNbt();
                var level = Minecraft.getInstance().level;
                if (level == null) return Blocks.AIR.defaultBlockState();
                if (tag != null) {
                    return NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag);
                }
                return Blocks.AIR.defaultBlockState();
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull BlockState state) {
                CompoundTag tag = NbtUtils.writeBlockState(state);
                buf.writeNbt(tag);
            }
        };
    }

    private static StreamCodec<RegistryFriendlyByteBuf, AABB> aabbCodec() {
        return StreamCodec.composite(
                ByteBufCodecs.DOUBLE, b -> b.minX,
                ByteBufCodecs.DOUBLE, b -> b.minY,
                ByteBufCodecs.DOUBLE, b -> b.minZ,
                ByteBufCodecs.DOUBLE, b -> b.maxX,
                ByteBufCodecs.DOUBLE, b -> b.maxY,
                ByteBufCodecs.DOUBLE, b -> b.maxZ,
                AABB::new
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理：更新本地 Contraption 的方块数据
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;

            var entity = level.getEntity(contraptionEntityId);
            if (!(entity instanceof AbstractContraptionEntity contraptionEntity)) return;

            Contraption contraption = contraptionEntity.getContraption();
            if (contraption == null) return;

            var existingInfo = contraption.getBlocks().get(localPos);
            ChangeType changeType = determineChangeType(existingInfo);

            if (changeType == ChangeType.REMOVE) {
                removeBlock(contraption);
            } else {
                updateBlock(contraption, existingInfo, changeType, level);
            }

            updateBounds(contraption);
            handleClientContraptionUpdate(contraption, existingInfo, changeType, level);
        }).exceptionally(e -> null);
    }

    private enum ChangeType {
        REMOVE,      // 方块被移除
        NEW_BLOCK,   // 新方块
        STATE_CHANGE, // 方块状态变化
        NBT_ONLY     // 仅 NBT 变化
    }

    private ChangeType determineChangeType(StructureTemplate.StructureBlockInfo existingInfo) {
        if (state.is(Blocks.AIR)) return ChangeType.REMOVE;
        if (existingInfo == null || existingInfo.state().isAir()) return ChangeType.NEW_BLOCK;
        if (!existingInfo.state().equals(state)) return ChangeType.STATE_CHANGE;
        if (nbt != null) return ChangeType.NBT_ONLY;
        return ChangeType.NBT_ONLY;
    }

    private void removeBlock(Contraption contraption) {
        contraption.getBlocks().remove(localPos);
        contraption.getInteractors().remove(localPos);
        contraption.getActors().removeIf(actor -> actor.getLeft().pos().equals(localPos));
        ((ContraptionAccessor) contraption).getUpdateTags().remove(localPos);
    }

    private void updateBlock(Contraption contraption, StructureTemplate.StructureBlockInfo existingInfo,
                             ChangeType changeType, net.minecraft.world.level.Level level) {
        StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(localPos, state, nbt);
        contraption.getBlocks().put(localPos, newInfo);

        if (nbt != null) {
            ((ContraptionAccessor) contraption).getUpdateTags().put(localPos, nbt.copy());
        }

        updateInteractor(contraption);
        updateActor(contraption, newInfo, changeType, level);
    }

    private void updateInteractor(Contraption contraption) {
        MovingInteractionBehaviour interactionBehaviour = MovingInteractionBehaviour.REGISTRY.get(state);
        if (interactionBehaviour != null) {
            contraption.getInteractors().put(localPos, interactionBehaviour);
        }
    }

    private void updateActor(Contraption contraption, StructureTemplate.StructureBlockInfo newInfo,
                             ChangeType changeType, net.minecraft.world.level.Level level) {
        var actors = contraption.getActors();
        
        MutablePair<StructureTemplate.StructureBlockInfo, ?> existingActor = null;
        for (var actor : actors) {
            if (actor.getLeft().pos().equals(localPos)) {
                existingActor = actor;
                break;
            }
        }

        if (changeType == ChangeType.NEW_BLOCK && existingActor == null) {
            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(state);
            if (movementBehaviour != null) {
                MovementContext ctx = new MovementContext(level, newInfo, contraption);
                actors.add(MutablePair.of(newInfo, ctx));
                return;
            }
        }

        if (existingActor != null) {
            existingActor.setLeft(newInfo);
        }
    }

    private void updateBounds(Contraption contraption) {
        if (updatedBounds == null) return;

        var clientContraptionRef = ((ContraptionAccessor) contraption).getClientContraption();
        var currentClientContraption = clientContraptionRef.getAcquire();
        boolean boundsChangedY = currentClientContraption != null &&
                (updatedBounds.minY < contraption.bounds.minY || updatedBounds.maxY > contraption.bounds.maxY);

        contraption.bounds = updatedBounds;

        if (boundsChangedY) {
            clientContraptionRef.set(null);
        }
    }

    private void handleClientContraptionUpdate(Contraption contraption, StructureTemplate.StructureBlockInfo existingInfo,
                                               ChangeType changeType, net.minecraft.world.level.Level level) {
        boolean needsRebuild = changeType == ChangeType.REMOVE || changeType == ChangeType.NEW_BLOCK;
        boolean nbtOnlyChange = changeType == ChangeType.NBT_ONLY;

        if (changeType == ChangeType.STATE_CHANGE && existingInfo != null) {
            if (existingInfo.state().getBlock() instanceof PotBlock && isOnlyShowOilChanged(existingInfo.state())) {
                nbtOnlyChange = true;
            } else {
                needsRebuild = true;
            }
        }

        if (needsRebuild) {
            contraption.resetClientContraption();
            contraption.invalidateColliders();
        } else if (nbtOnlyChange) {
            updateBlockEntityNbt(contraption, level);
            contraption.invalidateClientContraptionChildren();
        }
    }

    private boolean isOnlyShowOilChanged(BlockState existingState) {
        for (var property : existingState.getProperties()) {
            if (property == PotBlock.SHOW_OIL) continue;
            if (!existingState.getValue(property).equals(state.getValue(property))) {
                return false;
            }
        }
        return nbt != null;
    }

    private void updateBlockEntityNbt(Contraption contraption, net.minecraft.world.level.Level level) {
        var clientContraptionRef = ((ContraptionAccessor) contraption).getClientContraption();
        var currentClientContraption = clientContraptionRef.getAcquire();
        if (currentClientContraption == null) return;

        var blockEntityClient = currentClientContraption.getBlockEntity(localPos);
        if (blockEntityClient != null && nbt != null) {
            blockEntityClient.loadWithComponents(nbt, level.registryAccess());
        }
    }
}
