package net.minecraft.world.item.alchemy;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Defines a type of potion in the game. These are used to associate one or more effects with items such as the bottled
 * potion or the tipped arrows.
 */
public class Potion implements net.minecraftforge.common.extensions.IForgePotion {
   /** The base name for the potion type. */
   @Nullable
   private final String name;
   private final ImmutableList<MobEffectInstance> effects;

   /**
    * Attempts to find a {@code Potion} using a name. The name will be parsed as a namespaced identifier which will be
    * used to look up the potion in the potion registry.
    * @return The potion that was found in the registry.
    * @param pName The name of the potion to search for. This name will be parsed as a namespaced identifier.
    */
   public static Potion byName(String pName) {
      return BuiltInRegistries.POTION.get(ResourceLocation.tryParse(pName));
   }

   public Potion(MobEffectInstance... pEffects) {
      this((String)null, pEffects);
   }

   public Potion(@Nullable String pName, MobEffectInstance... pEffects) {
      this.name = pName;
      this.effects = ImmutableList.copyOf(pEffects);
   }

   /**
    * Gets the prefixed potion name. This is often used to create a localization key for items like the tipped arrows or
    * bottled potion.
    * @return The prefixed potion name.
    * @param pPrefix The prefix to add on to the base name.
    */
   public String getName(String pPrefix) {
      return pPrefix + (this.name == null ? BuiltInRegistries.POTION.getKey(this).getPath() : this.name);
   }

   /**
    * Gets the base effects applied by the potion.
    * @return The effects applied by the potion.
    */
   public List<MobEffectInstance> getEffects() {
      return this.effects;
   }

   /**
    * Checks if the potion contains any instant effects such as instant health or instant damage.
    * @return Whether the potion contained an instant effect.
    */
   public boolean hasInstantEffects() {
      if (!this.effects.isEmpty()) {
         for(MobEffectInstance mobeffectinstance : this.effects) {
            if (mobeffectinstance.getEffect().isInstantenous()) {
               return true;
            }
         }
      }

      return false;
   }
}
