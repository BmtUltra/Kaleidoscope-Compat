package com.bmt.kaleidoscope_compat.compat.rei;

import com.bmt.kaleidoscope_compat.compat.rei.category.ReiWhirlwindBarbecueCategory;
import com.bmt.kaleidoscope_compat.config.category.OtherCategory;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public class ReiPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        if (!OtherCategory.jeiCompatEnabled) return;

        ReiWhirlwindBarbecueCategory.registerCategories(registry);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        if (!OtherCategory.jeiCompatEnabled) return;

        ReiWhirlwindBarbecueCategory.registerDisplays(registry);
    }
}