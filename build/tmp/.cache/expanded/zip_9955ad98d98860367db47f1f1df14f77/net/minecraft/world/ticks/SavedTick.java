package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public record SavedTick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
   private static final String TAG_ID = "i";
   private static final String TAG_X = "x";
   private static final String TAG_Y = "y";
   private static final String TAG_Z = "z";
   private static final String TAG_DELAY = "t";
   private static final String TAG_PRIORITY = "p";
   public static final Hash.Strategy<SavedTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<SavedTick<?>>() {
      public int hashCode(SavedTick<?> p_193364_) {
         return 31 * p_193364_.pos().hashCode() + p_193364_.type().hashCode();
      }

      public boolean equals(@Nullable SavedTick<?> p_193366_, @Nullable SavedTick<?> p_193367_) {
         if (p_193366_ == p_193367_) {
            return true;
         } else if (p_193366_ != null && p_193367_ != null) {
            return p_193366_.type() == p_193367_.type() && p_193366_.pos().equals(p_193367_.pos());
         } else {
            return false;
         }
      }
   };

   public static <T> void loadTickList(ListTag pTag, Function<String, Optional<T>> pIdParser, ChunkPos pChunkPos, Consumer<SavedTick<T>> pOutput) {
      long i = pChunkPos.toLong();

      for(int j = 0; j < pTag.size(); ++j) {
         CompoundTag compoundtag = pTag.getCompound(j);
         loadTick(compoundtag, pIdParser).ifPresent((p_210665_) -> {
            if (ChunkPos.asLong(p_210665_.pos()) == i) {
               pOutput.accept(p_210665_);
            }

         });
      }

   }

   public static <T> Optional<SavedTick<T>> loadTick(CompoundTag pTag, Function<String, Optional<T>> pIdParser) {
      return pIdParser.apply(pTag.getString("i")).map((p_210668_) -> {
         BlockPos blockpos = new BlockPos(pTag.getInt("x"), pTag.getInt("y"), pTag.getInt("z"));
         return new SavedTick<>(p_210668_, blockpos, pTag.getInt("t"), TickPriority.byValue(pTag.getInt("p")));
      });
   }

   private static CompoundTag saveTick(String pId, BlockPos pPos, int pDelay, TickPriority pPriority) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("i", pId);
      compoundtag.putInt("x", pPos.getX());
      compoundtag.putInt("y", pPos.getY());
      compoundtag.putInt("z", pPos.getZ());
      compoundtag.putInt("t", pDelay);
      compoundtag.putInt("p", pPriority.getValue());
      return compoundtag;
   }

   public static <T> CompoundTag saveTick(ScheduledTick<T> pTick, Function<T, String> pIdGetter, long pGameTime) {
      return saveTick(pIdGetter.apply(pTick.type()), pTick.pos(), (int)(pTick.triggerTick() - pGameTime), pTick.priority());
   }

   public CompoundTag save(Function<T, String> pIdGetter) {
      return saveTick(pIdGetter.apply(this.type), this.pos, this.delay, this.priority);
   }

   public ScheduledTick<T> unpack(long pGameTime, long pSubTickOrder) {
      return new ScheduledTick<T>(this.type, this.pos, pGameTime + (long)this.delay, this.priority, pSubTickOrder);
   }

   public static <T> SavedTick<T> probe(T pType, BlockPos pPos) {
      return new SavedTick<>(pType, pPos, 0, TickPriority.NORMAL);
   }
}