package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.compat.create.arm.*;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CreateCompat {
    public static void init(IEventBus modEventBus) {
        if (!MainConfig.createCompatEnabledValue) {
            return;
        }
        ModList.get().getModContainerById("create").ifPresent(modContainer -> {
            if (MainConfig.createArmPotEnabledValue) {
                CreatePotArm.init(modEventBus);}
            if (MainConfig.createArmStockpotEnabledValue) {
                CreateStockpotArm.init(modEventBus);}
            if (MainConfig.createArmSteamerEnabledValue) {
                CreateSteamerArm.init(modEventBus);}
            if (MainConfig.createArmMillstoneEnabledValue) {
                CreateMillstoneArm.init(modEventBus);}
            if (MainConfig.createArmShawarmaSpitEnabledValue) {
                CreateShawarmaSpitArm.init(modEventBus);}
            if (MainConfig.createArmTeapotEnabledValue) {
                CreateTeapotArm.init(modEventBus);}
        });
    }
}