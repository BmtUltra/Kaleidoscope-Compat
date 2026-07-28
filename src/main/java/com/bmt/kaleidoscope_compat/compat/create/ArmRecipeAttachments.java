package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public final class ArmRecipeAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, KaleidoscopeCompat.MOD_ID);

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<RecipeItem.RecipeRecord>> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull Optional<RecipeItem.RecipeRecord> decode(RegistryFriendlyByteBuf buffer) {
                    return buffer.readBoolean()
                            ? Optional.of(RecipeItem.RecipeRecord.STREAM_CODEC.decode(buffer))
                            : Optional.empty();
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, Optional<RecipeItem.RecipeRecord> value) {
                    buffer.writeBoolean(value.isPresent());
                    value.ifPresent(recipe -> RecipeItem.RecipeRecord.STREAM_CODEC.encode(buffer, recipe));
                }
            };

    public static final Supplier<AttachmentType<Optional<RecipeItem.RecipeRecord>>> ARM_RECIPE =
            ATTACHMENT_TYPES.register("arm_recipe", () -> AttachmentType
                    .<Optional<RecipeItem.RecipeRecord>>builder(Optional::empty)
                    .serialize(RecipeItem.RecipeRecord.CODEC.optionalFieldOf("recipe").codec(), Optional::isPresent)
                    .sync(STREAM_CODEC)
                    .build());

    private ArmRecipeAttachments() {
    }
}
