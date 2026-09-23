package com.jhonatan.arcanefamiliar.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ReadParchmentScreen extends Screen {
    private final String autor;
    private final String destinatario;
    private final String mensagem;

    // Construtor: Recebe os dados quando a tela é aberta
    public ReadParchmentScreen(String autor, String destinatario, String mensagem) {
        super(Component.literal("Lendo Pergaminho"));
        this.autor = autor;
        this.destinatario = destinatario;
        this.mensagem = mensagem;
    }

    @Override
    protected void init() {
        super.init();
        int largura = 240;
        int altura = 200;
        int x = (this.width - largura) / 2;
        int y = (this.height - altura) / 2;

        // Botão "X" no topo direito (pequeno quadrado 15x15)
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("X"), button -> {
            this.onClose();
        }).bounds(x + largura - 20, y + 5, 15, 15).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int largura = 240;
        int altura = 200;
        int x = (this.width - largura) / 2;
        int y = (this.height - altura) / 2;

        // Fundo cor de papel e borda
        guiGraphics.fill(x, y, x + largura, y + altura, 0xFFF5E4B5);
        guiGraphics.renderOutline(x, y, largura, altura, 0xFF333333);

        // Desenha o Cabeçalho
        guiGraphics.drawString(this.font, "De: " + autor, x + 10, y + 10, 0x333333, false);
        guiGraphics.drawString(this.font, "Para: " + destinatario, x + 10, y + 25, 0x333333, false);

        // Desenha uma linha decorativa para separar o cabeçalho do texto
        guiGraphics.fill(x + 10, y + 40, x + largura - 10, y + 41, 0xFF888888);

        // Desenha a Mensagem (o drawWordWrap faz o texto quebrar de linha automaticamente!)
        guiGraphics.drawWordWrap(this.font, Component.literal(mensagem), x + 10, y + 48, largura - 20, 0x333333);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}