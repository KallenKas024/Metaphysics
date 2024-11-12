package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropperBlock extends DispenserBlock {
   private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

   public DropperBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   protected DispenseItemBehavior getDispenseMethod(ItemStack pStack) {
      return DISPENSE_BEHAVIOUR;
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new DropperBlockEntity(pPos, pState);
   }

   protected void dispenseFrom(ServerLevel pLevel, BlockPos pPos) {
      BlockSourceImpl blocksourceimpl = new BlockSourceImpl(pLevel, pPos);
      DispenserBlockEntity dispenserblockentity = blocksourceimpl.getEntity();
      int i = dispenserblockentity.getRandomSlot(pLevel.random);
      if (i < 0) {
         pLevel.levelEvent(1001, pPos, 0);
      } else {
         ItemStack itemstack = dispenserblockentity.getItem(i);
         if (!itemstack.isEmpty() && net.minecraftforge.items.VanillaInventoryCodeHooks.dropperInsertHook(pLevel, pPos, dispenserblockentity, i, itemstack)) {
            Direction direction = pLevel.getBlockState(pPos).getValue(FACING);
            Container container = HopperBlockEntity.getContainerAt(pLevel, pPos.relative(direction));
            ItemStack itemstack1;
            if (container == null) {
               itemstack1 = DISPENSE_BEHAVIOUR.dispense(blocksourceimpl, itemstack);
            } else {
               itemstack1 = HopperBlockEntity.addItem(dispenserblockentity, container, itemstack.copy().split(1), direction.getOpposite());
               if (itemstack1.isEmpty()) {
                  itemstack1 = itemstack.copy();
                  itemstack1.shrink(1);
               } else {
                  itemstack1 = itemstack.copy();
               }
            }

            dispenserblockentity.setItem(i, itemstack1);
         }
      }
   }
}
