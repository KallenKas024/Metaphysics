package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WritableBookItem extends Item {
   public WritableBookItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.is(Blocks.LECTERN)) {
         return LecternBlock.tryPlaceBook(pContext.getPlayer(), level, blockpos, blockstate, pContext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      pPlayer.openItemGui(itemstack, pHand);
      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
   }

   /**
    * Returns {@code true} if the book's NBT Tag List "pages" is valid.
    */
   public static boolean makeSureTagIsValid(@Nullable CompoundTag pCompoundTag) {
      if (pCompoundTag == null) {
         return false;
      } else if (!pCompoundTag.contains("pages", 9)) {
         return false;
      } else {
         ListTag listtag = pCompoundTag.getList("pages", 8);

         for(int i = 0; i < listtag.size(); ++i) {
            String s = listtag.getString(i);
            if (s.length() > 32767) {
               return false;
            }
         }

         return true;
      }
   }
}