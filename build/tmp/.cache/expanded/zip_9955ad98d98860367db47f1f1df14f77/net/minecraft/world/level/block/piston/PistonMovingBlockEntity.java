package net.minecraft.world.level.block.piston;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonMovingBlockEntity extends BlockEntity {
   private static final int TICKS_TO_EXTEND = 2;
   private static final double PUSH_OFFSET = 0.01D;
   public static final double TICK_MOVEMENT = 0.51D;
   private BlockState movedState = Blocks.AIR.defaultBlockState();
   private Direction direction;
   /** Whether this piston is extending. */
   private boolean extending;
   private boolean isSourcePiston;
   private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> {
      return null;
   });
   private float progress;
   /** The extension / retraction progress */
   private float progressO;
   private long lastTicked;
   private int deathTicks;

   public PistonMovingBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.PISTON, pPos, pBlockState);
   }

   public PistonMovingBlockEntity(BlockPos pPos, BlockState pBlockState, BlockState pMovedState, Direction pDirection, boolean pExtending, boolean pIsSourcePiston) {
      this(pPos, pBlockState);
      this.movedState = pMovedState;
      this.direction = pDirection;
      this.extending = pExtending;
      this.isSourcePiston = pIsSourcePiston;
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   /**
    * @return whether this piston is extending
    */
   public boolean isExtending() {
      return this.extending;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public boolean isSourcePiston() {
      return this.isSourcePiston;
   }

   /**
    * @return interpolated progress value (between lastProgress and progress) given the partialTicks
    */
   public float getProgress(float pPartialTicks) {
      if (pPartialTicks > 1.0F) {
         pPartialTicks = 1.0F;
      }

      return Mth.lerp(pPartialTicks, this.progressO, this.progress);
   }

   public float getXOff(float pPartialTicks) {
      return (float)this.direction.getStepX() * this.getExtendedProgress(this.getProgress(pPartialTicks));
   }

   public float getYOff(float pPartialTicks) {
      return (float)this.direction.getStepY() * this.getExtendedProgress(this.getProgress(pPartialTicks));
   }

   public float getZOff(float pPartialTicks) {
      return (float)this.direction.getStepZ() * this.getExtendedProgress(this.getProgress(pPartialTicks));
   }

   private float getExtendedProgress(float pProgress) {
      return this.extending ? pProgress - 1.0F : 1.0F - pProgress;
   }

   private BlockState getCollisionRelatedBlockState() {
      return !this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof PistonBaseBlock ? Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.progress > 0.25F)).setValue(PistonHeadBlock.TYPE, this.movedState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT).setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING)) : this.movedState;
   }

   private static void moveCollidedEntities(Level pLevel, BlockPos pPos, float pPartialTick, PistonMovingBlockEntity pPiston) {
      Direction direction = pPiston.getMovementDirection();
      double d0 = (double)(pPartialTick - pPiston.progress);
      VoxelShape voxelshape = pPiston.getCollisionRelatedBlockState().getCollisionShape(pLevel, pPos);
      if (!voxelshape.isEmpty()) {
         AABB aabb = moveByPositionAndProgress(pPos, voxelshape.bounds(), pPiston);
         List<Entity> list = pLevel.getEntities((Entity)null, PistonMath.getMovementArea(aabb, direction, d0).minmax(aabb));
         if (!list.isEmpty()) {
            List<AABB> list1 = voxelshape.toAabbs();
            boolean flag = pPiston.movedState.isSlimeBlock(); //TODO: is this patch really needed the logic of the original seems sound revisit later
            Iterator iterator = list.iterator();

            while(true) {
               Entity entity;
               while(true) {
                  if (!iterator.hasNext()) {
                     return;
                  }

                  entity = (Entity)iterator.next();
                  if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                     if (!flag) {
                        break;
                     }

                     if (!(entity instanceof ServerPlayer)) {
                        Vec3 vec3 = entity.getDeltaMovement();
                        double d1 = vec3.x;
                        double d2 = vec3.y;
                        double d3 = vec3.z;
                        switch (direction.getAxis()) {
                           case X:
                              d1 = (double)direction.getStepX();
                              break;
                           case Y:
                              d2 = (double)direction.getStepY();
                              break;
                           case Z:
                              d3 = (double)direction.getStepZ();
                        }

                        entity.setDeltaMovement(d1, d2, d3);
                        break;
                     }
                  }
               }

               double d4 = 0.0D;

               for(AABB aabb2 : list1) {
                  AABB aabb1 = PistonMath.getMovementArea(moveByPositionAndProgress(pPos, aabb2, pPiston), direction, d0);
                  AABB aabb3 = entity.getBoundingBox();
                  if (aabb1.intersects(aabb3)) {
                     d4 = Math.max(d4, getMovement(aabb1, direction, aabb3));
                     if (d4 >= d0) {
                        break;
                     }
                  }
               }

               if (!(d4 <= 0.0D)) {
                  d4 = Math.min(d4, d0) + 0.01D;
                  moveEntityByPiston(direction, entity, d4, direction);
                  if (!pPiston.extending && pPiston.isSourcePiston) {
                     fixEntityWithinPistonBase(pPos, entity, direction, d0);
                  }
               }
            }
         }
      }
   }

   private static void moveEntityByPiston(Direction pNoClipDirection, Entity pEntity, double pProgress, Direction pDirection) {
      NOCLIP.set(pNoClipDirection);
      pEntity.move(MoverType.PISTON, new Vec3(pProgress * (double)pDirection.getStepX(), pProgress * (double)pDirection.getStepY(), pProgress * (double)pDirection.getStepZ()));
      NOCLIP.set((Direction)null);
   }

   private static void moveStuckEntities(Level pLevel, BlockPos pPos, float pPartialTick, PistonMovingBlockEntity pPiston) {
      if (pPiston.isStickyForEntities()) {
         Direction direction = pPiston.getMovementDirection();
         if (direction.getAxis().isHorizontal()) {
            double d0 = pPiston.movedState.getCollisionShape(pLevel, pPos).max(Direction.Axis.Y);
            AABB aabb = moveByPositionAndProgress(pPos, new AABB(0.0D, d0, 0.0D, 1.0D, 1.5000010000000001D, 1.0D), pPiston);
            double d1 = (double)(pPartialTick - pPiston.progress);

            for(Entity entity : pLevel.getEntities((Entity)null, aabb, (p_287552_) -> {
               return matchesStickyCritera(aabb, p_287552_, pPos);
            })) {
               moveEntityByPiston(direction, entity, d1, direction);
            }

         }
      }
   }

   private static boolean matchesStickyCritera(AABB pBox, Entity pEntity, BlockPos pPos) {
      return pEntity.getPistonPushReaction() == PushReaction.NORMAL && pEntity.onGround() && (pEntity.isSupportedBy(pPos) || pEntity.getX() >= pBox.minX && pEntity.getX() <= pBox.maxX && pEntity.getZ() >= pBox.minZ && pEntity.getZ() <= pBox.maxZ);
   }

   private boolean isStickyForEntities() {
      return this.movedState.is(Blocks.HONEY_BLOCK);
   }

   public Direction getMovementDirection() {
      return this.extending ? this.direction : this.direction.getOpposite();
   }

   private static double getMovement(AABB pHeadShape, Direction pDirection, AABB pFacing) {
      switch (pDirection) {
         case EAST:
            return pHeadShape.maxX - pFacing.minX;
         case WEST:
            return pFacing.maxX - pHeadShape.minX;
         case UP:
         default:
            return pHeadShape.maxY - pFacing.minY;
         case DOWN:
            return pFacing.maxY - pHeadShape.minY;
         case SOUTH:
            return pHeadShape.maxZ - pFacing.minZ;
         case NORTH:
            return pFacing.maxZ - pHeadShape.minZ;
      }
   }

   private static AABB moveByPositionAndProgress(BlockPos pPos, AABB pAabb, PistonMovingBlockEntity pPistonMovingBlockEntity) {
      double d0 = (double)pPistonMovingBlockEntity.getExtendedProgress(pPistonMovingBlockEntity.progress);
      return pAabb.move((double)pPos.getX() + d0 * (double)pPistonMovingBlockEntity.direction.getStepX(), (double)pPos.getY() + d0 * (double)pPistonMovingBlockEntity.direction.getStepY(), (double)pPos.getZ() + d0 * (double)pPistonMovingBlockEntity.direction.getStepZ());
   }

   private static void fixEntityWithinPistonBase(BlockPos pPos, Entity pEntity, Direction pDir, double pProgress) {
      AABB aabb = pEntity.getBoundingBox();
      AABB aabb1 = Shapes.block().bounds().move(pPos);
      if (aabb.intersects(aabb1)) {
         Direction direction = pDir.getOpposite();
         double d0 = getMovement(aabb1, direction, aabb) + 0.01D;
         double d1 = getMovement(aabb1, direction, aabb.intersect(aabb1)) + 0.01D;
         if (Math.abs(d0 - d1) < 0.01D) {
            d0 = Math.min(d0, pProgress) + 0.01D;
            moveEntityByPiston(pDir, pEntity, d0, direction);
         }
      }

   }

   public BlockState getMovedState() {
      return this.movedState;
   }

   /**
    * Removes the piston's BlockEntity and stops any movement
    */
   public void finalTick() {
      if (this.level != null && (this.progressO < 1.0F || this.level.isClientSide)) {
         this.progress = 1.0F;
         this.progressO = this.progress;
         this.level.removeBlockEntity(this.worldPosition);
         this.setRemoved();
         if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
            BlockState blockstate;
            if (this.isSourcePiston) {
               blockstate = Blocks.AIR.defaultBlockState();
            } else {
               blockstate = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
            }

            this.level.setBlock(this.worldPosition, blockstate, 3);
            this.level.neighborChanged(this.worldPosition, blockstate.getBlock(), this.worldPosition);
         }
      }

   }

   public static void tick(Level pLevel, BlockPos pPos, BlockState pState, PistonMovingBlockEntity pBlockEntity) {
      pBlockEntity.lastTicked = pLevel.getGameTime();
      pBlockEntity.progressO = pBlockEntity.progress;
      if (pBlockEntity.progressO >= 1.0F) {
         if (pLevel.isClientSide && pBlockEntity.deathTicks < 5) {
            ++pBlockEntity.deathTicks;
         } else {
            pLevel.removeBlockEntity(pPos);
            pBlockEntity.setRemoved();
            if (pLevel.getBlockState(pPos).is(Blocks.MOVING_PISTON)) {
               BlockState blockstate = Block.updateFromNeighbourShapes(pBlockEntity.movedState, pLevel, pPos);
               if (blockstate.isAir()) {
                  pLevel.setBlock(pPos, pBlockEntity.movedState, 84);
                  Block.updateOrDestroy(pBlockEntity.movedState, blockstate, pLevel, pPos, 3);
               } else {
                  if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate.getValue(BlockStateProperties.WATERLOGGED)) {
                     blockstate = blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
                  }

                  pLevel.setBlock(pPos, blockstate, 67);
                  pLevel.neighborChanged(pPos, blockstate.getBlock(), pPos);
               }
            }

         }
      } else {
         float f = pBlockEntity.progress + 0.5F;
         moveCollidedEntities(pLevel, pPos, f, pBlockEntity);
         moveStuckEntities(pLevel, pPos, f, pBlockEntity);
         pBlockEntity.progress = f;
         if (pBlockEntity.progress >= 1.0F) {
            pBlockEntity.progress = 1.0F;
         }

      }
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      HolderGetter<Block> holdergetter = (HolderGetter<Block>)(this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup());
      this.movedState = NbtUtils.readBlockState(holdergetter, pTag.getCompound("blockState"));
      this.direction = Direction.from3DDataValue(pTag.getInt("facing"));
      this.progress = pTag.getFloat("progress");
      this.progressO = this.progress;
      this.extending = pTag.getBoolean("extending");
      this.isSourcePiston = pTag.getBoolean("source");
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.put("blockState", NbtUtils.writeBlockState(this.movedState));
      pTag.putInt("facing", this.direction.get3DDataValue());
      pTag.putFloat("progress", this.progressO);
      pTag.putBoolean("extending", this.extending);
      pTag.putBoolean("source", this.isSourcePiston);
   }

   public VoxelShape getCollisionShape(BlockGetter pLevel, BlockPos pPos) {
      VoxelShape voxelshape;
      if (!this.extending && this.isSourcePiston && this.movedState.getBlock() instanceof PistonBaseBlock) {
         voxelshape = this.movedState.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true)).getCollisionShape(pLevel, pPos);
      } else {
         voxelshape = Shapes.empty();
      }

      Direction direction = NOCLIP.get();
      if ((double)this.progress < 1.0D && direction == this.getMovementDirection()) {
         return voxelshape;
      } else {
         BlockState blockstate;
         if (this.isSourcePiston()) {
            blockstate = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, this.direction).setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.extending != 1.0F - this.progress < 0.25F));
         } else {
            blockstate = this.movedState;
         }

         float f = this.getExtendedProgress(this.progress);
         double d0 = (double)((float)this.direction.getStepX() * f);
         double d1 = (double)((float)this.direction.getStepY() * f);
         double d2 = (double)((float)this.direction.getStepZ() * f);
         return Shapes.or(voxelshape, blockstate.getCollisionShape(pLevel, pPos).move(d0, d1, d2));
      }
   }

   public long getLastTicked() {
      return this.lastTicked;
   }

   public void setLevel(Level pLevel) {
      super.setLevel(pLevel);
      if (pLevel.holderLookup(Registries.BLOCK).get(this.movedState.getBlock().builtInRegistryHolder().key()).isEmpty()) {
         this.movedState = Blocks.AIR.defaultBlockState();
      }

   }
}
