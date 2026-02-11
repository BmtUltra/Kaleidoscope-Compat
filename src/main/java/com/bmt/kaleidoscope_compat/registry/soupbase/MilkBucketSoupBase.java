package com.bmt.kaleidoscope_compat.registry.soupbase;

import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.client.render.soupbase.SimpleSoupBaseRender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MilkBucketSoupBase implements ISoupBase {
    private static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath("minecraft", "milk");
    private static final int BUBBLE_COLOR = 0xFFFFFF;
    private static final ResourceLocation MILK_TEXTURE = ResourceLocation.fromNamespaceAndPath("kaleidoscope_compat", "block/milk_still");

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public int getBubbleColor() {
        return BUBBLE_COLOR;
    }

    @Override
    public ItemStack getDisplayStack() {
        return new ItemStack(Items.MILK_BUCKET);
    }

    @Override
    public boolean isSoupBase(ItemStack stack) {
        return stack.getItem() == Items.MILK_BUCKET;
    }

    @Override
    public ItemStack getReturnContainer(Level level, LivingEntity user, ItemStack soupBase) {
        return new ItemStack(Items.BUCKET);
    }

    @Override
    public boolean isContainer(ItemStack stack) {
        return stack.getItem() == Items.BUCKET;
    }

    @Override
    public ItemStack getReturnSoupBase(Level level, LivingEntity user, ItemStack container) {
        return new ItemStack(Items.MILK_BUCKET);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public com.github.ysbbbbbb.kaleidoscopecookery.api.client.render.ISoupBaseRender getRender() {
        return new SimpleSoupBaseRender(MILK_TEXTURE);
    }
}