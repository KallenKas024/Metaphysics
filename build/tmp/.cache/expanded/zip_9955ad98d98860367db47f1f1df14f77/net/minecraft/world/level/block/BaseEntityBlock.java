package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseEntityBlock extends Block implements EntityBlock {
   protected BaseEntityBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.INVISIBLE;
   }

   /**
    * Called on server when {@link net.minecraft.world.level.Level#blockEvent} is called. If server returns true, then
    * also called on the client. On the Server, this may perform additional changes to the world, like pistons replacing
    * the block with an extended base. On the client, the update may involve replacing block entities or effects such as
    * sounds or particles
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#triggerEvent}
    * whenever possible. Implementing/overriding is fine.
    */
   public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
      super.triggerEvent(pState, pLevel, pPos, pId, pParam);
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity == null ? false : blockentity.triggerEvent(pId, pParam);
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity instanceof MenuProvider ? (MenuProvider)blockentity : null;
   }

   @Nullable
   protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
      return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
   }
}