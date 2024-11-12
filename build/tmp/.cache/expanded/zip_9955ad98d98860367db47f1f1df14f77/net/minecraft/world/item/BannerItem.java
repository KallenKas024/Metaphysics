package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;

public class BannerItem extends StandingAndWallBlockItem {
   private static final String PATTERN_PREFIX = "block.minecraft.banner.";

   public BannerItem(Block pBlock, Block pWallBlock, Item.Properties pProperties) {
      super(pBlock, pWallBlock, pProperties, Direction.DOWN);
      Validate.isInstanceOf(AbstractBannerBlock.class, pBlock);
      Validate.isInstanceOf(AbstractBannerBlock.class, pWallBlock);
   }

   public static void appendHoverTextFromBannerBlockEntityTag(ItemStack pStack, List<Component> pTooltipComponents) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      if (compoundtag != null && compoundtag.contains("Patterns")) {
         ListTag listtag = compoundtag.getList("Patterns", 10);

         for(int i = 0; i < listtag.size() && i < 6; ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            DyeColor dyecolor = DyeColor.byId(compoundtag1.getInt("Color"));
            Holder<BannerPattern> holder = BannerPattern.byHash(compoundtag1.getString("Pattern"));
            if (holder != null) {
               holder.unwrapKey().map((p_220002_) -> {
                  return p_220002_.location().toShortLanguageKey();
               }).ifPresent((p_220006_) -> {
                  net.minecraft.resources.ResourceLocation fileLoc = new net.minecraft.resources.ResourceLocation(p_220006_);
                  pTooltipComponents.add(Component.translatable("block." + fileLoc.getNamespace() + ".banner." + fileLoc.getPath() + "." + dyecolor.getName()).withStyle(ChatFormatting.GRAY));
               });
            }
         }

      }
   }

   public DyeColor getColor() {
      return ((AbstractBannerBlock)this.getBlock()).getColor();
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      appendHoverTextFromBannerBlockEntityTag(pStack, pTooltip);
   }
}
