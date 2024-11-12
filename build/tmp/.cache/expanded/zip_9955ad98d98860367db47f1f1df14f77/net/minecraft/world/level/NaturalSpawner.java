package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

// TODO: ForgeHooks.canEntitySpawn
public final class NaturalSpawner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MIN_SPAWN_DISTANCE = 24;
   public static final int SPAWN_DISTANCE_CHUNK = 8;
   public static final int SPAWN_DISTANCE_BLOCK = 128;
   static final int MAGIC_NUMBER = (int)Math.pow(17.0D, 2.0D);
   private static final MobCategory[] SPAWNING_CATEGORIES = Stream.of(MobCategory.values()).filter((p_47037_) -> {
      return p_47037_ != MobCategory.MISC;
   }).toArray((p_46983_) -> {
      return new MobCategory[p_46983_];
   });

   private NaturalSpawner() {
   }

   public static NaturalSpawner.SpawnState createState(int pSpawnableChunkCount, Iterable<Entity> pEntities, NaturalSpawner.ChunkGetter pChunkGetter, LocalMobCapCalculator pCalculator) {
      PotentialCalculator potentialcalculator = new PotentialCalculator();
      Object2IntOpenHashMap<MobCategory> object2intopenhashmap = new Object2IntOpenHashMap<>();

      for(Entity entity : pEntities) {
         if (entity instanceof Mob mob) {
            if (mob.isPersistenceRequired() || mob.requiresCustomPersistence()) {
               continue;
            }
         }

         MobCategory mobcategory = entity.getClassification(true);
         if (mobcategory != MobCategory.MISC) {
            BlockPos blockpos = entity.blockPosition();
            pChunkGetter.query(ChunkPos.asLong(blockpos), (p_275163_) -> {
               MobSpawnSettings.MobSpawnCost mobspawnsettings$mobspawncost = getRoughBiome(blockpos, p_275163_).getMobSettings().getMobSpawnCost(entity.getType());
               if (mobspawnsettings$mobspawncost != null) {
                  potentialcalculator.addCharge(entity.blockPosition(), mobspawnsettings$mobspawncost.charge());
               }

               if (entity instanceof Mob) {
                  pCalculator.addMob(p_275163_.getPos(), mobcategory);
               }

               object2intopenhashmap.addTo(mobcategory, 1);
            });
         }
      }

      return new NaturalSpawner.SpawnState(pSpawnableChunkCount, object2intopenhashmap, potentialcalculator, pCalculator);
   }

   static Biome getRoughBiome(BlockPos pPos, ChunkAccess pChunk) {
      return pChunk.getNoiseBiome(QuartPos.fromBlock(pPos.getX()), QuartPos.fromBlock(pPos.getY()), QuartPos.fromBlock(pPos.getZ())).value();
   }

   public static void spawnForChunk(ServerLevel pLevel, LevelChunk pChunk, NaturalSpawner.SpawnState pSpawnState, boolean pSpawnFriendlies, boolean pSpawnMonsters, boolean pForcedDespawn) {
      pLevel.getProfiler().push("spawner");

      for(MobCategory mobcategory : SPAWNING_CATEGORIES) {
         if ((pSpawnFriendlies || !mobcategory.isFriendly()) && (pSpawnMonsters || mobcategory.isFriendly()) && (pForcedDespawn || !mobcategory.isPersistent()) && pSpawnState.canSpawnForCategory(mobcategory, pChunk.getPos())) {
            spawnCategoryForChunk(mobcategory, pLevel, pChunk, pSpawnState::canSpawn, pSpawnState::afterSpawn);
         }
      }

      pLevel.getProfiler().pop();
   }

   public static void spawnCategoryForChunk(MobCategory pCategory, ServerLevel pLevel, LevelChunk pChunk, NaturalSpawner.SpawnPredicate pFilter, NaturalSpawner.AfterSpawnCallback pCallback) {
      BlockPos blockpos = getRandomPosWithin(pLevel, pChunk);
      if (blockpos.getY() >= pLevel.getMinBuildHeight() + 1) {
         spawnCategoryForPosition(pCategory, pLevel, pChunk, blockpos, pFilter, pCallback);
      }
   }

   @VisibleForDebug
   public static void spawnCategoryForPosition(MobCategory pCategory, ServerLevel pLevel, BlockPos pPos) {
      spawnCategoryForPosition(pCategory, pLevel, pLevel.getChunk(pPos), pPos, (p_151606_, p_151607_, p_151608_) -> {
         return true;
      }, (p_151610_, p_151611_) -> {
      });
   }

   public static void spawnCategoryForPosition(MobCategory pCategory, ServerLevel pLevel, ChunkAccess pChunk, BlockPos pPos, NaturalSpawner.SpawnPredicate pFilter, NaturalSpawner.AfterSpawnCallback pCallback) {
      StructureManager structuremanager = pLevel.structureManager();
      ChunkGenerator chunkgenerator = pLevel.getChunkSource().getGenerator();
      int i = pPos.getY();
      BlockState blockstate = pChunk.getBlockState(pPos);
      if (!blockstate.isRedstoneConductor(pChunk, pPos)) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         int j = 0;

         for(int k = 0; k < 3; ++k) {
            int l = pPos.getX();
            int i1 = pPos.getZ();
            int j1 = 6;
            MobSpawnSettings.SpawnerData mobspawnsettings$spawnerdata = null;
            SpawnGroupData spawngroupdata = null;
            int k1 = Mth.ceil(pLevel.random.nextFloat() * 4.0F);
            int l1 = 0;

            for(int i2 = 0; i2 < k1; ++i2) {
               l += pLevel.random.nextInt(6) - pLevel.random.nextInt(6);
               i1 += pLevel.random.nextInt(6) - pLevel.random.nextInt(6);
               blockpos$mutableblockpos.set(l, i, i1);
               double d0 = (double)l + 0.5D;
               double d1 = (double)i1 + 0.5D;
               Player player = pLevel.getNearestPlayer(d0, (double)i, d1, -1.0D, false);
               if (player != null) {
                  double d2 = player.distanceToSqr(d0, (double)i, d1);
                  if (isRightDistanceToPlayerAndSpawnPoint(pLevel, pChunk, blockpos$mutableblockpos, d2)) {
                     if (mobspawnsettings$spawnerdata == null) {
                        Optional<MobSpawnSettings.SpawnerData> optional = getRandomSpawnMobAt(pLevel, structuremanager, chunkgenerator, pCategory, pLevel.random, blockpos$mutableblockpos);
                        if (optional.isEmpty()) {
                           break;
                        }

                        mobspawnsettings$spawnerdata = optional.get();
                        k1 = mobspawnsettings$spawnerdata.minCount + pLevel.random.nextInt(1 + mobspawnsettings$spawnerdata.maxCount - mobspawnsettings$spawnerdata.minCount);
                     }

                     if (isValidSpawnPostitionForType(pLevel, pCategory, structuremanager, chunkgenerator, mobspawnsettings$spawnerdata, blockpos$mutableblockpos, d2) && pFilter.test(mobspawnsettings$spawnerdata.type, blockpos$mutableblockpos, pChunk)) {
                        Mob mob = getMobForSpawn(pLevel, mobspawnsettings$spawnerdata.type);
                        if (mob == null) {
                           return;
                        }

                        mob.moveTo(d0, (double)i, d1, pLevel.random.nextFloat() * 360.0F, 0.0F);
                        if (isValidPositionForMob(pLevel, mob, d2)) {
                           spawngroupdata = mob.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawngroupdata, (CompoundTag)null);
                           ++j;
                           ++l1;
                           pLevel.addFreshEntityWithPassengers(mob);
                           pCallback.run(mob, pChunk);
                           if (j >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(mob)) {
                              return;
                           }

                           if (mob.isMaxGroupSizeReached(l1)) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel pLevel, ChunkAccess pChunk, BlockPos.MutableBlockPos pPos, double pDistance) {
      if (pDistance <= 576.0D) {
         return false;
      } else if (pLevel.getSharedSpawnPos().closerToCenterThan(new Vec3((double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D), 24.0D)) {
         return false;
      } else {
         return Objects.equals(new ChunkPos(pPos), pChunk.getPos()) || pLevel.isNaturalSpawningAllowed(pPos);
      }
   }

   private static boolean isValidSpawnPostitionForType(ServerLevel pLevel, MobCategory pCategory, StructureManager pStructureManager, ChunkGenerator pGenerator, MobSpawnSettings.SpawnerData pData, BlockPos.MutableBlockPos pPos, double pDistance) {
      EntityType<?> entitytype = pData.type;
      if (entitytype.getCategory() == MobCategory.MISC) {
         return false;
      } else if (!entitytype.canSpawnFarFromPlayer() && pDistance > (double)(entitytype.getCategory().getDespawnDistance() * entitytype.getCategory().getDespawnDistance())) {
         return false;
      } else if (entitytype.canSummon() && canSpawnMobAt(pLevel, pStructureManager, pGenerator, pCategory, pData, pPos)) {
         SpawnPlacements.Type spawnplacements$type = SpawnPlacements.getPlacementType(entitytype);
         if (!isSpawnPositionOk(spawnplacements$type, pLevel, pPos, entitytype)) {
            return false;
         } else if (!SpawnPlacements.checkSpawnRules(entitytype, pLevel, MobSpawnType.NATURAL, pPos, pLevel.random)) {
            return false;
         } else {
            return pLevel.noCollision(entitytype.getAABB((double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D));
         }
      } else {
         return false;
      }
   }

   @Nullable
   private static Mob getMobForSpawn(ServerLevel pLevel, EntityType<?> pEntityType) {
      try {
         Entity entity = pEntityType.create(pLevel);
         if (entity instanceof Mob) {
            return (Mob)entity;
         }

         LOGGER.warn("Can't spawn entity of type: {}", (Object)BuiltInRegistries.ENTITY_TYPE.getKey(pEntityType));
      } catch (Exception exception) {
         LOGGER.warn("Failed to create mob", (Throwable)exception);
      }

      return null;
   }

   private static boolean isValidPositionForMob(ServerLevel pLevel, Mob pMob, double pDistance) {
      if (pDistance > (double)(pMob.getType().getCategory().getDespawnDistance() * pMob.getType().getCategory().getDespawnDistance()) && pMob.removeWhenFarAway(pDistance)) {
         return false;
      } else {
         return net.minecraftforge.event.ForgeEventFactory.checkSpawnPosition(pMob, pLevel, MobSpawnType.NATURAL);
      }
   }

   private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(ServerLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, MobCategory pCategory, RandomSource pRandom, BlockPos pPos) {
      Holder<Biome> holder = pLevel.getBiome(pPos);
      return pCategory == MobCategory.WATER_AMBIENT && holder.is(BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS) && pRandom.nextFloat() < 0.98F ? Optional.empty() : mobsAt(pLevel, pStructureManager, pGenerator, pCategory, pPos, holder).getRandom(pRandom);
   }

   private static boolean canSpawnMobAt(ServerLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, MobCategory pCategory, MobSpawnSettings.SpawnerData pData, BlockPos pPos) {
      return mobsAt(pLevel, pStructureManager, pGenerator, pCategory, pPos, (Holder<Biome>)null).unwrap().contains(pData);
   }

   private static WeightedRandomList<MobSpawnSettings.SpawnerData> mobsAt(ServerLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, MobCategory pCategory, BlockPos pPos, @Nullable Holder<Biome> pBiome) {
      // Forge: Add in potential spawns, and replace hardcoded nether fortress mob list
      return net.minecraftforge.event.ForgeEventFactory.getPotentialSpawns(pLevel, pCategory, pPos, isInNetherFortressBounds(pPos, pLevel, pCategory, pStructureManager) ? pStructureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.FORTRESS).spawnOverrides().get(MobCategory.MONSTER).spawns() : pGenerator.getMobsAt(pBiome != null ? pBiome : pLevel.getBiome(pPos), pStructureManager, pCategory, pPos));
   }

   public static boolean isInNetherFortressBounds(BlockPos pPos, ServerLevel pLevel, MobCategory pCategory, StructureManager pStructureManager) {
      if (pCategory == MobCategory.MONSTER && pLevel.getBlockState(pPos.below()).is(Blocks.NETHER_BRICKS)) {
         Structure structure = pStructureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.FORTRESS);
         return structure == null ? false : pStructureManager.getStructureAt(pPos, structure).isValid();
      } else {
         return false;
      }
   }

   private static BlockPos getRandomPosWithin(Level pLevel, LevelChunk pChunk) {
      ChunkPos chunkpos = pChunk.getPos();
      int i = chunkpos.getMinBlockX() + pLevel.random.nextInt(16);
      int j = chunkpos.getMinBlockZ() + pLevel.random.nextInt(16);
      int k = pChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
      int l = Mth.randomBetweenInclusive(pLevel.random, pLevel.getMinBuildHeight(), k);
      return new BlockPos(i, l, j);
   }

   public static boolean isValidEmptySpawnBlock(BlockGetter pBlock, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, EntityType<?> pEntityType) {
      if (pBlockState.isCollisionShapeFullBlock(pBlock, pPos)) {
         return false;
      } else if (pBlockState.isSignalSource()) {
         return false;
      } else if (!pFluidState.isEmpty()) {
         return false;
      } else if (pBlockState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
         return false;
      } else {
         return !pEntityType.isBlockDangerous(pBlockState);
      }
   }

   public static boolean isSpawnPositionOk(SpawnPlacements.Type pPlaceType, LevelReader pLevel, BlockPos pPos, @Nullable EntityType<?> pEntityType) {
      if (pPlaceType == SpawnPlacements.Type.NO_RESTRICTIONS) {
         return true;
      } else if (pEntityType != null && pLevel.getWorldBorder().isWithinBounds(pPos)) {
         return pPlaceType.canSpawnAt(pLevel, pPos, pEntityType);
      }
      return false;
   }

   public static boolean canSpawnAtBody(SpawnPlacements.Type pPlaceType, LevelReader pLevel, BlockPos pPos, @Nullable EntityType<?> pEntityType) {
      {
         BlockState blockstate = pLevel.getBlockState(pPos);
         FluidState fluidstate = pLevel.getFluidState(pPos);
         BlockPos blockpos = pPos.above();
         BlockPos blockpos1 = pPos.below();
         switch (pPlaceType) {
            case IN_WATER:
               return fluidstate.is(FluidTags.WATER) && !pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos);
            case IN_LAVA:
               return fluidstate.is(FluidTags.LAVA);
            case ON_GROUND:
            default:
               BlockState blockstate1 = pLevel.getBlockState(blockpos1);
            if (!blockstate1.isValidSpawn(pLevel, blockpos1, pPlaceType, pEntityType)) {
                  return false;
               } else {
                  return isValidEmptySpawnBlock(pLevel, pPos, blockstate, fluidstate, pEntityType) && isValidEmptySpawnBlock(pLevel, blockpos, pLevel.getBlockState(blockpos), pLevel.getFluidState(blockpos), pEntityType);
               }
         }
      }
   }

   public static void spawnMobsForChunkGeneration(ServerLevelAccessor pLevelAccessor, Holder<Biome> pBiome, ChunkPos pChunkPos, RandomSource pRandom) {
      MobSpawnSettings mobspawnsettings = pBiome.value().getMobSettings();
      WeightedRandomList<MobSpawnSettings.SpawnerData> weightedrandomlist = mobspawnsettings.getMobs(MobCategory.CREATURE);
      if (!weightedrandomlist.isEmpty()) {
         int i = pChunkPos.getMinBlockX();
         int j = pChunkPos.getMinBlockZ();

         while(pRandom.nextFloat() < mobspawnsettings.getCreatureProbability()) {
            Optional<MobSpawnSettings.SpawnerData> optional = weightedrandomlist.getRandom(pRandom);
            if (optional.isPresent()) {
               MobSpawnSettings.SpawnerData mobspawnsettings$spawnerdata = optional.get();
               int k = mobspawnsettings$spawnerdata.minCount + pRandom.nextInt(1 + mobspawnsettings$spawnerdata.maxCount - mobspawnsettings$spawnerdata.minCount);
               SpawnGroupData spawngroupdata = null;
               int l = i + pRandom.nextInt(16);
               int i1 = j + pRandom.nextInt(16);
               int j1 = l;
               int k1 = i1;

               for(int l1 = 0; l1 < k; ++l1) {
                  boolean flag = false;

                  for(int i2 = 0; !flag && i2 < 4; ++i2) {
                     BlockPos blockpos = getTopNonCollidingPos(pLevelAccessor, mobspawnsettings$spawnerdata.type, l, i1);
                     if (mobspawnsettings$spawnerdata.type.canSummon() && isSpawnPositionOk(SpawnPlacements.getPlacementType(mobspawnsettings$spawnerdata.type), pLevelAccessor, blockpos, mobspawnsettings$spawnerdata.type)) {
                        float f = mobspawnsettings$spawnerdata.type.getWidth();
                        double d0 = Mth.clamp((double)l, (double)i + (double)f, (double)i + 16.0D - (double)f);
                        double d1 = Mth.clamp((double)i1, (double)j + (double)f, (double)j + 16.0D - (double)f);
                        if (!pLevelAccessor.noCollision(mobspawnsettings$spawnerdata.type.getAABB(d0, (double)blockpos.getY(), d1)) || !SpawnPlacements.checkSpawnRules(mobspawnsettings$spawnerdata.type, pLevelAccessor, MobSpawnType.CHUNK_GENERATION, BlockPos.containing(d0, (double)blockpos.getY(), d1), pLevelAccessor.getRandom())) {
                           continue;
                        }

                        Entity entity;
                        try {
                           entity = mobspawnsettings$spawnerdata.type.create(pLevelAccessor.getLevel());
                        } catch (Exception exception) {
                           LOGGER.warn("Failed to create mob", (Throwable)exception);
                           continue;
                        }

                        if (entity == null) {
                           continue;
                        }

                        entity.moveTo(d0, (double)blockpos.getY(), d1, pRandom.nextFloat() * 360.0F, 0.0F);
                        if (entity instanceof Mob) {
                           Mob mob = (Mob)entity;
                           if (net.minecraftforge.event.ForgeEventFactory.checkSpawnPosition(mob, pLevelAccessor, MobSpawnType.CHUNK_GENERATION)) {
                              spawngroupdata = mob.finalizeSpawn(pLevelAccessor, pLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawngroupdata, (CompoundTag)null);
                              pLevelAccessor.addFreshEntityWithPassengers(mob);
                              flag = true;
                           }
                        }
                     }

                     l += pRandom.nextInt(5) - pRandom.nextInt(5);

                     for(i1 += pRandom.nextInt(5) - pRandom.nextInt(5); l < i || l >= i + 16 || i1 < j || i1 >= j + 16; i1 = k1 + pRandom.nextInt(5) - pRandom.nextInt(5)) {
                        l = j1 + pRandom.nextInt(5) - pRandom.nextInt(5);
                     }
                  }
               }
            }
         }

      }
   }

   private static BlockPos getTopNonCollidingPos(LevelReader pLevel, EntityType<?> pEntityType, int pX, int pZ) {
      int i = pLevel.getHeight(SpawnPlacements.getHeightmapType(pEntityType), pX, pZ);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(pX, i, pZ);
      if (pLevel.dimensionType().hasCeiling()) {
         do {
            blockpos$mutableblockpos.move(Direction.DOWN);
         } while(!pLevel.getBlockState(blockpos$mutableblockpos).isAir());

         do {
            blockpos$mutableblockpos.move(Direction.DOWN);
         } while(pLevel.getBlockState(blockpos$mutableblockpos).isAir() && blockpos$mutableblockpos.getY() > pLevel.getMinBuildHeight());
      }

      if (SpawnPlacements.getPlacementType(pEntityType) == SpawnPlacements.Type.ON_GROUND) {
         BlockPos blockpos = blockpos$mutableblockpos.below();
         if (pLevel.getBlockState(blockpos).isPathfindable(pLevel, blockpos, PathComputationType.LAND)) {
            return blockpos;
         }
      }

      return blockpos$mutableblockpos.immutable();
   }

   @FunctionalInterface
   public interface AfterSpawnCallback {
      void run(Mob pMob, ChunkAccess pChunk);
   }

   @FunctionalInterface
   public interface ChunkGetter {
      void query(long pChunkPos, Consumer<LevelChunk> pChunkConsumer);
   }

   @FunctionalInterface
   public interface SpawnPredicate {
      boolean test(EntityType<?> pEntityType, BlockPos pPos, ChunkAccess pChunk);
   }

   public static class SpawnState {
      private final int spawnableChunkCount;
      private final Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
      private final PotentialCalculator spawnPotential;
      private final Object2IntMap<MobCategory> unmodifiableMobCategoryCounts;
      private final LocalMobCapCalculator localMobCapCalculator;
      @Nullable
      private BlockPos lastCheckedPos;
      @Nullable
      private EntityType<?> lastCheckedType;
      private double lastCharge;

      SpawnState(int pSpawnableChunkCount, Object2IntOpenHashMap<MobCategory> pMobCategoryCounts, PotentialCalculator pSpawnPotential, LocalMobCapCalculator pLocalMobCapCalculator) {
         this.spawnableChunkCount = pSpawnableChunkCount;
         this.mobCategoryCounts = pMobCategoryCounts;
         this.spawnPotential = pSpawnPotential;
         this.localMobCapCalculator = pLocalMobCapCalculator;
         this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(pMobCategoryCounts);
      }

      private boolean canSpawn(EntityType<?> pEntityType, BlockPos pPos, ChunkAccess pChunk) {
         this.lastCheckedPos = pPos;
         this.lastCheckedType = pEntityType;
         MobSpawnSettings.MobSpawnCost mobspawnsettings$mobspawncost = NaturalSpawner.getRoughBiome(pPos, pChunk).getMobSettings().getMobSpawnCost(pEntityType);
         if (mobspawnsettings$mobspawncost == null) {
            this.lastCharge = 0.0D;
            return true;
         } else {
            double d0 = mobspawnsettings$mobspawncost.charge();
            this.lastCharge = d0;
            double d1 = this.spawnPotential.getPotentialEnergyChange(pPos, d0);
            return d1 <= mobspawnsettings$mobspawncost.energyBudget();
         }
      }

      private void afterSpawn(Mob pMob, ChunkAccess pChunk) {
         EntityType<?> entitytype = pMob.getType();
         BlockPos blockpos = pMob.blockPosition();
         double d0;
         if (blockpos.equals(this.lastCheckedPos) && entitytype == this.lastCheckedType) {
            d0 = this.lastCharge;
         } else {
            MobSpawnSettings.MobSpawnCost mobspawnsettings$mobspawncost = NaturalSpawner.getRoughBiome(blockpos, pChunk).getMobSettings().getMobSpawnCost(entitytype);
            if (mobspawnsettings$mobspawncost != null) {
               d0 = mobspawnsettings$mobspawncost.charge();
            } else {
               d0 = 0.0D;
            }
         }

         this.spawnPotential.addCharge(blockpos, d0);
         MobCategory mobcategory = entitytype.getCategory();
         this.mobCategoryCounts.addTo(mobcategory, 1);
         this.localMobCapCalculator.addMob(new ChunkPos(blockpos), mobcategory);
      }

      public int getSpawnableChunkCount() {
         return this.spawnableChunkCount;
      }

      public Object2IntMap<MobCategory> getMobCategoryCounts() {
         return this.unmodifiableMobCategoryCounts;
      }

      boolean canSpawnForCategory(MobCategory pCategory, ChunkPos pPos) {
         int i = pCategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / NaturalSpawner.MAGIC_NUMBER;
         if (this.mobCategoryCounts.getInt(pCategory) >= i) {
            return false;
         } else {
            return this.localMobCapCalculator.canSpawn(pCategory, pPos);
         }
      }
   }
}
