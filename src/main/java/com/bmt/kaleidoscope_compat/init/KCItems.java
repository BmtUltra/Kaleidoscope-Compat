package com.bmt.kaleidoscope_compat.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.item.JuiceBucketItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class KCItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KaleidoscopeTavern.MOD_ID);

    public static final DeferredItem<Item> APPLE_JUICE_BUCKET = ITEMS.register("apple_juice_bucket", () -> new JuiceBucketItem(KCFluids.APPLE_JUICE));
}