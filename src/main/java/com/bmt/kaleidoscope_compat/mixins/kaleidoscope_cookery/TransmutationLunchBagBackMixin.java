package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
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
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@Mixin(TransmutationLunchBagItem.class)
public class TransmutationLunchBagBackMixin {

    /**
     * @author BmtUltra
     * @reason 把饭袋改回原来的样子，农业模组特有的没轻没重（
     */
    @Overwrite
    public ItemStack finishUsingItem(ItemStack bag, Level level, LivingEntity entity) {
        if (!MainConfig.transmutationLunchBagBackEnabled) {
            return ((TransmutationLunchBagItem) (Object) this).finishUsingItem(bag, level, entity);
        }

        if (!TransmutationLunchBagItem.hasItems(bag)) {
            return bag;
        }

        ItemStack food = ItemStack.EMPTY;
        List<Pair<MobEffectInstance, Float>> allEffects = new ArrayList<>();

        ItemStackHandler items = TransmutationLunchBagItem.getItems(bag);

        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stackInSlot = items.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                continue;
            }

            FoodProperties foodProperties = stackInSlot.getItem().getFoodProperties(stackInSlot, null);
            if (foodProperties != null) {
                List<Pair<MobEffectInstance, Float>> foodEffects = foodProperties.getEffects();
                allEffects.addAll(foodEffects);
                food = items.extractItem(i, 1, false);
                break;
            }

            if (stackInSlot.is(Items.POTION)) {
                List<MobEffectInstance> potionEffects = PotionUtils.getMobEffects(stackInSlot);
                for (MobEffectInstance effect : potionEffects) {
                    allEffects.add(Pair.of(effect, 1.0F));
                }
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
                    allEffects.addAll(foodEffects);
                    continue;
                }

                if (stackInSlot.is(Items.POTION)) {
                    List<MobEffectInstance> potionEffects = PotionUtils.getMobEffects(stackInSlot);
                    for (MobEffectInstance effect : potionEffects) {
                        allEffects.add(Pair.of(effect, 1.0F));
                    }
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
                for (Pair<MobEffectInstance, Float> effectPair : allEffects) {
                    if (effectPair.getSecond() <= 0.0F || level.random.nextFloat() >= effectPair.getSecond()) {
                        continue;
                    }
                    entity.addEffect(new MobEffectInstance(effectPair.getFirst()));
                }
            }

            if (entity instanceof ServerPlayer player) {
                ModTrigger.EVENT.trigger(player, ModEventTriggerType.USE_TRANSMUTATION_LUNCH_BAG);
            }

            TransmutationLunchBagItem.setItems(bag, items);
            return bag;
        }
        return bag;
    }
}