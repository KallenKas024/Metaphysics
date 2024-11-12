package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public abstract class AbstractSkullBlock extends BaseEntityBlock implements Equipable {
   private final SkullBlock.Type type;

   public AbstractSkullBlock(SkullBlock.Type pType, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.type = pType;
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SkullBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      if (pLevel.isClientSide) {
         boolean flag = pState.is(Blocks.DRAGON_HEAD) || pState.is(Blocks.DRAGON_WALL_HEAD) || pState.is(Blocks.PIGLIN_HEAD) || pState.is(Blocks.PIGLIN_WALL_HEAD);
         if (flag) {
            return createTickerHelper(pBlockEntityType, BlockEntityType.SKULL, SkullBlockEntity::animation);
         }
      }

      return null;
   }

   public SkullBlock.Type getType() {
      return this.type;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }
}