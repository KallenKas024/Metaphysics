package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.model.ChestedHorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestedHorseRenderer<T extends AbstractChestedHorse> extends AbstractHorseRenderer<T, ChestedHorseModel<T>> {
   private static final Map<EntityType<?>, ResourceLocation> MAP = Maps.newHashMap(ImmutableMap.of(EntityType.DONKEY, new ResourceLocation("textures/entity/horse/donkey.png"), EntityType.MULE, new ResourceLocation("textures/entity/horse/mule.png")));

   public ChestedHorseRenderer(EntityRendererProvider.Context pContext, float pScale, ModelLayerLocation pLayer) {
      super(pContext, new ChestedHorseModel<>(pContext.bakeLayer(pLayer)), pScale);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(T pEntity) {
      return MAP.get(pEntity.getType());
   }
}