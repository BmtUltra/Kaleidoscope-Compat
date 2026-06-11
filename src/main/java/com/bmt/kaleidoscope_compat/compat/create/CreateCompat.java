package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.compat.create.arm.*;
import com.bmt.kaleidoscope_compat.config.category.CreateCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CreateCompat {
    public static void init(IEventBus modEventBus) {
        if (!CreateCategory.createCompatEnabled) {
            return;
        }
        ModList.get().getModContainerById("create").ifPresent(modContainer -> {
            if (CreateCategory.createArmPotEnabled) {
                CreatePotArm.init(modEventBus);}
            if (CreateCategory.createArmStockpotEnabled) {
                CreateStockpotArm.init(modEventBus);}
            if (CreateCategory.createArmSteamerEnabled) {
                CreateSteamerArm.init(modEventBus);}
            if (CreateCategory.createArmMillstoneEnabled) {
                CreateMillstoneArm.init(modEventBus);}
            if (CreateCategory.createArmShawarmaSpitEnabled) {
                CreateShawarmaSpitArm.init(modEventBus);}
            if (CreateCategory.createArmTeapotEnabled) {
                CreateTeapotArm.init(modEventBus);}
        });
    }
}