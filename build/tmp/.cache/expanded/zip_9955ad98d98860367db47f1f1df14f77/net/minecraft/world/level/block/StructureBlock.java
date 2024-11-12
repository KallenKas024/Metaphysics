package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.BlockHitResult;

public class StructureBlock extends BaseEntityBlock implements GameMasterBlock {
   public static final EnumProperty<StructureMode> MODE = BlockStateProperties.STRUCTUREBLOCK_MODE;

   public StructureBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(MODE, StructureMode.LOAD));
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new StructureBlockEntity(pPos, pState);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof StructureBlockEntity) {
         return ((StructureBlockEntity)blockentity).usedBy(pPlayer) ? InteractionResult.sidedSuccess(pLevel.isClientSide) : InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      if (!pLevel.isClientSide) {
         if (pPlacer != null) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof StructureBlockEntity) {
               ((StructureBlockEntity)blockentity).createdBy(pPlacer);
            }
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

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(MODE);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (pLevel instanceof ServerLevel) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof StructureBlockEntity) {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)blockentity;
            boolean flag = pLevel.hasNeighborSignal(pPos);
            boolean flag1 = structureblockentity.isPowered();
            if (flag && !flag1) {
               structureblockentity.setPowered(true);
               this.trigger((ServerLevel)pLevel, structureblockentity);
            } else if (!flag && flag1) {
               structureblockentity.setPowered(false);
            }

         }
      }
   }

   private void trigger(ServerLevel pLevel, StructureBlockEntity pBlockEntity) {
      switch (pBlockEntity.getMode()) {
         case SAVE:
            pBlockEntity.saveStructure(false);
            break;
         case LOAD:
            pBlockEntity.loadStructure(pLevel, false);
            break;
         case CORNER:
            pBlockEntity.unloadStructure();
         case DATA:
      }

   }
}