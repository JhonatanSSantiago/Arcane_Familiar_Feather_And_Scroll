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

public class SealedParchmentItem extends Item {

    public SealedParchmentItem(Properties properties) {
        super(properties);
    }

    // Este método é chamado quando o jogador clica com o botão direito a segurar o item
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // As interfaces gráficas (Telas) só existem no lado do "Cliente" (o seu ecrã)
        if (level.isClientSide()) {
            // Toca o som de manusear papel no lado do cliente
            player.playSound(net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);

            CompoundTag nbt = stack.getTag();

            // Valores padrão caso dê algum erro e o item não tenha NBT
            String autor = "Desconhecido";
            String destinatario = "Desconhecido";
            String mensagem = "";

            if (nbt != null) {
                if (nbt.contains("Author")) autor = nbt.getString("Author");
                if (nbt.contains("Recipient")) destinatario = nbt.getString("Recipient");
                if (nbt.contains("MessageText")) mensagem = nbt.getString("MessageText");
            }

            // Abre a tela de leitura passando os dados que acabámos de ler
            Minecraft.getInstance().setScreen(new com.jhonatan.arcanefamiliar.client.gui.ReadParchmentScreen(autor, destinatario, mensagem));
        }

        return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        // Lê a base de dados (NBT) do item
        CompoundTag nbt = stack.getTag();

        if (nbt != null) {
            if (nbt.contains("Recipient")) {
                tooltip.add(Component.literal("§cPara: §f" + nbt.getString("Recipient")));
            }
            // Se tiver autor, adiciona uma linha amarela
            if (nbt.contains("Author")) {
                tooltip.add(Component.literal("§7Assinado por: §e" + nbt.getString("Author")));
            }
            // Se tiver mensagem, adiciona uma linha cinzenta
            if (nbt.contains("MessageText")) {
                tooltip.add(Component.literal("§8" + nbt.getString("MessageText")));
            }
        }
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}