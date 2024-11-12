package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;

public abstract class Enchantment implements net.minecraftforge.common.extensions.IForgeEnchantment {
   /** The valid equipment slots for the enchantment. */
   private final EquipmentSlot[] slots;
   /** The rarity of the enchantment. */
   private final Enchantment.Rarity rarity;
   /**
    * The category of the enchantment. This often controls which items the enchantment can be applied to and which
    * creative tabs the enchantment will be displayed under.
    */
   public final EnchantmentCategory category;
   /** The descriptionId for the enchantment. Commonly used to create localization keys. */
   @Nullable
   protected String descriptionId;

   /**
    * Gets an {@code Enchantment} from the registry using its internal numeric Id.
    * @param pId The internal numeric id of the enchantment.
    */
   @Nullable
   public static Enchantment byId(int pId) {
      return BuiltInRegistries.ENCHANTMENT.byId(pId);
   }

   protected Enchantment(Enchantment.Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot[] pApplicableSlots) {
      this.rarity = pRarity;
      this.category = pCategory;
      this.slots = pApplicableSlots;
   }

   /**
    * Creates a new map containing all items equipped by an entity in {@linkplain #slots slots that the enchantment
    * cares about}. These items are not tested for having the enchantment.
    * @param pEntity The entity to collect equipment for.
    */
   public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity pEntity) {
      Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);

      for(EquipmentSlot equipmentslot : this.slots) {
         ItemStack itemstack = pEntity.getItemBySlot(equipmentslot);
         if (!itemstack.isEmpty()) {
            map.put(equipmentslot, itemstack);
         }
      }

      return map;
   }

   /**
    * Gets the rarity of the enchantment. This determines how rare the enchantment will be in enchantment pools such as
    * the enchanting table and loot tables.
    */
   public Enchantment.Rarity getRarity() {
      return this.rarity;
   }

   /**
    * Gets the minimum level of the enchantment under normal circumstances such as the enchanting table. This limit is
    * not strictly enforced and may be ignored through custom item NBT or other customizations.
    */
   public int getMinLevel() {
      return 1;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 1;
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pLevel) {
      return 1 + pLevel * 10;
   }

   public int getMaxCost(int pLevel) {
      return this.getMinCost(pLevel) + 5;
   }

   /**
    * Calculates the damage protection of the enchantment based on level and damage source passed.
    * @param pLevel The level of the enchantment being used.
    * @param pSource The source of the damage.
    */
   public int getDamageProtection(int pLevel, DamageSource pSource) {
      return 0;
   }

   /**
    * Calculates the additional damage that will be dealt by an item with this enchantment. This alternative to
    * calcModifierDamage is sensitive to the targets EnumCreatureAttribute.
    * @param pLevel The level of the enchantment being used.
    */
   @Deprecated // Forge: Use ItemStack aware version in IForgeEnchantment
   public float getDamageBonus(int pLevel, MobType pType) {
      return 0.0F;
   }

   /**
    * Checks if the enchantment is compatible with another enchantment.
    * @return Whether both enchantments agree that they are compatible with each other.
    * @param pOther The other enchantment to test compatibility with.
    */
   public final boolean isCompatibleWith(Enchantment pOther) {
      return this.checkCompatibility(pOther) && pOther.checkCompatibility(this);
   }

   /**
    * Determines if the enchantment passed can be applied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   protected boolean checkCompatibility(Enchantment pOther) {
      return this != pOther;
   }

   /**
    * Lazily initializes a {@code descriptionId} for an enchantment using the enchantment's registry name.
    */
   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this));
      }

      return this.descriptionId;
   }

   /**
    * Gets the description Id of the enchantment. This is commonly used to create localization keys.
    */
   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   /**
    * Gets the name of the enchantment with the level appended to the end. The game will attempt to convert the letter
    * to roman numerals.
    * @return The name of the enchantment with the level appended to the end.
    * @param pLevel The level of the enchantment.
    */
   public Component getFullname(int pLevel) {
      MutableComponent mutablecomponent = Component.translatable(this.getDescriptionId());
      if (this.isCurse()) {
         mutablecomponent.withStyle(ChatFormatting.RED);
      } else {
         mutablecomponent.withStyle(ChatFormatting.GRAY);
      }

      if (pLevel != 1 || this.getMaxLevel() != 1) {
         mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + pLevel));
      }

      return mutablecomponent;
   }

   /**
    * Determines if this enchantment can be applied to a specific ItemStack.
    * @param pStack The ItemStack to test.
    */
   public boolean canEnchant(ItemStack pStack) {
      return canApplyAtEnchantingTable(pStack);
   }

   /**
    * Called whenever a mob is damaged with an item that has this enchantment on it.
    * @param pUser The user of the enchantment.
    * @param pTarget The entity being attacked.
    * @param pLevel The level of the enchantment.
    */
   public void doPostAttack(LivingEntity pAttacker, Entity pTarget, int pLevel) {
   }

   /**
    * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be called.
    * @param pUser The target with the enchantment.
    * @param pAttacker The entity that attacked the target.
    * @param pLevel The level of the enchantment.
    */
   public void doPostHurt(LivingEntity pTarget, Entity pAttacker, int pLevel) {
   }

   /**
    * Checks if the enchantment should be considered a treasure enchantment. These enchantments can not be obtained
    * using the enchantment table. The mending enchantment is an example of a treasure enchantment.
    * @return Whether the enchantment is a treasure enchantment.
    */
   public boolean isTreasureOnly() {
      return false;
   }

   /**
    * Checks if the enchantment is considered a curse. These enchantments are treated as debuffs and can not be removed
    * from items under normal circumstances.
    * @return Whether the enchantment is a curse.
    */
   public boolean isCurse() {
      return false;
   }

   /**
    * Checks if the enchantment can be traded by NPCs like villagers.
    * @return Whether this enchantment can be traded.
    */
   public boolean isTradeable() {
      return true;
   }

   /**
    * Checks if the enchantment can be discovered by game mechanics which pull random enchantments from the enchantment
    * registry.
    * @return Whether the enchantment can be discovered.
    */
   public boolean isDiscoverable() {
      return true;
   }

   /**
    * This applies specifically to applying at the enchanting table. The other method {@link #canEnchant(ItemStack)}
    * applies for <i>all possible</i> enchantments.
    * @param stack
    * @return
    */
   public boolean canApplyAtEnchantingTable(ItemStack stack) {
      return stack.canApplyAtEnchantingTable(this);
   }

   /**
    * Is this enchantment allowed to be enchanted on books via Enchantment Table
    * @return false to disable the vanilla feature
    */
   public boolean isAllowedOnBooks() {
      return true;
   }

   public static enum Rarity {
      COMMON(10),
      UNCOMMON(5),
      RARE(2),
      VERY_RARE(1);

      private final int weight;

      private Rarity(int pWeight) {
         this.weight = pWeight;
      }

      /**
       * Retrieves the weight of Rarity.
       */
      public int getWeight() {
         return this.weight;
      }
   }
}
