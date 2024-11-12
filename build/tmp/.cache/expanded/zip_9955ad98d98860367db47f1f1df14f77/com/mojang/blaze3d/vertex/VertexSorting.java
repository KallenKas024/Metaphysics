package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public interface VertexSorting {
   VertexSorting DISTANCE_TO_ORIGIN = byDistance(0.0F, 0.0F, 0.0F);
   VertexSorting ORTHOGRAPHIC_Z = byDistance((p_277433_) -> {
      return -p_277433_.z();
   });

   static VertexSorting byDistance(float pX, float pY, float pZ) {
      return byDistance(new Vector3f(pX, pY, pZ));
   }

   static VertexSorting byDistance(Vector3f pVector) {
      return byDistance(pVector::distanceSquared);
   }

   static VertexSorting byDistance(VertexSorting.DistanceFunction pDistanceFunction) {
      return (p_278083_) -> {
         float[] afloat = new float[p_278083_.length];
         int[] aint = new int[p_278083_.length];

         for(int i = 0; i < p_278083_.length; aint[i] = i++) {
            afloat[i] = pDistanceFunction.apply(p_278083_[i]);
         }

         IntArrays.mergeSort(aint, (p_277443_, p_277864_) -> {
            return Floats.compare(afloat[p_277864_], afloat[p_277443_]);
         });
         return aint;
      };
   }

   int[] sort(Vector3f[] pVectors);

   @OnlyIn(Dist.CLIENT)
   public interface DistanceFunction {
      float apply(Vector3f pVector);
   }
}