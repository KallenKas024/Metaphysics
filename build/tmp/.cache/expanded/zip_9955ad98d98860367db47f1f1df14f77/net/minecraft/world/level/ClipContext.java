package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClipContext {
   private final Vec3 from;
   private final Vec3 to;
   private final ClipContext.Block block;
   private final ClipContext.Fluid fluid;
   private final CollisionContext collisionContext;

   public ClipContext(Vec3 pFrom, Vec3 pTo, ClipContext.Block pBlock, ClipContext.Fluid pFluid, @org.jetbrains.annotations.Nullable Entity pEntity) {
      this.from = pFrom;
      this.to = pTo;
      this.block = pBlock;
      this.fluid = pFluid;
      this.collisionContext = pEntity == null ? CollisionContext.empty() : CollisionContext.of(pEntity);
   }

   public Vec3 getTo() {
      return this.to;
   }

   public Vec3 getFrom() {
      return this.from;
   }

   public VoxelShape getBlockShape(BlockState pBlockState, BlockGetter pLevel, BlockPos pPos) {
      return this.block.get(pBlockState, pLevel, pPos, this.collisionContext);
   }

   public VoxelShape getFluidShape(FluidState pState, BlockGetter pLevel, BlockPos pPos) {
      return this.fluid.canPick(pState) ? pState.getShape(pLevel, pPos) : Shapes.empty();
   }

   public static enum Block implements ClipContext.ShapeGetter {
      COLLIDER(BlockBehaviour.BlockStateBase::getCollisionShape),
      OUTLINE(BlockBehaviour.BlockStateBase::getShape),
      VISUAL(BlockBehaviour.BlockStateBase::getVisualShape),
      FALLDAMAGE_RESETTING((p_201982_, p_201983_, p_201984_, p_201985_) -> {
         return p_201982_.is(BlockTags.FALL_DAMAGE_RESETTING) ? Shapes.block() : Shapes.empty();
      });

      private final ClipContext.ShapeGetter shapeGetter;

      private Block(ClipContext.ShapeGetter pShapeGetter) {
         this.shapeGetter = pShapeGetter;
      }

      public VoxelShape get(BlockState pState, BlockGetter pBlock, BlockPos pPos, CollisionContext pCollisionContext) {
         return this.shapeGetter.get(pState, pBlock, pPos, pCollisionContext);
      }
   }

   public static enum Fluid {
      NONE((p_45736_) -> {
         return false;
      }),
      SOURCE_ONLY(FluidState::isSource),
      ANY((p_45734_) -> {
         return !p_45734_.isEmpty();
      }),
      WATER((p_201988_) -> {
         return p_201988_.is(FluidTags.WATER);
      });

      private final Predicate<FluidState> canPick;

      private Fluid(Predicate<FluidState> pCanPick) {
         this.canPick = pCanPick;
      }

      public boolean canPick(FluidState pState) {
         return this.canPick.test(pState);
      }
   }

   public interface ShapeGetter {
      VoxelShape get(BlockState pState, BlockGetter pBlock, BlockPos pPos, CollisionContext pCollisionContext);
   }
}
