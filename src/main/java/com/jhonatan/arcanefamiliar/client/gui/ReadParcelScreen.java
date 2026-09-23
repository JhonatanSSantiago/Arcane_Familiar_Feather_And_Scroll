package com.jhonatan.arcanefamiliar.client.gui;

import com.jhonatan.arcanefamiliar.menu.ReadParcelMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ReadParcelScreen extends AbstractContainerScreen<ReadParcelMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("arcanefamiliar:textures/gui/parcel_gui.png");

    public ReadParcelScreen(ReadParcelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelX = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Renderiza os nomes recebidos pelo Menu
        guiGraphics.drawString(this.font, "De: " + this.menu.getAuthor(), this.leftPos + 8, this.topPos + 6, 0x404040, false);
        guiGraphics.drawString(this.font, "Para: " + this.menu.getRecipient(), this.leftPos + 8, this.topPos + 24, 0x404040, false);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}