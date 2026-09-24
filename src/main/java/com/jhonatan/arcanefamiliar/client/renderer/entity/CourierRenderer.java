package com.jhonatan.arcanefamiliar.client.renderer.entity;

import com.jhonatan.arcanefamiliar.entity.CourierEntity;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CourierRenderer extends MobRenderer<CourierEntity, ChickenModel<CourierEntity>> {

    // Textura da galinha com o namespace "minecraft" explícito para remover o aviso
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/chicken.png");

    public CourierRenderer(EntityRendererProvider.Context context) {
        // Usa o modelo da galinha que aceita a nossa entidade sem erros de restrição
        super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(CourierEntity entity) {
        return TEXTURE;
    }
}