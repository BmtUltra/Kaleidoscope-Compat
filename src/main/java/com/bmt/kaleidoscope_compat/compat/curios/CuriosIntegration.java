package com.bmt.kaleidoscope_compat.compat.curios;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

@SuppressWarnings("all")
public class CuriosIntegration {

    private static final String CURIOS_MOD_ID = "curios";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MOD_ID);
    }

    public static boolean hasStrawHatInCurios(LivingEntity entity) {
        if (!isLoaded()) return false;

        Optional<ICuriosItemHandler> handlerOptional = CuriosApi.getCuriosInventory(entity).resolve();
        if (handlerOptional.isEmpty()) {
            return false;
        }

        ICuriosItemHandler handler = handlerOptional.get();

        for (String slotType : handler.getCurios().keySet()) {
            var slotResult = handler.findCurio(slotType, 0);
            if (slotResult.isPresent()) {
                SlotResult result = slotResult.get();
                if (result.stack().is(TagUtil.Items.STRAW_HATS)) {
                    return true;
                }
            }
        }
        return false;
    }
}