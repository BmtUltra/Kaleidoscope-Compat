package com.bmt.kaleidoscope_compat.network;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络包注册入口
 */
@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class KCPackets {

    public static void registerPackets(PayloadRegistrar registrar) {
        registrar.playToClient(
                ArmRecipeSyncPayload.TYPE,
                ArmRecipeSyncPayload.STREAM_CODEC,
                ArmRecipeSyncPayload::handle
        );
        registrar.playToClient(
                ContraptionBlockChangePayload.TYPE,
                ContraptionBlockChangePayload.STREAM_CODEC,
                ContraptionBlockChangePayload::handle
        );
        registrar.playToServer(
                ContraptionTakePayload.TYPE,
                ContraptionTakePayload.STREAM_CODEC,
                ContraptionTakePayload::handle
        );
        registrar.playToServer(
                ContraptionRacksInteractPayload.TYPE,
                ContraptionRacksInteractPayload.STREAM_CODEC,
                ContraptionRacksInteractPayload::handle
        );
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registerPackets(registrar);
    }
}
