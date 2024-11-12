package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface DensityFunction {
   Codec<DensityFunction> DIRECT_CODEC = DensityFunctions.DIRECT_CODEC;
   Codec<Holder<DensityFunction>> CODEC = RegistryFileCodec.create(Registries.DENSITY_FUNCTION, DIRECT_CODEC);
   Codec<DensityFunction> HOLDER_HELPER_CODEC = CODEC.xmap(DensityFunctions.HolderHolder::new, (p_208226_) -> {
      if (p_208226_ instanceof DensityFunctions.HolderHolder densityfunctions$holderholder) {
         return densityfunctions$holderholder.function();
      } else {
         return new Holder.Direct<>(p_208226_);
      }
   });

   double compute(DensityFunction.FunctionContext pContext);

   void fillArray(double[] pArray, DensityFunction.ContextProvider pContextProvider);

   DensityFunction mapAll(DensityFunction.Visitor pVisitor);

   double minValue();

   double maxValue();

   KeyDispatchDataCodec<? extends DensityFunction> codec();

   default DensityFunction clamp(double pMinValue, double pMaxValue) {
      return new DensityFunctions.Clamp(this, pMinValue, pMaxValue);
   }

   default DensityFunction abs() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
   }

   default DensityFunction square() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
   }

   default DensityFunction cube() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
   }

   default DensityFunction halfNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
   }

   default DensityFunction quarterNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
   }

   default DensityFunction squeeze() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
   }

   public interface ContextProvider {
      DensityFunction.FunctionContext forIndex(int pArrayIndex);

      void fillAllDirectly(double[] pValues, DensityFunction pFunction);
   }

   public interface FunctionContext {
      int blockX();

      int blockY();

      int blockZ();

      default Blender getBlender() {
         return Blender.empty();
      }
   }

   public static record NoiseHolder(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) {
      public static final Codec<DensityFunction.NoiseHolder> CODEC = NormalNoise.NoiseParameters.CODEC.xmap((p_224011_) -> {
         return new DensityFunction.NoiseHolder(p_224011_, (NormalNoise)null);
      }, DensityFunction.NoiseHolder::noiseData);

      public NoiseHolder(Holder<NormalNoise.NoiseParameters> pNoiseData) {
         this(pNoiseData, (NormalNoise)null);
      }

      public double getValue(double pX, double pY, double pZ) {
         return this.noise == null ? 0.0D : this.noise.getValue(pX, pY, pZ);
      }

      public double maxValue() {
         return this.noise == null ? 2.0D : this.noise.maxValue();
      }
   }

   public interface SimpleFunction extends DensityFunction {
      default void fillArray(double[] p_208241_, DensityFunction.ContextProvider p_208242_) {
         p_208242_.fillAllDirectly(p_208241_, this);
      }

      default DensityFunction mapAll(DensityFunction.Visitor p_208239_) {
         return p_208239_.apply(this);
      }
   }

   public static record SinglePointContext(int blockX, int blockY, int blockZ) implements DensityFunction.FunctionContext {
      public int blockX() {
         return this.blockX;
      }

      public int blockY() {
         return this.blockY;
      }

      public int blockZ() {
         return this.blockZ;
      }
   }

   public interface Visitor {
      DensityFunction apply(DensityFunction pDensityFunction);

      default DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder pNoiseHolder) {
         return pNoiseHolder;
      }
   }
}