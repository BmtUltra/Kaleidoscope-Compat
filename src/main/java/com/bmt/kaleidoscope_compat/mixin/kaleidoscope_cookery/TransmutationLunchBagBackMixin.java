package com.bmt.kaleidoscope_compat.mixin.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TransmutationLunchBagItem.class)
public abstract class TransmutationLunchBagBackMixin {

    @Inject(
            method = "finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void kaleidoscopeCompat$finishUsingItem(ItemStack bag, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (!MainConfig.transmutationLunchBagBackEnabled) {
            return;
        }

        if (!TransmutationLunchBagItem.hasItems(bag)) {
            cir.setReturnValue(bag);
            return;
        }

        ItemStack food = ItemStack.EMPTY;
        List<FoodProperties.PossibleEffect> allEffects = new ArrayList<>();

        ItemStackHandler items = TransmutationLunchBagItem.getItems(bag);

        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stackInSlot = items.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                continue;
            }

            FoodProperties foodProperties = stackInSlot.get(DataComponents.FOOD);
            if (foodProperties != null) {
                List<FoodProperties.PossibleEffect> foodEffects = foodProperties.effects();
                allEffects.addAll(foodEffects);
                food = items.extractItem(i, 1, false);
                break;
            }

            PotionContents potionContents = stackInSlot.get(DataComponents.POTION_CONTENTS);
            if (potionContents != null) {
                potionContents.forEachEffect(effect -> {
                    allEffects.add(new FoodProperties.PossibleEffect(() -> effect, 1.0F));
                });
                food = items.extractItem(i, 1, false);
                break;
            }
        }

        if (!food.isEmpty()) {
            for (int i = 0; i < items.getSlots(); i++) {
                ItemStack stackInSlot = items.getStackInSlot(i);
                if (stackInSlot.isEmpty()) {
                    continue;
                }

                FoodProperties foodProperties = stackInSlot.get(DataComponents.FOOD);
                if (foodProperties != null) {
                    List<FoodProperties.PossibleEffect> foodEffects = foodProperties.effects();
                    allEffects.addAll(foodEffects);
                    continue;
                }

                PotionContents potionContents = stackInSlot.get(DataComponents.POTION_CONTENTS);
                if (potionContents != null) {
                    potionContents.forEachEffect(effect -> {
                        allEffects.add(new FoodProperties.PossibleEffect(() -> effect, 1.0F));
                    });
                }
            }

            ItemStack returnStack = food.finishUsingItem(level, entity);
            Item containerItem = ItemUtils.getContainerItem(food);

            if (!returnStack.isEmpty()) {
                if (!(entity instanceof ServerPlayer player) || !player.getAbilities().instabuild) {
                    ItemUtils.getItemToLivingEntity(entity, returnStack);
                }
            } else if (containerItem != Items.AIR) {
                ItemUtils.getItemToLivingEntity(entity, containerItem.getDefaultInstance());
            }

            if (!level.isClientSide) {
                for (FoodProperties.PossibleEffect effect : allEffects) {
                    if (effect.probability() <= 0.0F || level.random.nextFloat() >= effect.probability()) {
                        continue;
                    }
                    entity.addEffect(new MobEffectInstance(effect.effect()));
                }
            }

            if (entity instanceof ServerPlayer player) {
                ModTrigger.EVENT.get().trigger(player, ModEventTriggerType.USE_TRANSMUTATION_LUNCH_BAG);
            }

            TransmutationLunchBagItem.setItems(bag, items);
            cir.setReturnValue(bag);
        } else {
            cir.setReturnValue(bag);
        }
    }
}