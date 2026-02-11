package com.bmt.kaleidoscope_compat.registry;

import com.bmt.kaleidoscope_compat.registry.soupbase.MilkBucketSoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import net.minecraft.resources.ResourceLocation;

public class KCSoupBases {
    public static final ResourceLocation MILK_BUCKET = ResourceLocation.fromNamespaceAndPath("minecraft", "milk");

    public static void registerAll() {
        SoupBaseManager.registerSoupBase(new MilkBucketSoupBase());
    }
}