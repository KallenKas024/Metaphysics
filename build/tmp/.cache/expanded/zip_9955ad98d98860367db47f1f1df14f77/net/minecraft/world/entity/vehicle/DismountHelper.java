package net.minecraft.world.entity.vehicle;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DismountHelper {
   public static int[][] offsetsForDirection(Direction pDirection) {
      Direction direction = pDirection.getClockWise();
      Direction direction1 = direction.getOpposite();
      Direction direction2 = pDirection.getOpposite();
      return new int[][]{{direction.getStepX(), direction.getStepZ()}, {direction1.getStepX(), direction1.getStepZ()}, {direction2.getStepX() + direction.getStepX(), direction2.getStepZ() + direction.getStepZ()}, {direction2.getStepX() + direction1.getStepX(), direction2.getStepZ() + direction1.getStepZ()}, {pDirection.getStepX() + direction.getStepX(), pDirection.getStepZ() + direction.getStepZ()}, {pDirection.getStepX() + direction1.getStepX(), pDirection.getStepZ() + direction1.getStepZ()}, {direction2.getStepX(), direction2.getStepZ()}, {pDirection.getStepX(), pDirection.getStepZ()}};
   }

   public static boolean isBlockFloorValid(double pDistance) {
      return !Double.isInfinite(pDistance) && pDistance < 1.0D;
   }

   public static boolean canDismountTo(CollisionGetter pLevel, LivingEntity pPassenger, AABB pBoundingBox) {
      for(VoxelShape voxelshape : pLevel.getBlockCollisions(pPassenger, pBoundingBox)) {
         if (!voxelshape.isEmpty()) {
            return false;
         }
      }

      return pLevel.getWorldBorder().isWithinBounds(pBoundingBox);
   }

   public static boolean canDismountTo(CollisionGetter pLevel, Vec3 pOffset, LivingEntity pPassenger, Pose pPose) {
      return canDismountTo(pLevel, pPassenger, pPassenger.getLocalBoundsForPose(pPose).move(pOffset));
   }

   public static VoxelShape nonClimbableShape(BlockGetter pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return !blockstate.is(BlockTags.CLIMBABLE) && (!(blockstate.getBlock() instanceof TrapDoorBlock) || !blockstate.getValue(TrapDoorBlock.OPEN)) ? blockstate.getCollisionShape(pLevel, pPos) : Shapes.empty();
   }

   public static double findCeilingFrom(BlockPos pPos, int pCeiling, Function<BlockPos, VoxelShape> pShapeForPos) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();
      int i = 0;

      while(i < pCeiling) {
         VoxelShape voxelshape = pShapeForPos.apply(blockpos$mutableblockpos);
         if (!voxelshape.isEmpty()) {
            return (double)(pPos.getY() + i) + voxelshape.min(Direction.Axis.Y);
         }

         ++i;
         blockpos$mutableblockpos.move(Direction.UP);
      }

      return Double.POSITIVE_INFINITY;
   }

   @Nullable
   public static Vec3 findSafeDismountLocation(EntityType<?> pEntityType, CollisionGetter pLevel, BlockPos pPos, boolean pOnlySafePositions) {
      if (pOnlySafePositions && pEntityType.isBlockDangerous(pLevel.getBlockState(pPos))) {
         return null;
      } else {
         double d0 = pLevel.getBlockFloorHeight(nonClimbableShape(pLevel, pPos), () -> {
            return nonClimbableShape(pLevel, pPos.below());
         });
         if (!isBlockFloorValid(d0)) {
            return null;
         } else if (pOnlySafePositions && d0 <= 0.0D && pEntityType.isBlockDangerous(pLevel.getBlockState(pPos.below()))) {
            return null;
         } else {
            Vec3 vec3 = Vec3.upFromBottomCenterOf(pPos, d0);
            AABB aabb = pEntityType.getDimensions().makeBoundingBox(vec3);

            for(VoxelShape voxelshape : pLevel.getBlockCollisions((Entity)null, aabb)) {
               if (!voxelshape.isEmpty()) {
                  return null;
               }
            }

            if (pEntityType != EntityType.PLAYER || !pLevel.getBlockState(pPos).is(BlockTags.INVALID_SPAWN_INSIDE) && !pLevel.getBlockState(pPos.above()).is(BlockTags.INVALID_SPAWN_INSIDE)) {
               return !pLevel.getWorldBorder().isWithinBounds(aabb) ? null : vec3;
            } else {
               return null;
            }
         }
      }
   }
}