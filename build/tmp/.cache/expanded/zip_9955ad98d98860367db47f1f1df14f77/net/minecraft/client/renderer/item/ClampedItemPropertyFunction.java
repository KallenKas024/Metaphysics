package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClampedItemPropertyFunction extends ItemPropertyFunction {
   /** @deprecated */
   @Deprecated
   default float call(ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
      return Mth.clamp(this.unclampedCall(pStack, pLevel, pEntity, pSeed), 0.0F, 1.0F);
   }

   float unclampedCall(ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed);
}