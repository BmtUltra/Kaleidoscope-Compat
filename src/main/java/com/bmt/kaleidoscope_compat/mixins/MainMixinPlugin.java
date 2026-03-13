package com.bmt.kaleidoscope_compat.mixins;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MainMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("Initializing Kaleidoscope Compat Mixins");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String[] split = mixinClassName.split("\\.");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("mixins") && FMLLoader.getLoadingModList().getModFileById(split[i + 1]) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
