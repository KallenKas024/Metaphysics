package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

public class FireworkStarItem extends Item {
   public FireworkStarItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      CompoundTag compoundtag = pStack.getTagElement("Explosion");
      if (compoundtag != null) {
         appendHoverText(compoundtag, pTooltip);
      }

   }

   public static void appendHoverText(CompoundTag pCompound, List<Component> pTooltipComponents) {
      FireworkRocketItem.Shape fireworkrocketitem$shape = FireworkRocketItem.Shape.getShape(pCompound);
      pTooltipComponents.add(Component.translatable("item.minecraft.firework_star.shape." + fireworkrocketitem$shape.getName()).withStyle(ChatFormatting.GRAY));
      int[] aint = pCompound.getIntArray("Colors");
      if (aint.length > 0) {
         pTooltipComponents.add(appendColors(Component.empty().withStyle(ChatFormatting.GRAY), aint));
      }

      int[] aint1 = pCompound.getIntArray("FadeColors");
      if (aint1.length > 0) {
         pTooltipComponents.add(appendColors(Component.translatable("item.minecraft.firework_star.fade_to").append(CommonComponents.SPACE).withStyle(ChatFormatting.GRAY), aint1));
      }

      if (pCompound.getBoolean("Trail")) {
         pTooltipComponents.add(Component.translatable("item.minecraft.firework_star.trail").withStyle(ChatFormatting.GRAY));
      }

      if (pCompound.getBoolean("Flicker")) {
         pTooltipComponents.add(Component.translatable("item.minecraft.firework_star.flicker").withStyle(ChatFormatting.GRAY));
      }

   }

   private static Component appendColors(MutableComponent pTooltipComponent, int[] pColors) {
      for(int i = 0; i < pColors.length; ++i) {
         if (i > 0) {
            pTooltipComponent.append(", ");
         }

         pTooltipComponent.append(getColorName(pColors[i]));
      }

      return pTooltipComponent;
   }

   private static Component getColorName(int pColor) {
      DyeColor dyecolor = DyeColor.byFireworkColor(pColor);
      return dyecolor == null ? Component.translatable("item.minecraft.firework_star.custom_color") : Component.translatable("item.minecraft.firework_star." + dyecolor.getName());
   }
}
