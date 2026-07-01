package com.bmt.kaleidoscope_compat.network;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll.ComputerMenuAccessor;
import com.bmt.kaleidoscope_compat.util.PlayerSkinFetcher;
import com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.inventory.ComputerMenu;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RequestPlayerDollPayload(String playerId) implements CustomPacketPayload {

    public static final Type<RequestPlayerDollPayload> TYPE = new Type<>(
            KaleidoscopeCompat.id("request_player_doll")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPlayerDollPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull RequestPlayerDollPayload decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new RequestPlayerDollPayload(buf.readUtf(256));
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, RequestPlayerDollPayload payload) {
            buf.writeUtf(payload.playerId(), 256);
        }
    };

    public static void handle(RequestPlayerDollPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                handleRequest(serverPlayer, payload.playerId());
            }
        });
    }

    private static void handleRequest(ServerPlayer player, String playerId) {
        if (!playerId.matches("^[a-zA-Z][a-zA-Z0-9_]{2,15}$")) {
            return;
        }

        if (!(player.containerMenu instanceof ComputerMenu menu)) {
            return;
        }

        ComputerMenuAccessor accessor = (ComputerMenuAccessor) menu;
        ItemStackHandler input = accessor.getInput();
        if (!input.getStackInSlot(0).is(TagItem.COMPUTER_TOKENS)) {
            return;
        }

        PlayerSkinFetcher.fetchPlayerUuid(playerId).thenAccept(uuidOpt -> {
            if (uuidOpt.isEmpty()) {
                return;
            }

            player.server.execute(() -> {
                if (!(player.containerMenu instanceof ComputerMenu m)) {
                    return;
                }
                ComputerMenuAccessor acc = (ComputerMenuAccessor) m;
                ItemStackHandler output = acc.getOutput();

                if (!acc.getInput().getStackInSlot(0).is(TagItem.COMPUTER_TOKENS)) {
                    return;
                }

                ItemStack dollStack = new ItemStack(ModItems.CUSTOM_DOLL.get());
                CustomDollItem.setModelId(dollStack, "player_doll:" + playerId.toLowerCase());
                output.setStackInSlot(0, dollStack);
            });
        }).exceptionally(e -> null);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}