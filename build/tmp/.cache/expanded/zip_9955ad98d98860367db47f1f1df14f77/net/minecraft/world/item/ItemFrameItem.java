package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;

public class ItemFrameItem extends HangingEntityItem {
   public ItemFrameItem(EntityType<? extends HangingEntity> pType, Item.Properties pProperties) {
      super(pType, pProperties);
   }

   protected boolean mayPlace(Player pPlayer, Direction pDirection, ItemStack pItemStack, BlockPos pPos) {
      return !pPlayer.level().isOutsideBuildHeight(pPos) && pPlayer.mayUseItemAt(pPos, pDirection, pItemStack);
   }
}