package com.bmt.kaleidoscope_compat.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.fluid.JuiceFluidType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@SuppressWarnings("all")
public class KCFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, KaleidoscopeTavern.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, KaleidoscopeTavern.MOD_ID);

    public static final ResourceLocation WHITE_GRAPE_JUICE_ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeTavern.MOD_ID, "white_grape_juice");
    public static final ResourceLocation FLOWING_WHITE_GRAPE_JUICE_ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeTavern.MOD_ID, "flowing_white_grape_juice");

    public static final ResourceLocation APPLE_JUICE_ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeTavern.MOD_ID, "apple_juice");
    public static final ResourceLocation FLOWING_APPLE_JUICE_ID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeTavern.MOD_ID, "flowing_apple_juice");


    public static final Supplier<FluidType> WHITE_GRAPE_JUICE_TYPE = FLUID_TYPES.register(
            "white_grape_juice",
            () -> new JuiceFluidType(WHITE_GRAPE_JUICE_ID, 0)
    );

    public static final Supplier<FluidType> APPLE_JUICE_TYPE = FLUID_TYPES.register(
            "apple_juice",
            () -> new JuiceFluidType(APPLE_JUICE_ID, 0)
    );


    public static final Supplier<BaseFlowingFluid.Source> WHITE_GRAPE_JUICE = FLUIDS.register(
            "white_grape_juice",
            () -> new BaseFlowingFluid.Source(whiteGrapeJuiceProperties())
    );

    public static final Supplier<BaseFlowingFluid.Flowing> FLOWING_WHITE_GRAPE_JUICE = FLUIDS.register(
            "flowing_white_grape_juice",
            () -> new BaseFlowingFluid.Flowing(whiteGrapeJuiceProperties())
    );

    public static final Supplier<BaseFlowingFluid.Source> APPLE_JUICE = FLUIDS.register(
            "apple_juice",
            () -> new BaseFlowingFluid.Source(appleJuiceProperties())
    );

    public static final Supplier<BaseFlowingFluid.Flowing> FLOWING_APPLE_JUICE = FLUIDS.register(
            "flowing_apple_juice",
            () -> new BaseFlowingFluid.Flowing(appleJuiceProperties())
    );


    private static BaseFlowingFluid.Properties whiteGrapeJuiceProperties() {
        return new BaseFlowingFluid.Properties(
                WHITE_GRAPE_JUICE_TYPE,
                WHITE_GRAPE_JUICE,
                FLOWING_WHITE_GRAPE_JUICE
        ).bucket(KCItems.WHITE_GRAPE_JUICE_BUCKET);
    }

    private static BaseFlowingFluid.Properties appleJuiceProperties() {
        return new BaseFlowingFluid.Properties(
                APPLE_JUICE_TYPE,
                APPLE_JUICE,
                FLOWING_APPLE_JUICE
        ).bucket(KCItems.APPLE_JUICE_BUCKET);
    }
}