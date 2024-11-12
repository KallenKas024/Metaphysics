package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GraphicsStatus implements OptionEnum {
   FAST(0, "options.graphics.fast"),
   FANCY(1, "options.graphics.fancy"),
   FABULOUS(2, "options.graphics.fabulous");

   private static final IntFunction<GraphicsStatus> BY_ID = ByIdMap.continuous(GraphicsStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
   private final int id;
   private final String key;

   private GraphicsStatus(int pId, String pKey) {
      this.id = pId;
      this.key = pKey;
   }

   public int getId() {
      return this.id;
   }

   public String getKey() {
      return this.key;
   }

   public String toString() {
      String s;
      switch (this) {
         case FAST:
            s = "fast";
            break;
         case FANCY:
            s = "fancy";
            break;
         case FABULOUS:
            s = "fabulous";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return s;
   }

   public static GraphicsStatus byId(int pId) {
      return BY_ID.apply(pId);
   }
}