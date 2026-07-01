package com.bmt.kaleidoscope_compat.mixins.kaleidoscope_doll;

import com.bmt.kaleidoscope_compat.network.RequestPlayerDollPayload;
import com.github.ysbbbbbb.kaleidoscopedoll.client.gui.ComputerMenuScreen;
import com.github.ysbbbbbb.kaleidoscopedoll.client.gui.DollButton;
import com.github.ysbbbbbb.kaleidoscopedoll.config.GeneralConfig;
import com.github.ysbbbbbb.kaleidoscopedoll.data.custom.ServerCustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.inventory.ComputerMenu;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.network.message.ComputerDollClickMessage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.StreamSupport;

@Mixin(ComputerMenuScreen.class)
public abstract class ComputerMenuScreenMixin extends AbstractContainerScreen<ComputerMenu> {

    @Shadow
    private EditBox searchField;

    public ComputerMenuScreenMixin(ComputerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "initSearch", at = @At("TAIL"))
    private void onInitSearch(CallbackInfo ci) {
        this.searchField.setWidth(65);
    }

    @Inject(method = "initDollButtons", at = @At("HEAD"), cancellable = true)
    private void onInitDollButtons(CallbackInfo ci) {
        String searchText = this.searchField.getValue().toLowerCase(java.util.Locale.ENGLISH);
        if (searchText.startsWith("@")) {
            kaleidoscopeCompat$showAllDolls();
            ci.cancel();
        }
    }

    @Unique
    private void kaleidoscopeCompat$showAllDolls() {
        ServerCustomDollLoader.getModels().forEach((id) -> {
            ItemStack stack = ModItems.CUSTOM_DOLL.get().getDefaultInstance();
            CustomDollItem.setModelId(stack, id);
            this.dolls.add(stack);
        });
        Iterable<Holder<Item>> tags = BuiltInRegistries.ITEM.getTagOrEmpty(TagItem.PLAYER_DOLLS);
        this.dolls.addAll(StreamSupport.stream(tags.spliterator(), false).map(Holder::value).map(Item::getDefaultInstance).filter(this::kaleidoscope_Compat_Dev_1$filterSponsoredDoll).toList());

        int start = this.currentPage * 32;
        int end = Math.min(start + 32, this.dolls.size());
        java.util.List<ItemStack> dollsToShow = this.dolls.subList(start, end);
        int xPos = this.leftPos + 7;
        int yPos = this.topPos + 17;
        int xOffset = 18;
        int yOffset = 18;

        for (int i = 0; i < dollsToShow.size(); ++i) {
            ItemStack item = dollsToShow.get(i);
            int x = xPos + i % 8 * xOffset;
            int y = yPos + i / 8 * yOffset;
            this.addRenderableWidget(new DollButton(x, y, item, (button) -> {
                button.setFocused(!button.isFocused());
                PacketDistributor.sendToServer(new ComputerDollClickMessage(item));
            }));
        }
    }

    @Unique
    private boolean kaleidoscope_Compat_Dev_1$filterSponsoredDoll(ItemStack item) {
        if (GeneralConfig.ENABLE_SPONSORED_DOLL.get()) {
            return true;
        } else {
            return !item.is(com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem.SPONSORED_DOLLS);
        }
    }

    @Shadow
    private java.util.List<ItemStack> dolls;

    @Shadow
    private int currentPage;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        Button btn = Button.builder(Component.literal("✔"), button -> {
            String text = this.searchField.getValue();
            kaleidoscopeCompat$tryCreatePlayerDoll(text);
            this.searchField.setValue("");
            this.searchField.setValue(text);
        }).bounds(this.leftPos + 83, this.topPos + 98, 16, 16).build();
        this.addRenderableWidget(btn);
    }

    @Unique
    private void kaleidoscopeCompat$tryCreatePlayerDoll(String text) {
        if (text.startsWith("@") && text.length() > 1) {
            String playerId = text.substring(1);
            if (playerId.matches("^[a-zA-Z][a-zA-Z0-9_]{2,15}$")) {
                PacketDistributor.sendToServer(new RequestPlayerDollPayload(playerId));
            }
        }
    }
}