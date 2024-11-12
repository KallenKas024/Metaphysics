package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class PotionUtils {
   public static final String TAG_CUSTOM_POTION_EFFECTS = "CustomPotionEffects";
   public static final String TAG_CUSTOM_POTION_COLOR = "CustomPotionColor";
   public static final String TAG_POTION = "Potion";
   private static final int EMPTY_COLOR = 16253176;
   private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

   /**
    * Creates a list of {@code MobEffectInstance} from data on the passed {@code ItemStack} {@code CompoundTag}.
    * @param pStack The passed {@code ItemStack}
    */
   public static List<MobEffectInstance> getMobEffects(ItemStack pStack) {
      return getAllEffects(pStack.getTag());
   }

   /**
    * Creates a list of {@code MobEffectInstance} from a {@code Potion} as well as a {@code Collection} of {@code
    * MobEffectInstance}
    * @param pPotion the {@code Potion} being passed in
    * @param pEffects a collection of various {@code MobEffectInstance}
    */
   public static List<MobEffectInstance> getAllEffects(Potion pPotion, Collection<MobEffectInstance> pEffects) {
      List<MobEffectInstance> list = Lists.newArrayList();
      list.addAll(pPotion.getEffects());
      list.addAll(pEffects);
      return list;
   }

   /**
    * Creates a list of {@code MobEffectInstance}s from data on a {@code CompoundTag}.
    * @param pCompoundTag the passed {@code CompoundTag}
    */
   public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag pCompoundTag) {
      List<MobEffectInstance> list = Lists.newArrayList();
      list.addAll(getPotion(pCompoundTag).getEffects());
      getCustomEffects(pCompoundTag, list);
      return list;
   }

   /**
    * Creates a list of {@code MobEffectInstance}s from data on the passed {@code {@code ItemStack}}
    * @param pStack the passed {@code ItemStack}
    */
   public static List<MobEffectInstance> getCustomEffects(ItemStack pStack) {
      return getCustomEffects(pStack.getTag());
   }

   /**
    * Creates a list of {@code MobEffectInstance} from data on the passed {@code CompoundTag}
    * @param pCompoundTag the passed {@code CompoundTag}
    */
   public static List<MobEffectInstance> getCustomEffects(@Nullable CompoundTag pCompoundTag) {
      List<MobEffectInstance> list = Lists.newArrayList();
      getCustomEffects(pCompoundTag, list);
      return list;
   }

   /**
    * Fills a predefined list with {@code MobEffectInstance} from a {@code CompoundTag}
    * @param pCompoundTag the passed {@code CompoundTag}
    * @param pEffectList the predefined List holding {@code MobEffectInstance}
    */
   public static void getCustomEffects(@Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList) {
      if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9)) {
         ListTag listtag = pCompoundTag.getList("CustomPotionEffects", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
            if (mobeffectinstance != null) {
               pEffectList.add(mobeffectinstance);
            }
         }
      }

   }

   /**
    * Gets the integer color of an {@link net.minecraft.world.item.ItemStack} as defined by its stored potion color tag.
    * @param pStack the passed {@code ItemStack}
    */
   public static int getColor(ItemStack pStack) {
      CompoundTag compoundtag = pStack.getTag();
      if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
         return compoundtag.getInt("CustomPotionColor");
      } else {
         return getPotion(pStack) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(pStack));
      }
   }

   /**
    * Gets the integer color of a {@link net.minecraft.world.item.alchemy.Potion}
    * @param pPotion the passed {@code Potion}
    */
   public static int getColor(Potion pPotion) {
      return pPotion == Potions.EMPTY ? 16253176 : getColor(pPotion.getEffects());
   }

   /**
    * Gets the merged integer color based from a {@code Collection} of {@code MobEffectInstance}
    * @param pEffects the passed {@code Collection} of {@code MobEffectInstance}
    */
   public static int getColor(Collection<MobEffectInstance> pEffects) {
      int i = 3694022;
      if (pEffects.isEmpty()) {
         return 3694022;
      } else {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         int j = 0;

         for(MobEffectInstance mobeffectinstance : pEffects) {
            if (mobeffectinstance.isVisible()) {
               int k = mobeffectinstance.getEffect().getColor();
               int l = mobeffectinstance.getAmplifier() + 1;
               f += (float)(l * (k >> 16 & 255)) / 255.0F;
               f1 += (float)(l * (k >> 8 & 255)) / 255.0F;
               f2 += (float)(l * (k >> 0 & 255)) / 255.0F;
               j += l;
            }
         }

         if (j == 0) {
            return 0;
         } else {
            f = f / (float)j * 255.0F;
            f1 = f1 / (float)j * 255.0F;
            f2 = f2 / (float)j * 255.0F;
            return (int)f << 16 | (int)f1 << 8 | (int)f2;
         }
      }
   }

   /**
    * Attempts to get the {@link net.minecraft.world.item.alchemy.Potion Potion} from an {@link
    * net.minecraft.world.item.ItemStack ItemStack}.
    * If it fails, returns {@link net.minecraft.world.item.alchemy.Potions#EMPTY Potions#EMPTY}.
    * @param pStack the passed {@link net.minecraft.world.item.ItemStack ItemStack}
    */
   public static Potion getPotion(ItemStack pStack) {
      return getPotion(pStack.getTag());
   }

   /**
    * Attempts to get the {@link net.minecraft.world.item.alchemy.Potion Potion} from a {@link
    * net.minecraft.nbt.CompoundTag CompoundTag}.
    * If it fails, returns {@code net.minecraft.world.item.alchemy.Potions#EMPTY Potions#EMPTY}.
    * @param pCompoundTag the passed {@code net.minecraft.nbt.CompoundTag CompoundTag}
    */
   public static Potion getPotion(@Nullable CompoundTag pCompoundTag) {
      return pCompoundTag == null ? Potions.EMPTY : Potion.byName(pCompoundTag.getString("Potion"));
   }

   /**
    * Sets the {@code Potion} type to the {@code ItemStack}
    * @param pStack the passed {@code ItemStack} to apply to
    * @param pPotion the passed {@code Potion} to use to apply to the {@code ItemStack}
    */
   public static ItemStack setPotion(ItemStack pStack, Potion pPotion) {
      ResourceLocation resourcelocation = BuiltInRegistries.POTION.getKey(pPotion);
      if (pPotion == Potions.EMPTY) {
         pStack.removeTagKey("Potion");
      } else {
         pStack.getOrCreateTag().putString("Potion", resourcelocation.toString());
      }

      return pStack;
   }

   /**
    * Sets a {@code Collection} of {@code MobEffectInstance} to a provided {@code ItemStack}'s NBT
    * @param pStack the passed {@code ItemStack}
    * @param pEffects the passed {@code Collection} of {@code MobEffectInstance}
    */
   public static ItemStack setCustomEffects(ItemStack pStack, Collection<MobEffectInstance> pEffects) {
      if (pEffects.isEmpty()) {
         return pStack;
      } else {
         CompoundTag compoundtag = pStack.getOrCreateTag();
         ListTag listtag = compoundtag.getList("CustomPotionEffects", 9);

         for(MobEffectInstance mobeffectinstance : pEffects) {
            listtag.add(mobeffectinstance.save(new CompoundTag()));
         }

         compoundtag.put("CustomPotionEffects", listtag);
         return pStack;
      }
   }

   /**
    * Adds the tooltip of the {@code Potion} stored on the {@code ItemStack} along a "durationFactor"
    * @param pStack the passed {@code ItemStack}
    * @param pTooltips the passed list of current {@code Component} tooltips
    * @param pDurationFactor the passed durationFactor of the {@code Potion}
    */
   public static void addPotionTooltip(ItemStack pStack, List<Component> pTooltips, float pDurationFactor) {
      addPotionTooltip(getMobEffects(pStack), pTooltips, pDurationFactor);
   }

   public static void addPotionTooltip(List<MobEffectInstance> pEffects, List<Component> pTooltips, float pDurationFactor) {
      List<Pair<Attribute, AttributeModifier>> list = Lists.newArrayList();
      if (pEffects.isEmpty()) {
         pTooltips.add(NO_EFFECT);
      } else {
         for(MobEffectInstance mobeffectinstance : pEffects) {
            MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
            MobEffect mobeffect = mobeffectinstance.getEffect();
            Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();
            if (!map.isEmpty()) {
               for(Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                  AttributeModifier attributemodifier = entry.getValue();
                  AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                  list.add(new Pair<>(entry.getKey(), attributemodifier1));
               }
            }

            if (mobeffectinstance.getAmplifier() > 0) {
               mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
            }

            if (!mobeffectinstance.endsWithin(20)) {
               mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, pDurationFactor));
            }

            pTooltips.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
         }
      }

      if (!list.isEmpty()) {
         pTooltips.add(CommonComponents.EMPTY);
         pTooltips.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

         for(Pair<Attribute, AttributeModifier> pair : list) {
            AttributeModifier attributemodifier2 = pair.getSecond();
            double d0 = attributemodifier2.getAmount();
            double d1;
            if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
               d1 = attributemodifier2.getAmount();
            } else {
               d1 = attributemodifier2.getAmount() * 100.0D;
            }

            if (d0 > 0.0D) {
               pTooltips.add(Component.translatable("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.BLUE));
            } else if (d0 < 0.0D) {
               d1 *= -1.0D;
               pTooltips.add(Component.translatable("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.RED));
            }
         }
      }

   }
}