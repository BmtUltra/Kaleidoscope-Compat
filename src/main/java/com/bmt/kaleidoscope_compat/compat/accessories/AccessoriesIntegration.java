package com.bmt.kaleidoscope_compat.compat.accessories;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class AccessoriesIntegration {

    private static final String ACCESSORIES_MOD_ID = "accessories";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(ACCESSORIES_MOD_ID);
    }

    public static boolean hasStrawHatInAccessories(net.minecraft.world.entity.LivingEntity entity) {
        if (!isLoaded()) return false;

        var capability = io.wispforest.accessories.api.AccessoriesCapability.get(entity);
        if (capability == null) return false;

        for (var ref : capability.getAllEquipped()) {
            ItemStack stack = ref.stack();
            if (stack.is(TagUtil.Items.STRAW_HATS)) {
                return true;
            }
        }
        return false;
    }
}