package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.compat.create.arm.*;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CreateCompat {
    public static void init(IEventBus modEventBus) {
        if (!MainConfig.createCompatEnabled) {
            return;
        }
        ModList.get().getModContainerById("create").ifPresent(modContainer -> {
            if (MainConfig.createArmPotEnabled) {
                CreatePotArm.init(modEventBus);}
            if (MainConfig.createArmStockpotEnabled) {
                CreateStockpotArm.init(modEventBus);}
            if (MainConfig.createArmSteamerEnabled) {
                CreateSteamerArm.init(modEventBus);}
            if (MainConfig.createArmMillstoneEnabled) {
                CreateMillstoneArm.init(modEventBus);}
            if (MainConfig.createArmShawarmaSpitEnabled) {
                CreateShawarmaSpitArm.init(modEventBus);}
            if (MainConfig.createArmTeapotEnabled) {
                CreateTeapotArm.init(modEventBus);}
        });
    }
}