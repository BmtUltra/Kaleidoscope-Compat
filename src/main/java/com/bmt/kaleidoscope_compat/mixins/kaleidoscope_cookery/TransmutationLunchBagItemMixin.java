package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TransmutationLunchBagItem.class)
public abstract class TransmutationLunchBagItemMixin {

    @Inject(
            method = "canAdd(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true, remap = false
    )
    private static void checkBlacklist(ItemStack food, CallbackInfoReturnable<Boolean> cir) {
        if (food.is(TagUtil.Items.LUNCH_BAG_BLACKLIST)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void kaleidoscopeCompat$finishUsingItem(ItemStack bag, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (!ForgeConfig.LUNCH_BAG_BACK_BEHAVIOR_ENABLED.get()) {
            return;
        }

        if (!TransmutationLunchBagItem.hasItems(bag)) {
            cir.setReturnValue(bag);
            return;
        }

        ItemStack food = ItemStack.EMPTY;
        List<List<Pair<MobEffectInstance, Float>>> allEffects = new ArrayList<>();

        ItemStackHandler items = TransmutationLunchBagItem.getItems(bag);

        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stackInSlot = items.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                continue;
            }

            FoodProperties foodProperties = stackInSlot.getItem().getFoodProperties(stackInSlot, null);
            if (foodProperties != null) {
                List<Pair<MobEffectInstance, Float>> foodEffects = foodProperties.getEffects();
                allEffects.add(foodEffects);
                food = items.extractItem(i, 1, false);
                break;
            }

            if (stackInSlot.is(Items.POTION)) {
                List<Pair<MobEffectInstance, Float>> potionEffects = Lists.newArrayList();
                PotionUtils.getMobEffects(stackInSlot).forEach(e -> potionEffects.add(Pair.of(e, 1F)));
                allEffects.add(potionEffects);
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

                FoodProperties foodProperties = stackInSlot.getItem().getFoodProperties(stackInSlot, null);
                if (foodProperties != null) {
                    List<Pair<MobEffectInstance, Float>> foodEffects = foodProperties.getEffects();
                    allEffects.add(foodEffects);
                    continue;
                }

                if (stackInSlot.is(Items.POTION)) {
                    List<Pair<MobEffectInstance, Float>> potionEffects = Lists.newArrayList();
                    PotionUtils.getMobEffects(stackInSlot).forEach(e -> potionEffects.add(Pair.of(e, 1F)));
                    allEffects.add(potionEffects);
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
                for (List<Pair<MobEffectInstance, Float>> effectList : allEffects) {
                    for (Pair<MobEffectInstance, Float> effect : effectList) {
                        if (effect.getSecond() <= 0.0F || level.random.nextFloat() >= effect.getSecond()) {
                            continue;
                        }
                        entity.addEffect(new MobEffectInstance(effect.getFirst()));
                    }
                }
            }

            if (entity instanceof ServerPlayer player) {
                ModTrigger.EVENT.trigger(player, ModEventTriggerType.USE_TRANSMUTATION_LUNCH_BAG);
            }

            TransmutationLunchBagItem.setItems(bag, items);
            cir.setReturnValue(bag);
        } else {
            cir.setReturnValue(bag);
        }
    }
}