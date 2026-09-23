package com.jhonatan.arcanefamiliar.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ParchmentScreen extends Screen {
    private MultiLineEditBox textBox;

    public ParchmentScreen() {
        super(Component.literal("Escrever Pergaminho"));
    }

    @Override
    protected void init() {
        super.init();

        // Define o tamanho da área de escrita e centraliza no ecrã
        int largura = 220;
        int altura = 160;
        int x = (this.width - largura) / 2;
        int y = (this.height - altura) / 2;

        // Inicializa a caixa de texto
        this.textBox = new MultiLineEditBox(this.font, x, y, largura, altura,
                Component.literal("Texto"),
                Component.literal("Escreva a sua mensagem aqui..."));
        //permite escrever
        this.addRenderableWidget(this.textBox);
        // Define o tamanho padrão dos botões
        int larguraBotao = 120;
        int alturaBotao = 20;
        // Botão Assinar e Selar (Posicionado à esquerda, abaixo da caixa de texto)
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Assinar e Selar"), button -> {
            // No futuro, colocaremos aqui o código para enviar o texto ao servidor (NBT)
            System.out.println("Texto a ser salvo: " + this.textBox.getValue());
            // ENVIAR A MENSAGEM PARA O SERVIDOR!
            com.jhonatan.arcanefamiliar.network.ModMessages.sendToServer(
                    new com.jhonatan.arcanefamiliar.network.SealParchmentC2SPacket(this.textBox.getValue())
            );
            this.onClose();
        }).bounds(x, y + altura + 10, larguraBotao, alturaBotao).build());

        // Botão Cancelar (Posicionado à direita, abaixo da caixa de texto)
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Cancelar"), button -> {
            this.onClose(); // Apenas fecha a interface descartando o texto
        }).bounds(x + largura - larguraBotao, y + altura + 10, larguraBotao, alturaBotao).build());

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics); // Escurece o fundo do jogo
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Permite que o jogo (ex: as aves) continue a mover-se no fundo
    }
}