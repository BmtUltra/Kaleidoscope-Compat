package com.bmt.kaleidoscope_compat.mixin.vinery;

import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.BarCabinetBlockEntityRender;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BarCabinetBlockEntityRender.class)
public abstract class BarCabinetBlockEntityRenderMixin {

    @Redirect(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"), remap = false)
    private BlockState kc$modifyVineryBlockState(Block instance) {
        BlockState state = instance.defaultBlockState();
        net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(instance);
        if ("vinery".equals(key.getNamespace())) {
            for (Property<?> property : state.getProperties()) {
                if (property.getName().equals("fake_model")) {
                    state = kc$setFakeModel(state, property);
                    break;
                }
            }
        }
        return state;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState kc$setFakeModel(BlockState state, Property<T> property) {
        if (property.getValueClass() == Boolean.class) {
            return state.setValue((Property<Boolean>) property, false);
        }
        return state;
    }
}