package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class GlobalPos {
   public static final Codec<GlobalPos> CODEC = RecordCodecBuilder.create((p_122642_) -> {
      return p_122642_.group(Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)).apply(p_122642_, GlobalPos::of);
   });
   private final ResourceKey<Level> dimension;
   private final BlockPos pos;

   private GlobalPos(ResourceKey<Level> pDimension, BlockPos pPos) {
      this.dimension = pDimension;
      this.pos = pPos;
   }

   public static GlobalPos of(ResourceKey<Level> p_122644_, BlockPos p_122645_) {
      return new GlobalPos(p_122644_, p_122645_);
   }

   public ResourceKey<Level> dimension() {
      return this.dimension;
   }

   public BlockPos pos() {
      return this.pos;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         GlobalPos globalpos = (GlobalPos)pOther;
         return Objects.equals(this.dimension, globalpos.dimension) && Objects.equals(this.pos, globalpos.pos);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.dimension, this.pos);
   }

   public String toString() {
      return this.dimension + " " + this.pos;
   }
}