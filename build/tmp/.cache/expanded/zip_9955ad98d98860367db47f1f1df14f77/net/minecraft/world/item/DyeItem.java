package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class DyeItem extends Item implements SignApplicator {
   private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
   private final DyeColor dyeColor;

   public DyeItem(DyeColor pDyeColor, Item.Properties pProperties) {
      super(pProperties);
      this.dyeColor = pDyeColor;
      ITEM_BY_COLOR.put(pDyeColor, this);
   }

   /**
    * Try interacting with given entity. Return {@code InteractionResult.PASS} if nothing should happen.
    */
   public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pTarget, InteractionHand pHand) {
      if (pTarget instanceof Sheep sheep) {
         if (sheep.isAlive() && !sheep.isSheared() && sheep.getColor() != this.dyeColor) {
            sheep.level().playSound(pPlayer, sheep, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!pPlayer.level().isClientSide) {
               sheep.setColor(this.dyeColor);
               pStack.shrink(1);
            }

            return InteractionResult.sidedSuccess(pPlayer.level().isClientSide);
         }
      }

      return InteractionResult.PASS;
   }

   public DyeColor getDyeColor() {
      return this.dyeColor;
   }

   public static DyeItem byColor(DyeColor pColor) {
      return ITEM_BY_COLOR.get(pColor);
   }

   public boolean tryApplyToSign(Level pLevel, SignBlockEntity pSign, boolean pIsFront, Player pPlayer) {
      if (pSign.updateText((p_277649_) -> {
         return p_277649_.setColor(this.getDyeColor());
      }, pIsFront)) {
         pLevel.playSound((Player)null, pSign.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}