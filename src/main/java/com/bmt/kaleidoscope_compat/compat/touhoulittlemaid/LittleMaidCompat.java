package com.bmt.kaleidoscope_compat.compat.touhoulittlemaid;

import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.task.TaskChoppingBoard;
import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.task.TaskMillstone;
import com.bmt.kaleidoscope_compat.compat.touhoulittlemaid.task.TaskPressingTub;
import com.bmt.kaleidoscope_compat.config.MainConfig;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.neoforged.fml.ModList;

public class LittleMaidCompat {
    public static final String ID = "touhoulittlemaid";
    public static final String KALEIDOSCOPE_TAVERN_ID = "kaleidoscope_tavern";
    public static boolean IS_LOADED = false;
    public static boolean IS_KALEIDOSCOPE_TAVERN_LOADED = false;

    public static void init() {
        if (!MainConfig.littleMaidCompatEnabledValue) {
            return;
        }
        ModList.get().getModContainerById(ID).ifPresent(modContainer -> IS_LOADED = true);
        IS_KALEIDOSCOPE_TAVERN_LOADED = ModList.get().isLoaded(KALEIDOSCOPE_TAVERN_ID);
    }

    @LittleMaidExtension
    public static class LittleMaidExtensionImpl implements ILittleMaid {
        @Override
        public void addMaidTask(TaskManager manager) {
            if (MainConfig.littleMaidChoppingBoardEnabledValue) {
                manager.add(new TaskChoppingBoard());
            }
            if (MainConfig.littleMaidMillstoneEnabledValue) {
                manager.add(new TaskMillstone());
            }
            if (IS_KALEIDOSCOPE_TAVERN_LOADED) {
                if (MainConfig.littleMaidPressingTubEnabledValue) {
                    manager.add(new TaskPressingTub());
                }
            }
        }
    }
}