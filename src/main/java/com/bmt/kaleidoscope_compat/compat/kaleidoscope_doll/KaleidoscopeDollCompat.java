package com.bmt.kaleidoscope_compat.compat.kaleidoscope_doll;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import com.bmt.kaleidoscope_compat.network.RequestPlayerDollPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class KaleidoscopeDollCompat {
    public static final String MOD_ID = "kaleidoscope_doll";
    public static boolean IS_LOADED = false;

    public static void init(IEventBus modEventBus) {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
        if (!IS_LOADED) {
            return;
        }

        modEventBus.addListener(KaleidoscopeDollCompat::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(KaleidoscopeCompat.MOD_ID + "_doll");
        registrar.playToServer(
                RequestPlayerDollPayload.TYPE,
                RequestPlayerDollPayload.STREAM_CODEC,
                RequestPlayerDollPayload::handle
        );
    }
}