package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum WallSide implements StringRepresentable {
   NONE("none"),
   LOW("low"),
   TALL("tall");

   private final String name;

   private WallSide(String pName) {
      this.name = pName;
   }

   public String toString() {
      return this.getSerializedName();
   }

   public String getSerializedName() {
      return this.name;
   }
}