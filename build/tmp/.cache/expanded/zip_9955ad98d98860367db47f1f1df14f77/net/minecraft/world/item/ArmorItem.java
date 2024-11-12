package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class ArmorItem extends Item implements Equipable {
   private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266744_) -> {
      p_266744_.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
      p_266744_.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
      p_266744_.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
      p_266744_.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
   });
   public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
      /**
       * Dispense the specified stack, play the dispense sound, and spawn particles.
       */
      protected ItemStack execute(BlockSource p_40408_, ItemStack p_40409_) {
         return ArmorItem.dispenseArmor(p_40408_, p_40409_) ? p_40409_ : super.execute(p_40408_, p_40409_);
      }
   };
   protected final ArmorItem.Type type;
   private final int defense;
   private final float toughness;
   protected final float knockbackResistance;
   protected final ArmorMaterial material;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public static boolean dispenseArmor(BlockSource pSource, ItemStack pStack) {
      BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
      List<LivingEntity> list = pSource.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(pStack)));
      if (list.isEmpty()) {
         return false;
      } else {
         LivingEntity livingentity = list.get(0);
         EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(pStack);
         ItemStack itemstack = pStack.split(1);
         livingentity.setItemSlot(equipmentslot, itemstack);
         if (livingentity instanceof Mob) {
            ((Mob)livingentity).setDropChance(equipmentslot, 2.0F);
            ((Mob)livingentity).setPersistenceRequired();
         }

         return true;
      }
   }

   public ArmorItem(ArmorMaterial pMaterial, ArmorItem.Type pType, Item.Properties pProperties) {
      super(pProperties.defaultDurability(pMaterial.getDurabilityForType(pType)));
      this.material = pMaterial;
      this.type = pType;
      this.defense = pMaterial.getDefenseForType(pType);
      this.toughness = pMaterial.getToughness();
      this.knockbackResistance = pMaterial.getKnockbackResistance();
      DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      UUID uuid = ARMOR_MODIFIER_UUID_PER_TYPE.get(pType);
      builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", (double)this.defense, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", (double)this.toughness, AttributeModifier.Operation.ADDITION));
      if (this.knockbackResistance > 0) {
         builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", (double)this.knockbackResistance, AttributeModifier.Operation.ADDITION));
      }

      this.defaultModifiers = builder.build();
   }

   public ArmorItem.Type getType() {
      return this.type;
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return this.material.getEnchantmentValue();
   }

   public ArmorMaterial getMaterial() {
      return this.material;
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return this.material.getRepairIngredient().test(pRepair) || super.isValidRepairItem(pToRepair, pRepair);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      return this.swapWithEquipmentSlot(this, pLevel, pPlayer, pHand);
   }

   /**
    * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    */
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
      return pEquipmentSlot == this.type.getSlot() ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
   }

   public int getDefense() {
      return this.defense;
   }

   public float getToughness() {
      return this.toughness;
   }

   public EquipmentSlot getEquipmentSlot() {
      return this.type.getSlot();
   }

   public SoundEvent getEquipSound() {
      return this.getMaterial().getEquipSound();
   }

   public static enum Type {
      HELMET(EquipmentSlot.HEAD, "helmet"),
      CHESTPLATE(EquipmentSlot.CHEST, "chestplate"),
      LEGGINGS(EquipmentSlot.LEGS, "leggings"),
      BOOTS(EquipmentSlot.FEET, "boots");

      private final EquipmentSlot slot;
      private final String name;

      private Type(EquipmentSlot pSlot, String pName) {
         this.slot = pSlot;
         this.name = pName;
      }

      public EquipmentSlot getSlot() {
         return this.slot;
      }

      public String getName() {
         return this.name;
      }
   }
}
