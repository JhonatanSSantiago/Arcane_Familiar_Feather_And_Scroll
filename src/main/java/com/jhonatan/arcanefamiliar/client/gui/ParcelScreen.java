package com.jhonatan.arcanefamiliar.client.gui;

import com.jhonatan.arcanefamiliar.menu.ParcelMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParcelScreen extends AbstractContainerScreen<ParcelMenu> {

    // Caminho para a textura da interface (vamos precisar de adicionar esta imagem depois!)
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("arcanefamiliar:textures/gui/parcel_gui.png");

    private EditBox recipientBox; // Nova caixa de texto para o destinatário
    private List<String> jogadoresOnline = new ArrayList<>(); // Guarda quem está no servidor

    public ParcelScreen(ParcelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelX = 10000;

        // Carrega a lista de jogadores online logo ao abrir a tela
        if (Minecraft.getInstance().getConnection() != null) {
            jogadoresOnline = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                    .map(info -> info.getProfile().getName())
                    .collect(java.util.stream.Collectors.toList());
        }

        // Caixa do Destinatário à esquerda
        this.recipientBox = new EditBox(this.font, this.leftPos + 8, this.topPos + 20, 68, 16, Component.literal("Destinatário"));
        this.recipientBox.setMaxLength(30);
        this.addRenderableWidget(this.recipientBox);

        // Botão Lacrar Pacote à direita
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Lacrar"), button -> {
            String destinatario = this.recipientBox.getValue().trim();

            // Bloqueia se o campo estiver vazio
            if (destinatario.isEmpty()) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("§cPreencha o nome do destinatário!"), true);
                }
                return;
            }

            // O SEGREDO ESTÁ AQUI: Envia a instrução de selagem para o servidor (consumindo o fio)
            com.jhonatan.arcanefamiliar.network.ModMessages.sendToServer(
                    new com.jhonatan.arcanefamiliar.network.SealParcelC2SPacket(destinatario)
            );

            // Fecha a interface após selar
            this.onClose();

        }).bounds(this.leftPos + 104, this.topPos + 18, 64, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos; // Posição X do baú
        int y = this.topPos;  // Posição Y do baú

        // Desenha a imagem de fundo
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics); // Escurece o fundo do jogo
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Escreve o texto "De:" na interface
        String playerName = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "Desconhecido";

        // Mantemos apenas o "De:" no topo esquerdo do ecrã
        guiGraphics.drawString(this.font, "De: " + playerName, this.leftPos + 8, this.topPos + 6, 0x404040, false);
        // O "Para:" foi removido, pois a caixa de texto já é autoexplicativa e ganhamos espaço visual

        // Lógica visual do Dropdown (Lista Suspensa) de jogadores
        if (this.recipientBox.isFocused() && !jogadoresOnline.isEmpty()) {
            int dropX = this.recipientBox.getX();
            int dropY = this.recipientBox.getY() + this.recipientBox.getHeight();
            int dropWidth = this.recipientBox.getWidth();
            int dropHeight = jogadoresOnline.size() * 12 + 4;

            // Fundo da lista (escuro)
            guiGraphics.fill(dropX, dropY, dropX + dropWidth, dropY + dropHeight, 0xFF222222);
            guiGraphics.renderOutline(dropX, dropY, dropWidth, dropHeight, 0xFF555555);

            for (int i = 0; i < jogadoresOnline.size(); i++) {
                String nome = jogadoresOnline.get(i);
                int textY = dropY + 4 + (i * 12);
                int corTexto = 0xAAAAAA;

                // Efeito visual ao passar o rato por cima do nome (Hover)
                if (mouseX >= dropX && mouseX <= dropX + dropWidth && mouseY >= textY && mouseY < textY + 12) {
                    guiGraphics.fill(dropX + 1, textY - 1, dropX + dropWidth - 1, textY + 11, 0xFF444444);
                    corTexto = 0xFFFFFF; // Fica branco
                }
                guiGraphics.drawString(this.font, nome, dropX + 4, textY, corTexto, false);
            }
        }

        renderTooltip(guiGraphics, mouseX, mouseY); // Mostra o nome dos itens ao passar o rato
    }

    // LÓGICA DE CLIQUE: O que acontece quando clica num nome da lista
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipientBox.isFocused() && !jogadoresOnline.isEmpty() && button == 0) {
            int dropX = this.recipientBox.getX();
            int dropY = this.recipientBox.getY() + this.recipientBox.getHeight();
            int dropWidth = this.recipientBox.getWidth();
            int dropHeight = jogadoresOnline.size() * 12 + 4;

            // Verifica se o clique foi dentro da nossa lista suspensa
            if (mouseX >= dropX && mouseX <= dropX + dropWidth && mouseY >= dropY && mouseY <= dropY + dropHeight) {
                int index = (int) ((mouseY - dropY - 4) / 12); // Descobre em qual nome clicou
                if (index >= 0 && index < jogadoresOnline.size()) {
                    this.recipientBox.setValue(jogadoresOnline.get(index)); // Preenche o nome
                    this.recipientBox.setFocused(false); // Fecha a lista automaticamente
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ESSENCIAL: Permite que as teclas funcionem na caixa de texto em vez de fecharem o inventário
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.recipientBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        // Impede que a tecla de abrir o inventário (ex: tecla 'E') feche a interface enquanto escrevemos um nome que contenha a letra 'e'
        if (this.recipientBox.isFocused() && keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}