package net.minecraft.client.resources;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.FoliageColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoliageColorReloadListener extends SimplePreparableReloadListener<int[]> {
   private static final ResourceLocation LOCATION = new ResourceLocation("textures/colormap/foliage.png");

   /**
    * Performs any reloading that can be done off-thread, such as file IO
    */
   protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      try {
         return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION);
      } catch (IOException ioexception) {
         throw new IllegalStateException("Failed to load foliage color texture", ioexception);
      }
   }

   /**
    * Applies the prepared sound event registrations and caches to the sound manager.
    * @param pObject The prepared sound event registrations and caches
    * @param pResourceManager The resource manager
    * @param pProfiler The profiler
    */
   protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      FoliageColor.init(pObject);
   }
}