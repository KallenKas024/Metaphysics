package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Giant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantZombieModel extends AbstractZombieModel<Giant> {
   public GiantZombieModel(ModelPart pRoot) {
      super(pRoot);
   }

   public boolean isAggressive(Giant pEntity) {
      return false;
   }
}