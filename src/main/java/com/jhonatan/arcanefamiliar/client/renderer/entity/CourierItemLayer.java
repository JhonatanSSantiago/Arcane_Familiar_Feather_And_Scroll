package com.jhonatan.arcanefamiliar.client.renderer.entity;

import com.jhonatan.arcanefamiliar.entity.OwlEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CourierItemLayer extends RenderLayer<OwlEntity, ChickenModel<OwlEntity>> {

    public CourierItemLayer(RenderLayerParent<OwlEntity, ChickenModel<OwlEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, OwlEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemCarregado = entity.getPacoteCarregado();

        if (!itemCarregado.isEmpty()) {
            poseStack.pushPose();

            // Para pôr o pacote no sítio das garras
            // O valor Y (o do meio) deve ser ajustado. Se -0.4 põe na cabeça, vamos tentar um valor positivo, como 1.4D, para o empurrar para baixo, perto dos pés.
            poseStack.translate(0.0D, 1.4D, 0.0D);

            // Deixa-o plano
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

            // Ajusta o tamanho
            float escala = 0.65F;
            poseStack.scale(escala, escala, escala);

            // Chama o renderizador global de itens do Minecraft
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    itemCarregado,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );

            poseStack.popPose();
        }
    }
}