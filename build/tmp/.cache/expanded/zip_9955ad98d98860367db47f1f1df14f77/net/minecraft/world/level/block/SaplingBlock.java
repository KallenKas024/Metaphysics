package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SaplingBlock extends BushBlock implements BonemealableBlock {
   public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
   private final AbstractTreeGrower treeGrower;

   public SaplingBlock(AbstractTreeGrower pTreeGrower, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.treeGrower = pTreeGrower;
      this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pLevel.isAreaLoaded(pPos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
      if (pLevel.getMaxLocalRawBrightness(pPos.above()) >= 9 && pRandom.nextInt(7) == 0) {
         this.advanceTree(pLevel, pPos, pState, pRandom);
      }

   }

   public void advanceTree(ServerLevel pLevel, BlockPos pPos, BlockState pState, RandomSource pRandom) {
      if (pState.getValue(STAGE) == 0) {
         pLevel.setBlock(pPos, pState.cycle(STAGE), 4);
      } else {
         this.treeGrower.growTree(pLevel, pLevel.getChunkSource().getGenerator(), pPos, pState, pRandom);
      }

   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return true;
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return (double)pLevel.random.nextFloat() < 0.45D;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      this.advanceTree(pLevel, pPos, pState, pRandom);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(STAGE);
   }
}
