package net.minecraft.world.entity.npc;

import net.minecraft.world.entity.VariantHolder;

public interface VillagerDataHolder extends VariantHolder<VillagerType> {
   VillagerData getVillagerData();

   void setVillagerData(VillagerData pData);

   default VillagerType getVariant() {
      return this.getVillagerData().getType();
   }

   default void setVariant(VillagerType pVariant) {
      this.setVillagerData(this.getVillagerData().setType(pVariant));
   }
}