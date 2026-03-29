package com.bmt.kaleidoscope_compat.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.item.JuiceBucketItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class KCItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KaleidoscopeTavern.MOD_ID);

    public static final RegistryObject<Item> WHITE_GRAPE_JUICE_BUCKET = ITEMS.register("white_grape_juice_bucket",
            () -> new JuiceBucketItem(KCFluids.WHITE_GRAPE_JUICE));

    public static final RegistryObject<Item> APPLE_JUICE_BUCKET = ITEMS.register("apple_juice_bucket",
            () -> new JuiceBucketItem(KCFluids.APPLE_JUICE));
}