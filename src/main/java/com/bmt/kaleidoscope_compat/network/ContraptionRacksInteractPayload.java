package com.bmt.kaleidoscope_compat.network;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.interaction.KitchenwareRacksMovingInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 厨具架交互侧网络包
 */
public record ContraptionRacksInteractPayload(int contraptionEntityId, BlockPos localPos, boolean isLeft)
        implements CustomPacketPayload {

    public static final Type<ContraptionRacksInteractPayload> TYPE = new Type<>(
            KaleidoscopeCompat.id("contraption_racks_interact")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionRacksInteractPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ContraptionRacksInteractPayload decode(@NotNull RegistryFriendlyByteBuf buf) {
            int id = buf.readInt();
            BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
            boolean isLeft = buf.readBoolean();
            return new ContraptionRacksInteractPayload(id, pos, isLeft);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, ContraptionRacksInteractPayload payload) {
            buf.writeInt(payload.contraptionEntityId);
            BlockPos.STREAM_CODEC.encode(buf, payload.localPos);
            buf.writeBoolean(payload.isLeft);
        }
    };

    /**
     * 客户端发送
     */
    public static void sendToServer(int contraptionEntityId, BlockPos localPos, boolean isLeft) {
        PacketDistributor.sendToServer(new ContraptionRacksInteractPayload(contraptionEntityId, localPos, isLeft));
    }

    /**
     * 服务端处理：将 isLeft 存入缓存，供后续 handlePlayerInteraction 读取
     */
    public static void handle(ContraptionRacksInteractPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                KitchenwareRacksMovingInteraction.cacheSide(serverPlayer, payload.isLeft);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
