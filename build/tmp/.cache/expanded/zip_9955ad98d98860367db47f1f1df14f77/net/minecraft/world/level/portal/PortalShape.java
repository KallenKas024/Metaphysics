package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalShape {
   private static final int MIN_WIDTH = 2;
   public static final int MAX_WIDTH = 21;
   private static final int MIN_HEIGHT = 3;
   public static final int MAX_HEIGHT = 21;
   private static final BlockBehaviour.StatePredicate FRAME = (p_77720_, p_77721_, p_77722_) -> {
      return p_77720_.isPortalFrame(p_77721_, p_77722_);
   };
   private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
   private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0D;
   private final LevelAccessor level;
   private final Direction.Axis axis;
   private final Direction rightDir;
   private int numPortalBlocks;
   @Nullable
   private BlockPos bottomLeft;
   private int height;
   private final int width;

   public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor pLevel, BlockPos pBottomLeft, Direction.Axis pAxis) {
      return findPortalShape(pLevel, pBottomLeft, (p_77727_) -> {
         return p_77727_.isValid() && p_77727_.numPortalBlocks == 0;
      }, pAxis);
   }

   public static Optional<PortalShape> findPortalShape(LevelAccessor pLevel, BlockPos pBottomLeft, Predicate<PortalShape> pPredicate, Direction.Axis pAxis) {
      Optional<PortalShape> optional = Optional.of(new PortalShape(pLevel, pBottomLeft, pAxis)).filter(pPredicate);
      if (optional.isPresent()) {
         return optional;
      } else {
         Direction.Axis direction$axis = pAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
         return Optional.of(new PortalShape(pLevel, pBottomLeft, direction$axis)).filter(pPredicate);
      }
   }

   public PortalShape(LevelAccessor pLevel, BlockPos pBottomLeft, Direction.Axis pAxis) {
      this.level = pLevel;
      this.axis = pAxis;
      this.rightDir = pAxis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
      this.bottomLeft = this.calculateBottomLeft(pBottomLeft);
      if (this.bottomLeft == null) {
         this.bottomLeft = pBottomLeft;
         this.width = 1;
         this.height = 1;
      } else {
         this.width = this.calculateWidth();
         if (this.width > 0) {
            this.height = this.calculateHeight();
         }
      }

   }

   @Nullable
   private BlockPos calculateBottomLeft(BlockPos pPos) {
      for(int i = Math.max(this.level.getMinBuildHeight(), pPos.getY() - 21); pPos.getY() > i && isEmpty(this.level.getBlockState(pPos.below())); pPos = pPos.below()) {
      }

      Direction direction = this.rightDir.getOpposite();
      int j = this.getDistanceUntilEdgeAboveFrame(pPos, direction) - 1;
      return j < 0 ? null : pPos.relative(direction, j);
   }

   private int calculateWidth() {
      int i = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
      return i >= 2 && i <= 21 ? i : 0;
   }

   private int getDistanceUntilEdgeAboveFrame(BlockPos pPos, Direction pDirection) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i <= 21; ++i) {
         blockpos$mutableblockpos.set(pPos).move(pDirection, i);
         BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);
         if (!isEmpty(blockstate)) {
            if (FRAME.test(blockstate, this.level, blockpos$mutableblockpos)) {
               return i;
            }
            break;
         }

         BlockState blockstate1 = this.level.getBlockState(blockpos$mutableblockpos.move(Direction.DOWN));
         if (!FRAME.test(blockstate1, this.level, blockpos$mutableblockpos)) {
            break;
         }
      }

      return 0;
   }

   private int calculateHeight() {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int i = this.getDistanceUntilTop(blockpos$mutableblockpos);
      return i >= 3 && i <= 21 && this.hasTopFrame(blockpos$mutableblockpos, i) ? i : 0;
   }

   private boolean hasTopFrame(BlockPos.MutableBlockPos pPos, int pDistanceToTop) {
      for(int i = 0; i < this.width; ++i) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.set(this.bottomLeft).move(Direction.UP, pDistanceToTop).move(this.rightDir, i);
         if (!FRAME.test(this.level.getBlockState(blockpos$mutableblockpos), this.level, blockpos$mutableblockpos)) {
            return false;
         }
      }

      return true;
   }

   private int getDistanceUntilTop(BlockPos.MutableBlockPos pPos) {
      for(int i = 0; i < 21; ++i) {
         pPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
         if (!FRAME.test(this.level.getBlockState(pPos), this.level, pPos)) {
            return i;
         }

         pPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
         if (!FRAME.test(this.level.getBlockState(pPos), this.level, pPos)) {
            return i;
         }

         for(int j = 0; j < this.width; ++j) {
            pPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
            BlockState blockstate = this.level.getBlockState(pPos);
            if (!isEmpty(blockstate)) {
               return i;
            }

            if (blockstate.is(Blocks.NETHER_PORTAL)) {
               ++this.numPortalBlocks;
            }
         }
      }

      return 21;
   }

   private static boolean isEmpty(BlockState pState) {
      return pState.isAir() || pState.is(BlockTags.FIRE) || pState.is(Blocks.NETHER_PORTAL);
   }

   public boolean isValid() {
      return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
   }

   public void createPortalBlocks() {
      BlockState blockstate = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
      BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach((p_77725_) -> {
         this.level.setBlock(p_77725_, blockstate, 18);
      });
   }

   public boolean isComplete() {
      return this.isValid() && this.numPortalBlocks == this.width * this.height;
   }

   public static Vec3 getRelativePosition(BlockUtil.FoundRectangle pFoundRectangle, Direction.Axis pAxis, Vec3 pPos, EntityDimensions pEntityDimensions) {
      double d0 = (double)pFoundRectangle.axis1Size - (double)pEntityDimensions.width;
      double d1 = (double)pFoundRectangle.axis2Size - (double)pEntityDimensions.height;
      BlockPos blockpos = pFoundRectangle.minCorner;
      double d2;
      if (d0 > 0.0D) {
         float f = (float)blockpos.get(pAxis) + pEntityDimensions.width / 2.0F;
         d2 = Mth.clamp(Mth.inverseLerp(pPos.get(pAxis) - (double)f, 0.0D, d0), 0.0D, 1.0D);
      } else {
         d2 = 0.5D;
      }

      double d4;
      if (d1 > 0.0D) {
         Direction.Axis direction$axis = Direction.Axis.Y;
         d4 = Mth.clamp(Mth.inverseLerp(pPos.get(direction$axis) - (double)blockpos.get(direction$axis), 0.0D, d1), 0.0D, 1.0D);
      } else {
         d4 = 0.0D;
      }

      Direction.Axis direction$axis1 = pAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
      double d3 = pPos.get(direction$axis1) - ((double)blockpos.get(direction$axis1) + 0.5D);
      return new Vec3(d2, d4, d3);
   }

   public static PortalInfo createPortalInfo(ServerLevel pLevel, BlockUtil.FoundRectangle pPortalPos, Direction.Axis pAxis, Vec3 pRelativePos, Entity pEntity, Vec3 pVelocity, float pYRot, float pXRot) {
      BlockPos blockpos = pPortalPos.minCorner;
      BlockState blockstate = pLevel.getBlockState(blockpos);
      Direction.Axis direction$axis = blockstate.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
      double d0 = (double)pPortalPos.axis1Size;
      double d1 = (double)pPortalPos.axis2Size;
      EntityDimensions entitydimensions = pEntity.getDimensions(pEntity.getPose());
      int i = pAxis == direction$axis ? 0 : 90;
      Vec3 vec3 = pAxis == direction$axis ? pVelocity : new Vec3(pVelocity.z, pVelocity.y, -pVelocity.x);
      double d2 = (double)entitydimensions.width / 2.0D + (d0 - (double)entitydimensions.width) * pRelativePos.x();
      double d3 = (d1 - (double)entitydimensions.height) * pRelativePos.y();
      double d4 = 0.5D + pRelativePos.z();
      boolean flag = direction$axis == Direction.Axis.X;
      Vec3 vec31 = new Vec3((double)blockpos.getX() + (flag ? d2 : d4), (double)blockpos.getY() + d3, (double)blockpos.getZ() + (flag ? d4 : d2));
      Vec3 vec32 = findCollisionFreePosition(vec31, pLevel, pEntity, entitydimensions);
      return new PortalInfo(vec32, vec3, pYRot + (float)i, pXRot);
   }

   private static Vec3 findCollisionFreePosition(Vec3 pPos, ServerLevel pLevel, Entity pEntity, EntityDimensions pDimensions) {
      if (!(pDimensions.width > 4.0F) && !(pDimensions.height > 4.0F)) {
         double d0 = (double)pDimensions.height / 2.0D;
         Vec3 vec3 = pPos.add(0.0D, d0, 0.0D);
         VoxelShape voxelshape = Shapes.create(AABB.ofSize(vec3, (double)pDimensions.width, 0.0D, (double)pDimensions.width).expandTowards(0.0D, 1.0D, 0.0D).inflate(1.0E-6D));
         Optional<Vec3> optional = pLevel.findFreePosition(pEntity, voxelshape, vec3, (double)pDimensions.width, (double)pDimensions.height, (double)pDimensions.width);
         Optional<Vec3> optional1 = optional.map((p_259019_) -> {
            return p_259019_.subtract(0.0D, d0, 0.0D);
         });
         return optional1.orElse(pPos);
      } else {
         return pPos;
      }
   }
}
