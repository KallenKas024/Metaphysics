package net.minecraft.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class RandomSequences extends SavedData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final long seed;
   private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap<>();

   public RandomSequences(long pSeed) {
      this.seed = pSeed;
   }

   public RandomSource get(ResourceLocation pLocation) {
      final RandomSource randomsource = this.sequences.computeIfAbsent(pLocation, (p_287666_) -> {
         return new RandomSequence(this.seed, p_287666_);
      }).random();
      return new RandomSource() {
         public RandomSource fork() {
            RandomSequences.this.setDirty();
            return randomsource.fork();
         }

         public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return randomsource.forkPositional();
         }

         public void setSeed(long p_287659_) {
            RandomSequences.this.setDirty();
            randomsource.setSeed(p_287659_);
         }

         public int nextInt() {
            RandomSequences.this.setDirty();
            return randomsource.nextInt();
         }

         public int nextInt(int p_287717_) {
            RandomSequences.this.setDirty();
            return randomsource.nextInt(p_287717_);
         }

         public long nextLong() {
            RandomSequences.this.setDirty();
            return randomsource.nextLong();
         }

         public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return randomsource.nextBoolean();
         }

         public float nextFloat() {
            RandomSequences.this.setDirty();
            return randomsource.nextFloat();
         }

         public double nextDouble() {
            RandomSequences.this.setDirty();
            return randomsource.nextDouble();
         }

         public double nextGaussian() {
            RandomSequences.this.setDirty();
            return randomsource.nextGaussian();
         }
      };
   }

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompoundTag the {@code CompoundTag} to save the {@code SavedData} to
    */
   public CompoundTag save(CompoundTag pCompoundTag) {
      this.sequences.forEach((p_287627_, p_287578_) -> {
         pCompoundTag.put(p_287627_.toString(), RandomSequence.CODEC.encodeStart(NbtOps.INSTANCE, p_287578_).result().orElseThrow());
      });
      return pCompoundTag;
   }

   public static RandomSequences load(long pSeed, CompoundTag pTag) {
      RandomSequences randomsequences = new RandomSequences(pSeed);

      for(String s : pTag.getAllKeys()) {
         try {
            RandomSequence randomsequence = RandomSequence.CODEC.decode(NbtOps.INSTANCE, pTag.get(s)).result().get().getFirst();
            randomsequences.sequences.put(new ResourceLocation(s), randomsequence);
         } catch (Exception exception) {
            LOGGER.error("Failed to load random sequence {}", s, exception);
         }
      }

      return randomsequences;
   }
}