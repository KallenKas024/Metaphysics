package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

public class EnchantedBookItem extends Item {
   public static final String TAG_STORED_ENCHANTMENTS = "StoredEnchantments";

   public EnchantedBookItem(Item.Properties pProperties) {
      super(pProperties);
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

   /**
    * Checks isDamagable and if it cannot be stacked
    */
   public boolean isEnchantable(ItemStack pStack) {
      return false;
   }

   public static ListTag getEnchantments(ItemStack pEnchantedBookStack) {
      CompoundTag compoundtag = pEnchantedBookStack.getTag();
      return compoundtag != null ? compoundtag.getList("StoredEnchantments", 10) : new ListTag();
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      ItemStack.appendEnchantmentNames(pTooltip, getEnchantments(pStack));
   }

   /**
    * Adds a stored enchantment to an enchanted book {@link net.minecraft.world.item.ItemStack}.
    */
   public static void addEnchantment(ItemStack pStack, EnchantmentInstance pInstance) {
      ListTag listtag = getEnchantments(pStack);
      boolean flag = true;
      ResourceLocation resourcelocation = EnchantmentHelper.getEnchantmentId(pInstance.enchantment);

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         ResourceLocation resourcelocation1 = EnchantmentHelper.getEnchantmentId(compoundtag);
         if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
            if (EnchantmentHelper.getEnchantmentLevel(compoundtag) < pInstance.level) {
               EnchantmentHelper.setEnchantmentLevel(compoundtag, pInstance.level);
            }

            flag = false;
            break;
         }
      }

      if (flag) {
         listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, pInstance.level));
      }

      pStack.getOrCreateTag().put("StoredEnchantments", listtag);
   }

   /**
    * Returns the ItemStack of an enchanted version of this item.
    */
   public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
      ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);
      addEnchantment(itemstack, pInstance);
      return itemstack;
   }
}