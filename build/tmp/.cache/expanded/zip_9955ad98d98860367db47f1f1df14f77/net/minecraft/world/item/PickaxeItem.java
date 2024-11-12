package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;

public class PickaxeItem extends DiggerItem {
   public PickaxeItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties) {
      super((float)pAttackDamageModifier, pAttackSpeedModifier, pTier, BlockTags.MINEABLE_WITH_PICKAXE, pProperties);
   }

   @Override
   public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
      return net.minecraftforge.common.ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction);
   }
}
