package com.bmt.kaleidoscope_compat;

import com.bmt.kaleidoscope_compat.compat.create.CreateCompat;
import com.bmt.kaleidoscope_compat.compat.farm_and_charm.FarmAndCharmCompat;
import com.bmt.kaleidoscope_compat.compat.spectrum.SpectrumCompat;
import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.LittleMaidCompat;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforgespi.Environment;

@Mod(KaleidoscopeCompat.MOD_ID)
public class KaleidoscopeCompat {
    public static final String MOD_ID = "kaleidoscope_compat";
    public static final Configurator CONFIGURATOR = new Configurator(MOD_ID);

    public KaleidoscopeCompat(IEventBus modEventBus, ModContainer modContainer) {
        // 注册主配置（服务端+客户端通用）
        CONFIGURATOR.register(MainConfig.class);

        // 注册 Resourceful Config 配置屏幕（客户端）
        if (Environment.get().getDist().isClient()) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                    (container, screen) -> new ConfigScreen(screen, CONFIGURATOR.getConfig(MainConfig.class)));
        }

        CreateCompat.init(modEventBus);
        SpectrumCompat.init(modEventBus);
        FarmAndCharmCompat.init();
        LittleMaidCompat.init();
    }
}