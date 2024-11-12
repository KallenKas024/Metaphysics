package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;

/**
 * Defines an entity {@linkplain net.minecraft.world.entity.ai.attributes.Attribute attribute} that is limited to a
 * range of values.
 */
public class RangedAttribute extends Attribute {
   /** The lowest possible value for the attribute. */
   private final double minValue;
   /** The highest possible value for the attribute. */
   private final double maxValue;

   public RangedAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
      super(pDescriptionId, pDefaultValue);
      this.minValue = pMin;
      this.maxValue = pMax;
      if (pMin > pMax) {
         throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
      } else if (pDefaultValue < pMin) {
         throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
      } else if (pDefaultValue > pMax) {
         throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
      }
   }

   /**
    * Gets the lowest possible value for the attribute.
    * @return The lowest possible value for the attribute; {@link #minValue}.
    */
   public double getMinValue() {
      return this.minValue;
   }

   /**
    * Gets the highest possible value for the attribute.
    * @return The highest possible value for the attribute; {@link #maxValue}.
    */
   public double getMaxValue() {
      return this.maxValue;
   }

   /**
    * Sanitizes the value of the attribute to fit within the expected parameter range of the attribute.
    * @return The sanitized attribute value.
    * @param pValue The value of the attribute to sanitize.
    */
   public double sanitizeValue(double pValue) {
      return Double.isNaN(pValue) ? this.minValue : Mth.clamp(pValue, this.minValue, this.maxValue);
   }
}