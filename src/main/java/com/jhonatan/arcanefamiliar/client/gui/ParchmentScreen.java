package com.jhonatan.arcanefamiliar.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParchmentScreen extends Screen {
    private MultiLineEditBox textBox;
    private EditBox recipientBox; // Nova caixa para o destinatário
    private List<String> jogadoresOnline = new ArrayList<>(); // Guarda quem está no servidor

    public ParchmentScreen() {
        super(Component.literal("Escrever Pergaminho"));
    }

    @Override
    protected void init() {
        super.init();

        // Define o tamanho da área de escrita e centraliza no ecrã
        int largura = 240;
        int altura = 200;
        int x = (this.width - largura) / 2;
        int y = (this.height - altura) / 2;

        // Carrega a lista de jogadores online logo ao abrir a tela
        if (Minecraft.getInstance().getConnection() != null) {
            jogadoresOnline = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                    .map(info -> info.getProfile().getName())
                    .collect(Collectors.toList());
        }

        // Caixa do Destinatário (EditBox de 1 linha)
        this.recipientBox = new EditBox(this.font, x + 40, y + 25, 185, 16, Component.literal("Destinatário"));
        this.recipientBox.setMaxLength(30);
        this.addRenderableWidget(this.recipientBox);

        // Caixa da Mensagem Principal
        this.textBox = new MultiLineEditBox(this.font, x + 10, y + 50, largura - 20, 110,
                Component.literal("Texto"),
                Component.literal("Escreva a sua mensagem aqui..."));
        // permite escrever
        this.addRenderableWidget(this.textBox);

        // Define o tamanho padrão dos botões
        int larguraBotao = 120;
        int alturaBotao = 20;

        // Botão "X" no topo direito (idêntico ao da tela de leitura)
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("X"), button -> {
            this.onClose();
        }).bounds(x + largura - 20, y + 5, 15, 15).build());

        // Botão Assinar e Selar
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Assinar e Selar"), button -> {
            String destinatario = this.recipientBox.getValue().trim();

            // Remove o @ caso o jogador tenha deixado (mantido como precaução)
            if (destinatario.startsWith("@")) {
                destinatario = destinatario.substring(1);
            }

            // VALIDAÇÃO: Bloqueia se o campo estiver vazio
            if (destinatario.isEmpty()) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("§cPreencha o nome do destinatário!"), true);
                }
                this.onClose(); // Fecha a interface caso não tenha destinatário
                return; // Interrompe o código e não envia a carta
            }

            // ENVIAR A MENSAGEM PARA O SERVIDOR!
            com.jhonatan.arcanefamiliar.network.ModMessages.sendToServer(
                    new com.jhonatan.arcanefamiliar.network.SealParchmentC2SPacket(this.textBox.getValue(), destinatario)
            );
            this.onClose();
        }).bounds(x + (largura - larguraBotao) / 2, y + altura - 30, larguraBotao, alturaBotao).build());

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics); // Escurece o fundo do jogo

        int largura = 240;
        int altura = 200;
        int x = (this.width - largura) / 2;
        int y = (this.height - altura) / 2;

        // Desenha o fundo cor de papel (Hex: #F5E4B5 -> ARGB: 0xFFF5E4B5)
        guiGraphics.fill(x, y, x + largura, y + altura, 0xFFF5E4B5);
        // Desenha uma borda escura à volta do papel
        guiGraphics.renderOutline(x, y, largura, altura, 0xFF333333);

        // Escreve os textos na interface (Cor 0x333333 é um cinza bem escuro, estilo tinta)
        String playerName = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "Desconhecido";
        guiGraphics.drawString(this.font, "De: " + playerName, x + 10, y + 10, 0x333333, false);
        guiGraphics.drawString(this.font, "Para:", x + 10, y + 29, 0x333333, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Desenha a Lista Suspensa (Dropdown) de jogadores online
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

        @Override
        public boolean isPauseScreen() {
            return false; // Permite que o jogo (ex: as aves) continue a mover-se no fundo
        }
}