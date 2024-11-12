package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientLevel extends Level {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final double FLUID_PARTICLE_SPAWN_OFFSET = 0.05D;
   private static final int NORMAL_LIGHT_UPDATES_PER_FRAME = 10;
   private static final int LIGHT_UPDATE_QUEUE_SIZE_THRESHOLD = 1000;
   final EntityTickList tickingEntities = new EntityTickList();
   private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(Entity.class, new ClientLevel.EntityCallbacks());
   private final ClientPacketListener connection;
   private final LevelRenderer levelRenderer;
   private final ClientLevel.ClientLevelData clientLevelData;
   private final DimensionSpecialEffects effects;
   private final Minecraft minecraft = Minecraft.getInstance();
   final List<AbstractClientPlayer> players = Lists.newArrayList();
   private Scoreboard scoreboard = new Scoreboard();
   private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
   private static final long CLOUD_COLOR = 16777215L;
   private int skyFlashTime;
   private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap<>(3), (p_194170_) -> {
      p_194170_.put(BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache((p_194181_) -> {
         return this.calculateBlockTint(p_194181_, BiomeColors.GRASS_COLOR_RESOLVER);
      }));
      p_194170_.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache((p_194177_) -> {
         return this.calculateBlockTint(p_194177_, BiomeColors.FOLIAGE_COLOR_RESOLVER);
      }));
      p_194170_.put(BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache((p_194168_) -> {
         return this.calculateBlockTint(p_194168_, BiomeColors.WATER_COLOR_RESOLVER);
      }));
      net.minecraftforge.client.ColorResolverManager.registerBlockTintCaches(ClientLevel.this, p_194170_);
   });
   private final ClientChunkCache chunkSource;
   private final Deque<Runnable> lightUpdateQueue = Queues.newArrayDeque();
   private int serverSimulationDistance;
   private final BlockStatePredictionHandler blockStatePredictionHandler = new BlockStatePredictionHandler();
   private static final Set<Item> MARKER_PARTICLE_ITEMS = Set.of(Items.BARRIER, Items.LIGHT);
   private final it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraftforge.entity.PartEntity<?>> partEntities = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();
   private final net.minecraftforge.client.model.data.ModelDataManager modelDataManager = new net.minecraftforge.client.model.data.ModelDataManager(this);

   public void handleBlockChangedAck(int pSequence) {
      this.blockStatePredictionHandler.endPredictionsUpTo(pSequence, this);
   }

   public void setServerVerifiedBlockState(BlockPos pPos, BlockState pState, int pFlags) {
      if (!this.blockStatePredictionHandler.updateKnownServerState(pPos, pState)) {
         super.setBlock(pPos, pState, pFlags, 512);
      }

   }

   public void syncBlockState(BlockPos pPos, BlockState pState, Vec3 pPlayerPos) {
      BlockState blockstate = this.getBlockState(pPos);
      if (blockstate != pState) {
         this.setBlock(pPos, pState, 19);
         Player player = this.minecraft.player;
         if (this == player.level() && player.isColliding(pPos, pState)) {
            player.absMoveTo(pPlayerPos.x, pPlayerPos.y, pPlayerPos.z);
         }
      }

   }

   BlockStatePredictionHandler getBlockStatePredictionHandler() {
      return this.blockStatePredictionHandler;
   }

   public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
      if (this.blockStatePredictionHandler.isPredicting()) {
         BlockState blockstate = this.getBlockState(pPos);
         boolean flag = super.setBlock(pPos, pState, pFlags, pRecursionLeft);
         if (flag) {
            this.blockStatePredictionHandler.retainKnownServerState(pPos, blockstate, this.minecraft.player);
         }

         return flag;
      } else {
         return super.setBlock(pPos, pState, pFlags, pRecursionLeft);
      }
   }

   public ClientLevel(ClientPacketListener pConnection, ClientLevel.ClientLevelData pClientLevelData, ResourceKey<Level> pDimension, Holder<DimensionType> pDimensionType, int pViewDistance, int pServerSimulationDistance, Supplier<ProfilerFiller> pProfiler, LevelRenderer pLevelRenderer, boolean pIsDebug, long pBiomeZoomSeed) {
      super(pClientLevelData, pDimension, pConnection.registryAccess(), pDimensionType, pProfiler, true, pIsDebug, pBiomeZoomSeed, 1000000);
      this.connection = pConnection;
      this.chunkSource = new ClientChunkCache(this, pViewDistance);
      this.clientLevelData = pClientLevelData;
      this.levelRenderer = pLevelRenderer;
      this.effects = DimensionSpecialEffects.forType(pDimensionType.value());
      this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
      this.serverSimulationDistance = pServerSimulationDistance;
      this.updateSkyBrightness();
      this.prepareWeather();
      this.gatherCapabilities();
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Load(this));
   }

   public void queueLightUpdate(Runnable pTask) {
      this.lightUpdateQueue.add(pTask);
   }

   public void pollLightUpdates() {
      int i = this.lightUpdateQueue.size();
      int j = i < 1000 ? Math.max(10, i / 10) : i;

      for(int k = 0; k < j; ++k) {
         Runnable runnable = this.lightUpdateQueue.poll();
         if (runnable == null) {
            break;
         }

         runnable.run();
      }

   }

   public boolean isLightUpdateQueueEmpty() {
      return this.lightUpdateQueue.isEmpty();
   }

   public DimensionSpecialEffects effects() {
      return this.effects;
   }

   /**
    * Runs a single tick for the world
    */
   public void tick(BooleanSupplier pHasTimeLeft) {
      this.getWorldBorder().tick();
      this.tickTime();
      if (this.skyFlashTime > 0) {
         this.setSkyFlashTime(this.skyFlashTime - 1);
      }

      this.getProfiler().push("blocks");
      this.chunkSource.tick(pHasTimeLeft, true);
      this.getProfiler().pop();
   }

   private void tickTime() {
      this.setGameTime(this.levelData.getGameTime() + 1L);
      if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
         this.setDayTime(this.levelData.getDayTime() + 1L);
      }

   }

   public void setGameTime(long pTime) {
      this.clientLevelData.setGameTime(pTime);
   }

   /**
    * Sets the world time.
    */
   public void setDayTime(long pTime) {
      if (pTime < 0L) {
         pTime = -pTime;
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, (MinecraftServer)null);
      } else {
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, (MinecraftServer)null);
      }

      this.clientLevelData.setDayTime(pTime);
   }

   public Iterable<Entity> entitiesForRendering() {
      return this.getEntities().getAll();
   }

   public void tickEntities() {
      ProfilerFiller profilerfiller = this.getProfiler();
      profilerfiller.push("entities");
      this.tickingEntities.forEach((p_194183_) -> {
         if (!p_194183_.isRemoved() && !p_194183_.isPassenger()) {
            this.guardEntityTick(this::tickNonPassenger, p_194183_);
         }
      });
      profilerfiller.pop();
      this.tickBlockEntities();
   }

   public boolean shouldTickDeath(Entity pEntity) {
      return pEntity.chunkPosition().getChessboardDistance(this.minecraft.player.chunkPosition()) <= this.serverSimulationDistance;
   }

   public void tickNonPassenger(Entity p_104640_) {
      p_104640_.setOldPosAndRot();
      ++p_104640_.tickCount;
      this.getProfiler().push(() -> {
         return BuiltInRegistries.ENTITY_TYPE.getKey(p_104640_.getType()).toString();
      });
      if (p_104640_.canUpdate())
      p_104640_.tick();
      this.getProfiler().pop();

      for(Entity entity : p_104640_.getPassengers()) {
         this.tickPassenger(p_104640_, entity);
      }

   }

   private void tickPassenger(Entity pMount, Entity pRider) {
      if (!pRider.isRemoved() && pRider.getVehicle() == pMount) {
         if (pRider instanceof Player || this.tickingEntities.contains(pRider)) {
            pRider.setOldPosAndRot();
            ++pRider.tickCount;
            pRider.rideTick();

            for(Entity entity : pRider.getPassengers()) {
               this.tickPassenger(pRider, entity);
            }

         }
      } else {
         pRider.stopRiding();
      }
   }

   public void unload(LevelChunk pChunk) {
      pChunk.clearAllBlockEntities();
      this.chunkSource.getLightEngine().setLightEnabled(pChunk.getPos(), false);
      this.entityStorage.stopTicking(pChunk.getPos());
   }

   public void onChunkLoaded(ChunkPos pChunkPos) {
      this.tintCaches.forEach((p_194154_, p_194155_) -> {
         p_194155_.invalidateForChunk(pChunkPos.x, pChunkPos.z);
      });
      this.entityStorage.startTicking(pChunkPos);
   }

   public void clearTintCaches() {
      this.tintCaches.forEach((p_194157_, p_194158_) -> {
         p_194158_.invalidateAll();
      });
   }

   public boolean hasChunk(int pChunkX, int pChunkZ) {
      return true;
   }

   public int getEntityCount() {
      return this.entityStorage.count();
   }

   public void addPlayer(int pPlayerId, AbstractClientPlayer pPlayerEntity) {
      this.addEntity(pPlayerId, pPlayerEntity);
   }

   public void putNonPlayerEntity(int pEntityId, Entity pEntityToSpawn) {
      this.addEntity(pEntityId, pEntityToSpawn);
   }

   private void addEntity(int pEntityId, Entity pEntityToSpawn) {
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinLevelEvent(pEntityToSpawn, this))) return;
      this.removeEntity(pEntityId, Entity.RemovalReason.DISCARDED);
      this.entityStorage.addEntity(pEntityToSpawn);
      pEntityToSpawn.onAddedToWorld();
   }

   public void removeEntity(int pEntityId, Entity.RemovalReason pReason) {
      Entity entity = this.getEntities().get(pEntityId);
      if (entity != null) {
         entity.setRemoved(pReason);
         entity.onClientRemoval();
      }

   }

   /**
    * Returns the Entity with the given ID, or null if it doesn't exist in this World.
    */
   @Nullable
   public Entity getEntity(int pId) {
      return this.getEntities().get(pId);
   }

   /**
    * If on MP, sends a quitting packet.
    */
   public void disconnect() {
      this.connection.getConnection().disconnect(Component.translatable("multiplayer.status.quitting"));
   }

   public void animateTick(int pPosX, int pPosY, int pPosZ) {
      int i = 32;
      RandomSource randomsource = RandomSource.create();
      Block block = this.getMarkerParticleTarget();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < 667; ++j) {
         this.doAnimateTick(pPosX, pPosY, pPosZ, 16, randomsource, block, blockpos$mutableblockpos);
         this.doAnimateTick(pPosX, pPosY, pPosZ, 32, randomsource, block, blockpos$mutableblockpos);
      }

   }

   @Nullable
   private Block getMarkerParticleTarget() {
      if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
         ItemStack itemstack = this.minecraft.player.getMainHandItem();
         Item item = itemstack.getItem();
         if (MARKER_PARTICLE_ITEMS.contains(item) && item instanceof BlockItem) {
            BlockItem blockitem = (BlockItem)item;
            return blockitem.getBlock();
         }
      }

      return null;
   }

   public void doAnimateTick(int pPosX, int pPosY, int pPosZ, int pRange, RandomSource pRandom, @Nullable Block pBlock, BlockPos.MutableBlockPos pBlockPos) {
      int i = pPosX + this.random.nextInt(pRange) - this.random.nextInt(pRange);
      int j = pPosY + this.random.nextInt(pRange) - this.random.nextInt(pRange);
      int k = pPosZ + this.random.nextInt(pRange) - this.random.nextInt(pRange);
      pBlockPos.set(i, j, k);
      BlockState blockstate = this.getBlockState(pBlockPos);
      blockstate.getBlock().animateTick(blockstate, this, pBlockPos, pRandom);
      FluidState fluidstate = this.getFluidState(pBlockPos);
      if (!fluidstate.isEmpty()) {
         fluidstate.animateTick(this, pBlockPos, pRandom);
         ParticleOptions particleoptions = fluidstate.getDripParticle();
         if (particleoptions != null && this.random.nextInt(10) == 0) {
            boolean flag = blockstate.isFaceSturdy(this, pBlockPos, Direction.DOWN);
            BlockPos blockpos = pBlockPos.below();
            this.trySpawnDripParticles(blockpos, this.getBlockState(blockpos), particleoptions, flag);
         }
      }

      if (pBlock == blockstate.getBlock()) {
         this.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, blockstate), (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 0.0D, 0.0D, 0.0D);
      }

      if (!blockstate.isCollisionShapeFullBlock(this, pBlockPos)) {
         this.getBiome(pBlockPos).value().getAmbientParticle().ifPresent((p_264703_) -> {
            if (p_264703_.canSpawn(this.random)) {
               this.addParticle(p_264703_.getOptions(), (double)pBlockPos.getX() + this.random.nextDouble(), (double)pBlockPos.getY() + this.random.nextDouble(), (double)pBlockPos.getZ() + this.random.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

         });
      }

   }

   private void trySpawnDripParticles(BlockPos pBlockPos, BlockState pBlockState, ParticleOptions pParticleData, boolean pShapeDownSolid) {
      if (pBlockState.getFluidState().isEmpty()) {
         VoxelShape voxelshape = pBlockState.getCollisionShape(this, pBlockPos);
         double d0 = voxelshape.max(Direction.Axis.Y);
         if (d0 < 1.0D) {
            if (pShapeDownSolid) {
               this.spawnFluidParticle((double)pBlockPos.getX(), (double)(pBlockPos.getX() + 1), (double)pBlockPos.getZ(), (double)(pBlockPos.getZ() + 1), (double)(pBlockPos.getY() + 1) - 0.05D, pParticleData);
            }
         } else if (!pBlockState.is(BlockTags.IMPERMEABLE)) {
            double d1 = voxelshape.min(Direction.Axis.Y);
            if (d1 > 0.0D) {
               this.spawnParticle(pBlockPos, pParticleData, voxelshape, (double)pBlockPos.getY() + d1 - 0.05D);
            } else {
               BlockPos blockpos = pBlockPos.below();
               BlockState blockstate = this.getBlockState(blockpos);
               VoxelShape voxelshape1 = blockstate.getCollisionShape(this, blockpos);
               double d2 = voxelshape1.max(Direction.Axis.Y);
               if (d2 < 1.0D && blockstate.getFluidState().isEmpty()) {
                  this.spawnParticle(pBlockPos, pParticleData, voxelshape, (double)pBlockPos.getY() - 0.05D);
               }
            }
         }

      }
   }

   private void spawnParticle(BlockPos pPos, ParticleOptions pParticleData, VoxelShape pVoxelShape, double pY) {
      this.spawnFluidParticle((double)pPos.getX() + pVoxelShape.min(Direction.Axis.X), (double)pPos.getX() + pVoxelShape.max(Direction.Axis.X), (double)pPos.getZ() + pVoxelShape.min(Direction.Axis.Z), (double)pPos.getZ() + pVoxelShape.max(Direction.Axis.Z), pY, pParticleData);
   }

   private void spawnFluidParticle(double pXStart, double pXEnd, double pZStart, double pZEnd, double pY, ParticleOptions pParticleData) {
      this.addParticle(pParticleData, Mth.lerp(this.random.nextDouble(), pXStart, pXEnd), pY, Mth.lerp(this.random.nextDouble(), pZStart, pZEnd), 0.0D, 0.0D, 0.0D);
   }

   /**
    * Adds some basic stats of the world to the given crash report.
    */
   public CrashReportCategory fillReportDetails(CrashReport pReport) {
      CrashReportCategory crashreportcategory = super.fillReportDetails(pReport);
      crashreportcategory.setDetail("Server brand", () -> {
         return this.minecraft.player.getServerBrand();
      });
      crashreportcategory.setDetail("Server type", () -> {
         return this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
      });
      return crashreportcategory;
   }

   public void playSeededSound(@Nullable Player pPlayer, double pX, double pY, double pZ, Holder<SoundEvent> pSound, SoundSource pSource, float pVolume, float pPitch, long pSeed) {
      net.minecraftforge.event.PlayLevelSoundEvent.AtPosition event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtPosition(this, pX, pY, pZ, pSound, pSource, pVolume, pPitch);
      if (event.isCanceled() || event.getSound() == null) return;
      pSound = event.getSound();
      pSource = event.getSource();
      pVolume = event.getNewVolume();
      pPitch = event.getNewPitch();
      if (pPlayer == this.minecraft.player) {
         this.playSound(pX, pY, pZ, pSound.value(), pSource, pVolume, pPitch, false, pSeed);
      }

   }

   public void playSeededSound(@Nullable Player pPlayer, Entity pEntity, Holder<SoundEvent> pSound, SoundSource pCategory, float pVolume, float pPitch, long pSeed) {
      net.minecraftforge.event.PlayLevelSoundEvent.AtEntity event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pEntity, pSound, pCategory, pVolume, pPitch);
      if (event.isCanceled() || event.getSound() == null) return;
      pSound = event.getSound();
      pCategory = event.getSource();
      pVolume = event.getNewVolume();
      pPitch = event.getNewPitch();
      if (pPlayer == this.minecraft.player) {
         this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(pSound.value(), pCategory, pVolume, pPitch, pEntity, pSeed));
      }

   }

   public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
      this.playSound(pX, pY, pZ, pSound, pCategory, pVolume, pPitch, pDistanceDelay, this.random.nextLong());
   }

   private void playSound(double pX, double pY, double pZ, SoundEvent pSoundEvent, SoundSource pSource, float pVolume, float pPitch, boolean pDistanceDelay, long pSeed) {
      double d0 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(pX, pY, pZ);
      SimpleSoundInstance simplesoundinstance = new SimpleSoundInstance(pSoundEvent, pSource, pVolume, pPitch, RandomSource.create(pSeed), pX, pY, pZ);
      if (pDistanceDelay && d0 > 100.0D) {
         double d1 = Math.sqrt(d0) / 40.0D;
         this.minecraft.getSoundManager().playDelayed(simplesoundinstance, (int)(d1 * 20.0D));
      } else {
         this.minecraft.getSoundManager().play(simplesoundinstance);
      }

   }

   public void createFireworks(double pX, double pY, double pZ, double pMotionX, double pMotionY, double pMotionZ, @Nullable CompoundTag pCompound) {
      this.minecraft.particleEngine.add(new FireworkParticles.Starter(this, pX, pY, pZ, pMotionX, pMotionY, pMotionZ, this.minecraft.particleEngine, pCompound));
   }

   public void sendPacketToServer(Packet<?> pPacket) {
      this.connection.send(pPacket);
   }

   public RecipeManager getRecipeManager() {
      return this.connection.getRecipeManager();
   }

   public void setScoreboard(Scoreboard pScoreboard) {
      this.scoreboard = pScoreboard;
   }

   public LevelTickAccess<Block> getBlockTicks() {
      return BlackholeTickAccess.emptyLevelList();
   }

   public LevelTickAccess<Fluid> getFluidTicks() {
      return BlackholeTickAccess.emptyLevelList();
   }

   /**
    * Gets the world's chunk provider
    */
   public ClientChunkCache getChunkSource() {
      return this.chunkSource;
   }

   @Nullable
   public MapItemSavedData getMapData(String pMapName) {
      return this.mapData.get(pMapName);
   }

   public void overrideMapData(String pMapName, MapItemSavedData pData) {
      this.mapData.put(pMapName, pData);
   }

   public void setMapData(String pMapName, MapItemSavedData pData) {
   }

   public int getFreeMapId() {
      return 0;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   /**
    * Flags are as in setBlockState
    */
   public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
      this.levelRenderer.blockChanged(this, pPos, pOldState, pNewState, pFlags);
   }

   public void setBlocksDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState) {
      this.levelRenderer.setBlockDirty(pBlockPos, pOldState, pNewState);
   }

   public void setSectionDirtyWithNeighbors(int pSectionX, int pSectionY, int pSectionZ) {
      this.levelRenderer.setSectionDirtyWithNeighbors(pSectionX, pSectionY, pSectionZ);
   }

   public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
      this.levelRenderer.destroyBlockProgress(pBreakerId, pPos, pProgress);
   }

   public void globalLevelEvent(int pId, BlockPos pPos, int pData) {
      this.levelRenderer.globalLevelEvent(pId, pPos, pData);
   }

   public void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData) {
      try {
         this.levelRenderer.levelEvent(pType, pPos, pData);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Playing level event");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Level event being played");
         crashreportcategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(this, pPos));
         crashreportcategory.setDetail("Event source", pPlayer);
         crashreportcategory.setDetail("Event type", pType);
         crashreportcategory.setDetail("Event data", pData);
         throw new ReportedException(crashreport);
      }
   }

   public void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter(), pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void addParticle(ParticleOptions pParticleData, boolean pForceAlwaysRender, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter() || pForceAlwaysRender, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void addAlwaysVisibleParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, false, true, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void addAlwaysVisibleParticle(ParticleOptions pParticleData, boolean pIgnoreRange, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter() || pIgnoreRange, true, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public List<AbstractClientPlayer> players() {
      return this.players;
   }

   public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
      return this.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
   }

   public float getSkyDarken(float pPartialTick) {
      float f = this.getTimeOfDay(pPartialTick);
      float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
      f1 = Mth.clamp(f1, 0.0F, 1.0F);
      f1 = 1.0F - f1;
      f1 *= 1.0F - this.getRainLevel(pPartialTick) * 5.0F / 16.0F;
      f1 *= 1.0F - this.getThunderLevel(pPartialTick) * 5.0F / 16.0F;
      return f1 * 0.8F + 0.2F;
   }

   public Vec3 getSkyColor(Vec3 pPos, float pPartialTick) {
      float f = this.getTimeOfDay(pPartialTick);
      Vec3 vec3 = pPos.subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
      BiomeManager biomemanager = this.getBiomeManager();
      Vec3 vec31 = CubicSampler.gaussianSampleVec3(vec3, (p_194161_, p_194162_, p_194163_) -> {
         return Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(p_194161_, p_194162_, p_194163_).value().getSkyColor());
      });
      float f1 = Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f1 = Mth.clamp(f1, 0.0F, 1.0F);
      float f2 = (float)vec31.x * f1;
      float f3 = (float)vec31.y * f1;
      float f4 = (float)vec31.z * f1;
      float f5 = this.getRainLevel(pPartialTick);
      if (f5 > 0.0F) {
         float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
         float f7 = 1.0F - f5 * 0.75F;
         f2 = f2 * f7 + f6 * (1.0F - f7);
         f3 = f3 * f7 + f6 * (1.0F - f7);
         f4 = f4 * f7 + f6 * (1.0F - f7);
      }

      float f9 = this.getThunderLevel(pPartialTick);
      if (f9 > 0.0F) {
         float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
         float f8 = 1.0F - f9 * 0.75F;
         f2 = f2 * f8 + f10 * (1.0F - f8);
         f3 = f3 * f8 + f10 * (1.0F - f8);
         f4 = f4 * f8 + f10 * (1.0F - f8);
      }

      int i = this.getSkyFlashTime();
      if (i > 0) {
         float f11 = (float)i - pPartialTick;
         if (f11 > 1.0F) {
            f11 = 1.0F;
         }

         f11 *= 0.45F;
         f2 = f2 * (1.0F - f11) + 0.8F * f11;
         f3 = f3 * (1.0F - f11) + 0.8F * f11;
         f4 = f4 * (1.0F - f11) + 1.0F * f11;
      }

      return new Vec3((double)f2, (double)f3, (double)f4);
   }

   public Vec3 getCloudColor(float pPartialTick) {
      float f = this.getTimeOfDay(pPartialTick);
      float f1 = Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f1 = Mth.clamp(f1, 0.0F, 1.0F);
      float f2 = 1.0F;
      float f3 = 1.0F;
      float f4 = 1.0F;
      float f5 = this.getRainLevel(pPartialTick);
      if (f5 > 0.0F) {
         float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
         float f7 = 1.0F - f5 * 0.95F;
         f2 = f2 * f7 + f6 * (1.0F - f7);
         f3 = f3 * f7 + f6 * (1.0F - f7);
         f4 = f4 * f7 + f6 * (1.0F - f7);
      }

      f2 *= f1 * 0.9F + 0.1F;
      f3 *= f1 * 0.9F + 0.1F;
      f4 *= f1 * 0.85F + 0.15F;
      float f9 = this.getThunderLevel(pPartialTick);
      if (f9 > 0.0F) {
         float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
         float f8 = 1.0F - f9 * 0.95F;
         f2 = f2 * f8 + f10 * (1.0F - f8);
         f3 = f3 * f8 + f10 * (1.0F - f8);
         f4 = f4 * f8 + f10 * (1.0F - f8);
      }

      return new Vec3((double)f2, (double)f3, (double)f4);
   }

   public float getStarBrightness(float pPartialTick) {
      float f = this.getTimeOfDay(pPartialTick);
      float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
      f1 = Mth.clamp(f1, 0.0F, 1.0F);
      return f1 * f1 * 0.5F;
   }

   public int getSkyFlashTime() {
      return this.minecraft.options.hideLightningFlash().get() ? 0 : this.skyFlashTime;
   }

   public void setSkyFlashTime(int pTimeFlash) {
      this.skyFlashTime = pTimeFlash;
   }

   public float getShade(Direction pDirection, boolean pShade) {
      boolean flag = this.effects().constantAmbientLight();
      if (!pShade) {
         return flag ? 0.9F : 1.0F;
      } else {
         switch (pDirection) {
            case DOWN:
               return flag ? 0.9F : 0.5F;
            case UP:
               return flag ? 0.9F : 1.0F;
            case NORTH:
            case SOUTH:
               return 0.8F;
            case WEST:
            case EAST:
               return 0.6F;
            default:
               return 1.0F;
         }
      }
   }

   public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
      BlockTintCache blocktintcache = this.tintCaches.get(pColorResolver);
      return blocktintcache.getColor(pBlockPos);
   }

   public int calculateBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
      int i = Minecraft.getInstance().options.biomeBlendRadius().get();
      if (i == 0) {
         return pColorResolver.getColor(this.getBiome(pBlockPos).value(), (double)pBlockPos.getX(), (double)pBlockPos.getZ());
      } else {
         int j = (i * 2 + 1) * (i * 2 + 1);
         int k = 0;
         int l = 0;
         int i1 = 0;
         Cursor3D cursor3d = new Cursor3D(pBlockPos.getX() - i, pBlockPos.getY(), pBlockPos.getZ() - i, pBlockPos.getX() + i, pBlockPos.getY(), pBlockPos.getZ() + i);

         int j1;
         for(BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(); cursor3d.advance(); i1 += j1 & 255) {
            blockpos$mutableblockpos.set(cursor3d.nextX(), cursor3d.nextY(), cursor3d.nextZ());
            j1 = pColorResolver.getColor(this.getBiome(blockpos$mutableblockpos).value(), (double)blockpos$mutableblockpos.getX(), (double)blockpos$mutableblockpos.getZ());
            k += (j1 & 16711680) >> 16;
            l += (j1 & '\uff00') >> 8;
         }

         return (k / j & 255) << 16 | (l / j & 255) << 8 | i1 / j & 255;
      }
   }

   public void setDefaultSpawnPos(BlockPos pSpawnPos, float pSpawnAngle) {
      this.levelData.setSpawn(pSpawnPos, pSpawnAngle);
   }

   public String toString() {
      return "ClientLevel";
   }

   /**
    * Returns the world's WorldInfo object
    */
   public ClientLevel.ClientLevelData getLevelData() {
      return this.clientLevelData;
   }

   public void gameEvent(GameEvent pEvent, Vec3 pPosition, GameEvent.Context pContext) {
   }

   protected Map<String, MapItemSavedData> getAllMapData() {
      return ImmutableMap.copyOf(this.mapData);
   }

   protected void addMapData(Map<String, MapItemSavedData> pMap) {
      this.mapData.putAll(pMap);
   }

   protected LevelEntityGetter<Entity> getEntities() {
      return this.entityStorage.getEntityGetter();
   }

   /**
    * Returns the name of the current chunk provider, by calling chunkprovider.makeString()
    */
   public String gatherChunkSourceStats() {
      return "Chunks[C] W: " + this.chunkSource.gatherStats() + " E: " + this.entityStorage.gatherStats();
   }

   public void addDestroyBlockEffect(BlockPos pPos, BlockState pState) {
      this.minecraft.particleEngine.destroy(pPos, pState);
   }

   public void setServerSimulationDistance(int pServerSimulationDistance) {
      this.serverSimulationDistance = pServerSimulationDistance;
   }

   public int getServerSimulationDistance() {
      return this.serverSimulationDistance;
   }

   public FeatureFlagSet enabledFeatures() {
      return this.connection.enabledFeatures();
   }

   @OnlyIn(Dist.CLIENT)
   public static class ClientLevelData implements WritableLevelData {
      private final boolean hardcore;
      private final GameRules gameRules;
      private final boolean isFlat;
      private int xSpawn;
      private int ySpawn;
      private int zSpawn;
      private float spawnAngle;
      private long gameTime;
      private long dayTime;
      private boolean raining;
      private Difficulty difficulty;
      private boolean difficultyLocked;

      public ClientLevelData(Difficulty pDifficulty, boolean pHardcore, boolean pIsFlat) {
         this.difficulty = pDifficulty;
         this.hardcore = pHardcore;
         this.isFlat = pIsFlat;
         this.gameRules = new GameRules();
      }

      /**
       * Returns the x spawn position
       */
      public int getXSpawn() {
         return this.xSpawn;
      }

      /**
       * Return the Y axis spawning point of the player.
       */
      public int getYSpawn() {
         return this.ySpawn;
      }

      /**
       * Returns the z spawn position
       */
      public int getZSpawn() {
         return this.zSpawn;
      }

      public float getSpawnAngle() {
         return this.spawnAngle;
      }

      public long getGameTime() {
         return this.gameTime;
      }

      /**
       * Get current world time
       */
      public long getDayTime() {
         return this.dayTime;
      }

      /**
       * Set the x spawn position to the passed in value
       */
      public void setXSpawn(int pX) {
         this.xSpawn = pX;
      }

      /**
       * Sets the y spawn position
       */
      public void setYSpawn(int pY) {
         this.ySpawn = pY;
      }

      /**
       * Set the z spawn position to the passed in value
       */
      public void setZSpawn(int pZ) {
         this.zSpawn = pZ;
      }

      public void setSpawnAngle(float pAngle) {
         this.spawnAngle = pAngle;
      }

      public void setGameTime(long pGameTime) {
         this.gameTime = pGameTime;
      }

      public void setDayTime(long pDayTime) {
         this.dayTime = pDayTime;
      }

      public void setSpawn(BlockPos pSpawnPoint, float pAngle) {
         this.xSpawn = pSpawnPoint.getX();
         this.ySpawn = pSpawnPoint.getY();
         this.zSpawn = pSpawnPoint.getZ();
         this.spawnAngle = pAngle;
      }

      /**
       * Returns {@code true} if it is thundering, {@code false} otherwise.
       */
      public boolean isThundering() {
         return false;
      }

      /**
       * Returns {@code true} if it is raining, {@code false} otherwise.
       */
      public boolean isRaining() {
         return this.raining;
      }

      /**
       * Sets whether it is raining or not.
       */
      public void setRaining(boolean pIsRaining) {
         this.raining = pIsRaining;
      }

      /**
       * Returns {@code true} if hardcore mode is enabled, otherwise {@code false}.
       */
      public boolean isHardcore() {
         return this.hardcore;
      }

      /**
       * Gets the GameRules class Instance.
       */
      public GameRules getGameRules() {
         return this.gameRules;
      }

      public Difficulty getDifficulty() {
         return this.difficulty;
      }

      public boolean isDifficultyLocked() {
         return this.difficultyLocked;
      }

      public void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
         WritableLevelData.super.fillCrashReportCategory(pCrashReportCategory, pLevel);
      }

      public void setDifficulty(Difficulty pDifficulty) {
         net.minecraftforge.common.ForgeHooks.onDifficultyChange(pDifficulty, this.difficulty);
         this.difficulty = pDifficulty;
      }

      public void setDifficultyLocked(boolean pDifficultyLocked) {
         this.difficultyLocked = pDifficultyLocked;
      }

      public double getHorizonHeight(LevelHeightAccessor pLevel) {
         return this.isFlat ? (double)pLevel.getMinBuildHeight() : 63.0D;
      }

      public float getClearColorScale() {
         return this.isFlat ? 1.0F : 0.03125F;
      }
   }

   @OnlyIn(Dist.CLIENT)
   final class EntityCallbacks implements LevelCallback<Entity> {
      public void onCreated(Entity p_171696_) {
      }

      public void onDestroyed(Entity p_171700_) {
      }

      public void onTickingStart(Entity p_171704_) {
         ClientLevel.this.tickingEntities.add(p_171704_);
      }

      public void onTickingEnd(Entity p_171708_) {
         ClientLevel.this.tickingEntities.remove(p_171708_);
      }

      public void onTrackingStart(Entity p_171712_) {
         if (p_171712_ instanceof AbstractClientPlayer) {
            ClientLevel.this.players.add((AbstractClientPlayer)p_171712_);
         }

         if (p_171712_.isMultipartEntity()) {
            for (net.minecraftforge.entity.PartEntity<?> part : p_171712_.getParts()) {
               ClientLevel.this.partEntities.put(part.getId(), part);
            }
         }
      }

      public void onTrackingEnd(Entity p_171716_) {
         p_171716_.unRide();
         ClientLevel.this.players.remove(p_171716_);

         p_171716_.onRemovedFromWorld();
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityLeaveLevelEvent(p_171716_, ClientLevel.this));

         if (p_171716_.isMultipartEntity()) {
            for (net.minecraftforge.entity.PartEntity<?> part : p_171716_.getParts()) {
               ClientLevel.this.partEntities.remove(part.getId());
            }
         }
      }

      public void onSectionChange(Entity p_233660_) {
      }
   }

   @Override
   public java.util.Collection<net.minecraftforge.entity.PartEntity<?>> getPartEntities() {
      return this.partEntities.values();
   }

   @Override
   public net.minecraftforge.client.model.data.ModelDataManager getModelDataManager() {
      return modelDataManager;
   }

   @Override
   public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
      boolean constantAmbientLight = this.effects().constantAmbientLight();
      if (!shade)
         return constantAmbientLight ? 0.9F : 1.0F;
      return net.minecraftforge.client.model.lighting.QuadLighter.calculateShade(normalX, normalY, normalZ, constantAmbientLight);
   }
}
