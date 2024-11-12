package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class BannerPattern {
   final String hashname;

   public BannerPattern(String pHashname) {
      this.hashname = pHashname;
   }

   /**
    * 
    * @param pBanner {@code true} for a banner, {@code false} for a shield
    */
   public static ResourceLocation location(ResourceKey<BannerPattern> pBannerPatternKey, boolean pBanner) {
      String s = pBanner ? "banner" : "shield";
      return pBannerPatternKey.location().withPrefix("entity/" + s + "/");
   }

   public String getHashname() {
      return this.hashname;
   }

   @Nullable
   public static Holder<BannerPattern> byHash(String pHashname) {
      return BuiltInRegistries.BANNER_PATTERN.holders().filter((p_222704_) -> {
         return (p_222704_.value()).hashname.equals(pHashname);
      }).findAny().orElse((Holder.Reference<BannerPattern>)null);
   }

   public static class Builder {
      private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns = Lists.newArrayList();

      public BannerPattern.Builder addPattern(ResourceKey<BannerPattern> pBannerPattern, DyeColor pColor) {
         return this.addPattern(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(pBannerPattern), pColor);
      }

      public BannerPattern.Builder addPattern(Holder<BannerPattern> pBannerPattern, DyeColor pColor) {
         return this.addPattern(Pair.of(pBannerPattern, pColor));
      }

      public BannerPattern.Builder addPattern(Pair<Holder<BannerPattern>, DyeColor> pPattern) {
         this.patterns.add(pPattern);
         return this;
      }

      /**
       * Creates the NBT data for the patterns.
       */
      public ListTag toListTag() {
         ListTag listtag = new ListTag();

         for(Pair<Holder<BannerPattern>, DyeColor> pair : this.patterns) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("Pattern", (pair.getFirst().value()).hashname);
            compoundtag.putInt("Color", pair.getSecond().getId());
            listtag.add(compoundtag);
         }

         return listtag;
      }
   }
}