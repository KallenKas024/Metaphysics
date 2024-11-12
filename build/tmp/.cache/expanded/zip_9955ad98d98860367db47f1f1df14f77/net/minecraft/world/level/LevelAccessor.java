package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
   default long dayTime() {
      return this.getLevelData().getDayTime();
   }

   long nextSubTickCount();

   LevelTickAccess<Block> getBlockTicks();

   private <T> ScheduledTick<T> createTick(BlockPos pPos, T pType, int pDelay, TickPriority pPriority) {
      return new ScheduledTick<>(pType, pPos, this.getLevelData().getGameTime() + (long)pDelay, pPriority, this.nextSubTickCount());
   }

   private <T> ScheduledTick<T> createTick(BlockPos pPos, T pType, int pDelay) {
      return new ScheduledTick<>(pType, pPos, this.getLevelData().getGameTime() + (long)pDelay, this.nextSubTickCount());
   }

   default void scheduleTick(BlockPos pPos, Block pBlock, int pDelay, TickPriority pPriority) {
      this.getBlockTicks().schedule(this.createTick(pPos, pBlock, pDelay, pPriority));
   }

   default void scheduleTick(BlockPos pPos, Block pBlock, int pDelay) {
      this.getBlockTicks().schedule(this.createTick(pPos, pBlock, pDelay));
   }

   LevelTickAccess<Fluid> getFluidTicks();

   default void scheduleTick(BlockPos pPos, Fluid pFluid, int pDelay, TickPriority pPriority) {
      this.getFluidTicks().schedule(this.createTick(pPos, pFluid, pDelay, pPriority));
   }

   default void scheduleTick(BlockPos pPos, Fluid pFluid, int pDelay) {
      this.getFluidTicks().schedule(this.createTick(pPos, pFluid, pDelay));
   }

   /**
    * Returns the world's WorldInfo object
    */
   LevelData getLevelData();

   DifficultyInstance getCurrentDifficultyAt(BlockPos pPos);

   @Nullable
   MinecraftServer getServer();

   default Difficulty getDifficulty() {
      return this.getLevelData().getDifficulty();
   }

   /**
    * Gets the world's chunk provider
    */
   ChunkSource getChunkSource();

   default boolean hasChunk(int pChunkX, int pChunkZ) {
      return this.getChunkSource().hasChunk(pChunkX, pChunkZ);
   }

   RandomSource getRandom();

   default void blockUpdated(BlockPos pPos, Block pBlock) {
   }

   /**
    * 
    * @param pQueried The block state of the current block
    * @param pPos The position of the neighbor block
    * @param pOffsetPos The position of the current block
    */
   default void neighborShapeChanged(Direction pDirection, BlockState pQueried, BlockPos pPos, BlockPos pOffsetPos, int pFlags, int pRecursionLevel) {
      NeighborUpdater.executeShapeUpdate(this, pDirection, pQueried, pPos, pOffsetPos, pFlags, pRecursionLevel - 1);
   }

   default void playSound(@Nullable Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pSource) {
      this.playSound(pPlayer, pPos, pSound, pSource, 1.0F, 1.0F);
   }

   /**
    * Plays a sound. On the server, the sound is broadcast to all nearby <em>except</em> the given player. On the
    * client, the sound only plays if the given player is the client player. Thus, this method is intended to be called
    * from code running on both sides. The client plays it locally and the server plays it for everyone else.
    */
   void playSound(@Nullable Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pSource, float pVolume, float pPitch);

   void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed);

   void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData);

   default void levelEvent(int pType, BlockPos pPos, int pData) {
      this.levelEvent((Player)null, pType, pPos, pData);
   }

   void gameEvent(GameEvent pEvent, Vec3 pPosition, GameEvent.Context pContext);

   default void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, Vec3 pPosition) {
      this.gameEvent(pEvent, pPosition, new GameEvent.Context(pEntity, (BlockState)null));
   }

   default void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos) {
      this.gameEvent(pEvent, pPos, new GameEvent.Context(pEntity, (BlockState)null));
   }

   default void gameEvent(GameEvent pEvent, BlockPos pPos, GameEvent.Context pContext) {
      this.gameEvent(pEvent, Vec3.atCenterOf(pPos), pContext);
   }
}