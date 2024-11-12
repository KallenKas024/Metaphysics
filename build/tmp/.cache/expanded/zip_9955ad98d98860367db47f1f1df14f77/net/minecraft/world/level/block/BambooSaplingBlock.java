package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BambooSaplingBlock extends Block implements BonemealableBlock {
   protected static final float SAPLING_AABB_OFFSET = 4.0F;
   protected static final VoxelShape SAPLING_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 12.0D, 12.0D);

   public BambooSaplingBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Vec3 vec3 = pState.getOffset(pLevel, pPos);
      return SAPLING_SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pRandom.nextInt(3) == 0 && pLevel.isEmptyBlock(pPos.above()) && pLevel.getRawBrightness(pPos.above(), 0) >= 9) {
         this.growBamboo(pLevel, pPos);
      }

   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (!pState.canSurvive(pLevel, pCurrentPos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if (pFacing == Direction.UP && pFacingState.is(Blocks.BAMBOO)) {
            pLevel.setBlock(pCurrentPos, Blocks.BAMBOO.defaultBlockState(), 2);
         }

         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(Items.BAMBOO);
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return pLevel.getBlockState(pPos.above()).isAir();
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      this.growBamboo(pLevel, pPos);
   }

   /**
    * Get the hardness of this Block relative to the ability of the given player
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getDestroyProgress} whenever possible.
    * Implementing/overriding is fine.
    */
   public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
      return pPlayer.getMainHandItem().canPerformAction(net.minecraftforge.common.ToolActions.SWORD_DIG) ? 1.0F : super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
   }

   protected void growBamboo(Level pLevel, BlockPos pState) {
      pLevel.setBlock(pState.above(), Blocks.BAMBOO.defaultBlockState().setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL), 3);
   }
}
