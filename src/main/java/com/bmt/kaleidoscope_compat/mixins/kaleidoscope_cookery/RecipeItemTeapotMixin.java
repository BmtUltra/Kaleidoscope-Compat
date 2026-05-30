package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ITeapot;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeItem.class)
public abstract class RecipeItemTeapotMixin {

    @Unique
    private static final ResourceLocation kaleidoscope_Compat_1_21_1_NeoForge$TEAPOT = ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "teapot");

    @Shadow
    public static void setRecipe(ItemStack stack, RecipeItem.RecipeRecord record) { }

    @Shadow
    public static RecipeItem.RecipeRecord getRecipe(ItemStack stack) { return null; }

    @Shadow
    public static boolean hasRecipe(ItemStack stack) { return false; }

    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    private void onUseOn(net.minecraft.world.item.context.UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.PASS) {
            return;
        }

        ItemStack itemInHand = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(clickedPos);
        Player player = context.getPlayer();

        if (blockEntity == null || player == null) {
            return;
        }

        if (!(blockEntity instanceof ITeapot teapot)) {
            return;
        }

        if (!(blockEntity instanceof TeapotBlockEntity teapotBlockEntity)) {
            return;
        }

        if (hasRecipe(itemInHand)) {
            InteractionResult result = this.kaleidoscope_Compat_1_21_1_NeoForge$onPutTeapotRecipe(teapot, teapotBlockEntity, player, itemInHand);
            cir.setReturnValue(result);
        } else {
            InteractionHand hand = context.getHand();
            InteractionResult result = this.kaleidoscope_Compat_1_21_1_NeoForge$onRecordTeapotRecipe(context.getLevel(), player, teapot, teapotBlockEntity, itemInHand, hand);
            cir.setReturnValue(result);
        }
    }

    @Unique
    private InteractionResult kaleidoscope_Compat_1_21_1_NeoForge$onPutTeapotRecipe(ITeapot teapot, TeapotBlockEntity teapotBlockEntity, Player player, ItemStack itemInHand) {
        RecipeItem.RecipeRecord record = getRecipe(itemInHand);
        if (record == null) {
            return InteractionResult.PASS;
        }

        if (!record.type().equals(kaleidoscope_Compat_1_21_1_NeoForge$TEAPOT)) {
            return InteractionResult.PASS;
        }

        if (teapot.getStatus() != ITeapot.PUT_INGREDIENT) {
            return InteractionResult.PASS;
        }

        if (teapotBlockEntity.getTeaFluidId().equals(
                ResourceLocation.parse("kaleidoscope_cookery:empty"))) {
            return InteractionResult.PASS;
        }

        if (!teapotBlockEntity.getInput().isEmpty()) {
            return InteractionResult.PASS;
        }

        List<ItemStack> inputs = record.input();
        if (inputs.isEmpty()) {
            return InteractionResult.PASS;
        }

        ItemStack inputItem = inputs.getFirst();
        if (inputItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        boolean success = teapot.addIngredient(player.level(), player, inputItem.copy());
        if (success) {
            itemInHand.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Unique
    private InteractionResult kaleidoscope_Compat_1_21_1_NeoForge$onRecordTeapotRecipe(Level level, Player player, ITeapot teapot, TeapotBlockEntity teapotBlockEntity, ItemStack itemInHand, InteractionHand hand) {
        if (teapot.getStatus() != ITeapot.PUT_INGREDIENT) {
            return InteractionResult.PASS;
        }

        if (teapotBlockEntity.getTeaFluidId().equals(
                ResourceLocation.parse("kaleidoscope_cookery:empty"))) {
            return InteractionResult.PASS;
        }

        ItemStack input = teapotBlockEntity.getInput();
        if (input.isEmpty()) {
            return InteractionResult.PASS;
        }

        TeapotInput container = new TeapotInput(input, teapotBlockEntity.getTeaFluidId());

        ItemStack recordStack = itemInHand.copyWithCount(1);
        int count = itemInHand.getCount();
        if (count > 1) {
            ItemStack returnStack = itemInHand.copyWithCount(count - 1);
            if (!player.getInventory().add(returnStack)) {
                player.drop(returnStack, false);
            }
        }

        level.getRecipeManager().getRecipeFor(ModRecipes.TEAPOT_RECIPE, container, level).ifPresentOrElse(recipe -> {
            TeapotRecipe teapotRecipe = recipe.value();
            ItemStack resultItem = teapotRecipe.assemble(container, level.registryAccess());
            setRecipe(recordStack, new RecipeItem.RecipeRecord(
                    List.of(input.copy()), 
                    resultItem,
                    kaleidoscope_Compat_1_21_1_NeoForge$TEAPOT
            ));
        }, () -> setRecipe(recordStack, new RecipeItem.RecipeRecord(
                List.of(input.copy()),
                input.copy(),
                kaleidoscope_Compat_1_21_1_NeoForge$TEAPOT
        )));
        player.setItemInHand(hand, recordStack);
        return InteractionResult.SUCCESS;
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void onGetName(ItemStack pStack, CallbackInfoReturnable<Component> cir) {
        if (hasRecipe(pStack)) {
            RecipeItem.RecipeRecord recipe = getRecipe(pStack);
            if (recipe != null && recipe.type().equals(kaleidoscope_Compat_1_21_1_NeoForge$TEAPOT)) {
                Component result = recipe.output().getHoverName();
                Component type = Component.translatable("block.kaleidoscope_cookery.teapot");
                Component name = Component.translatable("block.kaleidoscope_cookery.recipe_block.has_record", result, type);
                cir.setReturnValue(name);
            }
        }
    }
}