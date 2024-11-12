package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelResourceLocation extends ResourceLocation {
   @VisibleForTesting
   static final char VARIANT_SEPARATOR = '#';
   private final String variant;

   private ModelResourceLocation(String pNamespace, String pPath, String pVariant, @Nullable ResourceLocation.Dummy pDummy) {
      super(pNamespace, pPath, pDummy);
      this.variant = pVariant;
   }

   public ModelResourceLocation(String pNamespace, String pLocation, String pPath) {
      super(pNamespace, pLocation);
      this.variant = lowercaseVariant(pPath);
   }

   public ModelResourceLocation(ResourceLocation pNamespace, String pPath) {
      this(pNamespace.getNamespace(), pNamespace.getPath(), lowercaseVariant(pPath), (ResourceLocation.Dummy)null);
   }

   public static ModelResourceLocation vanilla(String pPath, String pVariant) {
      return new ModelResourceLocation("minecraft", pPath, pVariant);
   }

   private static String lowercaseVariant(String pVariant) {
      return pVariant.toLowerCase(Locale.ROOT);
   }

   public String getVariant() {
      return this.variant;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther instanceof ModelResourceLocation && super.equals(pOther)) {
         ModelResourceLocation modelresourcelocation = (ModelResourceLocation)pOther;
         return this.variant.equals(modelresourcelocation.variant);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.variant.hashCode();
   }

   public String toString() {
      return super.toString() + "#" + this.variant;
   }
}