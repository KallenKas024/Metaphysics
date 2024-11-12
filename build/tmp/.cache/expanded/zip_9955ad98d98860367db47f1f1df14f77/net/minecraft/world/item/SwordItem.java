package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwordItem extends TieredItem implements Vanishable {
   private final float attackDamage;
   /** Modifiers applied when the item is in the mainhand of a user. */
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public SwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties) {
      super(pTier, pProperties);
      this.attackDamage = (float)pAttackDamageModifier + pTier.getAttackDamageBonus();
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)pAttackSpeedModifier, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = builder.build();
   }

   public float getDamage() {
      return this.attackDamage;
   }

   public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      return !pPlayer.isCreative();
   }

   public float getDestroySpeed(ItemStack pStack, BlockState pState) {
      if (pState.is(Blocks.COBWEB)) {
         return 15.0F;
      } else {
         return pState.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
      }
   }

   /**
    * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
    * the damage on the stack.
    */
   public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
      pStack.hurtAndBreak(1, pAttacker, (p_43296_) -> {
         p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
      });
      return true;
   }

   /**
    * Called when a {@link net.minecraft.world.level.block.Block} is destroyed using this Item. Return {@code true} to
    * trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
      if (pState.getDestroySpeed(pLevel, pPos) != 0.0F) {
         pStack.hurtAndBreak(2, pEntityLiving, (p_43276_) -> {
            p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
         });
      }

      return true;
   }

   /**
    * Check whether this Item can harvest the given Block
    */
   public boolean isCorrectToolForDrops(BlockState pBlock) {
      return pBlock.is(Blocks.COBWEB);
   }

   /**
    * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    */
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
      return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
   }

   @Override
   public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
      return net.minecraftforge.common.ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
   }
}
