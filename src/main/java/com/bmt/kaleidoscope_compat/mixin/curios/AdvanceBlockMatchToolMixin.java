package com.bmt.kaleidoscope_compat.mixin.curios;

import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceBlockMatchTool;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(AdvanceBlockMatchTool.class)
public class AdvanceBlockMatchToolMixin {

    @Unique
    private static final Logger kaleidoscopecompat_template_1_21_1$LOGGER = LoggerFactory.getLogger("KaleidoscopeCompat");

    @Shadow
    @Final
    private EquipmentSlot slot;

    @Shadow
    @Final
    private net.minecraft.advancements.critereon.ItemPredicate predicate;

    @Inject(
            method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void checkCuriosHeadSlot(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        if (this.slot != EquipmentSlot.HEAD) {
            return;
        }

        if (context.hasParam(LootContextParams.THIS_ENTITY)) {
            Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
            if (entity instanceof LivingEntity livingEntity) {
                CuriosApi.getCuriosInventory(livingEntity).flatMap(curiosHandler -> curiosHandler.getStacksHandler("head")).ifPresent(headHandler -> {
                    var stacks = headHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            var itemTags = stack.getTags();
                            kaleidoscopecompat_template_1_21_1$LOGGER.debug("Checking Curios head slot item: {}, tags: {}", stack.getItem(), itemTags.map(tag -> tag.location().toString()).toList());

                            boolean matches = this.predicate.test(stack);
                            kaleidoscopecompat_template_1_21_1$LOGGER.debug("Item {} matches predicate: {}", stack.getItem(), matches);

                            if (matches) {
                                kaleidoscopecompat_template_1_21_1$LOGGER.debug("Found matching item in Curios head slot: {}", stack.getItem());
                                cir.setReturnValue(true);
                                return;
                            }
                        }
                    }
                });
            }
        }
    }
}
