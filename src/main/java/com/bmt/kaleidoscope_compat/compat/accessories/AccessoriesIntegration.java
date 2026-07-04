package com.bmt.kaleidoscope_compat.compat.accessories;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class AccessoriesIntegration {
    public static boolean hasStrawHatInAccessories(LivingEntity entity) {
        if (!ModList.get().isLoaded("accessories"))
            return false;
        var capability = AccessoriesCapability.get(entity);
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