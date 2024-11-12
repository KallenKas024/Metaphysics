package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LegacyStuffWrapper {
   /** @deprecated */
   @Deprecated
   public static int[] getPixels(ResourceManager pManager, ResourceLocation pLocation) throws IOException {
      try (
         InputStream inputstream = pManager.open(pLocation);
         NativeImage nativeimage = NativeImage.read(inputstream);
      ) {
         return nativeimage.makePixelArray();
      }
   }
}