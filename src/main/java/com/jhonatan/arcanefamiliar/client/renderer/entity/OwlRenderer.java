package com.jhonatan.arcanefamiliar.client.renderer.entity;

import com.jhonatan.arcanefamiliar.ArcaneFamiliar;
import com.jhonatan.arcanefamiliar.entity.OwlEntity;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class OwlRenderer extends MobRenderer<OwlEntity, ChickenModel<OwlEntity>> {

    public OwlRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3f);

        // Adiciona a camada corrigida sem o operador diamante problemático
        this.addLayer(new CourierItemLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(OwlEntity entity) {
        // Retorna exclusivamente a textura da coruja
        return new ResourceLocation(ArcaneFamiliar.MODID, "textures/entity/owl.png");
    }
}