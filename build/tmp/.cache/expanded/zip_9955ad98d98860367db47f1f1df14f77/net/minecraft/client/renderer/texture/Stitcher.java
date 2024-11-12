package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Stitcher<T extends Stitcher.Entry> {
   private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

   private static final Comparator<Stitcher.Holder<?>> HOLDER_COMPARATOR = Comparator.<Stitcher.Holder<?>, Integer>comparing((p_118201_) -> {
      return -p_118201_.height;
   }).thenComparing((p_118199_) -> {
      return -p_118199_.width;
   }).thenComparing((p_247945_) -> {
      return p_247945_.entry.name();
   });
   private final int mipLevel;
   private final List<Stitcher.Holder<T>> texturesToBeStitched = new ArrayList<>();
   private final List<Stitcher.Region<T>> storage = new ArrayList<>();
   private int storageX;
   private int storageY;
   private final int maxWidth;
   private final int maxHeight;

   public Stitcher(int pMaxWidth, int pMaxHeight, int pMipLevel) {
      this.mipLevel = pMipLevel;
      this.maxWidth = pMaxWidth;
      this.maxHeight = pMaxHeight;
   }

   public int getWidth() {
      return this.storageX;
   }

   public int getHeight() {
      return this.storageY;
   }

   public void registerSprite(T pStitcherEntry) {
      Stitcher.Holder<T> holder = new Stitcher.Holder<>(pStitcherEntry, this.mipLevel);
      this.texturesToBeStitched.add(holder);
   }

   public void stitch() {
      List<Stitcher.Holder<T>> list = new ArrayList<>(this.texturesToBeStitched);
      list.sort(HOLDER_COMPARATOR);

      for(Stitcher.Holder<T> holder : list) {
         if (!this.addToStorage(holder)) {
            if (LOGGER.isInfoEnabled()) {
               StringBuilder sb = new StringBuilder();
               sb.append("Unable to fit: ").append(holder.entry().name());
               sb.append(" - size: ").append(holder.entry.width()).append("x").append(holder.entry.height());
               sb.append(" - Maybe try a lower resolution resourcepack?\n");
               list.forEach(h -> sb.append("\t").append(h).append("\n"));
               LOGGER.info(sb.toString());
            }
            throw new StitcherException(holder.entry, list.stream().map((p_247946_) -> {
               return p_247946_.entry;
            }).collect(ImmutableList.toImmutableList()));
         }
      }

   }

   public void gatherSprites(Stitcher.SpriteLoader<T> pLoader) {
      for(Stitcher.Region<T> region : this.storage) {
         region.walk(pLoader);
      }

   }

   static int smallestFittingMinTexel(int pDimension, int pMipLevel) {
      return (pDimension >> pMipLevel) + ((pDimension & (1 << pMipLevel) - 1) == 0 ? 0 : 1) << pMipLevel;
   }

   /**
    * Attempts to find space for specified {@code holder}.
    * 
    * @return {@code true} if there was space; {@code false} otherwise
    */
   private boolean addToStorage(Stitcher.Holder<T> pHolder) {
      for(Stitcher.Region<T> region : this.storage) {
         if (region.add(pHolder)) {
            return true;
         }
      }

      return this.expand(pHolder);
   }

   /**
    * Attempts to expand stitched texture in order to make space for specified {@code holder}.
    * 
    * @return {@code true} if there was enough space to expand the texture; {@code false} otherwise
    */
   private boolean expand(Stitcher.Holder<T> pHolder) {
      int i = Mth.smallestEncompassingPowerOfTwo(this.storageX);
      int j = Mth.smallestEncompassingPowerOfTwo(this.storageY);
      int k = Mth.smallestEncompassingPowerOfTwo(this.storageX + pHolder.width);
      int l = Mth.smallestEncompassingPowerOfTwo(this.storageY + pHolder.height);
      boolean flag1 = k <= this.maxWidth;
      boolean flag2 = l <= this.maxHeight;
      if (!flag1 && !flag2) {
         return false;
      } else {
         boolean flag3 = flag1 && i != k;
         boolean flag4 = flag2 && j != l;
         boolean flag;
         if (flag3 ^ flag4) {
            flag = !flag3 && flag1; // Forge: Fix stitcher not expanding entire height before growing width, and (potentially) growing larger then the max size.
         } else {
            flag = flag1 && i <= j;
         }

         Stitcher.Region<T> region;
         if (flag) {
            if (this.storageY == 0) {
               this.storageY = l;
            }

            region = new Stitcher.Region<>(this.storageX, 0, k - this.storageX, this.storageY);
            this.storageX = k;
         } else {
            region = new Stitcher.Region<>(0, this.storageY, this.storageX, l - this.storageY);
            this.storageY = l;
         }

         region.add(pHolder);
         this.storage.add(region);
         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public interface Entry {
      int width();

      int height();

      ResourceLocation name();
   }

   @OnlyIn(Dist.CLIENT)
   static record Holder<T extends Stitcher.Entry>(T entry, int width, int height) {
      public Holder(T pEntry, int pMipLevel) {
         this(pEntry, Stitcher.smallestFittingMinTexel(pEntry.width(), pMipLevel), Stitcher.smallestFittingMinTexel(pEntry.height(), pMipLevel));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Region<T extends Stitcher.Entry> {
      private final int originX;
      private final int originY;
      private final int width;
      private final int height;
      @Nullable
      private List<Stitcher.Region<T>> subSlots;
      @Nullable
      private Stitcher.Holder<T> holder;

      public Region(int pOriginX, int pOriginY, int pWidth, int pHeight) {
         this.originX = pOriginX;
         this.originY = pOriginY;
         this.width = pWidth;
         this.height = pHeight;
      }

      public int getX() {
         return this.originX;
      }

      public int getY() {
         return this.originY;
      }

      public boolean add(Stitcher.Holder<T> pHolder) {
         if (this.holder != null) {
            return false;
         } else {
            int i = pHolder.width;
            int j = pHolder.height;
            if (i <= this.width && j <= this.height) {
               if (i == this.width && j == this.height) {
                  this.holder = pHolder;
                  return true;
               } else {
                  if (this.subSlots == null) {
                     this.subSlots = new ArrayList<>(1);
                     this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY, i, j));
                     int k = this.width - i;
                     int l = this.height - j;
                     if (l > 0 && k > 0) {
                        int i1 = Math.max(this.height, k);
                        int j1 = Math.max(this.width, l);
                        if (i1 >= j1) {
                           this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY + j, i, l));
                           this.subSlots.add(new Stitcher.Region<>(this.originX + i, this.originY, k, this.height));
                        } else {
                           this.subSlots.add(new Stitcher.Region<>(this.originX + i, this.originY, k, j));
                           this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY + j, this.width, l));
                        }
                     } else if (k == 0) {
                        this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY + j, i, l));
                     } else if (l == 0) {
                        this.subSlots.add(new Stitcher.Region<>(this.originX + i, this.originY, k, j));
                     }
                  }

                  for(Stitcher.Region<T> region : this.subSlots) {
                     if (region.add(pHolder)) {
                        return true;
                     }
                  }

                  return false;
               }
            } else {
               return false;
            }
         }
      }

      public void walk(Stitcher.SpriteLoader<T> pSpriteLoader) {
         if (this.holder != null) {
            pSpriteLoader.load(this.holder.entry, this.getX(), this.getY());
         } else if (this.subSlots != null) {
            for(Stitcher.Region<T> region : this.subSlots) {
               region.walk(pSpriteLoader);
            }
         }

      }

      public String toString() {
         return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.holder + ", subSlots=" + this.subSlots + "}";
      }
   }

   @OnlyIn(Dist.CLIENT)
   public interface SpriteLoader<T extends Stitcher.Entry> {
      void load(T pEntry, int pX, int pY);
   }
}
