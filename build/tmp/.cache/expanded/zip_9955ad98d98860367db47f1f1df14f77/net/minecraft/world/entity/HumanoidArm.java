package net.minecraft.world.entity;

import net.minecraft.util.OptionEnum;

public enum HumanoidArm implements OptionEnum {
   LEFT(0, "options.mainHand.left"),
   RIGHT(1, "options.mainHand.right");

   private final int id;
   private final String name;

   private HumanoidArm(int pId, String pName) {
      this.id = pId;
      this.name = pName;
   }

   public HumanoidArm getOpposite() {
      return this == LEFT ? RIGHT : LEFT;
   }

   public int getId() {
      return this.id;
   }

   public String getKey() {
      return this.name;
   }
}