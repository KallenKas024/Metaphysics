package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SPAWN_TIME = 200;
   private static final int COOLDOWN_TIME = 40;
   private static final int ATTENTION_INTERVAL = 2400;
   private static final int EVENT_COOLDOWN = 1;
   private static final int GATEWAY_HEIGHT_ABOVE_SURFACE = 10;
   private long age;
   private int teleportCooldown;
   @Nullable
   private BlockPos exitPortal;
   private boolean exactTeleport;

   public TheEndGatewayBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.END_GATEWAY, pPos, pBlockState);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putLong("Age", this.age);
      if (this.exitPortal != null) {
         pTag.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
      }

      if (this.exactTeleport) {
         pTag.putBoolean("ExactTeleport", true);
      }

   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.age = pTag.getLong("Age");
      if (pTag.contains("ExitPortal", 10)) {
         BlockPos blockpos = NbtUtils.readBlockPos(pTag.getCompound("ExitPortal"));
         if (Level.isInSpawnableBounds(blockpos)) {
            this.exitPortal = blockpos;
         }
      }

      this.exactTeleport = pTag.getBoolean("ExactTeleport");
   }

   public static void beamAnimationTick(Level pLevel, BlockPos pPos, BlockState pState, TheEndGatewayBlockEntity pBlockEntity) {
      ++pBlockEntity.age;
      if (pBlockEntity.isCoolingDown()) {
         --pBlockEntity.teleportCooldown;
      }

   }

   public static void teleportTick(Level pLevel, BlockPos pPos, BlockState pState, TheEndGatewayBlockEntity pBlockEntity) {
      boolean flag = pBlockEntity.isSpawning();
      boolean flag1 = pBlockEntity.isCoolingDown();
      ++pBlockEntity.age;
      if (flag1) {
         --pBlockEntity.teleportCooldown;
      } else {
         List<Entity> list = pLevel.getEntitiesOfClass(Entity.class, new AABB(pPos), TheEndGatewayBlockEntity::canEntityTeleport);
         if (!list.isEmpty()) {
            teleportEntity(pLevel, pPos, pState, list.get(pLevel.random.nextInt(list.size())), pBlockEntity);
         }

         if (pBlockEntity.age % 2400L == 0L) {
            triggerCooldown(pLevel, pPos, pState, pBlockEntity);
         }
      }

      if (flag != pBlockEntity.isSpawning() || flag1 != pBlockEntity.isCoolingDown()) {
         setChanged(pLevel, pPos, pState);
      }

   }

   public static boolean canEntityTeleport(Entity p_59941_) {
      return EntitySelector.NO_SPECTATORS.test(p_59941_) && !p_59941_.getRootVehicle().isOnPortalCooldown();
   }

   public boolean isSpawning() {
      return this.age < 200L;
   }

   public boolean isCoolingDown() {
      return this.teleportCooldown > 0;
   }

   public float getSpawnPercent(float pPartialTicks) {
      return Mth.clamp(((float)this.age + pPartialTicks) / 200.0F, 0.0F, 1.0F);
   }

   public float getCooldownPercent(float pPartialTicks) {
      return 1.0F - Mth.clamp(((float)this.teleportCooldown - pPartialTicks) / 40.0F, 0.0F, 1.0F);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   private static void triggerCooldown(Level pLevel, BlockPos pPos, BlockState pState, TheEndGatewayBlockEntity pBlockEntity) {
      if (!pLevel.isClientSide) {
         pBlockEntity.teleportCooldown = 40;
         pLevel.blockEvent(pPos, pState.getBlock(), 1, 0);
         setChanged(pLevel, pPos, pState);
      }

   }

   public boolean triggerEvent(int pId, int pType) {
      if (pId == 1) {
         this.teleportCooldown = 40;
         return true;
      } else {
         return super.triggerEvent(pId, pType);
      }
   }

   public static void teleportEntity(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity, TheEndGatewayBlockEntity pBlockEntity) {
      if (pLevel instanceof ServerLevel serverlevel && !pBlockEntity.isCoolingDown()) {
         pBlockEntity.teleportCooldown = 100;
         if (pBlockEntity.exitPortal == null && pLevel.dimension() == Level.END) {
            BlockPos blockpos = findOrCreateValidTeleportPos(serverlevel, pPos);
            blockpos = blockpos.above(10);
            LOGGER.debug("Creating portal at {}", (Object)blockpos);
            spawnGatewayPortal(serverlevel, blockpos, EndGatewayConfiguration.knownExit(pPos, false));
            pBlockEntity.exitPortal = blockpos;
         }

         if (pBlockEntity.exitPortal != null) {
            BlockPos blockpos1 = pBlockEntity.exactTeleport ? pBlockEntity.exitPortal : findExitPosition(pLevel, pBlockEntity.exitPortal);
            Entity entity;
            if (pEntity instanceof ThrownEnderpearl) {
               Entity entity1 = ((ThrownEnderpearl)pEntity).getOwner();
               if (entity1 instanceof ServerPlayer) {
                  CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)entity1, pState);
               }

               if (entity1 != null) {
                  entity = entity1;
                  pEntity.discard();
               } else {
                  entity = pEntity;
               }
            } else {
               entity = pEntity.getRootVehicle();
            }

            entity.setPortalCooldown();
            entity.teleportToWithTicket((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D);
         }

         triggerCooldown(pLevel, pPos, pState, pBlockEntity);
      }
   }

   private static BlockPos findExitPosition(Level pLevel, BlockPos pPos) {
      BlockPos blockpos = findTallestBlock(pLevel, pPos.offset(0, 2, 0), 5, false);
      LOGGER.debug("Best exit position for portal at {} is {}", pPos, blockpos);
      return blockpos.above();
   }

   private static BlockPos findOrCreateValidTeleportPos(ServerLevel pLevel, BlockPos pPos) {
      Vec3 vec3 = findExitPortalXZPosTentative(pLevel, pPos);
      LevelChunk levelchunk = getChunk(pLevel, vec3);
      BlockPos blockpos = findValidSpawnInChunk(levelchunk);
      if (blockpos == null) {
         BlockPos blockpos1 = BlockPos.containing(vec3.x + 0.5D, 75.0D, vec3.z + 0.5D);
         LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", (Object)blockpos1);
         pLevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((p_258975_) -> {
            return p_258975_.getHolder(EndFeatures.END_ISLAND);
         }).ifPresent((p_256040_) -> {
            p_256040_.value().place(pLevel, pLevel.getChunkSource().getGenerator(), RandomSource.create(blockpos1.asLong()), blockpos1);
         });
         blockpos = blockpos1;
      } else {
         LOGGER.debug("Found suitable block to teleport to: {}", (Object)blockpos);
      }

      return findTallestBlock(pLevel, blockpos, 16, true);
   }

   private static Vec3 findExitPortalXZPosTentative(ServerLevel pLevel, BlockPos pPos) {
      Vec3 vec3 = (new Vec3((double)pPos.getX(), 0.0D, (double)pPos.getZ())).normalize();
      int i = 1024;
      Vec3 vec31 = vec3.scale(1024.0D);

      for(int j = 16; !isChunkEmpty(pLevel, vec31) && j-- > 0; vec31 = vec31.add(vec3.scale(-16.0D))) {
         LOGGER.debug("Skipping backwards past nonempty chunk at {}", (Object)vec31);
      }

      for(int k = 16; isChunkEmpty(pLevel, vec31) && k-- > 0; vec31 = vec31.add(vec3.scale(16.0D))) {
         LOGGER.debug("Skipping forward past empty chunk at {}", (Object)vec31);
      }

      LOGGER.debug("Found chunk at {}", (Object)vec31);
      return vec31;
   }

   private static boolean isChunkEmpty(ServerLevel pLevel, Vec3 pPos) {
      return getChunk(pLevel, pPos).getHighestFilledSectionIndex() == -1;
   }

   private static BlockPos findTallestBlock(BlockGetter pLevel, BlockPos pPos, int pRadius, boolean pAllowBedrock) {
      BlockPos blockpos = null;

      for(int i = -pRadius; i <= pRadius; ++i) {
         for(int j = -pRadius; j <= pRadius; ++j) {
            if (i != 0 || j != 0 || pAllowBedrock) {
               for(int k = pLevel.getMaxBuildHeight() - 1; k > (blockpos == null ? pLevel.getMinBuildHeight() : blockpos.getY()); --k) {
                  BlockPos blockpos1 = new BlockPos(pPos.getX() + i, k, pPos.getZ() + j);
                  BlockState blockstate = pLevel.getBlockState(blockpos1);
                  if (blockstate.isCollisionShapeFullBlock(pLevel, blockpos1) && (pAllowBedrock || !blockstate.is(Blocks.BEDROCK))) {
                     blockpos = blockpos1;
                     break;
                  }
               }
            }
         }
      }

      return blockpos == null ? pPos : blockpos;
   }

   private static LevelChunk getChunk(Level pLevel, Vec3 pPos) {
      return pLevel.getChunk(Mth.floor(pPos.x / 16.0D), Mth.floor(pPos.z / 16.0D));
   }

   @Nullable
   private static BlockPos findValidSpawnInChunk(LevelChunk pChunk) {
      ChunkPos chunkpos = pChunk.getPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 30, chunkpos.getMinBlockZ());
      int i = pChunk.getHighestSectionPosition() + 16 - 1;
      BlockPos blockpos1 = new BlockPos(chunkpos.getMaxBlockX(), i, chunkpos.getMaxBlockZ());
      BlockPos blockpos2 = null;
      double d0 = 0.0D;

      for(BlockPos blockpos3 : BlockPos.betweenClosed(blockpos, blockpos1)) {
         BlockState blockstate = pChunk.getBlockState(blockpos3);
         BlockPos blockpos4 = blockpos3.above();
         BlockPos blockpos5 = blockpos3.above(2);
         if (blockstate.is(Blocks.END_STONE) && !pChunk.getBlockState(blockpos4).isCollisionShapeFullBlock(pChunk, blockpos4) && !pChunk.getBlockState(blockpos5).isCollisionShapeFullBlock(pChunk, blockpos5)) {
            double d1 = blockpos3.distToCenterSqr(0.0D, 0.0D, 0.0D);
            if (blockpos2 == null || d1 < d0) {
               blockpos2 = blockpos3;
               d0 = d1;
            }
         }
      }

      return blockpos2;
   }

   private static void spawnGatewayPortal(ServerLevel pLevel, BlockPos pPos, EndGatewayConfiguration pConfig) {
      Feature.END_GATEWAY.place(pConfig, pLevel, pLevel.getChunkSource().getGenerator(), RandomSource.create(), pPos);
   }

   public boolean shouldRenderFace(Direction pFace) {
      return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), pFace, this.getBlockPos().relative(pFace));
   }

   public int getParticleAmount() {
      int i = 0;

      for(Direction direction : Direction.values()) {
         i += this.shouldRenderFace(direction) ? 1 : 0;
      }

      return i;
   }

   public void setExitPosition(BlockPos pExitPortal, boolean pExactTeleport) {
      this.exactTeleport = pExactTeleport;
      this.exitPortal = pExitPortal;
   }
}