package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShulkerBoxBlock extends BaseEntityBlock {
   private static final float OPEN_AABB_SIZE = 1.0F;
   private static final VoxelShape UP_OPEN_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape DOWN_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
   private static final VoxelShape WES_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_OPEN_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private static final Map<Direction, VoxelShape> OPEN_SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (p_258974_) -> {
      p_258974_.put(Direction.NORTH, NORTH_OPEN_AABB);
      p_258974_.put(Direction.EAST, EAST_OPEN_AABB);
      p_258974_.put(Direction.SOUTH, SOUTH_OPEN_AABB);
      p_258974_.put(Direction.WEST, WES_OPEN_AABB);
      p_258974_.put(Direction.UP, UP_OPEN_AABB);
      p_258974_.put(Direction.DOWN, DOWN_OPEN_AABB);
   });
   public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
   public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
   @Nullable
   private final DyeColor color;

   public ShulkerBoxBlock(@Nullable DyeColor pColor, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.color = pColor;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new ShulkerBoxBlockEntity(this.color, pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, BlockEntityType.SHULKER_BOX, ShulkerBoxBlockEntity::tick);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else if (pPlayer.isSpectator()) {
         return InteractionResult.CONSUME;
      } else {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulkerboxblockentity = (ShulkerBoxBlockEntity)blockentity;
            if (canOpen(pState, pLevel, pPos, shulkerboxblockentity)) {
               pPlayer.openMenu(shulkerboxblockentity);
               pPlayer.awardStat(Stats.OPEN_SHULKER_BOX);
               PiglinAi.angerNearbyPiglins(pPlayer, true);
            }

            return InteractionResult.CONSUME;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   private static boolean canOpen(BlockState pState, Level pLevel, BlockPos pPos, ShulkerBoxBlockEntity pBlockEntity) {
      if (pBlockEntity.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
         return true;
      } else {
         AABB aabb = Shulker.getProgressDeltaAabb(pState.getValue(FACING), 0.0F, 0.5F).move(pPos).deflate(1.0E-6D);
         return pLevel.noCollision(aabb);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getClickedFace());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING);
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof ShulkerBoxBlockEntity shulkerboxblockentity) {
         if (!pLevel.isClientSide && pPlayer.isCreative() && !shulkerboxblockentity.isEmpty()) {
            ItemStack itemstack = getColoredItemStack(this.getColor());
            blockentity.saveToItem(itemstack);
            if (shulkerboxblockentity.hasCustomName()) {
               itemstack.setHoverName(shulkerboxblockentity.getCustomName());
            }

            ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
         } else {
            shulkerboxblockentity.unpackLootTable(pPlayer);
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
      BlockEntity blockentity = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (blockentity instanceof ShulkerBoxBlockEntity shulkerboxblockentity) {
         pParams = pParams.withDynamicDrop(CONTENTS, (p_56219_) -> {
            for(int i = 0; i < shulkerboxblockentity.getContainerSize(); ++i) {
               p_56219_.accept(shulkerboxblockentity.getItem(i));
            }

         });
      }

      return super.getDrops(pState, pParams);
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof ShulkerBoxBlockEntity) {
            ((ShulkerBoxBlockEntity)blockentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof ShulkerBoxBlockEntity) {
            pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      if (compoundtag != null) {
         if (compoundtag.contains("LootTable", 8)) {
            pTooltip.add(Component.literal("???????"));
         }

         if (compoundtag.contains("Items", 9)) {
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(compoundtag, nonnulllist);
            int i = 0;
            int j = 0;

            for(ItemStack itemstack : nonnulllist) {
               if (!itemstack.isEmpty()) {
                  ++j;
                  if (i <= 4) {
                     ++i;
                     MutableComponent mutablecomponent = itemstack.getHoverName().copy();
                     mutablecomponent.append(" x").append(String.valueOf(itemstack.getCount()));
                     pTooltip.add(mutablecomponent);
                  }
               }
            }

            if (j - i > 0) {
               pTooltip.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
            }
         }
      }

   }

   public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof ShulkerBoxBlockEntity shulkerboxblockentity) {
         if (!shulkerboxblockentity.isClosed()) {
            return OPEN_SHAPE_BY_DIRECTION.get(pState.getValue(FACING).getOpposite());
         }
      }

      return Shapes.block();
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity instanceof ShulkerBoxBlockEntity ? Shapes.create(((ShulkerBoxBlockEntity)blockentity).getBoundingBox(pState)) : Shapes.block();
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)pLevel.getBlockEntity(pPos));
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      ItemStack itemstack = super.getCloneItemStack(pLevel, pPos, pState);
      pLevel.getBlockEntity(pPos, BlockEntityType.SHULKER_BOX).ifPresent((p_187446_) -> {
         p_187446_.saveToItem(itemstack);
      });
      return itemstack;
   }

   @Nullable
   public static DyeColor getColorFromItem(Item pItem) {
      return getColorFromBlock(Block.byItem(pItem));
   }

   @Nullable
   public static DyeColor getColorFromBlock(Block pBlock) {
      return pBlock instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock)pBlock).getColor() : null;
   }

   public static Block getBlockByColor(@Nullable DyeColor pColor) {
      if (pColor == null) {
         return Blocks.SHULKER_BOX;
      } else {
         switch (pColor) {
            case WHITE:
               return Blocks.WHITE_SHULKER_BOX;
            case ORANGE:
               return Blocks.ORANGE_SHULKER_BOX;
            case MAGENTA:
               return Blocks.MAGENTA_SHULKER_BOX;
            case LIGHT_BLUE:
               return Blocks.LIGHT_BLUE_SHULKER_BOX;
            case YELLOW:
               return Blocks.YELLOW_SHULKER_BOX;
            case LIME:
               return Blocks.LIME_SHULKER_BOX;
            case PINK:
               return Blocks.PINK_SHULKER_BOX;
            case GRAY:
               return Blocks.GRAY_SHULKER_BOX;
            case LIGHT_GRAY:
               return Blocks.LIGHT_GRAY_SHULKER_BOX;
            case CYAN:
               return Blocks.CYAN_SHULKER_BOX;
            case PURPLE:
            default:
               return Blocks.PURPLE_SHULKER_BOX;
            case BLUE:
               return Blocks.BLUE_SHULKER_BOX;
            case BROWN:
               return Blocks.BROWN_SHULKER_BOX;
            case GREEN:
               return Blocks.GREEN_SHULKER_BOX;
            case RED:
               return Blocks.RED_SHULKER_BOX;
            case BLACK:
               return Blocks.BLACK_SHULKER_BOX;
         }
      }
   }

   @Nullable
   public DyeColor getColor() {
      return this.color;
   }

   public static ItemStack getColoredItemStack(@Nullable DyeColor pColor) {
      return new ItemStack(getBlockByColor(pColor));
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRot) {
      return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }
}