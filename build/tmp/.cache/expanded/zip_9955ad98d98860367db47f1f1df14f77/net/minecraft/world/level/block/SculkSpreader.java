package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SculkSpreader {
   public static final int MAX_GROWTH_RATE_RADIUS = 24;
   public static final int MAX_CHARGE = 1000;
   public static final float MAX_DECAY_FACTOR = 0.5F;
   private static final int MAX_CURSORS = 32;
   public static final int SHRIEKER_PLACEMENT_RATE = 11;
   final boolean isWorldGeneration;
   private final TagKey<Block> replaceableBlocks;
   private final int growthSpawnCost;
   private final int noGrowthRadius;
   private final int chargeDecayRate;
   private final int additionalDecayRate;
   private List<SculkSpreader.ChargeCursor> cursors = new ArrayList<>();
   private static final Logger LOGGER = LogUtils.getLogger();

   public SculkSpreader(boolean pIsWorldGeneration, TagKey<Block> pReplaceableBlocks, int pGrowthSpawnCoat, int pNoGrowthRadius, int pChargeDecayRate, int pAdditionalDecayRate) {
      this.isWorldGeneration = pIsWorldGeneration;
      this.replaceableBlocks = pReplaceableBlocks;
      this.growthSpawnCost = pGrowthSpawnCoat;
      this.noGrowthRadius = pNoGrowthRadius;
      this.chargeDecayRate = pChargeDecayRate;
      this.additionalDecayRate = pAdditionalDecayRate;
   }

   public static SculkSpreader createLevelSpreader() {
      return new SculkSpreader(false, BlockTags.SCULK_REPLACEABLE, 10, 4, 10, 5);
   }

   public static SculkSpreader createWorldGenSpreader() {
      return new SculkSpreader(true, BlockTags.SCULK_REPLACEABLE_WORLD_GEN, 50, 1, 5, 10);
   }

   public TagKey<Block> replaceableBlocks() {
      return this.replaceableBlocks;
   }

   public int growthSpawnCost() {
      return this.growthSpawnCost;
   }

   public int noGrowthRadius() {
      return this.noGrowthRadius;
   }

   public int chargeDecayRate() {
      return this.chargeDecayRate;
   }

   public int additionalDecayRate() {
      return this.additionalDecayRate;
   }

   public boolean isWorldGeneration() {
      return this.isWorldGeneration;
   }

   @VisibleForTesting
   public List<SculkSpreader.ChargeCursor> getCursors() {
      return this.cursors;
   }

   public void clear() {
      this.cursors.clear();
   }

   public void load(CompoundTag pTag) {
      if (pTag.contains("cursors", 9)) {
         this.cursors.clear();
         List<SculkSpreader.ChargeCursor> list = SculkSpreader.ChargeCursor.CODEC.listOf().parse(new Dynamic<>(NbtOps.INSTANCE, pTag.getList("cursors", 10))).resultOrPartial(LOGGER::error).orElseGet(ArrayList::new);
         int i = Math.min(list.size(), 32);

         for(int j = 0; j < i; ++j) {
            this.addCursor(list.get(j));
         }
      }

   }

   public void save(CompoundTag pTag) {
      SculkSpreader.ChargeCursor.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.cursors).resultOrPartial(LOGGER::error).ifPresent((p_222273_) -> {
         pTag.put("cursors", p_222273_);
      });
   }

   public void addCursors(BlockPos pPos, int pCharge) {
      while(pCharge > 0) {
         int i = Math.min(pCharge, 1000);
         this.addCursor(new SculkSpreader.ChargeCursor(pPos, i));
         pCharge -= i;
      }

   }

   private void addCursor(SculkSpreader.ChargeCursor pCursor) {
      if (this.cursors.size() < 32) {
         this.cursors.add(pCursor);
      }
   }

   public void updateCursors(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, boolean pShouldConvertBlocks) {
      if (!this.cursors.isEmpty()) {
         List<SculkSpreader.ChargeCursor> list = new ArrayList<>();
         Map<BlockPos, SculkSpreader.ChargeCursor> map = new HashMap<>();
         Object2IntMap<BlockPos> object2intmap = new Object2IntOpenHashMap<>();

         for(SculkSpreader.ChargeCursor sculkspreader$chargecursor : this.cursors) {
            sculkspreader$chargecursor.update(pLevel, pPos, pRandom, this, pShouldConvertBlocks);
            if (sculkspreader$chargecursor.charge <= 0) {
               pLevel.levelEvent(3006, sculkspreader$chargecursor.getPos(), 0);
            } else {
               BlockPos blockpos = sculkspreader$chargecursor.getPos();
               object2intmap.computeInt(blockpos, (p_222264_, p_222265_) -> {
                  return (p_222265_ == null ? 0 : p_222265_) + sculkspreader$chargecursor.charge;
               });
               SculkSpreader.ChargeCursor sculkspreader$chargecursor1 = map.get(blockpos);
               if (sculkspreader$chargecursor1 == null) {
                  map.put(blockpos, sculkspreader$chargecursor);
                  list.add(sculkspreader$chargecursor);
               } else if (!this.isWorldGeneration() && sculkspreader$chargecursor.charge + sculkspreader$chargecursor1.charge <= 1000) {
                  sculkspreader$chargecursor1.mergeWith(sculkspreader$chargecursor);
               } else {
                  list.add(sculkspreader$chargecursor);
                  if (sculkspreader$chargecursor.charge < sculkspreader$chargecursor1.charge) {
                     map.put(blockpos, sculkspreader$chargecursor);
                  }
               }
            }
         }

         for(Object2IntMap.Entry<BlockPos> entry : object2intmap.object2IntEntrySet()) {
            BlockPos blockpos1 = entry.getKey();
            int k = entry.getIntValue();
            SculkSpreader.ChargeCursor sculkspreader$chargecursor2 = map.get(blockpos1);
            Collection<Direction> collection = sculkspreader$chargecursor2 == null ? null : sculkspreader$chargecursor2.getFacingData();
            if (k > 0 && collection != null) {
               int i = (int)(Math.log1p((double)k) / (double)2.3F) + 1;
               int j = (i << 6) + MultifaceBlock.pack(collection);
               pLevel.levelEvent(3006, blockpos1, j);
            }
         }

         this.cursors = list;
      }
   }

   public static class ChargeCursor {
      private static final ObjectArrayList<Vec3i> NON_CORNER_NEIGHBOURS = Util.make(new ObjectArrayList<>(18), (p_222338_) -> {
         BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).filter((p_222336_) -> {
            return (p_222336_.getX() == 0 || p_222336_.getY() == 0 || p_222336_.getZ() == 0) && !p_222336_.equals(BlockPos.ZERO);
         }).map(BlockPos::immutable).forEach(p_222338_::add);
      });
      public static final int MAX_CURSOR_DECAY_DELAY = 1;
      private BlockPos pos;
      int charge;
      private int updateDelay;
      private int decayDelay;
      @Nullable
      private Set<Direction> facings;
      private static final Codec<Set<Direction>> DIRECTION_SET = Direction.CODEC.listOf().xmap((p_222340_) -> {
         return Sets.newEnumSet(p_222340_, Direction.class);
      }, Lists::newArrayList);
      public static final Codec<SculkSpreader.ChargeCursor> CODEC = RecordCodecBuilder.create((p_222330_) -> {
         return p_222330_.group(BlockPos.CODEC.fieldOf("pos").forGetter(SculkSpreader.ChargeCursor::getPos), Codec.intRange(0, 1000).fieldOf("charge").orElse(0).forGetter(SculkSpreader.ChargeCursor::getCharge), Codec.intRange(0, 1).fieldOf("decay_delay").orElse(1).forGetter(SculkSpreader.ChargeCursor::getDecayDelay), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("update_delay").orElse(0).forGetter((p_222346_) -> {
            return p_222346_.updateDelay;
         }), DIRECTION_SET.optionalFieldOf("facings").forGetter((p_222343_) -> {
            return Optional.ofNullable(p_222343_.getFacingData());
         })).apply(p_222330_, SculkSpreader.ChargeCursor::new);
      });

      private ChargeCursor(BlockPos p_222299_, int p_222300_, int p_222301_, int p_222302_, Optional<Set<Direction>> p_222303_) {
         this.pos = p_222299_;
         this.charge = p_222300_;
         this.decayDelay = p_222301_;
         this.updateDelay = p_222302_;
         this.facings = p_222303_.orElse((Set<Direction>)null);
      }

      public ChargeCursor(BlockPos pPos, int pCharge) {
         this(pPos, pCharge, 1, 0, Optional.empty());
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public int getCharge() {
         return this.charge;
      }

      public int getDecayDelay() {
         return this.decayDelay;
      }

      @Nullable
      public Set<Direction> getFacingData() {
         return this.facings;
      }

      private boolean shouldUpdate(LevelAccessor pLevel, BlockPos pPos, boolean pIsWorldGeneration) {
         if (this.charge <= 0) {
            return false;
         } else if (pIsWorldGeneration) {
            return true;
         } else if (pLevel instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)pLevel;
            return serverlevel.shouldTickBlocksAt(pPos);
         } else {
            return false;
         }
      }

      public void update(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, SculkSpreader pSpreader, boolean pShouldConvertBlocks) {
         if (this.shouldUpdate(pLevel, pPos, pSpreader.isWorldGeneration)) {
            if (this.updateDelay > 0) {
               --this.updateDelay;
            } else {
               BlockState blockstate = pLevel.getBlockState(this.pos);
               SculkBehaviour sculkbehaviour = getBlockBehaviour(blockstate);
               if (pShouldConvertBlocks && sculkbehaviour.attemptSpreadVein(pLevel, this.pos, blockstate, this.facings, pSpreader.isWorldGeneration())) {
                  if (sculkbehaviour.canChangeBlockStateOnSpread()) {
                     blockstate = pLevel.getBlockState(this.pos);
                     sculkbehaviour = getBlockBehaviour(blockstate);
                  }

                  pLevel.playSound((Player)null, this.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
               }

               this.charge = sculkbehaviour.attemptUseCharge(this, pLevel, pPos, pRandom, pSpreader, pShouldConvertBlocks);
               if (this.charge <= 0) {
                  sculkbehaviour.onDischarged(pLevel, blockstate, this.pos, pRandom);
               } else {
                  BlockPos blockpos = getValidMovementPos(pLevel, this.pos, pRandom);
                  if (blockpos != null) {
                     sculkbehaviour.onDischarged(pLevel, blockstate, this.pos, pRandom);
                     this.pos = blockpos.immutable();
                     if (pSpreader.isWorldGeneration() && !this.pos.closerThan(new Vec3i(pPos.getX(), this.pos.getY(), pPos.getZ()), 15.0D)) {
                        this.charge = 0;
                        return;
                     }

                     blockstate = pLevel.getBlockState(blockpos);
                  }

                  if (blockstate.getBlock() instanceof SculkBehaviour) {
                     this.facings = MultifaceBlock.availableFaces(blockstate);
                  }

                  this.decayDelay = sculkbehaviour.updateDecayDelay(this.decayDelay);
                  this.updateDelay = sculkbehaviour.getSculkSpreadDelay();
               }
            }
         }
      }

      void mergeWith(SculkSpreader.ChargeCursor pCursor) {
         this.charge += pCursor.charge;
         pCursor.charge = 0;
         this.updateDelay = Math.min(this.updateDelay, pCursor.updateDelay);
      }

      private static SculkBehaviour getBlockBehaviour(BlockState pState) {
         Block block = pState.getBlock();
         SculkBehaviour sculkbehaviour1;
         if (block instanceof SculkBehaviour sculkbehaviour) {
            sculkbehaviour1 = sculkbehaviour;
         } else {
            sculkbehaviour1 = SculkBehaviour.DEFAULT;
         }

         return sculkbehaviour1;
      }

      private static List<Vec3i> getRandomizedNonCornerNeighbourOffsets(RandomSource pRandom) {
         return Util.shuffledCopy(NON_CORNER_NEIGHBOURS, pRandom);
      }

      @Nullable
      private static BlockPos getValidMovementPos(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();
         BlockPos.MutableBlockPos blockpos$mutableblockpos1 = pPos.mutable();

         for(Vec3i vec3i : getRandomizedNonCornerNeighbourOffsets(pRandom)) {
            blockpos$mutableblockpos1.setWithOffset(pPos, vec3i);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos1);
            if (blockstate.getBlock() instanceof SculkBehaviour && isMovementUnobstructed(pLevel, pPos, blockpos$mutableblockpos1)) {
               blockpos$mutableblockpos.set(blockpos$mutableblockpos1);
               if (SculkVeinBlock.hasSubstrateAccess(pLevel, blockstate, blockpos$mutableblockpos1)) {
                  break;
               }
            }
         }

         return blockpos$mutableblockpos.equals(pPos) ? null : blockpos$mutableblockpos;
      }

      private static boolean isMovementUnobstructed(LevelAccessor pLevel, BlockPos pFromPos, BlockPos pToPos) {
         if (pFromPos.distManhattan(pToPos) == 1) {
            return true;
         } else {
            BlockPos blockpos = pToPos.subtract(pFromPos);
            Direction direction = Direction.fromAxisAndDirection(Direction.Axis.X, blockpos.getX() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction1 = Direction.fromAxisAndDirection(Direction.Axis.Y, blockpos.getY() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction2 = Direction.fromAxisAndDirection(Direction.Axis.Z, blockpos.getZ() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            if (blockpos.getX() == 0) {
               return isUnobstructed(pLevel, pFromPos, direction1) || isUnobstructed(pLevel, pFromPos, direction2);
            } else if (blockpos.getY() == 0) {
               return isUnobstructed(pLevel, pFromPos, direction) || isUnobstructed(pLevel, pFromPos, direction2);
            } else {
               return isUnobstructed(pLevel, pFromPos, direction) || isUnobstructed(pLevel, pFromPos, direction1);
            }
         }
      }

      private static boolean isUnobstructed(LevelAccessor pLevel, BlockPos pPos, Direction pDirection) {
         BlockPos blockpos = pPos.relative(pDirection);
         return !pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, pDirection.getOpposite());
      }
   }
}