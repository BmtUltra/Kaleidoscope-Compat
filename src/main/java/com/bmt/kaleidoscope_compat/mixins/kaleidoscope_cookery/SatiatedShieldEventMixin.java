package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.event.effect.SatiatedShieldEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.config.GeneralConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModEffects;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.ysbbbbbb.kaleidoscopecookery.config.GeneralConfig.SATIATED_SHIELD_ABSORB_EXCESS_DAMAGE;

@Mixin(value = SatiatedShieldEvent.class,remap = false)
public class SatiatedShieldEventMixin {

    @Inject(
            method = "onPlayerHurt",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void weakenSatiatedShield$onPlayerHurt(LivingDamageEvent event, CallbackInfo ci) {
        if (!MainConfig.satiatedShieldWeakenEnabled) {
            return;
        }

        float originalDamage = event.getAmount();
        int amount = Math.round(originalDamage) * 2;
        DamageSource source = event.getSource();

        if (event.getEntity() instanceof Player player && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!GeneralConfig.SATIATED_SHIELD_ABSORB_ENABLED.get()) {
                return;
            }

            if (player.getFoodData().getFoodLevel() > 0 && player.hasEffect(ModEffects.SATIATED_SHIELD.get())) {
                if (source.is(TagMod.SATIATED_SHIELD_WEAKNESS)) {
                    amount *= 2;
                }

                float exhaustionLevel = Math.max(0, amount / 4f);
                float playerFoodLevel = player.getFoodData().getFoodLevel();
                player.causeFoodExhaustion(exhaustionLevel);

                float damageReductionRatio = (float) MainConfig.satiatedShieldDamageReductionRatio;

                if (SATIATED_SHIELD_ABSORB_EXCESS_DAMAGE.get()) {
                    float reducedDamage = originalDamage * (1.0f - damageReductionRatio);
                    event.setAmount(reducedDamage);
                } else {
                    float consumedFoodLevel = exhaustionLevel / 4;

                    if (consumedFoodLevel >= playerFoodLevel) {
                        float extraDamage;
                        if (source.is(TagMod.SATIATED_SHIELD_WEAKNESS)) {
                            extraDamage = (consumedFoodLevel - playerFoodLevel) * 2;
                        } else {
                            extraDamage = (consumedFoodLevel - playerFoodLevel) * 4;
                        }

                        float baseDamage = originalDamage * (1.0f - damageReductionRatio);
                        float remainingDamage = baseDamage - extraDamage;
                        event.setAmount(Math.max(0, remainingDamage));
                    } else {
                        float reducedDamage = originalDamage * (1.0f - damageReductionRatio);
                        event.setAmount(reducedDamage);
                    }
                }
                ci.cancel();
            }
        }
    }
}