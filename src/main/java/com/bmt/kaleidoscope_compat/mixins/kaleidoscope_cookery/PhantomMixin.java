package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.entity.ScarecrowEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Phantom.class)
public abstract class PhantomMixin extends LivingEntity {

    protected PhantomMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static final int CHECK_INTERVAL = 40;
    @Unique
    private static final double SCARECROW_RANGE = 24.0;
    @Unique
    private static final int FREEZE_DURATION = 300;
    @Unique
    private int kaleidoscopeCompat$nextCheckTick = 0;
    @Unique
    private boolean kaleidoscopeCompat$scarecrowNearby = false;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void checkScarecrowNearby(CallbackInfo ci) {
        if (!MainConfig.scarecrowRepelPhantomsValue) {
            return;
        }

        Phantom phantom = (Phantom) (Object) this;

        if (phantom.tickCount < kaleidoscopeCompat$nextCheckTick) {
            if (kaleidoscopeCompat$scarecrowNearby) {
                kaleidoscope_Compat_1_21_1_NeoForge$applyScarecrowRepel(phantom);
            }
            return;
        }

        AABB searchArea = phantom.getBoundingBox().inflate(SCARECROW_RANGE);
        List<ScarecrowEntity> scarecrows = this.level().getEntitiesOfClass(
                ScarecrowEntity.class,
                searchArea,
                scarecrow -> scarecrow.isAlive()
                        && !scarecrow.isRemoved()
                        && !scarecrow.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
        );

        kaleidoscopeCompat$scarecrowNearby = !scarecrows.isEmpty();

        if (kaleidoscopeCompat$scarecrowNearby) {
            kaleidoscopeCompat$nextCheckTick = phantom.tickCount + FREEZE_DURATION;
            kaleidoscope_Compat_1_21_1_NeoForge$applyScarecrowRepel(phantom);
        } else {
            kaleidoscopeCompat$nextCheckTick = phantom.tickCount + CHECK_INTERVAL;
        }
    }

    @Unique
    private void kaleidoscope_Compat_1_21_1_NeoForge$applyScarecrowRepel(Phantom phantom) {
        phantom.setTarget(null);
        phantom.setLastHurtByMob(null);
        phantom.setNoActionTime(FREEZE_DURATION);
    }
}