package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.config.kitchen.entity.ScarecrowConfig;
import com.github.ysbbbbbb.kaleidoscopecookery.entity.ScarecrowEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class PhantomScarecrowHandler {

    private static final int CHECK_INTERVAL = 40;
    private static final double SCARECROW_RANGE = 48.0;
    private static final int FREEZE_DURATION = 300;

    private static final Map<Phantom, PhantomState> phantomStates = new IdentityHashMap<>();

    @SubscribeEvent
    public static void onPhantomTick(EntityTickEvent.Pre event) {
        if (!ScarecrowConfig.repelPhantoms) {
            return;
        }

        if (!(event.getEntity() instanceof Phantom phantom)) {
            return;
        }

        if (!phantom.isAlive() || phantom.isRemoved()) {
            phantomStates.remove(phantom);
            return;
        }

        PhantomState state = phantomStates.computeIfAbsent(phantom, k -> new PhantomState());

        if (phantom.tickCount < state.nextCheckTick) {
            if (state.scarecrowNearby) {
                applyScarecrowRepel(phantom);
            }
            return;
        }

        AABB searchArea = phantom.getBoundingBox().inflate(SCARECROW_RANGE);
        List<ScarecrowEntity> scarecrows = phantom.level().getEntitiesOfClass(
                ScarecrowEntity.class, searchArea,
                scarecrow -> scarecrow.isAlive() && !scarecrow.isRemoved() && !scarecrow.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
        );

        state.scarecrowNearby = !scarecrows.isEmpty();

        if (state.scarecrowNearby) {
            state.nextCheckTick = phantom.tickCount + FREEZE_DURATION;
            applyScarecrowRepel(phantom);
        } else {
            state.nextCheckTick = phantom.tickCount + CHECK_INTERVAL;
        }
    }

    @SubscribeEvent
    public static void onPhantomDeath(LivingDeathEvent event) {
        if (!ScarecrowConfig.repelPhantoms) {
            return;
        }

        if (event.getEntity() instanceof Phantom phantom) {
            phantomStates.remove(phantom);
        }
    }

    private static void applyScarecrowRepel(Phantom phantom) {
        phantom.setTarget(null);
        phantom.setLastHurtByMob(null);
        phantom.setNoActionTime(FREEZE_DURATION);
    }

    private static class PhantomState {
        int nextCheckTick = 0;
        boolean scarecrowNearby = false;
    }
}