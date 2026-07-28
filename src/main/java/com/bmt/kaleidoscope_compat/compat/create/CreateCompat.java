package com.bmt.kaleidoscope_compat.compat.create;

import com.bmt.kaleidoscope_compat.compat.create.arm.*;
import com.bmt.kaleidoscope_compat.compat.create.automation.DeployerAutomation;
import com.bmt.kaleidoscope_compat.compat.create.automation.RecipeItemAutomation;
import com.bmt.kaleidoscope_compat.config.category.CreateCategory;
import com.bmt.kaleidoscope_compat.config.category.arm.ArmConfig;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

public class CreateCompat {
    public static void init(IEventBus modEventBus) {
        ModList.get().getModContainerById("create").ifPresent(modContainer -> {
            ArmRecipeAttachments.ATTACHMENT_TYPES.register(modEventBus);
            NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, DeployerAutomation::onRightClickBlock);
            NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RecipeItemAutomation::onRightClickBlock);
            NeoForge.EVENT_BUS.addListener(RecipeItemAutomation::onItemTooltip);
            if (!CreateCategory.createCompatEnabled) {
                return;
            }
            if (ArmConfig.potEnabled) {
                CreatePotArm.init(modEventBus);}
            if (ArmConfig.stockpotEnabled) {
                CreateStockpotArm.init(modEventBus);}
            if (ArmConfig.steamerEnabled) {
                CreateSteamerArm.init(modEventBus);}
            if (ArmConfig.millstoneEnabled) {
                CreateMillstoneArm.init(modEventBus);}
            if (ArmConfig.shawarmaSpitEnabled) {
                CreateShawarmaSpitArm.init(modEventBus);}
            if (ArmConfig.teapotEnabled) {
                CreateTeapotArm.init(modEventBus);}
        });
    }
}
