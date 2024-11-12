package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ShieldItem extends Item implements Equipable {
   public static final int EFFECTIVE_BLOCK_DELAY = 5;
   public static final float MINIMUM_DURABILITY_DAMAGE = 3.0F;
   public static final String TAG_BASE_COLOR = "Base";

   public ShieldItem(Item.Properties pProperties) {
      super(pProperties);
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return BlockItem.getBlockEntityData(pStack) != null ? this.getDescriptionId() + "." + getColor(pStack).getName() : super.getDescriptionId(pStack);
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      BannerItem.appendHoverTextFromBannerBlockEntityTag(pStack, pTooltip);
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.BLOCK;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 72000;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      pPlayer.startUsingItem(pHand);
      return InteractionResultHolder.consume(itemstack);
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return pRepair.is(ItemTags.PLANKS) || super.isValidRepairItem(pToRepair, pRepair);
   }

   public static DyeColor getColor(ItemStack pStack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      return compoundtag != null ? DyeColor.byId(compoundtag.getInt("Base")) : DyeColor.WHITE;
   }

   /* ******************** FORGE START ******************** */

   @Override
   public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
      return net.minecraftforge.common.ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.OFFHAND;
   }
}
