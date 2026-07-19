package com.bmt.kaleidoscope_compat.mixins.vinery;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.satisfy.vinery.client.render.block.storage.BigBottleRenderer;
import net.satisfy.vinery.client.render.block.storage.FourBottleRenderer;
import net.satisfy.vinery.client.render.block.storage.NineBottleRenderer;
import net.satisfy.vinery.client.render.block.storage.WineBoxRenderer;
import net.satisfy.vinery.core.block.WineBottleBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {BigBottleRenderer.class, FourBottleRenderer.class, NineBottleRenderer.class, WineBoxRenderer.class})
public class WineCompatRendererMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
            )
    )
    private Object vkbc$redirectSetValue(BlockState state, Property<?> property, Comparable<?> value) {
        if (property == WineBottleBlock.FAKE_MODEL && !state.hasProperty(property)) {
            return state;
        }
        return state.setValue(WineBottleBlock.FAKE_MODEL, false);
    }
}
