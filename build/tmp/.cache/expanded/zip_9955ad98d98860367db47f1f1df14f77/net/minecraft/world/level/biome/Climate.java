package net.minecraft.world.level.biome;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class Climate {
   private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
   private static final float QUANTIZATION_FACTOR = 10000.0F;
   @VisibleForTesting
   protected static final int PARAMETER_COUNT = 7;

   public static Climate.TargetPoint target(float pTemperature, float pHumidity, float pContinentalness, float pErosion, float pDepth, float pWeirdness) {
      return new Climate.TargetPoint(quantizeCoord(pTemperature), quantizeCoord(pHumidity), quantizeCoord(pContinentalness), quantizeCoord(pErosion), quantizeCoord(pDepth), quantizeCoord(pWeirdness));
   }

   public static Climate.ParameterPoint parameters(float pTemperature, float pHumidity, float pContinentalness, float pErosion, float pDepth, float pWeirdness, float pOffset) {
      return new Climate.ParameterPoint(Climate.Parameter.point(pTemperature), Climate.Parameter.point(pHumidity), Climate.Parameter.point(pContinentalness), Climate.Parameter.point(pErosion), Climate.Parameter.point(pDepth), Climate.Parameter.point(pWeirdness), quantizeCoord(pOffset));
   }

   public static Climate.ParameterPoint parameters(Climate.Parameter pTemperature, Climate.Parameter pHumidity, Climate.Parameter pContinentalness, Climate.Parameter pErosion, Climate.Parameter pDepth, Climate.Parameter pWeirdness, float pOffset) {
      return new Climate.ParameterPoint(pTemperature, pHumidity, pContinentalness, pErosion, pDepth, pWeirdness, quantizeCoord(pOffset));
   }

   public static long quantizeCoord(float pCoord) {
      return (long)(pCoord * 10000.0F);
   }

   public static float unquantizeCoord(long pCoord) {
      return (float)pCoord / 10000.0F;
   }

   public static Climate.Sampler empty() {
      DensityFunction densityfunction = DensityFunctions.zero();
      return new Climate.Sampler(densityfunction, densityfunction, densityfunction, densityfunction, densityfunction, densityfunction, List.of());
   }

   public static BlockPos findSpawnPosition(List<Climate.ParameterPoint> pPoints, Climate.Sampler pSampler) {
      return (new Climate.SpawnFinder(pPoints, pSampler)).result.location();
   }

   interface DistanceMetric<T> {
      long distance(Climate.RTree.Node<T> pNode, long[] pSearchedValues);
   }

   public static record Parameter(long min, long max) {
      public static final Codec<Climate.Parameter> CODEC = ExtraCodecs.intervalCodec(Codec.floatRange(-2.0F, 2.0F), "min", "max", (p_275164_, p_275165_) -> {
         return p_275164_.compareTo(p_275165_) > 0 ? DataResult.error(() -> {
            return "Cannon construct interval, min > max (" + p_275164_ + " > " + p_275165_ + ")";
         }) : DataResult.success(new Climate.Parameter(Climate.quantizeCoord(p_275164_), Climate.quantizeCoord(p_275165_)));
      }, (p_186841_) -> {
         return Climate.unquantizeCoord(p_186841_.min());
      }, (p_186839_) -> {
         return Climate.unquantizeCoord(p_186839_.max());
      });

      public static Climate.Parameter point(float pValue) {
         return span(pValue, pValue);
      }

      public static Climate.Parameter span(float pMin, float pMax) {
         if (pMin > pMax) {
            throw new IllegalArgumentException("min > max: " + pMin + " " + pMax);
         } else {
            return new Climate.Parameter(Climate.quantizeCoord(pMin), Climate.quantizeCoord(pMax));
         }
      }

      public static Climate.Parameter span(Climate.Parameter pMin, Climate.Parameter pMax) {
         if (pMin.min() > pMax.max()) {
            throw new IllegalArgumentException("min > max: " + pMin + " " + pMax);
         } else {
            return new Climate.Parameter(pMin.min(), pMax.max());
         }
      }

      public String toString() {
         return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min) : String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
      }

      public long distance(long pPointValue) {
         long i = pPointValue - this.max;
         long j = this.min - pPointValue;
         return i > 0L ? i : Math.max(j, 0L);
      }

      public long distance(Climate.Parameter pParameter) {
         long i = pParameter.min() - this.max;
         long j = this.min - pParameter.max();
         return i > 0L ? i : Math.max(j, 0L);
      }

      public Climate.Parameter span(@Nullable Climate.Parameter pParam) {
         return pParam == null ? this : new Climate.Parameter(Math.min(this.min, pParam.min()), Math.max(this.max, pParam.max()));
      }
   }

   public static class ParameterList<T> {
      private final List<Pair<Climate.ParameterPoint, T>> values;
      private final Climate.RTree<T> index;

      public static <T> Codec<Climate.ParameterList<T>> codec(MapCodec<T> pCodec) {
         return ExtraCodecs.nonEmptyList(RecordCodecBuilder.<Pair<Climate.ParameterPoint, T>>create((p_275233_) -> {
            return p_275233_.group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), pCodec.forGetter(Pair::getSecond)).apply(p_275233_, Pair::of);
         }).listOf()).xmap(Climate.ParameterList::new, Climate.ParameterList::values);
      }

      public ParameterList(List<Pair<Climate.ParameterPoint, T>> p_186849_) {
         this.values = p_186849_;
         this.index = Climate.RTree.create(p_186849_);
      }

      public List<Pair<Climate.ParameterPoint, T>> values() {
         return this.values;
      }

      public T findValue(Climate.TargetPoint pTargetPoint) {
         return this.findValueIndex(pTargetPoint);
      }

      @VisibleForTesting
      public T findValueBruteForce(Climate.TargetPoint pTargetPoint) {
         Iterator<Pair<Climate.ParameterPoint, T>> iterator = this.values().iterator();
         Pair<Climate.ParameterPoint, T> pair = iterator.next();
         long i = pair.getFirst().fitness(pTargetPoint);
         T t = pair.getSecond();

         while(iterator.hasNext()) {
            Pair<Climate.ParameterPoint, T> pair1 = iterator.next();
            long j = pair1.getFirst().fitness(pTargetPoint);
            if (j < i) {
               i = j;
               t = pair1.getSecond();
            }
         }

         return t;
      }

      public T findValueIndex(Climate.TargetPoint pTargetPoint) {
         return this.findValueIndex(pTargetPoint, Climate.RTree.Node::distance);
      }

      protected T findValueIndex(Climate.TargetPoint pTargetPoint, Climate.DistanceMetric<T> pDistanceMetric) {
         return this.index.search(pTargetPoint, pDistanceMetric);
      }
   }

   public static record ParameterPoint(Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter depth, Climate.Parameter weirdness, long offset) {
      public static final Codec<Climate.ParameterPoint> CODEC = RecordCodecBuilder.create((p_186885_) -> {
         return p_186885_.group(Climate.Parameter.CODEC.fieldOf("temperature").forGetter((p_186905_) -> {
            return p_186905_.temperature;
         }), Climate.Parameter.CODEC.fieldOf("humidity").forGetter((p_186902_) -> {
            return p_186902_.humidity;
         }), Climate.Parameter.CODEC.fieldOf("continentalness").forGetter((p_186897_) -> {
            return p_186897_.continentalness;
         }), Climate.Parameter.CODEC.fieldOf("erosion").forGetter((p_186894_) -> {
            return p_186894_.erosion;
         }), Climate.Parameter.CODEC.fieldOf("depth").forGetter((p_186891_) -> {
            return p_186891_.depth;
         }), Climate.Parameter.CODEC.fieldOf("weirdness").forGetter((p_186888_) -> {
            return p_186888_.weirdness;
         }), Codec.floatRange(0.0F, 1.0F).fieldOf("offset").xmap(Climate::quantizeCoord, Climate::unquantizeCoord).forGetter((p_186881_) -> {
            return p_186881_.offset;
         })).apply(p_186885_, Climate.ParameterPoint::new);
      });

      long fitness(Climate.TargetPoint pPoint) {
         return Mth.square(this.temperature.distance(pPoint.temperature)) + Mth.square(this.humidity.distance(pPoint.humidity)) + Mth.square(this.continentalness.distance(pPoint.continentalness)) + Mth.square(this.erosion.distance(pPoint.erosion)) + Mth.square(this.depth.distance(pPoint.depth)) + Mth.square(this.weirdness.distance(pPoint.weirdness)) + Mth.square(this.offset);
      }

      protected List<Climate.Parameter> parameterSpace() {
         return ImmutableList.of(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, new Climate.Parameter(this.offset, this.offset));
      }
   }

   protected static final class RTree<T> {
      private static final int CHILDREN_PER_NODE = 6;
      private final Climate.RTree.Node<T> root;
      private final ThreadLocal<Climate.RTree.Leaf<T>> lastResult = new ThreadLocal<>();

      private RTree(Climate.RTree.Node<T> pRoot) {
         this.root = pRoot;
      }

      public static <T> Climate.RTree<T> create(List<Pair<Climate.ParameterPoint, T>> pNodes) {
         if (pNodes.isEmpty()) {
            throw new IllegalArgumentException("Need at least one value to build the search tree.");
         } else {
            int i = pNodes.get(0).getFirst().parameterSpace().size();
            if (i != 7) {
               throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
            } else {
               List<Climate.RTree.Leaf<T>> list = pNodes.stream().map((p_186934_) -> {
                  return new Climate.RTree.Leaf<T>(p_186934_.getFirst(), p_186934_.getSecond());
               }).collect(Collectors.toCollection(ArrayList::new));
               return new Climate.RTree<>(build(i, list));
            }
         }
      }

      private static <T> Climate.RTree.Node<T> build(int pParamSpaceSize, List<? extends Climate.RTree.Node<T>> pChildren) {
         if (pChildren.isEmpty()) {
            throw new IllegalStateException("Need at least one child to build a node");
         } else if (pChildren.size() == 1) {
            return pChildren.get(0);
         } else if (pChildren.size() <= 6) {
            pChildren.sort(Comparator.comparingLong((p_186916_) -> {
               long i1 = 0L;

               for(int j1 = 0; j1 < pParamSpaceSize; ++j1) {
                  Climate.Parameter climate$parameter = p_186916_.parameterSpace[j1];
                  i1 += Math.abs((climate$parameter.min() + climate$parameter.max()) / 2L);
               }

               return i1;
            }));
            return new Climate.RTree.SubTree<>(pChildren);
         } else {
            long i = Long.MAX_VALUE;
            int j = -1;
            List<Climate.RTree.SubTree<T>> list = null;

            for(int k = 0; k < pParamSpaceSize; ++k) {
               sort(pChildren, pParamSpaceSize, k, false);
               List<Climate.RTree.SubTree<T>> list1 = bucketize(pChildren);
               long l = 0L;

               for(Climate.RTree.SubTree<T> subtree : list1) {
                  l += cost(subtree.parameterSpace);
               }

               if (i > l) {
                  i = l;
                  j = k;
                  list = list1;
               }
            }

            sort(list, pParamSpaceSize, j, true);
            return new Climate.RTree.SubTree<>(list.stream().map((p_186919_) -> {
               return build(pParamSpaceSize, Arrays.asList(p_186919_.children));
            }).collect(Collectors.toList()));
         }
      }

      private static <T> void sort(List<? extends Climate.RTree.Node<T>> pChildren, int pParamSpaceSize, int pSize, boolean pAbsolute) {
         Comparator<Climate.RTree.Node<T>> comparator = comparator(pSize, pAbsolute);

         for(int i = 1; i < pParamSpaceSize; ++i) {
            comparator = comparator.thenComparing(comparator((pSize + i) % pParamSpaceSize, pAbsolute));
         }

         pChildren.sort(comparator);
      }

      private static <T> Comparator<Climate.RTree.Node<T>> comparator(int pSize, boolean pAbsolute) {
         return Comparator.comparingLong((p_186929_) -> {
            Climate.Parameter climate$parameter = p_186929_.parameterSpace[pSize];
            long i = (climate$parameter.min() + climate$parameter.max()) / 2L;
            return pAbsolute ? Math.abs(i) : i;
         });
      }

      private static <T> List<Climate.RTree.SubTree<T>> bucketize(List<? extends Climate.RTree.Node<T>> pNodes) {
         List<Climate.RTree.SubTree<T>> list = Lists.newArrayList();
         List<Climate.RTree.Node<T>> list1 = Lists.newArrayList();
         int i = (int)Math.pow(6.0D, Math.floor(Math.log((double)pNodes.size() - 0.01D) / Math.log(6.0D)));

         for(Climate.RTree.Node<T> node : pNodes) {
            list1.add(node);
            if (list1.size() >= i) {
               list.add(new Climate.RTree.SubTree<>(list1));
               list1 = Lists.newArrayList();
            }
         }

         if (!list1.isEmpty()) {
            list.add(new Climate.RTree.SubTree<>(list1));
         }

         return list;
      }

      private static long cost(Climate.Parameter[] pParameters) {
         long i = 0L;

         for(Climate.Parameter climate$parameter : pParameters) {
            i += Math.abs(climate$parameter.max() - climate$parameter.min());
         }

         return i;
      }

      static <T> List<Climate.Parameter> buildParameterSpace(List<? extends Climate.RTree.Node<T>> pChildren) {
         if (pChildren.isEmpty()) {
            throw new IllegalArgumentException("SubTree needs at least one child");
         } else {
            int i = 7;
            List<Climate.Parameter> list = Lists.newArrayList();

            for(int j = 0; j < 7; ++j) {
               list.add((Climate.Parameter)null);
            }

            for(Climate.RTree.Node<T> node : pChildren) {
               for(int k = 0; k < 7; ++k) {
                  list.set(k, node.parameterSpace[k].span(list.get(k)));
               }
            }

            return list;
         }
      }

      public T search(Climate.TargetPoint pTargetPoint, Climate.DistanceMetric<T> pDistanceMetric) {
         long[] along = pTargetPoint.toParameterArray();
         Climate.RTree.Leaf<T> leaf = this.root.search(along, this.lastResult.get(), pDistanceMetric);
         this.lastResult.set(leaf);
         return leaf.value;
      }

      static final class Leaf<T> extends Climate.RTree.Node<T> {
         final T value;

         Leaf(Climate.ParameterPoint pPoint, T pValue) {
            super(pPoint.parameterSpace());
            this.value = pValue;
         }

         protected Climate.RTree.Leaf<T> search(long[] pSearchedValues, @Nullable Climate.RTree.Leaf<T> pLeaf, Climate.DistanceMetric<T> pMetric) {
            return this;
         }
      }

      abstract static class Node<T> {
         protected final Climate.Parameter[] parameterSpace;

         protected Node(List<Climate.Parameter> pParameters) {
            this.parameterSpace = pParameters.toArray(new Climate.Parameter[0]);
         }

         protected abstract Climate.RTree.Leaf<T> search(long[] pSearchedValues, @Nullable Climate.RTree.Leaf<T> pLeaf, Climate.DistanceMetric<T> pMetric);

         protected long distance(long[] pValues) {
            long i = 0L;

            for(int j = 0; j < 7; ++j) {
               i += Mth.square(this.parameterSpace[j].distance(pValues[j]));
            }

            return i;
         }

         public String toString() {
            return Arrays.toString((Object[])this.parameterSpace);
         }
      }

      static final class SubTree<T> extends Climate.RTree.Node<T> {
         final Climate.RTree.Node<T>[] children;

         protected SubTree(List<? extends Climate.RTree.Node<T>> pParameters) {
            this(Climate.RTree.buildParameterSpace(pParameters), pParameters);
         }

         protected SubTree(List<Climate.Parameter> pParameters, List<? extends Climate.RTree.Node<T>> pChildren) {
            super(pParameters);
            this.children = pChildren.toArray(new Climate.RTree.Node[0]);
         }

         protected Climate.RTree.Leaf<T> search(long[] pSearchedValues, @Nullable Climate.RTree.Leaf<T> pLeaf, Climate.DistanceMetric<T> pMetric) {
            long i = pLeaf == null ? Long.MAX_VALUE : pMetric.distance(pLeaf, pSearchedValues);
            Climate.RTree.Leaf<T> leaf = pLeaf;

            for(Climate.RTree.Node<T> node : this.children) {
               long j = pMetric.distance(node, pSearchedValues);
               if (i > j) {
                  Climate.RTree.Leaf<T> leaf1 = node.search(pSearchedValues, leaf, pMetric);
                  long k = node == leaf1 ? j : pMetric.distance(leaf1, pSearchedValues);
                  if (i > k) {
                     i = k;
                     leaf = leaf1;
                  }
               }
            }

            return leaf;
         }
      }
   }

   public static record Sampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness, DensityFunction erosion, DensityFunction depth, DensityFunction weirdness, List<Climate.ParameterPoint> spawnTarget) {
      public Climate.TargetPoint sample(int pX, int pY, int pZ) {
         int i = QuartPos.toBlock(pX);
         int j = QuartPos.toBlock(pY);
         int k = QuartPos.toBlock(pZ);
         DensityFunction.SinglePointContext densityfunction$singlepointcontext = new DensityFunction.SinglePointContext(i, j, k);
         return Climate.target((float)this.temperature.compute(densityfunction$singlepointcontext), (float)this.humidity.compute(densityfunction$singlepointcontext), (float)this.continentalness.compute(densityfunction$singlepointcontext), (float)this.erosion.compute(densityfunction$singlepointcontext), (float)this.depth.compute(densityfunction$singlepointcontext), (float)this.weirdness.compute(densityfunction$singlepointcontext));
      }

      public BlockPos findSpawnPosition() {
         return this.spawnTarget.isEmpty() ? BlockPos.ZERO : Climate.findSpawnPosition(this.spawnTarget, this);
      }
   }

   static class SpawnFinder {
      Climate.SpawnFinder.Result result;

      SpawnFinder(List<Climate.ParameterPoint> pPoints, Climate.Sampler pSampler) {
         this.result = getSpawnPositionAndFitness(pPoints, pSampler, 0, 0);
         this.radialSearch(pPoints, pSampler, 2048.0F, 512.0F);
         this.radialSearch(pPoints, pSampler, 512.0F, 32.0F);
      }

      private void radialSearch(List<Climate.ParameterPoint> pPoint, Climate.Sampler pSampler, float pMax, float pMin) {
         float f = 0.0F;
         float f1 = pMin;
         BlockPos blockpos = this.result.location();

         while(f1 <= pMax) {
            int i = blockpos.getX() + (int)(Math.sin((double)f) * (double)f1);
            int j = blockpos.getZ() + (int)(Math.cos((double)f) * (double)f1);
            Climate.SpawnFinder.Result climate$spawnfinder$result = getSpawnPositionAndFitness(pPoint, pSampler, i, j);
            if (climate$spawnfinder$result.fitness() < this.result.fitness()) {
               this.result = climate$spawnfinder$result;
            }

            f += pMin / f1;
            if ((double)f > (Math.PI * 2D)) {
               f = 0.0F;
               f1 += pMin;
            }
         }

      }

      private static Climate.SpawnFinder.Result getSpawnPositionAndFitness(List<Climate.ParameterPoint> pPoints, Climate.Sampler pSampler, int pX, int pZ) {
         double d0 = Mth.square(2500.0D);
         int i = 2;
         long j = (long)((double)Mth.square(10000.0F) * Math.pow((double)(Mth.square((long)pX) + Mth.square((long)pZ)) / d0, 2.0D));
         Climate.TargetPoint climate$targetpoint = pSampler.sample(QuartPos.fromBlock(pX), 0, QuartPos.fromBlock(pZ));
         Climate.TargetPoint climate$targetpoint1 = new Climate.TargetPoint(climate$targetpoint.temperature(), climate$targetpoint.humidity(), climate$targetpoint.continentalness(), climate$targetpoint.erosion(), 0L, climate$targetpoint.weirdness());
         long k = Long.MAX_VALUE;

         for(Climate.ParameterPoint climate$parameterpoint : pPoints) {
            k = Math.min(k, climate$parameterpoint.fitness(climate$targetpoint1));
         }

         return new Climate.SpawnFinder.Result(new BlockPos(pX, 0, pZ), j + k);
      }

      static record Result(BlockPos location, long fitness) {
      }
   }

   public static record TargetPoint(long temperature, long humidity, long continentalness, long erosion, long depth, long weirdness) {
      @VisibleForTesting
      protected long[] toParameterArray() {
         return new long[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0L};
      }
   }
}