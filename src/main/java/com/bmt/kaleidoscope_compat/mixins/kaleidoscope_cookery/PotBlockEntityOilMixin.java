package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.HAS_OIL;
import static com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock.SHOW_OIL;

@Mixin(value = PotBlockEntity.class, remap = false)
public abstract class PotBlockEntityOilMixin {

    @Shadow
    private int currentTick;

    @Shadow
    private int status;

    @Inject(
            method = "onPlaceOil",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kaleidoscopeCompat$onPlaceOil(Level level, LivingEntity user, ItemStack stack,
                                               CallbackInfoReturnable<Boolean> cir) {
        PotBlockEntity pot = (PotBlockEntity) (Object) this;

        if (stack.is(TagMod.OIL)) {
            return;
        } else if (stack.is(ModItems.KITCHEN_SHOVEL.get()) && KitchenShovelItem.hasOil(stack)) {
            return;
        } else if (stack.is(ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack)) {
            return;
        }

        if (stack.is(TagUtil.Items.BOTTLE_OIL)) {
            kaleidoscopeCompat$handleBottleOil(level, user, stack, pot);
            cir.setReturnValue(true);
            return;
        }

        if (stack.is(TagUtil.Items.BUCKET_OIL)) {
            kaleidoscopeCompat$handleBucketOil(level, user, stack, pot);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void kaleidoscopeCompat$handleBottleOil(Level level, LivingEntity user, ItemStack stack, PotBlockEntity pot) {
        kaleidoscopeCompat$executePlaceOilLogic(level, user, pot);
        stack.shrink(1);

        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        ItemUtils.getItemToLivingEntity(user, bottle);
    }

    @Unique
    private void kaleidoscopeCompat$handleBucketOil(Level level, LivingEntity user, ItemStack stack, PotBlockEntity pot) {
        kaleidoscopeCompat$executePlaceOilLogic(level, user, pot);
        stack.shrink(1);

        ItemStack bucket = new ItemStack(Items.BUCKET);
        ItemUtils.getItemToLivingEntity(user, bucket);
    }

    @Unique
    private void kaleidoscopeCompat$executePlaceOilLogic(Level level, LivingEntity user, PotBlockEntity pot) {
        this.currentTick = 60 * 20;
        this.status = 0;

        BlockState state = level.getBlockState(pot.getBlockPos());
        level.setBlockAndUpdate(pot.getBlockPos(),
                state.setValue(HAS_OIL, true).setValue(SHOW_OIL, true));

        RandomSource random = level.random;
        level.playSound(user, pot.getBlockPos(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1F,
                (random.nextFloat() - random.nextFloat()) * 0.8F);

        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.SMOKE,
                    pot.getBlockPos().getX() + 0.5 + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                    pot.getBlockPos().getY() + 0.25 + random.nextDouble() / 3,
                    pot.getBlockPos().getZ() + 0.5 + random.nextDouble() / 3 * (random.nextBoolean() ? 1 : -1),
                    0, 0.05, 0);
        }
        ModTrigger.EVENT.trigger(user, com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTriggerType.PUT_OIL_IN_POT);
        pot.setChanged();
    }
}