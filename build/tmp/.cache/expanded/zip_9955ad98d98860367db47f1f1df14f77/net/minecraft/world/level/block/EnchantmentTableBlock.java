package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantmentTableBlock extends BaseEntityBlock {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
   public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2).filter((p_207914_) -> {
      return Math.abs(p_207914_.getX()) == 2 || Math.abs(p_207914_.getZ()) == 2;
   }).map(BlockPos::immutable).toList();

   public EnchantmentTableBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public static boolean isValidBookShelf(Level pLevel, BlockPos pTablePos, BlockPos pOffsetPos) {
      return pLevel.getBlockState(pTablePos.offset(pOffsetPos)).getEnchantPowerBonus(pLevel, pTablePos.offset(pOffsetPos)) != 0 && pLevel.getBlockState(pTablePos.offset(pOffsetPos.getX() / 2, pOffsetPos.getY(), pOffsetPos.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      super.animateTick(pState, pLevel, pPos, pRandom);

      for(BlockPos blockpos : BOOKSHELF_OFFSETS) {
         if (pRandom.nextInt(16) == 0 && isValidBookShelf(pLevel, pPos, blockpos)) {
            pLevel.addParticle(ParticleTypes.ENCHANT, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 2.0D, (double)pPos.getZ() + 0.5D, (double)((float)blockpos.getX() + pRandom.nextFloat()) - 0.5D, (double)((float)blockpos.getY() - pRandom.nextFloat() - 1.0F), (double)((float)blockpos.getZ() + pRandom.nextFloat()) - 0.5D);
         }
      }

   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new EnchantmentTableBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, BlockEntityType.ENCHANTING_TABLE, EnchantmentTableBlockEntity::bookAnimationTick) : null;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
         return InteractionResult.CONSUME;
      }
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof EnchantmentTableBlockEntity) {
         Component component = ((Nameable)blockentity).getDisplayName();
         return new SimpleMenuProvider((p_207906_, p_207907_, p_207908_) -> {
            return new EnchantmentMenu(p_207906_, p_207907_, ContainerLevelAccess.create(pLevel, pPos));
         }, component);
      } else {
         return null;
      }
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof EnchantmentTableBlockEntity) {
            ((EnchantmentTableBlockEntity)blockentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}
