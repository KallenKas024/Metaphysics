package net.minecraft.world.entity.ambient;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class AmbientCreature extends Mob {
   protected AmbientCreature(EntityType<? extends AmbientCreature> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public boolean canBeLeashed(Player pPlayer) {
      return false;
   }
}