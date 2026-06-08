package com.bmt.kaleidoscope_compat.compat.curios;

import com.bmt.kaleidoscope_compat.util.TagUtil;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public class CuriosIntegration {
    public static boolean hasStrawHatInCurios(LivingEntity entity) {
        if (!ModList.get().isLoaded("curios")) return false;

        Optional<SlotResult> result = CuriosApi.getCuriosInventory(entity)
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.is(TagUtil.Items.STRAW_HATS)));

        return result.isPresent();
    }
}