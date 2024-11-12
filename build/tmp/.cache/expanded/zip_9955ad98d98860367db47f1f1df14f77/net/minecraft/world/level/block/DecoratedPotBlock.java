package net.minecraft.world.level.block;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final ResourceLocation SHERDS_DYNAMIC_DROP_ID = new ResourceLocation("sherds");
   private static final VoxelShape BOUNDING_BOX = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
   private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final BooleanProperty CRACKED = BlockStateProperties.CRACKED;
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   public DecoratedPotBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(CRACKED, Boolean.valueOf(false)));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      return this.defaultBlockState().setValue(HORIZONTAL_FACING, pContext.getHorizontalDirection()).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER)).setValue(CRACKED, Boolean.valueOf(false));
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return BOUNDING_BOX;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HORIZONTAL_FACING, WATERLOGGED, CRACKED);
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new DecoratedPotBlockEntity(pPos, pState);
   }

   public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
      BlockEntity blockentity = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (blockentity instanceof DecoratedPotBlockEntity decoratedpotblockentity) {
         pParams.withDynamicDrop(SHERDS_DYNAMIC_DROP_ID, (p_284876_) -> {
            decoratedpotblockentity.getDecorations().sorted().map(Item::getDefaultInstance).forEach(p_284876_);
         });
      }

      return super.getDrops(pState, pParams);
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      ItemStack itemstack = pPlayer.getMainHandItem();
      BlockState blockstate = pState;
      if (itemstack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(itemstack)) {
         blockstate = pState.setValue(CRACKED, Boolean.valueOf(true));
         pLevel.setBlock(pPos, blockstate, 4);
      }

      super.playerWillDestroy(pLevel, pPos, blockstate, pPlayer);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public SoundType getSoundType(BlockState pState) {
      return pState.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
   }

   public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      DecoratedPotBlockEntity.Decorations decoratedpotblockentity$decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(pStack));
      if (!decoratedpotblockentity$decorations.equals(DecoratedPotBlockEntity.Decorations.EMPTY)) {
         pTooltip.add(CommonComponents.EMPTY);
         Stream.of(decoratedpotblockentity$decorations.front(), decoratedpotblockentity$decorations.left(), decoratedpotblockentity$decorations.right(), decoratedpotblockentity$decorations.back()).forEach((p_284873_) -> {
            pTooltip.add((new ItemStack(p_284873_, 1)).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY));
         });
      }
   }
}