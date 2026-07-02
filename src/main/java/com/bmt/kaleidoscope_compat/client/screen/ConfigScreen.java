package com.bmt.kaleidoscope_compat.client.screen;

import com.bmt.kaleidoscope_compat.config.ForgeConfig;
import com.bmt.kaleidoscope_compat.datapack.DatapackMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {
    private final Screen parent;
    private int currentTab = 0;

    private Button generalTabButton;
    private Button kitchenTabButton;
    private Button farmersDelightTabButton;
    private Button youkaisFeastsTabButton;
    private Button farmAndCharmTabButton;
    private Button vineryTabButton;
    private Button littleMaidTabButton;
    private Button otherTabButton;

    private Checkbox soupDatapackCheckbox;
    private Button datapackModeButton;
    private DatapackMode currentDatapackMode;

    private Checkbox fuzzyRecipesCheckbox;
    private Checkbox blockEnabledCheckbox;
    private Checkbox lunchBagBackBehaviorCheckbox;
    private Checkbox lunchBagAppleskinCheckbox;
    private Checkbox scarecrowRepelPhantomsCheckbox;

    private Checkbox fdCookingPotRecipesCheckbox;
    private Checkbox fdCookingPotGuiCheckbox;
    private Checkbox fdCuttingBoardRecipesCheckbox;
    private Checkbox fdRichSoilHoeCheckbox;

    private Checkbox yfSteamingRecipesCheckbox;

    private Checkbox facCookingPotRecipesCheckbox;
    private Checkbox facCompatCheckbox;

    private Checkbox vineryBarrelRecipesCheckbox;

    private Checkbox lmChoppingBoardCheckbox;
    private Checkbox lmMillstoneCheckbox;
    private Checkbox lmPressingTubCheckbox;

    private Checkbox otherJeiCompatCheckbox;
    private Checkbox otherThirstCompatCheckbox;
    private Checkbox otherQuarkSickleCheckbox;
    private Checkbox otherSuppressTagErrorsCheckbox;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("config.kaleidoscope_compat.title"));
        this.parent = parent;
        this.currentDatapackMode = DatapackMode.valueOf(ForgeConfig.DATAPACK_MODE.get());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 35;

        generalTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.tab.general"),
                (button) -> switchTab(0)).bounds(centerX - 180, y, 70, 20).build();
        kitchenTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.category.kitchen"),
                (button) -> switchTab(1)).bounds(centerX - 110, y, 70, 20).build();
        farmersDelightTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.category.farmersdelight"),
                (button) -> switchTab(2)).bounds(centerX - 40, y, 70, 20).build();
        youkaisFeastsTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.category.youkaisfeasts"),
                (button) -> switchTab(3)).bounds(centerX + 30, y, 70, 20).build();
        farmAndCharmTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.category.farm_and_charm"),
                (button) -> switchTab(4)).bounds(centerX + 100, y, 70, 20).build();

        y += 22;
        vineryTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.category.vinery"),
                (button) -> switchTab(5)).bounds(centerX - 110, y, 70, 20).build();
        littleMaidTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.category.little_maid"),
                (button) -> switchTab(6)).bounds(centerX - 40, y, 70, 20).build();
        otherTabButton = Button.builder(Component.translatable("config.kaleidoscope_compat.category.other"),
                (button) -> switchTab(7)).bounds(centerX + 30, y, 70, 20).build();

        this.addRenderableWidget(generalTabButton);
        this.addRenderableWidget(kitchenTabButton);
        this.addRenderableWidget(farmersDelightTabButton);
        this.addRenderableWidget(youkaisFeastsTabButton);
        this.addRenderableWidget(farmAndCharmTabButton);
        this.addRenderableWidget(vineryTabButton);
        this.addRenderableWidget(littleMaidTabButton);
        this.addRenderableWidget(otherTabButton);

        initGeneralComponents();
        initKitchenComponents();
        initFarmersDelightComponents();
        initYoukaisFeastsComponents();
        initFarmAndCharmComponents();
        initVineryComponents();
        initLittleMaidComponents();
        initOtherComponents();

        updateComponentVisibility();

        y = this.height - 30;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                (button) -> this.onClose()).bounds(centerX - 100, y, 200, 20).build());
    }

    private void switchTab(int tabIndex) {
        this.currentTab = tabIndex;
        updateComponentVisibility();
        updateTabButtonStyles();
    }

    private void updateTabButtonStyles() {
        generalTabButton.active = currentTab != 0;
        kitchenTabButton.active = currentTab != 1;
        farmersDelightTabButton.active = currentTab != 2;
        youkaisFeastsTabButton.active = currentTab != 3;
        farmAndCharmTabButton.active = currentTab != 4;
        vineryTabButton.active = currentTab != 5;
        littleMaidTabButton.active = currentTab != 6;
        otherTabButton.active = currentTab != 7;
    }

    private void initGeneralComponents() {
        int centerX = this.width / 2;
        int y = 80;

        datapackModeButton = Button.builder(
                Component.translatable("config.kaleidoscope_compat.datapack.mode")
                        .append(": ")
                        .append(Component.translatable("datapack_mode.kaleidoscope_compat." + currentDatapackMode.name().toLowerCase())),
                (button) -> {
                    DatapackMode[] modes = DatapackMode.values();
                    int currentIndex = currentDatapackMode.ordinal();
                    currentDatapackMode = modes[(currentIndex + 1) % modes.length];
                    updateDatapackModeButton();
                }).bounds(centerX - 100, y, 200, 20).build();
        datapackModeButton.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.datapack.mode.comment")));
        this.addRenderableWidget(datapackModeButton);

        y += 24;
        soupDatapackCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.datapack.soup_enabled"),
                ForgeConfig.SOUP_DATAPACK_ENABLED.get());
        soupDatapackCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.datapack.soup_enabled.comment")));
        this.addRenderableWidget(soupDatapackCheckbox);
    }

    private void updateDatapackModeButton() {
        datapackModeButton.setMessage(
                Component.translatable("config.kaleidoscope_compat.datapack.mode")
                        .append(": ")
                        .append(Component.translatable("datapack_mode.kaleidoscope_compat." + currentDatapackMode.name().toLowerCase())));
    }

    private void initKitchenComponents() {
        int centerX = this.width / 2;
        int y = 80;

        fuzzyRecipesCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.kitchen.fuzzy_recipes_enabled"),
                ForgeConfig.KITCHEN_FUZZY_RECIPES_ENABLED.get());
        fuzzyRecipesCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.kitchen.fuzzy_recipes_enabled.comment")));
        this.addRenderableWidget(fuzzyRecipesCheckbox);

        y += 22;
        blockEnabledCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.kitchen.effect.block_enabled"),
                ForgeConfig.KITCHEN_BLOCK_ENABLED.get());
        blockEnabledCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.kitchen.effect.block_enabled.comment")));
        this.addRenderableWidget(blockEnabledCheckbox);

        y += 22;
        lunchBagBackBehaviorCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.kitchen.lunch_bag.back_behavior_enabled"),
                ForgeConfig.LUNCH_BAG_BACK_BEHAVIOR_ENABLED.get());
        lunchBagBackBehaviorCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.kitchen.lunch_bag.back_behavior_enabled.comment")));
        this.addRenderableWidget(lunchBagBackBehaviorCheckbox);

        y += 22;
        lunchBagAppleskinCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.kitchen.lunch_bag.appleskin_compat_enabled"),
                ForgeConfig.LUNCH_BAG_APPLESKIN_COMPAT_ENABLED.get());
        lunchBagAppleskinCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.kitchen.lunch_bag.appleskin_compat_enabled.comment")));
        this.addRenderableWidget(lunchBagAppleskinCheckbox);

        y += 22;
        scarecrowRepelPhantomsCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.kitchen.scarecrow.repel_phantoms"),
                ForgeConfig.SCARECROW_REPEL_PHANTOMS.get());
        scarecrowRepelPhantomsCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.kitchen.scarecrow.repel_phantoms.comment")));
        this.addRenderableWidget(scarecrowRepelPhantomsCheckbox);
    }

    private void initFarmersDelightComponents() {
        int centerX = this.width / 2;
        int y = 80;

        fdCookingPotRecipesCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.farmersdelight.cooking_pot_recipes_disabled"),
                ForgeConfig.FARMERSDELIGHT_COOKING_POT_RECIPES_DISABLED.get());
        fdCookingPotRecipesCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.farmersdelight.cooking_pot_recipes_disabled.comment")));
        this.addRenderableWidget(fdCookingPotRecipesCheckbox);

        y += 22;
        fdCookingPotGuiCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.farmersdelight.cooking_pot_gui_disabled"),
                ForgeConfig.FARMERSDELIGHT_COOKING_POT_GUI_DISABLED.get());
        fdCookingPotGuiCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.farmersdelight.cooking_pot_gui_disabled.comment")));
        this.addRenderableWidget(fdCookingPotGuiCheckbox);

        y += 22;
        fdCuttingBoardRecipesCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.farmersdelight.cutting_board_recipes_disabled"),
                ForgeConfig.FARMERSDELIGHT_CUTTING_BOARD_RECIPES_DISABLED.get());
        fdCuttingBoardRecipesCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.farmersdelight.cutting_board_recipes_disabled.comment")));
        this.addRenderableWidget(fdCuttingBoardRecipesCheckbox);

        y += 22;
        fdRichSoilHoeCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.farmersdelight.hoe_enabled"),
                ForgeConfig.FARMERSDELIGHT_RICH_SOIL_HOE_ENABLED.get());
        fdRichSoilHoeCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.farmersdelight.hoe_enabled.comment")));
        this.addRenderableWidget(fdRichSoilHoeCheckbox);
    }

    private void initYoukaisFeastsComponents() {
        int centerX = this.width / 2;
        int y = 80;

        yfSteamingRecipesCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.youkaisfeasts.steaming_recipes_disabled"),
                ForgeConfig.YOUKAISFEASTS_STEAMING_RECIPES_DISABLED.get());
        yfSteamingRecipesCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.youkaisfeasts.steaming_recipes_disabled.comment")));
        this.addRenderableWidget(yfSteamingRecipesCheckbox);
    }

    private void initFarmAndCharmComponents() {
        int centerX = this.width / 2;
        int y = 80;

        facCookingPotRecipesCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.farm_and_charm.cooking_pot_recipes_disabled"),
                ForgeConfig.FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED.get());
        facCookingPotRecipesCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.farm_and_charm.cooking_pot_recipes_disabled.comment")));
        this.addRenderableWidget(facCookingPotRecipesCheckbox);

        y += 22;
        facCompatCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.farm_and_charm.compat_enabled"),
                ForgeConfig.FARM_AND_CHARM_COMPAT_ENABLED.get());
        facCompatCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.farm_and_charm.compat_enabled.comment")));
        this.addRenderableWidget(facCompatCheckbox);
    }

    private void initVineryComponents() {
        int centerX = this.width / 2;
        int y = 80;

        vineryBarrelRecipesCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.vinery.barrel_recipes_disabled"),
                ForgeConfig.VINERY_BARREL_RECIPES_DISABLED.get());
        vineryBarrelRecipesCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.vinery.barrel_recipes_disabled.comment")));
        this.addRenderableWidget(vineryBarrelRecipesCheckbox);
    }

    private void initLittleMaidComponents() {
        int centerX = this.width / 2;
        int y = 80;

        lmChoppingBoardCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.little_maid.chopping_board_enabled"),
                ForgeConfig.LITTLE_MAID_CHOPPING_BOARD_ENABLED.get());
        lmChoppingBoardCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.little_maid.chopping_board_enabled.comment")));
        this.addRenderableWidget(lmChoppingBoardCheckbox);

        y += 22;
        lmMillstoneCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.little_maid.millstone_enabled"),
                ForgeConfig.LITTLE_MAID_MILLSTONE_ENABLED.get());
        lmMillstoneCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.little_maid.millstone_enabled.comment")));
        this.addRenderableWidget(lmMillstoneCheckbox);

        y += 22;
        lmPressingTubCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.little_maid.pressing_tub_enabled"),
                ForgeConfig.LITTLE_MAID_PRESSING_TUB_ENABLED.get());
        lmPressingTubCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.little_maid.pressing_tub_enabled.comment")));
        this.addRenderableWidget(lmPressingTubCheckbox);
    }

    private void initOtherComponents() {
        int centerX = this.width / 2;
        int y = 80;

        otherJeiCompatCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.compat.jei_compat_enabled"),
                ForgeConfig.OTHER_JEI_COMPAT_ENABLED.get());
        otherJeiCompatCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.compat.jei_compat_enabled.comment")));
        this.addRenderableWidget(otherJeiCompatCheckbox);

        y += 22;
        otherThirstCompatCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.compat.thirst_compat_enabled"),
                ForgeConfig.OTHER_THIRST_COMPAT_ENABLED.get());
        otherThirstCompatCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.compat.thirst_compat_enabled.comment")));
        this.addRenderableWidget(otherThirstCompatCheckbox);

        y += 22;
        otherQuarkSickleCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.compat.quark_sickle_harvest_fix_enabled"),
                ForgeConfig.OTHER_QUARK_SICKLE_HARVEST_FIX_ENABLED.get());
        otherQuarkSickleCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.compat.quark_sickle_harvest_fix_enabled.comment")));
        this.addRenderableWidget(otherQuarkSickleCheckbox);

        y += 22;
        otherSuppressTagErrorsCheckbox = new Checkbox(centerX - 100, y, 20, 20,
                Component.translatable("config.kaleidoscope_compat.compat.suppress_tag_load_errors"),
                ForgeConfig.OTHER_SUPPRESS_TAG_LOAD_ERRORS.get());
        otherSuppressTagErrorsCheckbox.setTooltip(Tooltip.create(Component.translatable("config.kaleidoscope_compat.compat.suppress_tag_load_errors.comment")));
        this.addRenderableWidget(otherSuppressTagErrorsCheckbox);
    }

    private void updateComponentVisibility() {
        datapackModeButton.visible = false;
        soupDatapackCheckbox.visible = false;

        fuzzyRecipesCheckbox.visible = false;
        blockEnabledCheckbox.visible = false;
        lunchBagBackBehaviorCheckbox.visible = false;
        lunchBagAppleskinCheckbox.visible = false;
        scarecrowRepelPhantomsCheckbox.visible = false;

        fdCookingPotRecipesCheckbox.visible = false;
        fdCookingPotGuiCheckbox.visible = false;
        fdCuttingBoardRecipesCheckbox.visible = false;
        fdRichSoilHoeCheckbox.visible = false;

        yfSteamingRecipesCheckbox.visible = false;

        facCookingPotRecipesCheckbox.visible = false;
        facCompatCheckbox.visible = false;

        vineryBarrelRecipesCheckbox.visible = false;

        lmChoppingBoardCheckbox.visible = false;
        lmMillstoneCheckbox.visible = false;
        lmPressingTubCheckbox.visible = false;

        otherJeiCompatCheckbox.visible = false;
        otherThirstCompatCheckbox.visible = false;
        otherQuarkSickleCheckbox.visible = false;
        otherSuppressTagErrorsCheckbox.visible = false;

        switch (currentTab) {
            case 0:
                datapackModeButton.visible = true;
                soupDatapackCheckbox.visible = true;
                break;
            case 1:
                fuzzyRecipesCheckbox.visible = true;
                blockEnabledCheckbox.visible = true;
                lunchBagBackBehaviorCheckbox.visible = true;
                lunchBagAppleskinCheckbox.visible = true;
                scarecrowRepelPhantomsCheckbox.visible = true;
                break;
            case 2:
                fdCookingPotRecipesCheckbox.visible = true;
                fdCookingPotGuiCheckbox.visible = true;
                fdCuttingBoardRecipesCheckbox.visible = true;
                fdRichSoilHoeCheckbox.visible = true;
                break;
            case 3:
                yfSteamingRecipesCheckbox.visible = true;
                break;
            case 4:
                facCookingPotRecipesCheckbox.visible = true;
                facCompatCheckbox.visible = true;
                break;
            case 5:
                vineryBarrelRecipesCheckbox.visible = true;
                break;
            case 6:
                lmChoppingBoardCheckbox.visible = true;
                lmMillstoneCheckbox.visible = true;
                lmPressingTubCheckbox.visible = true;
                break;
            case 7:
                otherJeiCompatCheckbox.visible = true;
                otherThirstCompatCheckbox.visible = true;
                otherQuarkSickleCheckbox.visible = true;
                otherSuppressTagErrorsCheckbox.visible = true;
                break;
        }

        updateTabButtonStyles();
    }

    @Override
    public void onClose() {
        ForgeConfig.DATAPACK_MODE.set(currentDatapackMode.name());

        ForgeConfig.SOUP_DATAPACK_ENABLED.set(soupDatapackCheckbox.selected());

        ForgeConfig.KITCHEN_FUZZY_RECIPES_ENABLED.set(fuzzyRecipesCheckbox.selected());
        ForgeConfig.KITCHEN_BLOCK_ENABLED.set(blockEnabledCheckbox.selected());
        ForgeConfig.LUNCH_BAG_BACK_BEHAVIOR_ENABLED.set(lunchBagBackBehaviorCheckbox.selected());
        ForgeConfig.LUNCH_BAG_APPLESKIN_COMPAT_ENABLED.set(lunchBagAppleskinCheckbox.selected());
        ForgeConfig.SCARECROW_REPEL_PHANTOMS.set(scarecrowRepelPhantomsCheckbox.selected());

        ForgeConfig.FARMERSDELIGHT_COOKING_POT_RECIPES_DISABLED.set(fdCookingPotRecipesCheckbox.selected());
        ForgeConfig.FARMERSDELIGHT_COOKING_POT_GUI_DISABLED.set(fdCookingPotGuiCheckbox.selected());
        ForgeConfig.FARMERSDELIGHT_CUTTING_BOARD_RECIPES_DISABLED.set(fdCuttingBoardRecipesCheckbox.selected());
        ForgeConfig.FARMERSDELIGHT_RICH_SOIL_HOE_ENABLED.set(fdRichSoilHoeCheckbox.selected());

        ForgeConfig.YOUKAISFEASTS_STEAMING_RECIPES_DISABLED.set(yfSteamingRecipesCheckbox.selected());

        ForgeConfig.FARM_AND_CHARM_COOKING_POT_RECIPES_DISABLED.set(facCookingPotRecipesCheckbox.selected());
        ForgeConfig.FARM_AND_CHARM_COMPAT_ENABLED.set(facCompatCheckbox.selected());

        ForgeConfig.VINERY_BARREL_RECIPES_DISABLED.set(vineryBarrelRecipesCheckbox.selected());

        ForgeConfig.LITTLE_MAID_CHOPPING_BOARD_ENABLED.set(lmChoppingBoardCheckbox.selected());
        ForgeConfig.LITTLE_MAID_MILLSTONE_ENABLED.set(lmMillstoneCheckbox.selected());
        ForgeConfig.LITTLE_MAID_PRESSING_TUB_ENABLED.set(lmPressingTubCheckbox.selected());

        ForgeConfig.OTHER_JEI_COMPAT_ENABLED.set(otherJeiCompatCheckbox.selected());
        ForgeConfig.OTHER_THIRST_COMPAT_ENABLED.set(otherThirstCompatCheckbox.selected());
        ForgeConfig.OTHER_QUARK_SICKLE_HARVEST_FIX_ENABLED.set(otherQuarkSickleCheckbox.selected());
        ForgeConfig.OTHER_SUPPRESS_TAG_LOAD_ERRORS.set(otherSuppressTagErrorsCheckbox.selected());

        ForgeConfig.SPEC.save();

        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}