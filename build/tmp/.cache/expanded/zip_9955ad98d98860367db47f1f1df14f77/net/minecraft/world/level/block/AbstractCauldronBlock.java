package net.minecraft.world.level.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractCauldronBlock extends Block {
   private static final int SIDE_THICKNESS = 2;
   private static final int LEG_WIDTH = 4;
   private static final int LEG_HEIGHT = 3;
   private static final int LEG_DEPTH = 2;
   protected static final int FLOOR_LEVEL = 4;
   private static final VoxelShape INSIDE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE), BooleanOp.ONLY_FIRST);
   private final Map<Item, CauldronInteraction> interactions;

   public AbstractCauldronBlock(BlockBehaviour.Properties pProperties, Map<Item, CauldronInteraction> pInteractions) {
      super(pProperties);
      this.interactions = pInteractions;
   }

   protected double getContentHeight(BlockState pState) {
      return 0.0D;
   }

   protected boolean isEntityInsideContent(BlockState pState, BlockPos pPos, Entity pEntity) {
      return pEntity.getY() < (double)pPos.getY() + this.getContentHeight(pState) && pEntity.getBoundingBox().maxY > (double)pPos.getY() + 0.25D;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      CauldronInteraction cauldroninteraction = this.interactions.get(itemstack.getItem());
      return cauldroninteraction.interact(pState, pLevel, pPos, pPlayer, pHand, itemstack);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return INSIDE;
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public abstract boolean isFull(BlockState pState);

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockPos blockpos = PointedDripstoneBlock.findStalactiteTipAboveCauldron(pLevel, pPos);
      if (blockpos != null) {
         Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(pLevel, blockpos);
         if (fluid != Fluids.EMPTY && this.canReceiveStalactiteDrip(fluid)) {
            this.receiveStalactiteDrip(pState, pLevel, pPos, fluid);
         }

      }
   }

   protected boolean canReceiveStalactiteDrip(Fluid pFluid) {
      return false;
   }

   protected void receiveStalactiteDrip(BlockState pState, Level pLevel, BlockPos pPos, Fluid pFluid) {
   }
}