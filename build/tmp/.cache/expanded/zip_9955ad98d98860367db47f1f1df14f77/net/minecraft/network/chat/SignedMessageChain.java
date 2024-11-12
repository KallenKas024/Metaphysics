package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class SignedMessageChain {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private SignedMessageLink nextLink;

   public SignedMessageChain(UUID pSender, UUID pSessionId) {
      this.nextLink = SignedMessageLink.root(pSender, pSessionId);
   }

   public SignedMessageChain.Encoder encoder(Signer pSigner) {
      return (p_248067_) -> {
         SignedMessageLink signedmessagelink = this.advanceLink();
         return signedmessagelink == null ? null : new MessageSignature(pSigner.sign((p_248065_) -> {
            PlayerChatMessage.updateSignature(p_248065_, signedmessagelink, p_248067_);
         }));
      };
   }

   public SignedMessageChain.Decoder decoder(ProfilePublicKey pPublicKey) {
      SignatureValidator signaturevalidator = pPublicKey.createSignatureValidator();
      return (p_248061_, p_248062_) -> {
         SignedMessageLink signedmessagelink = this.advanceLink();
         if (signedmessagelink == null) {
            throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.chain_broken"), false);
         } else if (pPublicKey.data().hasExpired()) {
            throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.expiredProfileKey"), false);
         } else {
            PlayerChatMessage playerchatmessage = new PlayerChatMessage(signedmessagelink, p_248061_, p_248062_, (Component)null, FilterMask.PASS_THROUGH);
            if (!playerchatmessage.verify(signaturevalidator)) {
               throw new SignedMessageChain.DecodeException(Component.translatable("multiplayer.disconnect.unsigned_chat"), true);
            } else {
               if (playerchatmessage.hasExpiredServer(Instant.now())) {
                  LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)p_248062_.content());
               }

               return playerchatmessage;
            }
         }
      };
   }

   @Nullable
   private SignedMessageLink advanceLink() {
      SignedMessageLink signedmessagelink = this.nextLink;
      if (signedmessagelink != null) {
         this.nextLink = signedmessagelink.advance();
      }

      return signedmessagelink;
   }

   public static class DecodeException extends ThrowingComponent {
      private final boolean shouldDisconnect;

      public DecodeException(Component pComponent, boolean pShouldDisconnect) {
         super(pComponent);
         this.shouldDisconnect = pShouldDisconnect;
      }

      public boolean shouldDisconnect() {
         return this.shouldDisconnect;
      }
   }

   @FunctionalInterface
   public interface Decoder {
      SignedMessageChain.Decoder REJECT_ALL = (p_253466_, p_253467_) -> {
         throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.missingProfileKey"), false);
      };

      static SignedMessageChain.Decoder unsigned(UUID pSender) {
         return (p_248069_, p_248070_) -> {
            return PlayerChatMessage.unsigned(pSender, p_248070_.content());
         };
      }

      PlayerChatMessage unpack(@Nullable MessageSignature pSignature, SignedMessageBody pBody) throws SignedMessageChain.DecodeException;
   }

   @FunctionalInterface
   public interface Encoder {
      SignedMessageChain.Encoder UNSIGNED = (p_250548_) -> {
         return null;
      };

      @Nullable
      MessageSignature pack(SignedMessageBody pBody);
   }
}