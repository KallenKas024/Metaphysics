package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiggerItem extends TieredItem implements Vanishable {
   /** Hardcoded set of blocks this tool can properly dig at full speed. Modders see instead. */
   private final TagKey<Block> blocks;
   protected final float speed;
   private final float attackDamageBaseline;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public DiggerItem(float pAttackDamageModifier, float pAttackSpeedModifier, Tier pTier, TagKey<Block> pBlocks, Item.Properties pProperties) {
      super(pTier, pProperties);
      this.blocks = pBlocks;
      this.speed = pTier.getSpeed();
      this.attackDamageBaseline = pAttackDamageModifier + pTier.getAttackDamageBonus();
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)pAttackSpeedModifier, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = builder.build();
   }

   public float getDestroySpeed(ItemStack pStack, BlockState pState) {
      return pState.is(this.blocks) ? this.speed : 1.0F;
   }

   /**
    * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
    * the damage on the stack.
    */
   public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
      pStack.hurtAndBreak(2, pAttacker, (p_41007_) -> {
         p_41007_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
      });
      return true;
   }

   /**
    * Called when a {@link net.minecraft.world.level.block.Block} is destroyed using this Item. Return {@code true} to
    * trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
      if (!pLevel.isClientSide && pState.getDestroySpeed(pLevel, pPos) != 0.0F) {
         pStack.hurtAndBreak(1, pEntityLiving, (p_40992_) -> {
            p_40992_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
         });
      }

      return true;
   }

   /**
    * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    */
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
      return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
   }

   public float getAttackDamage() {
      return this.attackDamageBaseline;
   }

   /**
    * Check whether this Item can harvest the given Block
    */
   @Deprecated // FORGE: Use stack sensitive variant below
   public boolean isCorrectToolForDrops(BlockState pBlock) {
      if (net.minecraftforge.common.TierSortingRegistry.isTierSorted(getTier())) {
         return net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(getTier(), pBlock) && pBlock.is(this.blocks);
      }
      int i = this.getTier().getLevel();
      if (i < 3 && pBlock.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
         return false;
      } else if (i < 2 && pBlock.is(BlockTags.NEEDS_IRON_TOOL)) {
         return false;
      } else {
         return i < 1 && pBlock.is(BlockTags.NEEDS_STONE_TOOL) ? false : pBlock.is(this.blocks);
      }
   }

   // FORGE START
   @Override
   public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
      return state.is(blocks) && net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(getTier(), state);
   }
}
