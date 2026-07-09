package com.bmt.kaleidoscope_compat.compat.solcarrot;

import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.FoodBiteBlockAccessor;
import com.cazsius.solcarrot.tracking.FoodList;
import com.github.ysbbbbbb.kaleidoscopecookery.block.food.FoodBiteBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.food.FoodBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class FoodBlockHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ModList.get().isLoaded("solcarrot")) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof FoodBlock)) {
            return;
        }

        Player player = event.getEntity();

        if (state.getBlock() instanceof FoodBiteBlock biteBlock) {
            int bites = state.getValue(biteBlock.getBites());
            if (bites >= biteBlock.getMaxBites()) {
                return;
            }

            FoodBiteBlockAccessor accessor = (FoodBiteBlockAccessor) biteBlock;
            FoodProperties foodProperties = accessor.getFoodProperties();
            if (!player.canEat(foodProperties.canAlwaysEat())) {
                return;
            }
        }

        ItemStack blockItemStack = state.getBlock().getCloneItemStack(level, pos, state);
        ResourceLocation blockId = BuiltInRegistries.ITEM.getKey(blockItemStack.getItem());

        FoodBiteRegistry.FoodData foodData = FoodBiteRegistry.FOOD_DATA_MAP.get(blockId);
        if (foodData != null) {
            FoodList foodList = FoodList.get(player);
            boolean alreadyEaten = foodList != null && foodList.hasEaten(blockItemStack);
            LivingEntityUseItemEvent.Finish finishEvent = new LivingEntityUseItemEvent.Finish(
                    player, blockItemStack, 32, blockItemStack
            );
            NeoForge.EVENT_BUS.post(finishEvent);

            if (!alreadyEaten && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundLevelParticlesPacket(
                        ParticleTypes.END_ROD,
                        false,
                        player.getX(),
                        player.getY() + player.getEyeHeight(),
                        player.getZ(),
                        0.5F, 0.5F, 0.5F,
                        0.0F,
                        12
                ));
            }
        }
    }
}