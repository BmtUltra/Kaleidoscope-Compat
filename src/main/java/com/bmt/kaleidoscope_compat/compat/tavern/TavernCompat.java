//package com.bmt.kaleidoscope_compat.compat.tavern;
//
//import com.bmt.kaleidoscope_compat.KaleidoscopeCompat;
//import net.neoforged.fml.ModList;
//
///**
// * KaleidoscopeTavern兼容性处理器
// * 检查Tavern模组是否存在，并处理相关注册
// */
//public class TavernCompat {
//
//    public static final String TAVERN_MOD_ID = "kaleidoscope_tavern";
//
//    /**
//     * 检查KaleidoscopeTavern模组是否加载
//     */
//    public static boolean isTavernLoaded() {
//        return ModList.get().isLoaded(TAVERN_MOD_ID);
//    }
//
//    /**
//     * 获取Tavern的命名空间
//     * 如果Tavern未加载，返回我们自己的命名空间作为备用
//     */
//    public static String getNamespace() {
//        return isTavernLoaded() ? TAVERN_MOD_ID : KaleidoscopeCompat.MOD_ID;
//    }
//
//    /**
//     * 安全地获取Tavern的MOD_ID
//     * 如果Tavern未加载，返回null
//     */
//    public static String getTavernModId() {
//        return isTavernLoaded() ? TAVERN_MOD_ID : null;
//    }
//}