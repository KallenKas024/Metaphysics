package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentHelper {
   private static final String TAG_ENCH_ID = "id";
   private static final String TAG_ENCH_LEVEL = "lvl";
   private static final float SWIFT_SNEAK_EXTRA_FACTOR = 0.15F;

   public static CompoundTag storeEnchantment(@Nullable ResourceLocation pId, int pLevel) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("id", String.valueOf((Object)pId));
      compoundtag.putShort("lvl", (short)pLevel);
      return compoundtag;
   }

   public static void setEnchantmentLevel(CompoundTag pCompoundTag, int pLevel) {
      pCompoundTag.putShort("lvl", (short)pLevel);
   }

   public static int getEnchantmentLevel(CompoundTag pCompoundTag) {
      return Mth.clamp(pCompoundTag.getInt("lvl"), 0, 255);
   }

   @Nullable
   public static ResourceLocation getEnchantmentId(CompoundTag pCompoundTag) {
      return ResourceLocation.tryParse(pCompoundTag.getString("id"));
   }

   @Nullable
   public static ResourceLocation getEnchantmentId(Enchantment pEnchantment) {
      return BuiltInRegistries.ENCHANTMENT.getKey(pEnchantment);
   }

   /** @deprecated forge: use {@link #getTagEnchantmentLevel(Enchantment, ItemStack)} or {@link ItemStack#getEnchantmentLevel(Enchantment)} */
   /**
    * Returns the level of enchantment on the ItemStack passed.
    */
   @Deprecated
   public static int getItemEnchantmentLevel(Enchantment pEnchantment, ItemStack pStack) {
      return pStack.getEnchantmentLevel(pEnchantment);
   }

   /** Gets the enchantment level from NBT. Generally should use {@link ItemStack#getEnchantmentLevel(Enchantment)} for gameplay logic */
   public static int getTagEnchantmentLevel(Enchantment pEnchantment, ItemStack pStack) {
      if (pStack.isEmpty()) {
         return 0;
      } else {
         ResourceLocation resourcelocation = getEnchantmentId(pEnchantment);
         ListTag listtag = pStack.getEnchantmentTags();

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation resourcelocation1 = getEnchantmentId(compoundtag);
            if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
               return getEnchantmentLevel(compoundtag);
            }
         }

         return 0;
      }
   }

   /**
    * Return the enchantments for the specified stack.
    */
   public static Map<Enchantment, Integer> getEnchantments(ItemStack pStack) {
      ListTag listtag = pStack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(pStack) : pStack.getEnchantmentTags();
      return deserializeEnchantments(listtag);
   }

   public static Map<Enchantment, Integer> deserializeEnchantments(ListTag pSerialized) {
      Map<Enchantment, Integer> map = Maps.newLinkedHashMap();

      for(int i = 0; i < pSerialized.size(); ++i) {
         CompoundTag compoundtag = pSerialized.getCompound(i);
         BuiltInRegistries.ENCHANTMENT.getOptional(getEnchantmentId(compoundtag)).ifPresent((p_44871_) -> {
            map.put(p_44871_, getEnchantmentLevel(compoundtag));
         });
      }

      return map;
   }

   /**
    * Set the enchantments for the specified stack.
    */
   public static void setEnchantments(Map<Enchantment, Integer> pEnchantmentsMap, ItemStack pStack) {
      ListTag listtag = new ListTag();

      for(Map.Entry<Enchantment, Integer> entry : pEnchantmentsMap.entrySet()) {
         Enchantment enchantment = entry.getKey();
         if (enchantment != null) {
            int i = entry.getValue();
            listtag.add(storeEnchantment(getEnchantmentId(enchantment), i));
            if (pStack.is(Items.ENCHANTED_BOOK)) {
               EnchantedBookItem.addEnchantment(pStack, new EnchantmentInstance(enchantment, i));
            }
         }
      }

      if (listtag.isEmpty()) {
         pStack.removeTagKey("Enchantments");
      } else if (!pStack.is(Items.ENCHANTED_BOOK)) {
         pStack.addTagElement("Enchantments", listtag);
      }

   }

   /**
    * Executes the enchantment modifier on the ItemStack passed.
    */
   private static void runIterationOnItem(EnchantmentHelper.EnchantmentVisitor pVisitor, ItemStack pStack) {
      if (!pStack.isEmpty()) {
         if (true) { // forge: redirect enchantment logic to allow non-NBT enchants
            for (Map.Entry<Enchantment, Integer> entry : pStack.getAllEnchantments().entrySet()) {
               pVisitor.accept(entry.getKey(), entry.getValue());
            }
            return;
         }

         ListTag listtag = pStack.getEnchantmentTags();

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            BuiltInRegistries.ENCHANTMENT.getOptional(getEnchantmentId(compoundtag)).ifPresent((p_182437_) -> {
               pVisitor.accept(p_182437_, getEnchantmentLevel(compoundtag));
            });
         }

      }
   }

   /**
    * Executes the enchantment modifier on the array of ItemStack passed.
    */
   private static void runIterationOnInventory(EnchantmentHelper.EnchantmentVisitor pVisitor, Iterable<ItemStack> pStacks) {
      for(ItemStack itemstack : pStacks) {
         runIterationOnItem(pVisitor, itemstack);
      }

   }

   /**
    * Returns the modifier of protection enchantments on armors equipped on player.
    */
   public static int getDamageProtection(Iterable<ItemStack> pStacks, DamageSource pSource) {
      MutableInt mutableint = new MutableInt();
      runIterationOnInventory((p_44892_, p_44893_) -> {
         mutableint.add(p_44892_.getDamageProtection(p_44893_, pSource));
      }, pStacks);
      return mutableint.intValue();
   }

   public static float getDamageBonus(ItemStack pStack, MobType pCreatureAttribute) {
      MutableFloat mutablefloat = new MutableFloat();
      runIterationOnItem((p_44887_, p_44888_) -> {
         mutablefloat.add(p_44887_.getDamageBonus(p_44888_, pCreatureAttribute, pStack));
      }, pStack);
      return mutablefloat.floatValue();
   }

   public static float getSweepingDamageRatio(LivingEntity pEntity) {
      int i = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, pEntity);
      return i > 0 ? SweepingEdgeEnchantment.getSweepingDamageRatio(i) : 0.0F;
   }

   public static void doPostHurtEffects(LivingEntity pTarget, Entity pAttacker) {
      EnchantmentHelper.EnchantmentVisitor enchantmenthelper$enchantmentvisitor = (p_44902_, p_44903_) -> {
         p_44902_.doPostHurt(pTarget, pAttacker, p_44903_);
      };
      if (pTarget != null) {
         runIterationOnInventory(enchantmenthelper$enchantmentvisitor, pTarget.getAllSlots());
      }

      if(false) // Forge: Fix MC-248272
      if (pAttacker instanceof Player) {
         runIterationOnItem(enchantmenthelper$enchantmentvisitor, pTarget.getMainHandItem());
      }

   }

   public static void doPostDamageEffects(LivingEntity pAttacker, Entity pTarget) {
      EnchantmentHelper.EnchantmentVisitor enchantmenthelper$enchantmentvisitor = (p_44829_, p_44830_) -> {
         p_44829_.doPostAttack(pAttacker, pTarget, p_44830_);
      };
      if (pAttacker != null) {
         runIterationOnInventory(enchantmenthelper$enchantmentvisitor, pAttacker.getAllSlots());
      }

      if(false) // Forge: Fix MC-248272
      if (pAttacker instanceof Player) {
         runIterationOnItem(enchantmenthelper$enchantmentvisitor, pAttacker.getMainHandItem());
      }

   }

   public static int getEnchantmentLevel(Enchantment pEnchantment, LivingEntity pEntity) {
      Iterable<ItemStack> iterable = pEnchantment.getSlotItems(pEntity).values();
      if (iterable == null) {
         return 0;
      } else {
         int i = 0;

         for(ItemStack itemstack : iterable) {
            int j = getItemEnchantmentLevel(pEnchantment, itemstack);
            if (j > i) {
               i = j;
            }
         }

         return i;
      }
   }

   public static float getSneakingSpeedBonus(LivingEntity pEntity) {
      return (float)getEnchantmentLevel(Enchantments.SWIFT_SNEAK, pEntity) * 0.15F;
   }

   /**
    * Returns the Knockback modifier of the enchantment on the players held item.
    */
   public static int getKnockbackBonus(LivingEntity pPlayer) {
      return getEnchantmentLevel(Enchantments.KNOCKBACK, pPlayer);
   }

   /**
    * Returns the fire aspect modifier of the players held item.
    */
   public static int getFireAspect(LivingEntity pPlayer) {
      return getEnchantmentLevel(Enchantments.FIRE_ASPECT, pPlayer);
   }

   public static int getRespiration(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.RESPIRATION, pEntity);
   }

   public static int getDepthStrider(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, pEntity);
   }

   public static int getBlockEfficiency(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, pEntity);
   }

   public static int getFishingLuckBonus(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.FISHING_LUCK, pStack);
   }

   public static int getFishingSpeedBonus(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.FISHING_SPEED, pStack);
   }

   public static int getMobLooting(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.MOB_LOOTING, pEntity);
   }

   public static boolean hasAquaAffinity(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, pEntity) > 0;
   }

   /**
    * Checks if the player has any armor enchanted with the frost walker enchantment.
    * @return If player has equipment with frost walker
    */
   public static boolean hasFrostWalker(LivingEntity pPlayer) {
      return getEnchantmentLevel(Enchantments.FROST_WALKER, pPlayer) > 0;
   }

   public static boolean hasSoulSpeed(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.SOUL_SPEED, pEntity) > 0;
   }

   public static boolean hasBindingCurse(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, pStack) > 0;
   }

   public static boolean hasVanishingCurse(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, pStack) > 0;
   }

   public static boolean hasSilkTouch(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.SILK_TOUCH, pStack) > 0;
   }

   public static int getLoyalty(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.LOYALTY, pStack);
   }

   public static int getRiptide(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.RIPTIDE, pStack);
   }

   public static boolean hasChanneling(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.CHANNELING, pStack) > 0;
   }

   /**
    * Gets an item with a specified enchantment from a living entity. If there are more than one valid items a random
    * one will be returned.
    */
   @Nullable
   public static Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment pTargetEnchantment, LivingEntity pEntity) {
      return getRandomItemWith(pTargetEnchantment, pEntity, (p_44941_) -> {
         return true;
      });
   }

   @Nullable
   public static Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment pEnchantment, LivingEntity pLivingEntity, Predicate<ItemStack> pStackCondition) {
      Map<EquipmentSlot, ItemStack> map = pEnchantment.getSlotItems(pLivingEntity);
      if (map.isEmpty()) {
         return null;
      } else {
         List<Map.Entry<EquipmentSlot, ItemStack>> list = Lists.newArrayList();

         for(Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
            ItemStack itemstack = entry.getValue();
            if (!itemstack.isEmpty() && getItemEnchantmentLevel(pEnchantment, itemstack) > 0 && pStackCondition.test(itemstack)) {
               list.add(entry);
            }
         }

         return list.isEmpty() ? null : list.get(pLivingEntity.getRandom().nextInt(list.size()));
      }
   }

   /**
    * Returns the enchantability of itemstack, using a separate calculation for each enchantNum (0, 1 or 2), cutting to
    * the max enchantability power of the table, which is locked to a max of 15.
    */
   public static int getEnchantmentCost(RandomSource pRandom, int pEnchantNum, int pPower, ItemStack pStack) {
      Item item = pStack.getItem();
      int i = pStack.getEnchantmentValue();
      if (i <= 0) {
         return 0;
      } else {
         if (pPower > 15) {
            pPower = 15;
         }

         int j = pRandom.nextInt(8) + 1 + (pPower >> 1) + pRandom.nextInt(pPower + 1);
         if (pEnchantNum == 0) {
            return Math.max(j / 3, 1);
         } else {
            return pEnchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, pPower * 2);
         }
      }
   }

   /**
    * Applies a random enchantment to the specified item.
    */
   public static ItemStack enchantItem(RandomSource pRandom, ItemStack pStack, int pLevel, boolean pAllowTreasure) {
      List<EnchantmentInstance> list = selectEnchantment(pRandom, pStack, pLevel, pAllowTreasure);
      boolean flag = pStack.is(Items.BOOK);
      if (flag) {
         pStack = new ItemStack(Items.ENCHANTED_BOOK);
      }

      for(EnchantmentInstance enchantmentinstance : list) {
         if (flag) {
            EnchantedBookItem.addEnchantment(pStack, enchantmentinstance);
         } else {
            pStack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
         }
      }

      return pStack;
   }

   /**
    * Create a list of random EnchantmentData (enchantments) that can be added together to the ItemStack, the 3rd
    * parameter is the total enchantability level.
    */
   public static List<EnchantmentInstance> selectEnchantment(RandomSource pRandom, ItemStack pItemStack, int pLevel, boolean pAllowTreasure) {
      List<EnchantmentInstance> list = Lists.newArrayList();
      Item item = pItemStack.getItem();
      int i = pItemStack.getEnchantmentValue();
      if (i <= 0) {
         return list;
      } else {
         pLevel += 1 + pRandom.nextInt(i / 4 + 1) + pRandom.nextInt(i / 4 + 1);
         float f = (pRandom.nextFloat() + pRandom.nextFloat() - 1.0F) * 0.15F;
         pLevel = Mth.clamp(Math.round((float)pLevel + (float)pLevel * f), 1, Integer.MAX_VALUE);
         List<EnchantmentInstance> list1 = getAvailableEnchantmentResults(pLevel, pItemStack, pAllowTreasure);
         if (!list1.isEmpty()) {
            WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);

            while(pRandom.nextInt(50) <= pLevel) {
               if (!list.isEmpty()) {
                  filterCompatibleEnchantments(list1, Util.lastOf(list));
               }

               if (list1.isEmpty()) {
                  break;
               }

               WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);
               pLevel /= 2;
            }
         }

         return list;
      }
   }

   public static void filterCompatibleEnchantments(List<EnchantmentInstance> pDataList, EnchantmentInstance pData) {
      Iterator<EnchantmentInstance> iterator = pDataList.iterator();

      while(iterator.hasNext()) {
         if (!pData.enchantment.isCompatibleWith((iterator.next()).enchantment)) {
            iterator.remove();
         }
      }

   }

   public static boolean isEnchantmentCompatible(Collection<Enchantment> pEnchantments, Enchantment pEnchantment) {
      for(Enchantment enchantment : pEnchantments) {
         if (!enchantment.isCompatibleWith(pEnchantment)) {
            return false;
         }
      }

      return true;
   }

   public static List<EnchantmentInstance> getAvailableEnchantmentResults(int pLevel, ItemStack pStack, boolean pAllowTreasure) {
      List<EnchantmentInstance> list = Lists.newArrayList();
      Item item = pStack.getItem();
      boolean flag = pStack.is(Items.BOOK);

      for(Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
         if ((!enchantment.isTreasureOnly() || pAllowTreasure) && enchantment.isDiscoverable() && (enchantment.canApplyAtEnchantingTable(pStack) || (flag && enchantment.isAllowedOnBooks()))) {
            for(int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
               if (pLevel >= enchantment.getMinCost(i) && pLevel <= enchantment.getMaxCost(i)) {
                  list.add(new EnchantmentInstance(enchantment, i));
                  break;
               }
            }
         }
      }

      return list;
   }

   @FunctionalInterface
   interface EnchantmentVisitor {
      void accept(Enchantment pEnchantment, int pLevel);
   }
}
