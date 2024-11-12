package net.minecraft.world.item;

import java.util.stream.Stream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils {
   public static InteractionResultHolder<ItemStack> startUsingInstantly(Level pLevel, Player pPlayer, InteractionHand pHand) {
      pPlayer.startUsingItem(pHand);
      return InteractionResultHolder.consume(pPlayer.getItemInHand(pHand));
   }

   public static ItemStack createFilledResult(ItemStack pEmptyStack, Player pPlayer, ItemStack pFilledStack, boolean pPreventDuplicates) {
      boolean flag = pPlayer.getAbilities().instabuild;
      if (pPreventDuplicates && flag) {
         if (!pPlayer.getInventory().contains(pFilledStack)) {
            pPlayer.getInventory().add(pFilledStack);
         }

         return pEmptyStack;
      } else {
         if (!flag) {
            pEmptyStack.shrink(1);
         }

         if (pEmptyStack.isEmpty()) {
            return pFilledStack;
         } else {
            if (!pPlayer.getInventory().add(pFilledStack)) {
               pPlayer.drop(pFilledStack, false);
            }

            return pEmptyStack;
         }
      }
   }

   public static ItemStack createFilledResult(ItemStack pEmptyStack, Player pPlayer, ItemStack pFilledStack) {
      return createFilledResult(pEmptyStack, pPlayer, pFilledStack, true);
   }

   public static void onContainerDestroyed(ItemEntity pItemEntity, Stream<ItemStack> pContainerContents) {
      Level level = pItemEntity.level();
      if (!level.isClientSide) {
         pContainerContents.forEach((p_289504_) -> {
            level.addFreshEntity(new ItemEntity(level, pItemEntity.getX(), pItemEntity.getY(), pItemEntity.getZ(), p_289504_));
         });
      }
   }
}