package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;

@FunctionalInterface
public interface SignedMessageValidator {
   SignedMessageValidator ACCEPT_UNSIGNED = (p_252109_) -> {
      return !p_252109_.hasSignature();
   };
   SignedMessageValidator REJECT_ALL = (p_251793_) -> {
      return false;
   };

   boolean updateAndValidate(PlayerChatMessage pMessage);

   public static class KeyBased implements SignedMessageValidator {
      private final SignatureValidator validator;
      @Nullable
      private PlayerChatMessage lastMessage;
      private boolean isChainValid = true;

      public KeyBased(SignatureValidator pValidator) {
         this.validator = pValidator;
      }

      private boolean validateChain(PlayerChatMessage pMessage) {
         if (pMessage.equals(this.lastMessage)) {
            return true;
         } else {
            return this.lastMessage == null || pMessage.link().isDescendantOf(this.lastMessage.link());
         }
      }

      public boolean updateAndValidate(PlayerChatMessage pMessage) {
         this.isChainValid = this.isChainValid && pMessage.verify(this.validator) && this.validateChain(pMessage);
         if (!this.isChainValid) {
            return false;
         } else {
            this.lastMessage = pMessage;
            return true;
         }
      }
   }
}