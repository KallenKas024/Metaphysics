package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Locale;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StitcherException extends RuntimeException {
   private final Collection<Stitcher.Entry> allSprites;

   public StitcherException(Stitcher.Entry pEntry, Collection<Stitcher.Entry> pAllSprites) {
      super(String.format(Locale.ROOT, "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", pEntry.name(), pEntry.width(), pEntry.height()));
      this.allSprites = pAllSprites;
   }

   public Collection<Stitcher.Entry> getAllSprites() {
      return this.allSprites;
   }
}