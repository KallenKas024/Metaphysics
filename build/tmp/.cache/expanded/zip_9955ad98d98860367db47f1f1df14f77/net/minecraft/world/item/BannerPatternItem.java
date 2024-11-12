package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternItem extends Item {
   private final TagKey<BannerPattern> bannerPattern;

   public BannerPatternItem(TagKey<BannerPattern> pBannerPattern, Item.Properties pProperties) {
      super(pProperties);
      this.bannerPattern = pBannerPattern;
   }

   public TagKey<BannerPattern> getBannerPattern() {
      return this.bannerPattern;
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      pTooltip.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
   }

   public MutableComponent getDisplayName() {
      return Component.translatable(this.getDescriptionId() + ".desc");
   }
}