package com.bmt.kaleidoscope_compat.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.fluid.JuiceFluidType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

@SuppressWarnings("all")
public class KCFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, KaleidoscopeTavern.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, KaleidoscopeTavern.MOD_ID);

    public static final ResourceLocation WHITE_GRAPE_JUICE_ID = new ResourceLocation(KaleidoscopeTavern.MOD_ID, "white_grape_juice");
    public static final ResourceLocation FLOWING_WHITE_GRAPE_JUICE_ID = new ResourceLocation(KaleidoscopeTavern.MOD_ID, "flowing_white_grape_juice");

    public static final ResourceLocation APPLE_JUICE_ID = new ResourceLocation(KaleidoscopeTavern.MOD_ID, "apple_juice");
    public static final ResourceLocation FLOWING_APPLE_JUICE_ID = new ResourceLocation(KaleidoscopeTavern.MOD_ID, "flowing_apple_juice");

    public static final Supplier<FluidType> WHITE_GRAPE_JUICE_TYPE = FLUID_TYPES.register(
            "white_grape_juice",
            () -> new JuiceFluidType(WHITE_GRAPE_JUICE_ID, 0)
    );

    public static final Supplier<FluidType> APPLE_JUICE_TYPE = FLUID_TYPES.register(
            "apple_juice",
            () -> new JuiceFluidType(APPLE_JUICE_ID, 0)
    );

    public static final Supplier<ForgeFlowingFluid.Source> WHITE_GRAPE_JUICE = FLUIDS.register(
            "white_grape_juice",
            () -> new ForgeFlowingFluid.Source(whiteGrapeJuiceProperties())
    );

    public static final Supplier<ForgeFlowingFluid.Flowing> FLOWING_WHITE_GRAPE_JUICE = FLUIDS.register(
            "flowing_white_grape_juice",
            () -> new ForgeFlowingFluid.Flowing(whiteGrapeJuiceProperties())
    );

    public static final Supplier<ForgeFlowingFluid.Source> APPLE_JUICE = FLUIDS.register(
            "apple_juice",
            () -> new ForgeFlowingFluid.Source(appleJuiceProperties())
    );

    public static final Supplier<ForgeFlowingFluid.Flowing> FLOWING_APPLE_JUICE = FLUIDS.register(
            "flowing_apple_juice",
            () -> new ForgeFlowingFluid.Flowing(appleJuiceProperties())
    );

    private static ForgeFlowingFluid.Properties whiteGrapeJuiceProperties() {
        return new ForgeFlowingFluid.Properties(
                WHITE_GRAPE_JUICE_TYPE,
                WHITE_GRAPE_JUICE,
                FLOWING_WHITE_GRAPE_JUICE
        ).bucket(KCItems.WHITE_GRAPE_JUICE_BUCKET);
    }

    private static ForgeFlowingFluid.Properties appleJuiceProperties() {
        return new ForgeFlowingFluid.Properties(
                APPLE_JUICE_TYPE,
                APPLE_JUICE,
                FLOWING_APPLE_JUICE
        ).bucket(KCItems.APPLE_JUICE_BUCKET);
    }
}