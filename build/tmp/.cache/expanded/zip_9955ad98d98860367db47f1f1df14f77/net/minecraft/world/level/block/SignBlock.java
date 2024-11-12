package net.minecraft.world.level.block;

import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final float AABB_OFFSET = 4.0F;
   protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
   private final WoodType type;

   protected SignBlock(BlockBehaviour.Properties pProperties, WoodType pType) {
      super(pProperties);
      this.type = pType;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public boolean isPossibleToRespawnInThis(BlockState pState) {
      return true;
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SignBlockEntity(pPos, pState);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      Item item = itemstack.getItem();
      Item $$11 = itemstack.getItem();
      SignApplicator signapplicator2;
      if ($$11 instanceof SignApplicator signapplicator1) {
         signapplicator2 = signapplicator1;
      } else {
         signapplicator2 = null;
      }

      SignApplicator signapplicator = signapplicator2;
      boolean flag1 = signapplicator != null && pPlayer.mayBuild();
      BlockEntity $$12 = pLevel.getBlockEntity(pPos);
      if ($$12 instanceof SignBlockEntity signblockentity) {
         if (!pLevel.isClientSide) {
            boolean flag2 = signblockentity.isFacingFrontText(pPlayer);
            SignText signtext = signblockentity.getText(flag2);
            boolean flag = signblockentity.executeClickCommandsIfPresent(pPlayer, pLevel, pPos, flag2);
            if (signblockentity.isWaxed()) {
               pLevel.playSound((Player)null, signblockentity.getBlockPos(), SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
               return InteractionResult.PASS;
            } else if (flag1 && !this.otherPlayerIsEditingSign(pPlayer, signblockentity) && signapplicator.canApplyToSign(signtext, pPlayer) && signapplicator.tryApplyToSign(pLevel, signblockentity, flag2, pPlayer)) {
               if (!pPlayer.isCreative()) {
                  itemstack.shrink(1);
               }

               pLevel.gameEvent(GameEvent.BLOCK_CHANGE, signblockentity.getBlockPos(), GameEvent.Context.of(pPlayer, signblockentity.getBlockState()));
               pPlayer.awardStat(Stats.ITEM_USED.get(item));
               return InteractionResult.SUCCESS;
            } else if (flag) {
               return InteractionResult.SUCCESS;
            } else if (!this.otherPlayerIsEditingSign(pPlayer, signblockentity) && pPlayer.mayBuild() && this.hasEditableText(pPlayer, signblockentity, flag2)) {
               this.openTextEdit(pPlayer, signblockentity, flag2);
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         } else {
            return !flag1 && !signblockentity.isWaxed() ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private boolean hasEditableText(Player pPlayer, SignBlockEntity pSignEntity, boolean pIsFrontText) {
      SignText signtext = pSignEntity.getText(pIsFrontText);
      return Arrays.stream(signtext.getMessages(pPlayer.isTextFilteringEnabled())).allMatch((p_279411_) -> {
         return p_279411_.equals(CommonComponents.EMPTY) || p_279411_.getContents() instanceof LiteralContents;
      });
   }

   public abstract float getYRotationDegrees(BlockState pState);

   public Vec3 getSignHitboxCenterPosition(BlockState pState) {
      return new Vec3(0.5D, 0.5D, 0.5D);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public WoodType type() {
      return this.type;
   }

   public static WoodType getWoodType(Block pBlock) {
      WoodType woodtype;
      if (pBlock instanceof SignBlock) {
         woodtype = ((SignBlock)pBlock).type();
      } else {
         woodtype = WoodType.OAK;
      }

      return woodtype;
   }

   public void openTextEdit(Player pPlayer, SignBlockEntity pSignEntity, boolean pIsFrontText) {
      pSignEntity.setAllowedPlayerEditor(pPlayer.getUUID());
      pPlayer.openTextEdit(pSignEntity, pIsFrontText);
   }

   private boolean otherPlayerIsEditingSign(Player pPlayer, SignBlockEntity pSignEntity) {
      UUID uuid = pSignEntity.getPlayerWhoMayEdit();
      return uuid != null && !uuid.equals(pPlayer.getUUID());
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, BlockEntityType.SIGN, SignBlockEntity::tick);
   }
}