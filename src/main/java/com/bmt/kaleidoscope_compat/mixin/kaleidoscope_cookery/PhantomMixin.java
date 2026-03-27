package com.bmt.kaleidoscope_compat.mixin.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.KCConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.entity.ScarecrowEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Phantom.class)
public abstract class PhantomMixin extends LivingEntity {

    protected PhantomMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void checkScarecrowNearby(CallbackInfo ci) {
        if (!KCConfig.scarecrowRepelPhantoms) {
            return;
        }

        Phantom phantom = (Phantom) (Object) this;

        boolean scarecrowNearby = this.level().getEntitiesOfClass(
                ScarecrowEntity.class,
                phantom.getBoundingBox().inflate(24.0),
                scarecrow -> scarecrow.isAlive() && !scarecrow.isRemoved()
        ).stream().anyMatch(scarecrow -> !scarecrow.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty());

        if (scarecrowNearby) {
            phantom.setTarget(null);

            if (phantom.getTarget() != null) {
                phantom.setLastHurtByMob(null);
            }
            phantom.setNoActionTime(300);
        }
    }
}