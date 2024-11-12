package net.minecraft.world.item.context;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlaceContext extends UseOnContext {
   private final BlockPos relativePos;
   protected boolean replaceClicked = true;

   public BlockPlaceContext(Player pPlayer, InteractionHand pHand, ItemStack pItemStack, BlockHitResult pHitResult) {
      this(pPlayer.level(), pPlayer, pHand, pItemStack, pHitResult);
   }

   public BlockPlaceContext(UseOnContext pContext) {
      this(pContext.getLevel(), pContext.getPlayer(), pContext.getHand(), pContext.getItemInHand(), pContext.getHitResult());
   }

   public BlockPlaceContext(Level pLevel, @Nullable Player pPlayer, InteractionHand pHand, ItemStack pItemStack, BlockHitResult pHitResult) {
      super(pLevel, pPlayer, pHand, pItemStack, pHitResult);
      this.relativePos = pHitResult.getBlockPos().relative(pHitResult.getDirection());
      this.replaceClicked = pLevel.getBlockState(pHitResult.getBlockPos()).canBeReplaced(this);
   }

   public static BlockPlaceContext at(BlockPlaceContext pContext, BlockPos pPos, Direction pDirection) {
      return new BlockPlaceContext(pContext.getLevel(), pContext.getPlayer(), pContext.getHand(), pContext.getItemInHand(), new BlockHitResult(new Vec3((double)pPos.getX() + 0.5D + (double)pDirection.getStepX() * 0.5D, (double)pPos.getY() + 0.5D + (double)pDirection.getStepY() * 0.5D, (double)pPos.getZ() + 0.5D + (double)pDirection.getStepZ() * 0.5D), pDirection, pPos, false));
   }

   public BlockPos getClickedPos() {
      return this.replaceClicked ? super.getClickedPos() : this.relativePos;
   }

   public boolean canPlace() {
      return this.replaceClicked || this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
   }

   public boolean replacingClickedOnBlock() {
      return this.replaceClicked;
   }

   public Direction getNearestLookingDirection() {
      return Direction.orderedByNearest(this.getPlayer())[0];
   }

   public Direction getNearestLookingVerticalDirection() {
      return Direction.getFacingAxis(this.getPlayer(), Direction.Axis.Y);
   }

   public Direction[] getNearestLookingDirections() {
      Direction[] adirection = Direction.orderedByNearest(this.getPlayer());
      if (this.replaceClicked) {
         return adirection;
      } else {
         Direction direction = this.getClickedFace();

         int i;
         for(i = 0; i < adirection.length && adirection[i] != direction.getOpposite(); ++i) {
         }

         if (i > 0) {
            System.arraycopy(adirection, 0, adirection, 1, i);
            adirection[0] = direction.getOpposite();
         }

         return adirection;
      }
   }
}