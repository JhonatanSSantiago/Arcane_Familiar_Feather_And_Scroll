package com.jhonatan.arcanefamiliar.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenedParchmentItem extends Item {

    public OpenedParchmentItem(Properties properties) {
        super(properties);
    }

    // Este método é chamado quando o jogador clica com o botão direito a segurar o item
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Como o item já está aberto, não precisamos fazer nada no servidor (ele não se gasta).
        // Apenas mostramos a interface de leitura no lado do "Cliente" (o ecrã do jogador).
        if (level.isClientSide()) {

            // Toca um som suave de folhear uma página de livro
            player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);

            CompoundTag nbt = stack.getTag();

            // Valores padrão caso dê algum erro e o item não tenha NBT
            String autor = "Desconhecido";
            String destinatario = "Desconhecido";
            String mensagem = "";

            if (nbt != null) {
                if (nbt.contains("Author")) autor = nbt.getString("Author");
                // Lê a chave "DestinatarioNome" para saber para quem era
                if (nbt.contains("DestinatarioNome")) destinatario = nbt.getString("DestinatarioNome");
                if (nbt.contains("MessageText")) mensagem = nbt.getString("MessageText");
            }

            // Abre a tela de leitura passando os dados que acabámos de ler
            Minecraft.getInstance().setScreen(new com.jhonatan.arcanefamiliar.client.gui.ReadParchmentScreen(autor, destinatario, mensagem));
        }

        // sidedSuccess garante que a animação da mão (balanço) acontece corretamente
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        // Lê a base de dados (NBT) do item
        CompoundTag nbt = stack.getTag();

        if (nbt != null) {
            // Mostra o Destinatário
            if (nbt.contains("DestinatarioNome")) {
                tooltip.add(Component.literal("§cPara: §f" + nbt.getString("DestinatarioNome")));
            }
            // Mostra o Autor
            if (nbt.contains("Author")) {
                tooltip.add(Component.literal("§7Assinado por: §e" + nbt.getString("Author")));
            }
            // Mostra a Mensagem
            if (nbt.contains("MessageText")) {
                tooltip.add(Component.literal("§8" + nbt.getString("MessageText")));
            }
        }
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}