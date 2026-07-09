package com.bmt.kaleidoscope_compat.command;

import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_tavern.accessor.BarrelBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("barrel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 6))
                                        .executes(BarrelCommand::setBrewLevel))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(BarrelCommand::resetBrew)))
                .then(Commands.literal("complete")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(BarrelCommand::completeBrew))));
    }

    private static int setBrewLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        int level = IntegerArgumentType.getInteger(context, "level");

        BarrelBlockEntity barrel = getBarrelEntity(source.getLevel(), pos);
        if (barrel == null) {
            source.sendFailure(Component.translatable("command.kaleidoscope_compat.barrel.not_found"));
            return 0;
        }

        ((BarrelBlockEntityAccessor) barrel).setBrewLevel(level);
        barrel.setChanged();
        barrel.refresh();

        source.sendSuccess(() -> Component.translatable("command.kaleidoscope_compat.barrel.set_success", level), true);
        return 1;
    }

    private static int resetBrew(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        BarrelBlockEntity barrel = getBarrelEntity(source.getLevel(), pos);
        if (barrel == null) {
            source.sendFailure(Component.translatable("command.kaleidoscope_compat.barrel.not_found"));
            return 0;
        }

        ((BarrelBlockEntityAccessor) barrel).setBrewLevel(0);
        ((BarrelBlockEntityAccessor) barrel).setBrewTime(-1);
        ((BarrelBlockEntityAccessor) barrel).setRecipeId(null);
        barrel.getOutput().setStackInSlot(0, ItemStack.EMPTY);
        barrel.setChanged();
        barrel.refresh();

        source.sendSuccess(() -> Component.translatable("command.kaleidoscope_compat.barrel.reset_success"), true);
        return 1;
    }

    private static int completeBrew(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        BarrelBlockEntity barrel = getBarrelEntity(source.getLevel(), pos);
        if (barrel == null) {
            source.sendFailure(Component.translatable("command.kaleidoscope_compat.barrel.not_found"));
            return 0;
        }

        ((BarrelBlockEntityAccessor) barrel).setBrewLevel(6);
        ((BarrelBlockEntityAccessor) barrel).setBrewTime(-1);
        barrel.setChanged();
        barrel.refresh();

        source.sendSuccess(() -> Component.translatable("command.kaleidoscope_compat.barrel.complete_success"), true);
        return 1;
    }

    private static BarrelBlockEntity getBarrelEntity(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BarrelBlock)) {
            return null;
        }
        BlockPos originPos = BarrelBlock.getOriginPos(pos, state);
        BlockEntity blockEntity = level.getBlockEntity(originPos);
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return barrel;
        }
        return null;
    }
}