package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;

public class PoiManager extends SectionStorage<PoiSection> {
   public static final int MAX_VILLAGE_DISTANCE = 6;
   public static final int VILLAGE_SECTION_SIZE = 1;
   private final PoiManager.DistanceTracker distanceTracker;
   private final LongSet loadedChunks = new LongOpenHashSet();

   public PoiManager(Path pFolder, DataFixer pFixerUpper, boolean pSync, RegistryAccess pRegistryAccess, LevelHeightAccessor pLevelHeightAccessor) {
      super(pFolder, PoiSection::codec, PoiSection::new, pFixerUpper, DataFixTypes.POI_CHUNK, pSync, pRegistryAccess, pLevelHeightAccessor);
      this.distanceTracker = new PoiManager.DistanceTracker();
   }

   public void add(BlockPos pPos, Holder<PoiType> pType) {
      this.getOrCreate(SectionPos.asLong(pPos)).add(pPos, pType);
   }

   public void remove(BlockPos pPos) {
      this.getOrLoad(SectionPos.asLong(pPos)).ifPresent((p_148657_) -> {
         p_148657_.remove(pPos);
      });
   }

   public long getCountInRange(Predicate<Holder<PoiType>> pTypePredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).count();
   }

   public boolean existsAtPosition(ResourceKey<PoiType> pType, BlockPos pPos) {
      return this.exists(pPos, (p_217879_) -> {
         return p_217879_.is(pType);
      });
   }

   public Stream<PoiRecord> getInSquare(Predicate<Holder<PoiType>> pTypePredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      int i = Math.floorDiv(pDistance, 16) + 1;
      return ChunkPos.rangeClosed(new ChunkPos(pPos), i).flatMap((p_217938_) -> {
         return this.getInChunk(pTypePredicate, p_217938_, pStatus);
      }).filter((p_217971_) -> {
         BlockPos blockpos = p_217971_.getPos();
         return Math.abs(blockpos.getX() - pPos.getX()) <= pDistance && Math.abs(blockpos.getZ() - pPos.getZ()) <= pDistance;
      });
   }

   public Stream<PoiRecord> getInRange(Predicate<Holder<PoiType>> pTypePredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      int i = pDistance * pDistance;
      return this.getInSquare(pTypePredicate, pPos, pDistance, pStatus).filter((p_217906_) -> {
         return p_217906_.getPos().distSqr(pPos) <= (double)i;
      });
   }

   @VisibleForDebug
   public Stream<PoiRecord> getInChunk(Predicate<Holder<PoiType>> pTypePredicate, ChunkPos pPosChunk, PoiManager.Occupancy pStatus) {
      return IntStream.range(this.levelHeightAccessor.getMinSection(), this.levelHeightAccessor.getMaxSection()).boxed().map((p_217886_) -> {
         return this.getOrLoad(SectionPos.of(pPosChunk, p_217886_).asLong());
      }).filter(Optional::isPresent).flatMap((p_217942_) -> {
         return p_217942_.get().getRecords(pTypePredicate, pStatus);
      });
   }

   public Stream<BlockPos> findAll(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).map(PoiRecord::getPos).filter(pPosPredicate);
   }

   public Stream<Pair<Holder<PoiType>, BlockPos>> findAllWithType(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).filter((p_217982_) -> {
         return pPosPredicate.test(p_217982_.getPos());
      }).map((p_217990_) -> {
         return Pair.of(p_217990_.getPoiType(), p_217990_.getPos());
      });
   }

   public Stream<Pair<Holder<PoiType>, BlockPos>> findAllClosestFirstWithType(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.findAllWithType(pTypePredicate, pPosPredicate, pPos, pDistance, pStatus).sorted(Comparator.comparingDouble((p_217915_) -> {
         return p_217915_.getSecond().distSqr(pPos);
      }));
   }

   public Optional<BlockPos> find(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.findAll(pTypePredicate, pPosPredicate, pPos, pDistance, pStatus).findFirst();
   }

   public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> pTypePredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).map(PoiRecord::getPos).min(Comparator.comparingDouble((p_217977_) -> {
         return p_217977_.distSqr(pPos);
      }));
   }

   public Optional<Pair<Holder<PoiType>, BlockPos>> findClosestWithType(Predicate<Holder<PoiType>> pTypePredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).min(Comparator.comparingDouble((p_217909_) -> {
         return p_217909_.getPos().distSqr(pPos);
      })).map((p_217959_) -> {
         return Pair.of(p_217959_.getPoiType(), p_217959_.getPos());
      });
   }

   public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus) {
      return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).map(PoiRecord::getPos).filter(pPosPredicate).min(Comparator.comparingDouble((p_217918_) -> {
         return p_217918_.distSqr(pPos);
      }));
   }

   public Optional<BlockPos> take(Predicate<Holder<PoiType>> pTypePredicate, BiPredicate<Holder<PoiType>, BlockPos> pCombinedTypePosPredicate, BlockPos pPos, int pDistance) {
      return this.getInRange(pTypePredicate, pPos, pDistance, PoiManager.Occupancy.HAS_SPACE).filter((p_217934_) -> {
         return pCombinedTypePosPredicate.test(p_217934_.getPoiType(), p_217934_.getPos());
      }).findFirst().map((p_217881_) -> {
         p_217881_.acquireTicket();
         return p_217881_.getPos();
      });
   }

   public Optional<BlockPos> getRandom(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, PoiManager.Occupancy pStatus, BlockPos pPos, int pDistance, RandomSource pRandom) {
      List<PoiRecord> list = Util.toShuffledList(this.getInRange(pTypePredicate, pPos, pDistance, pStatus), pRandom);
      return list.stream().filter((p_217945_) -> {
         return pPosPredicate.test(p_217945_.getPos());
      }).findFirst().map(PoiRecord::getPos);
   }

   public boolean release(BlockPos pPos) {
      return this.getOrLoad(SectionPos.asLong(pPos)).map((p_217993_) -> {
         return p_217993_.release(pPos);
      }).orElseThrow(() -> {
         return Util.pauseInIde(new IllegalStateException("POI never registered at " + pPos));
      });
   }

   public boolean exists(BlockPos pPos, Predicate<Holder<PoiType>> pTypePredicate) {
      return this.getOrLoad(SectionPos.asLong(pPos)).map((p_217925_) -> {
         return p_217925_.exists(pPos, pTypePredicate);
      }).orElse(false);
   }

   public Optional<Holder<PoiType>> getType(BlockPos pPos) {
      return this.getOrLoad(SectionPos.asLong(pPos)).flatMap((p_217974_) -> {
         return p_217974_.getType(pPos);
      });
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public int getFreeTickets(BlockPos pPos) {
      return this.getOrLoad(SectionPos.asLong(pPos)).map((p_217912_) -> {
         return p_217912_.getFreeTickets(pPos);
      }).orElse(0);
   }

   public int sectionsToVillage(SectionPos pSectionPos) {
      this.distanceTracker.runAllUpdates();
      return this.distanceTracker.getLevel(pSectionPos.asLong());
   }

   boolean isVillageCenter(long pChunkPos) {
      Optional<PoiSection> optional = this.get(pChunkPos);
      return optional == null ? false : optional.map((p_217883_) -> {
         return p_217883_.getRecords((p_217927_) -> {
            return p_217927_.is(PoiTypeTags.VILLAGE);
         }, PoiManager.Occupancy.IS_OCCUPIED).findAny().isPresent();
      }).orElse(false);
   }

   public void tick(BooleanSupplier pAheadOfTime) {
      super.tick(pAheadOfTime);
      this.distanceTracker.runAllUpdates();
   }

   protected void setDirty(long pSectionPos) {
      super.setDirty(pSectionPos);
      this.distanceTracker.update(pSectionPos, this.distanceTracker.getLevelFromSource(pSectionPos), false);
   }

   protected void onSectionLoad(long pSectionKey) {
      this.distanceTracker.update(pSectionKey, this.distanceTracker.getLevelFromSource(pSectionKey), false);
   }

   public void checkConsistencyWithBlocks(SectionPos pSectionPos, LevelChunkSection pLevelChunkSection) {
      Util.ifElse(this.getOrLoad(pSectionPos.asLong()), (p_217898_) -> {
         p_217898_.refresh((p_217967_) -> {
            if (mayHavePoi(pLevelChunkSection)) {
               this.updateFromSection(pLevelChunkSection, pSectionPos, p_217967_);
            }

         });
      }, () -> {
         if (mayHavePoi(pLevelChunkSection)) {
            PoiSection poisection = this.getOrCreate(pSectionPos.asLong());
            this.updateFromSection(pLevelChunkSection, pSectionPos, poisection::add);
         }

      });
   }

   private static boolean mayHavePoi(LevelChunkSection pSection) {
      return pSection.maybeHas(PoiTypes::hasPoi);
   }

   private void updateFromSection(LevelChunkSection pSection, SectionPos pSectionPos, BiConsumer<BlockPos, Holder<PoiType>> pPosToTypeConsumer) {
      pSectionPos.blocksInside().forEach((p_217902_) -> {
         BlockState blockstate = pSection.getBlockState(SectionPos.sectionRelative(p_217902_.getX()), SectionPos.sectionRelative(p_217902_.getY()), SectionPos.sectionRelative(p_217902_.getZ()));
         PoiTypes.forState(blockstate).ifPresent((p_217931_) -> {
            pPosToTypeConsumer.accept(p_217902_, p_217931_);
         });
      });
   }

   public void ensureLoadedAndValid(LevelReader pLevelReader, BlockPos pPos, int pCoordinateOffset) {
      SectionPos.aroundChunk(new ChunkPos(pPos), Math.floorDiv(pCoordinateOffset, 16), this.levelHeightAccessor.getMinSection(), this.levelHeightAccessor.getMaxSection()).map((p_217979_) -> {
         return Pair.of(p_217979_, this.getOrLoad(p_217979_.asLong()));
      }).filter((p_217963_) -> {
         return !p_217963_.getSecond().map(PoiSection::isValid).orElse(false);
      }).map((p_217891_) -> {
         return p_217891_.getFirst().chunk();
      }).filter((p_217961_) -> {
         return this.loadedChunks.add(p_217961_.toLong());
      }).forEach((p_217889_) -> {
         pLevelReader.getChunk(p_217889_.x, p_217889_.z, ChunkStatus.EMPTY);
      });
   }

   final class DistanceTracker extends SectionTracker {
      private final Long2ByteMap levels = new Long2ByteOpenHashMap();

      protected DistanceTracker() {
         super(7, 16, 256);
         this.levels.defaultReturnValue((byte)7);
      }

      protected int getLevelFromSource(long pPos) {
         return PoiManager.this.isVillageCenter(pPos) ? 0 : 7;
      }

      protected int getLevel(long pSectionPos) {
         return this.levels.get(pSectionPos);
      }

      protected void setLevel(long pSectionPos, int pLevel) {
         if (pLevel > 6) {
            this.levels.remove(pSectionPos);
         } else {
            this.levels.put(pSectionPos, (byte)pLevel);
         }

      }

      public void runAllUpdates() {
         super.runUpdates(Integer.MAX_VALUE);
      }
   }

   public static enum Occupancy {
      HAS_SPACE(PoiRecord::hasSpace),
      IS_OCCUPIED(PoiRecord::isOccupied),
      ANY((p_27223_) -> {
         return true;
      });

      private final Predicate<? super PoiRecord> test;

      private Occupancy(Predicate<? super PoiRecord> pTest) {
         this.test = pTest;
      }

      public Predicate<? super PoiRecord> getTest() {
         return this.test;
      }
   }
}