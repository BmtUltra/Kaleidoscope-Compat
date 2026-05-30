package com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.task;

import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.MaidChoppingBoardBehavior;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaskChoppingBoard implements IMaidTask {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("kaleidoscope_compat", "chopping_board");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        Item choppingBoard = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "chopping_board")
        );
        return choppingBoard.getDefaultInstance();
    }

    @Override
    public @NotNull SoundEvent getAmbientSound(@NotNull EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_IDLE.get(), 0.5F);
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(@NotNull EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of(5, new MaidChoppingBoardBehavior(0.6F))
        );
    }

    @Override
    public @NotNull String getMaidActionSummary() {
        return "Cut ingredients on the chopping board from Kaleidoscope Cookery";
    }
}