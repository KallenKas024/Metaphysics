package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Represents individual steps that the features and carvers chunk status go through, respectively.
 */
public class GenerationStep {
   public static enum Carving implements StringRepresentable {
      AIR("air"),
      LIQUID("liquid");

      public static final Codec<GenerationStep.Carving> CODEC = StringRepresentable.fromEnum(GenerationStep.Carving::values);
      private final String name;

      private Carving(String pName) {
         this.name = pName;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public static enum Decoration implements StringRepresentable {
      RAW_GENERATION("raw_generation"),
      LAKES("lakes"),
      LOCAL_MODIFICATIONS("local_modifications"),
      UNDERGROUND_STRUCTURES("underground_structures"),
      SURFACE_STRUCTURES("surface_structures"),
      STRONGHOLDS("strongholds"),
      UNDERGROUND_ORES("underground_ores"),
      UNDERGROUND_DECORATION("underground_decoration"),
      FLUID_SPRINGS("fluid_springs"),
      VEGETAL_DECORATION("vegetal_decoration"),
      TOP_LAYER_MODIFICATION("top_layer_modification");

      public static final Codec<GenerationStep.Decoration> CODEC = StringRepresentable.fromEnum(GenerationStep.Decoration::values);
      private final String name;

      private Decoration(String pName) {
         this.name = pName;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}