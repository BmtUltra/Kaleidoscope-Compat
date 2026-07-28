package com.bmt.kaleidoscope_compat.mixins;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MainMixinPlugin implements IMixinConfigPlugin {

    private static boolean isModLoaded(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }

    private static boolean isClassPresent() {
        try {
            Class.forName("com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation", false, MainMixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".create.")) {
            return isModLoaded("create");
        }

        if (mixinClassName.contains("GoggleMixin")) {
            return isModLoaded("create")
                    && isClassPresent();
        }

        if (mixinClassName.contains(".kaleidoscope_doll.")) {
            return isModLoaded("kaleidoscope_doll");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}