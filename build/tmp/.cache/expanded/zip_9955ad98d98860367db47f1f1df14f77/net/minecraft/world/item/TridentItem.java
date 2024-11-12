package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TridentItem extends Item implements Vanishable {
   public static final int THROW_THRESHOLD_TIME = 10;
   public static final float BASE_DAMAGE = 8.0F;
   public static final float SHOOT_POWER = 2.5F;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public TridentItem(Item.Properties pProperties) {
      super(pProperties);
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0D, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)-2.9F, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = builder.build();
   }

   public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      return !pPlayer.isCreative();
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.SPEAR;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 72000;
   }

   /**
    * Called when the player stops using an Item (stops holding the right mouse button).
    */
   public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
      if (pEntityLiving instanceof Player player) {
         int i = this.getUseDuration(pStack) - pTimeLeft;
         if (i >= 10) {
            int j = EnchantmentHelper.getRiptide(pStack);
            if (j <= 0 || player.isInWaterOrRain()) {
               if (!pLevel.isClientSide) {
                  pStack.hurtAndBreak(1, player, (p_43388_) -> {
                     p_43388_.broadcastBreakEvent(pEntityLiving.getUsedItemHand());
                  });
                  if (j == 0) {
                     ThrownTrident throwntrident = new ThrownTrident(pLevel, player, pStack);
                     throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + (float)j * 0.5F, 1.0F);
                     if (player.getAbilities().instabuild) {
                        throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                     }

                     pLevel.addFreshEntity(throwntrident);
                     pLevel.playSound((Player)null, throwntrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                     if (!player.getAbilities().instabuild) {
                        player.getInventory().removeItem(pStack);
                     }
                  }
               }

               player.awardStat(Stats.ITEM_USED.get(this));
               if (j > 0) {
                  float f7 = player.getYRot();
                  float f = player.getXRot();
                  float f1 = -Mth.sin(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
                  float f2 = -Mth.sin(f * ((float)Math.PI / 180F));
                  float f3 = Mth.cos(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
                  float f4 = Mth.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
                  float f5 = 3.0F * ((1.0F + (float)j) / 4.0F);
                  f1 *= f5 / f4;
                  f2 *= f5 / f4;
                  f3 *= f5 / f4;
                  player.push((double)f1, (double)f2, (double)f3);
                  player.startAutoSpinAttack(20);
                  if (player.onGround()) {
                     float f6 = 1.1999999F;
                     player.move(MoverType.SELF, new Vec3(0.0D, (double)1.1999999F, 0.0D));
                  }

                  SoundEvent soundevent;
                  if (j >= 3) {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                  } else if (j == 2) {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                  } else {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                  }

                  pLevel.playSound((Player)null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
               }

            }
         }
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1) {
         return InteractionResultHolder.fail(itemstack);
      } else if (EnchantmentHelper.getRiptide(itemstack) > 0 && !pPlayer.isInWaterOrRain()) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         pPlayer.startUsingItem(pHand);
         return InteractionResultHolder.consume(itemstack);
      }
   }

   /**
    * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
    * the damage on the stack.
    */
   public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
      pStack.hurtAndBreak(1, pAttacker, (p_43414_) -> {
         p_43414_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
      });
      return true;
   }

   /**
    * Called when a {@link net.minecraft.world.level.block.Block} is destroyed using this Item. Return {@code true} to
    * trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
      if ((double)pState.getDestroySpeed(pLevel, pPos) != 0.0D) {
         pStack.hurtAndBreak(2, pEntityLiving, (p_43385_) -> {
            p_43385_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
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

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return 1;
   }
}