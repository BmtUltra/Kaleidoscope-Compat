//package com.bmt.kaleidoscope_compat.compat.farm_and_charm;
//
//import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.ModList;
//
//import java.util.List;
//
//public class FarmAndCharmCompat {
//    public static final String ID = "farm_and_charm";
//    public static boolean IS_LOADED = false;
//
//    public static void init() {
//        ModList.get().getModContainerById(ID).ifPresent(modContainer -> {
//            IS_LOADED = true;
//            MinecraftForge.EVENT_BUS.addListener(CookingPotCompat::afterStockpotRecipeMatch);
//        });
//    }
//
//    public static void getTransformRecipeForJei(Level level, List<StockpotRecipe> recipes) {
//        if (IS_LOADED) {
//            CookingPotCompat.getTransformRecipeForJei(level, recipes);
//        }
//    }
//}