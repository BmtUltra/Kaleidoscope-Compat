package com.bmt.kaleidoscope_compat.init;

import com.bmt.kaleidoscope_compat.init.soupbase.MilkBucketSoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("all")
public class KCSoupBases {
    public static final ResourceLocation MILK_BUCKET = ResourceLocation.fromNamespaceAndPath("minecraft", "milk");

    public static void registerAll() {
        SoupBaseManager.registerSoupBase(new MilkBucketSoupBase());
    }
}