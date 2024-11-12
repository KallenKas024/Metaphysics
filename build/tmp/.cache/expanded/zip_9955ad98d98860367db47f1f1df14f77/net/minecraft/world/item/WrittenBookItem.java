package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WrittenBookItem extends Item {
   public static final int TITLE_LENGTH = 16;
   public static final int TITLE_MAX_LENGTH = 32;
   public static final int PAGE_EDIT_LENGTH = 1024;
   public static final int PAGE_LENGTH = 32767;
   public static final int MAX_PAGES = 100;
   public static final int MAX_GENERATION = 2;
   public static final String TAG_TITLE = "title";
   public static final String TAG_FILTERED_TITLE = "filtered_title";
   public static final String TAG_AUTHOR = "author";
   public static final String TAG_PAGES = "pages";
   public static final String TAG_FILTERED_PAGES = "filtered_pages";
   public static final String TAG_GENERATION = "generation";
   public static final String TAG_RESOLVED = "resolved";

   public WrittenBookItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public static boolean makeSureTagIsValid(@Nullable CompoundTag pCompoundTag) {
      if (!WritableBookItem.makeSureTagIsValid(pCompoundTag)) {
         return false;
      } else if (!pCompoundTag.contains("title", 8)) {
         return false;
      } else {
         String s = pCompoundTag.getString("title");
         return s.length() > 32 ? false : pCompoundTag.contains("author", 8);
      }
   }

   /**
    * Gets the generation of the book (how many times it has been cloned)
    */
   public static int getGeneration(ItemStack pBookStack) {
      return pBookStack.getTag().getInt("generation");
   }

   /**
    * Gets the page count of the book
    */
   public static int getPageCount(ItemStack pBookSTack) {
      CompoundTag compoundtag = pBookSTack.getTag();
      return compoundtag != null ? compoundtag.getList("pages", 8).size() : 0;
   }

   /**
    * Gets the title name of the book
    */
   public Component getName(ItemStack pStack) {
      CompoundTag compoundtag = pStack.getTag();
      if (compoundtag != null) {
         String s = compoundtag.getString("title");
         if (!StringUtil.isNullOrEmpty(s)) {
            return Component.literal(s);
         }
      }

      return super.getName(pStack);
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      if (pStack.hasTag()) {
         CompoundTag compoundtag = pStack.getTag();
         String s = compoundtag.getString("author");
         if (!StringUtil.isNullOrEmpty(s)) {
            pTooltip.add(Component.translatable("book.byAuthor", s).withStyle(ChatFormatting.GRAY));
         }

         pTooltip.add(Component.translatable("book.generation." + compoundtag.getInt("generation")).withStyle(ChatFormatting.GRAY));
      }

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

   public static boolean resolveBookComponents(ItemStack pBookStack, @Nullable CommandSourceStack pResolvingSource, @Nullable Player pResolvingPlayer) {
      CompoundTag compoundtag = pBookStack.getTag();
      if (compoundtag != null && !compoundtag.getBoolean("resolved")) {
         compoundtag.putBoolean("resolved", true);
         if (!makeSureTagIsValid(compoundtag)) {
            return false;
         } else {
            ListTag listtag = compoundtag.getList("pages", 8);
            ListTag listtag1 = new ListTag();

            for(int i = 0; i < listtag.size(); ++i) {
               String s = resolvePage(pResolvingSource, pResolvingPlayer, listtag.getString(i));
               if (s.length() > 32767) {
                  return false;
               }

               listtag1.add(i, (Tag)StringTag.valueOf(s));
            }

            if (compoundtag.contains("filtered_pages", 10)) {
               CompoundTag compoundtag1 = compoundtag.getCompound("filtered_pages");
               CompoundTag compoundtag2 = new CompoundTag();

               for(String s1 : compoundtag1.getAllKeys()) {
                  String s2 = resolvePage(pResolvingSource, pResolvingPlayer, compoundtag1.getString(s1));
                  if (s2.length() > 32767) {
                     return false;
                  }

                  compoundtag2.putString(s1, s2);
               }

               compoundtag.put("filtered_pages", compoundtag2);
            }

            compoundtag.put("pages", listtag1);
            return true;
         }
      } else {
         return false;
      }
   }

   private static String resolvePage(@Nullable CommandSourceStack pResolvingSource, @Nullable Player pResolvingPlayer, String pResolvingPageContents) {
      Component component;
      try {
         component = Component.Serializer.fromJsonLenient(pResolvingPageContents);
         component = ComponentUtils.updateForEntity(pResolvingSource, component, pResolvingPlayer, 0);
      } catch (Exception exception) {
         component = Component.literal(pResolvingPageContents);
      }

      return Component.Serializer.toJson(component);
   }

   /**
    * Returns {@code true} if this item has an enchantment glint. By default, this returns
    * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
    * true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return true;
   }
}