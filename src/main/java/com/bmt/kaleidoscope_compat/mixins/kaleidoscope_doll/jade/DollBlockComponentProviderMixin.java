package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll.jade;

import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll.CustomDollBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopedoll.block.CustomDollBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.block.entity.CustomDollBlockEntity;
import com.github.ysbbbbbb.kaleidoscopedoll.compat.jade.DollBlockComponentProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(value = DollBlockComponentProvider.class, remap = false)
public abstract class DollBlockComponentProviderMixin {

    @Unique
    private static final String PLAYER_DOLL_PREFIX = "player_doll:";

    @Inject(method = "appendTooltip*", at = @At("HEAD"), cancellable = true)
    private void onAppendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig pluginConfig, CallbackInfo ci) {
        if (!(accessor.getBlock() instanceof CustomDollBlock)) return;
        
        if (accessor.getBlockEntity() instanceof CustomDollBlockEntityAccessor blockEntityAccessor) {
            String modelId = blockEntityAccessor.getModelId();
            if (modelId != null && modelId.startsWith(PLAYER_DOLL_PREFIX)) {
                tooltip.add(Component.translatable("tooltip.kaleidoscope_compat.player_doll").withStyle(ChatFormatting.AQUA));
                ci.cancel();
            }
        }
    }
}