package com.bmt.kaleidoscope_compat.mixins.farmersdelight;

import com.bmt.kaleidoscope_compat.datamap.replacement.ReplacementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.TomatoBlock;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModSounds;

@Mixin(TomatoBlock.class)
public class TomatoHarvestMixin {

    @Unique
    private static final String REPLACEMENT_TYPE = "croploot";

    @Inject(
            method = "useWithoutItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHarvestTomato(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit,
                                 CallbackInfoReturnable<InteractionResult> cir) {
        int age = state.getValue(((TomatoBlock) (Object) this).getAgeProperty());
        boolean isMature = age == ((TomatoBlock) (Object) this).getMaxAge();
        if (!isMature) {
            return;
        }

        ResourceLocation tomatoId = BuiltInRegistries.ITEM.getKey(ModItems.TOMATO.get());
        ResourceLocation replacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, tomatoId);

        if (replacement != null) {
            Item newItem = BuiltInRegistries.ITEM.get(replacement);
            if (newItem != Items.AIR) {
                cir.cancel();
                int quantity = 1 + level.random.nextInt(2);
                CropBlock.popResource(level, pos, new ItemStack(newItem, quantity));
                ResourceLocation rottenTomatoId = BuiltInRegistries.ITEM.getKey(ModItems.ROTTEN_TOMATO.get());
                ResourceLocation rottenReplacement = ReplacementManager.getItemReplacement(REPLACEMENT_TYPE, rottenTomatoId);

                if (level.random.nextFloat() < 0.05) {
                    if (rottenReplacement != null) {
                        Item rottenNewItem = BuiltInRegistries.ITEM.get(rottenReplacement);
                        if (rottenNewItem != Items.AIR) {
                            CropBlock.popResource(level, pos, new ItemStack(rottenNewItem));
                        } else {
                            CropBlock.popResource(level, pos, new ItemStack(ModItems.ROTTEN_TOMATO.get()));
                        }
                    } else {
                        CropBlock.popResource(level, pos, new ItemStack(ModItems.ROTTEN_TOMATO.get()));
                    }
                }

                level.playSound(null, pos, ModSounds.BLOCK_TOMATOES_PICK_TOMATOES.get(), SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                level.setBlock(pos, state.setValue(((TomatoBlock) (Object) this).getAgeProperty(), 0), 2);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}