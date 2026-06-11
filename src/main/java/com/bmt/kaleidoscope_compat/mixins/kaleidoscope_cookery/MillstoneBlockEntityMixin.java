package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery;

import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.BlockEntityAccessor;
import com.bmt.kaleidoscope_compat.mixins.kaleidoscope_cookery.accessor.MillstoneBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.MillstoneBlockEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MillstoneBlockEntity.class)
public abstract class MillstoneBlockEntityMixin {
    @Shadow
    public abstract ItemStack getOutput();

    @Unique
    private MillstoneBlockEntityAccessor kaleidoscope_Compat_1_21_1_NeoForge$getAccessor() {
        return (MillstoneBlockEntityAccessor) this;
    }

    @Unique
    private BlockEntityAccessor kaleidoscope_Compat_1_21_1_NeoForge$getBlockEntityAccessor() {
        return (BlockEntityAccessor) this;
    }

    @Inject(method = "onPutItem", at = @At("HEAD"), cancellable = true)
    private void onOnPutItem(Level level, ItemStack putOnItem, CallbackInfoReturnable<Boolean> cir) {
        MillstoneBlockEntityAccessor accessor = kaleidoscope_Compat_1_21_1_NeoForge$getAccessor();
        BlockEntityAccessor blockEntityAccessor = kaleidoscope_Compat_1_21_1_NeoForge$getBlockEntityAccessor();

        if (!this.getOutput().isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        if (accessor.getProgress() > 0 && !accessor.getInput().isEmpty()) {
            if (!com.bmt.kaleidoscope_compat.config.MainConfig.millstoneStackingEnabled) {
                cir.setReturnValue(false);
                return;
            }

            if (!ItemStack.isSameItemSameComponents(accessor.getInput(), putOnItem)) {
                cir.setReturnValue(false);
                return;
            }

            int currentCount = accessor.getInput().getCount();
            int canAdd = Math.min(putOnItem.getCount(), MillstoneBlockEntity.MAX_INPUT_COUNT - currentCount);
            if (canAdd <= 0) {
                cir.setReturnValue(false);
                return;
            }
            accessor.getInput().grow(canAdd);
            putOnItem.shrink(canAdd);

            accessor.setProgress(Math.max(Math.round(accessor.getRotSpeedTick()), 1));

            blockEntityAccessor.invokeSetChanged();
            Level world = blockEntityAccessor.getLevel();
            if (world != null) {
                BlockState state = ((MillstoneBlockEntity) (Object) this).getBlockState();
                world.sendBlockUpdated(blockEntityAccessor.getWorldPosition(), state, state, Block.UPDATE_ALL);
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canBindEntity", at = @At("HEAD"), cancellable = true)
    private void onCanBindEntity(Mob mob, CallbackInfoReturnable<Boolean> cir) {
        String className = mob.getClass().getName();
        if ("com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid".equals(className)) {
            try {
                Object task = mob.getClass().getMethod("getTask").invoke(mob);
                String uid = task.getClass().getMethod("getUid").invoke(task).toString();
                if (uid.contains("millstone")) {
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            } catch (Exception ignored) {
            }
        }
    }
}