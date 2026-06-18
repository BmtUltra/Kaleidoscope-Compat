package com.bmt.kaleidoscope_compat.mixins.create.accessor;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ClientContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Contraption.class)
public interface ContraptionAccessor {
    @Accessor("updateTags")
    Map<BlockPos, CompoundTag> getUpdateTags();

    @Accessor("clientContraption")
    AtomicReference<ClientContraption> getClientContraption();
}
