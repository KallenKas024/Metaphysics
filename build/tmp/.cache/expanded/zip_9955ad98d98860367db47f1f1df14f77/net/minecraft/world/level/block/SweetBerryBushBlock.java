package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SweetBerryBushBlock extends BushBlock implements BonemealableBlock {
   private static final float HURT_SPEED_THRESHOLD = 0.003F;
   public static final int MAX_AGE = 3;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
   private static final VoxelShape SAPLING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);
   private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public SweetBerryBushBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(Items.SWEET_BERRIES);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (pState.getValue(AGE) == 0) {
         return SAPLING_SHAPE;
      } else {
         return pState.getValue(AGE) < 3 ? MID_GROWTH_SHAPE : super.getShape(pState, pLevel, pPos, pContext);
      }
   }

   /**
    * @return whether this block needs random ticking.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getValue(AGE) < 3;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      int i = pState.getValue(AGE);
      if (i < 3 && pLevel.getRawBrightness(pPos.above(), 0) >= 9 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt(5) == 0)) {
         BlockState blockstate = pState.setValue(AGE, Integer.valueOf(i + 1));
         pLevel.setBlock(pPos, blockstate, 2);
         pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
         net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
      }

   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      if (pEntity instanceof LivingEntity && pEntity.getType() != EntityType.FOX && pEntity.getType() != EntityType.BEE) {
         pEntity.makeStuckInBlock(pState, new Vec3((double)0.8F, 0.75D, (double)0.8F));
         if (!pLevel.isClientSide && pState.getValue(AGE) > 0 && (pEntity.xOld != pEntity.getX() || pEntity.zOld != pEntity.getZ())) {
            double d0 = Math.abs(pEntity.getX() - pEntity.xOld);
            double d1 = Math.abs(pEntity.getZ() - pEntity.zOld);
            if (d0 >= (double)0.003F || d1 >= (double)0.003F) {
               pEntity.hurt(pLevel.damageSources().sweetBerryBush(), 1.0F);
            }
         }

      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      int i = pState.getValue(AGE);
      boolean flag = i == 3;
      if (!flag && pPlayer.getItemInHand(pHand).is(Items.BONE_MEAL)) {
         return InteractionResult.PASS;
      } else if (i > 1) {
         int j = 1 + pLevel.random.nextInt(2);
         popResource(pLevel, pPos, new ItemStack(Items.SWEET_BERRIES, j + (flag ? 1 : 0)));
         pLevel.playSound((Player)null, pPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + pLevel.random.nextFloat() * 0.4F);
         BlockState blockstate = pState.setValue(AGE, Integer.valueOf(1));
         pLevel.setBlock(pPos, blockstate, 2);
         pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pPlayer, blockstate));
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return pState.getValue(AGE) < 3;
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      int i = Math.min(3, pState.getValue(AGE) + 1);
      pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(i)), 2);
   }
}
