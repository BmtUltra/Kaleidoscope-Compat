package com.bmt.kaleidoscope_compat.network;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class KCPackets {

    public static void registerPackets(PayloadRegistrar registrar) {
        registrar.playToClient(
                ArmRecipeSyncPayload.TYPE,
                ArmRecipeSyncPayload.STREAM_CODEC,
                ArmRecipeSyncPayload::handle
        );
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registerPackets(registrar);
    }
}