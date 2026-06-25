package com.bmt.kaleidoscope_compat.network;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.compat.create.PotArmAutomation;
import com.bmt.kaleidoscope_compat.compat.create.StockpotArmAutomation;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ArmRecipeSyncPayload(BlockPos pos, CompoundTag recipeData) implements CustomPacketPayload {

    public static final Type<ArmRecipeSyncPayload> TYPE = new Type<>(
            KaleidoscopeCompat.id("arm_recipe_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, ArmRecipeSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ArmRecipeSyncPayload decode(@NotNull FriendlyByteBuf buf) {
            BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
            boolean hasRecipe = buf.readBoolean();
            CompoundTag recipeData = hasRecipe ? buf.readNbt() : null;
            return new ArmRecipeSyncPayload(pos, recipeData);
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buf, ArmRecipeSyncPayload payload) {
            BlockPos.STREAM_CODEC.encode(buf, payload.pos);
            buf.writeBoolean(payload.recipeData != null);
            if (payload.recipeData != null) {
                buf.writeNbt(payload.recipeData);
            }
        }
    };

    public static void handle(ArmRecipeSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.player().level().getBlockEntity(payload.pos);
            RecipeItem.RecipeRecord recipe = null;
            if (payload.recipeData != null && payload.recipeData.contains("recipe")) {
                recipe = RecipeItem.RecipeRecord.CODEC
                        .parse(new Dynamic<>(NbtOps.INSTANCE, payload.recipeData.get("recipe")))
                        .getOrThrow();
            }
            if (be instanceof PotArmAutomation automation) {
                automation.kaleidoscopeCompat$setStoredRecipeClient(recipe);
            } else if (be instanceof StockpotArmAutomation automation) {
                automation.kaleidoscopeCompat$setStoredRecipeClient(recipe);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}