package com.bmt.kaleidoscope_compat.event;

import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = KaleidoscopeCompat.MOD_ID)
public class RichSoilHoeEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof HoeItem)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        if (!"farmersdelight".equals(blockId.getNamespace()) || !"rich_soil".equals(blockId.getPath())) {
            return;
        }

        BlockPos above = pos.above();
        FluidState fluidState = level.getFluidState(above);
        boolean isWater = fluidState.is(FluidTags.WATER);

        if (!isWater) {
            return;
        }

        Block richSoilFarmland = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("farmersdelight", "rich_soil_farmland")
        );

        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, richSoilFarmland.defaultBlockState());
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}