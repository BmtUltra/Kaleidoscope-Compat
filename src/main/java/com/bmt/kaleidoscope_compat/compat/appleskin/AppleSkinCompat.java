//package com.bmt.kaleidoscope_compat.compat.appleskin;
//
//import com.github.ysbbbbbb.kaleidoscopecookery.item.TransmutationLunchBagItem;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.food.FoodProperties;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.event.entity.player.PlayerEvent;
//import net.minecraftforge.fml.ModList;
//import net.minecraftforge.items.ItemStackHandler;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//public class AppleSkinCompat {
//
//    private static final boolean APPLE_SKIN_LOADED = ModList.get().isLoaded("appleskin");
//
//    private static final Map<UUID, ItemStack> lastHeldLunchBag = new HashMap<>();
//    private static final Map<UUID, Boolean> lastLunchBagHasItems = new HashMap<>();
//
//    public static void register() {
//        if (APPLE_SKIN_LOADED) {
//            MinecraftForge.EVENT_BUS.register(AppleSkinCompat.class);
//        }
//    }
//
//    @net.minecraftforge.eventbus.api.SubscribeEvent
//    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        if (!APPLE_SKIN_LOADED) {
//            return;
//        }
//
//        if (event.phase != TickEvent.Phase.END) {
//            return;
//        }
//
//        if (event.player instanceof ServerPlayer player) {
//            ItemStack mainHandItem = player.getMainHandItem();
//            UUID playerId = player.getUUID();
//
//            boolean isLunchBag = mainHandItem.getItem() instanceof TransmutationLunchBagItem;
//
//            ItemStack lastBag = lastHeldLunchBag.get(playerId);
//            boolean lastHadItems = lastLunchBagHasItems.getOrDefault(playerId, false);
//
//            boolean hasItems = isLunchBag && TransmutationLunchBagItem.hasItems(mainHandItem);
//
//            boolean shouldUpdate = false;
//
//            if (isLunchBag) {
//                if (lastBag == null || !ItemStack.matches(mainHandItem, lastBag)) {
//                    shouldUpdate = true;
//                } else if (hasItems != lastHadItems) {
//                    shouldUpdate = true;
//                } else if (hasItems) {
//                    ItemStackHandler currentItems = TransmutationLunchBagItem.getItems(mainHandItem);
//                    ItemStackHandler lastItems = TransmutationLunchBagItem.getItems(lastBag);
//
//                    if (!areItemHandlersEqual(currentItems, lastItems)) {
//                        shouldUpdate = true;
//                    }
//                }
//            } else if (lastBag != null) {
//                shouldUpdate = true;
//            }
//
//            if (shouldUpdate) {
//                updateLunchBagFoodProperties(player, mainHandItem, isLunchBag, hasItems);
//
//                if (isLunchBag) {
//                    lastHeldLunchBag.put(playerId, mainHandItem.copy());
//                    lastLunchBagHasItems.put(playerId, hasItems);
//                } else {
//                    lastHeldLunchBag.remove(playerId);
//                    lastLunchBagHasItems.remove(playerId);
//                }
//            }
//        }
//    }
//
//    @net.minecraftforge.eventbus.api.SubscribeEvent
//    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
//        if (!APPLE_SKIN_LOADED) {
//            return;
//        }
//
//        if (event.getEntity() instanceof ServerPlayer player) {
//            UUID playerId = player.getUUID();
//            lastHeldLunchBag.remove(playerId);
//            lastLunchBagHasItems.remove(playerId);
//        }
//    }
//
//    private static void updateLunchBagFoodProperties(ServerPlayer player, ItemStack lunchBag, boolean isLunchBag, boolean hasItems) {
//        if (!isLunchBag) {
//            return;
//        }
//
//        if (hasItems) {
//            ItemStackHandler items = TransmutationLunchBagItem.getItems(lunchBag);
//
//            ItemStack firstFood = ItemStack.EMPTY;
//            for (int i = 0; i < items.getSlots(); i++) {
//                ItemStack foodStack = items.getStackInSlot(i);
//                if (!foodStack.isEmpty()) {
//                    if (foodStack.getItem() instanceof TransmutationLunchBagItem) {
//                        continue;
//                    }
//
//                    FoodProperties foodProperties = foodStack.getItem().getFoodProperties(foodStack, player);
//                    if (foodProperties != null) {
//                        firstFood = foodStack;
//                        break;
//                    }
//                }
//            }
//            firstFood.isEmpty();
//        }
//    }
//
//    private static boolean areItemHandlersEqual(ItemStackHandler handler1, ItemStackHandler handler2) {
//        if (handler1.getSlots() != handler2.getSlots()) {
//            return false;
//        }
//
//        for (int i = 0; i < handler1.getSlots(); i++) {
//            ItemStack stack1 = handler1.getStackInSlot(i);
//            ItemStack stack2 = handler2.getStackInSlot(i);
//
//            if (!ItemStack.matches(stack1, stack2)) {
//                return false;
//            }
//        }
//        return true;
//    }
//}