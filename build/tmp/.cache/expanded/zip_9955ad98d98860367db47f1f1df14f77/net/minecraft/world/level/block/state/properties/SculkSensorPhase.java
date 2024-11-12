package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum SculkSensorPhase implements StringRepresentable {
   INACTIVE("inactive"),
   ACTIVE("active"),
   COOLDOWN("cooldown");

   private final String name;

   private SculkSensorPhase(String pName) {
      this.name = pName;
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}