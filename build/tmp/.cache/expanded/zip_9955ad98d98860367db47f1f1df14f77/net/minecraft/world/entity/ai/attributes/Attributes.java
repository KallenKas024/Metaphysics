package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Contains all entity attributes defined and registered by the vanilla game.
 */
public class Attributes {
   /** Handles the maximum health of an entity. */
   public static final Attribute MAX_HEALTH = register("generic.max_health", (new RangedAttribute("attribute.name.generic.max_health", 20.0D, 1.0D, 1024.0D)).setSyncable(true));
   /** Handles the range in blocks that a mob will notice and track players and other potential targets. */
   public static final Attribute FOLLOW_RANGE = register("generic.follow_range", new RangedAttribute("attribute.name.generic.follow_range", 32.0D, 0.0D, 2048.0D));
   /** Handles the reduction of horizontal knockback when damaged by attacks or projectiles. */
   public static final Attribute KNOCKBACK_RESISTANCE = register("generic.knockback_resistance", new RangedAttribute("attribute.name.generic.knockback_resistance", 0.0D, 0.0D, 1.0D));
   /** Handles the movement speed of entities. */
   public static final Attribute MOVEMENT_SPEED = register("generic.movement_speed", (new RangedAttribute("attribute.name.generic.movement_speed", (double)0.7F, 0.0D, 1024.0D)).setSyncable(true));
   /** Handles the movement speed of flying entities such as parrots and bees. */
   public static final Attribute FLYING_SPEED = register("generic.flying_speed", (new RangedAttribute("attribute.name.generic.flying_speed", (double)0.4F, 0.0D, 1024.0D)).setSyncable(true));
   /** Handles the attack damage inflicted by entities. The value of this attribute represents half hearts. */
   public static final Attribute ATTACK_DAMAGE = register("generic.attack_damage", new RangedAttribute("attribute.name.generic.attack_damage", 2.0D, 0.0D, 2048.0D));
   /** Handles additional horizontal knockback when damaging another entity. */
   public static final Attribute ATTACK_KNOCKBACK = register("generic.attack_knockback", new RangedAttribute("attribute.name.generic.attack_knockback", 0.0D, 0.0D, 5.0D));
   /**
    * Handles the cooldown rate when attacking with an item. The value represents the number of full strength attacks
    * that can be performed per second.
    */
   public static final Attribute ATTACK_SPEED = register("generic.attack_speed", (new RangedAttribute("attribute.name.generic.attack_speed", 4.0D, 0.0D, 1024.0D)).setSyncable(true));
   /** Handles the armor points for an entity. Each point represents half a chestplate of armor on the armor bar. */
   public static final Attribute ARMOR = register("generic.armor", (new RangedAttribute("attribute.name.generic.armor", 0.0D, 0.0D, 30.0D)).setSyncable(true));
   /** Handles the amount of damage mitigated by wearing armor. */
   public static final Attribute ARMOR_TOUGHNESS = register("generic.armor_toughness", (new RangedAttribute("attribute.name.generic.armor_toughness", 0.0D, 0.0D, 20.0D)).setSyncable(true));
   /**
    * Handles luck when a player generates loot from a loot table. This can impact the quality of loot and influence
    * bonus rolls.
    */
   public static final Attribute LUCK = register("generic.luck", (new RangedAttribute("attribute.name.generic.luck", 0.0D, -1024.0D, 1024.0D)).setSyncable(true));
   /** Handles the chance for a zombie to summon reinforcements when attacked. */
   public static final Attribute SPAWN_REINFORCEMENTS_CHANCE = register("zombie.spawn_reinforcements", new RangedAttribute("attribute.name.zombie.spawn_reinforcements", 0.0D, 0.0D, 1.0D));
   /** Handles the jump strength for horses. */
   public static final Attribute JUMP_STRENGTH = register("horse.jump_strength", (new RangedAttribute("attribute.name.horse.jump_strength", 0.7D, 0.0D, 2.0D)).setSyncable(true));

   /**
    * Registers a new attribute with the attribute registry.
    * @return The attribute that was registered.
    * @param pId The ID of the attribute to register.
    * @param pAttribute The attribute to register.
    */
   private static Attribute register(String pId, Attribute pAttribute) {
      return Registry.register(BuiltInRegistries.ATTRIBUTE, pId, pAttribute);
   }
}