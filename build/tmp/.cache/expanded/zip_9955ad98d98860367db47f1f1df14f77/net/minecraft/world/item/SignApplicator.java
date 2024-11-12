package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

public interface SignApplicator {
   boolean tryApplyToSign(Level pLevel, SignBlockEntity pSign, boolean pIsFront, Player pPlayer);

   default boolean canApplyToSign(SignText pText, Player pPlayer) {
      return pText.hasMessage(pPlayer);
   }
}