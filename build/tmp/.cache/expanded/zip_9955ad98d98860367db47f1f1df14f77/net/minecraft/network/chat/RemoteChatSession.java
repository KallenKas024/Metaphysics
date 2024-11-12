package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
   public SignedMessageValidator createMessageValidator() {
      return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator());
   }

   public SignedMessageChain.Decoder createMessageDecoder(UUID pSender) {
      return (new SignedMessageChain(pSender, this.sessionId)).decoder(this.profilePublicKey);
   }

   public RemoteChatSession.Data asData() {
      return new RemoteChatSession.Data(this.sessionId, this.profilePublicKey.data());
   }

   public boolean hasExpired() {
      return this.profilePublicKey.data().hasExpired();
   }

   public static record Data(UUID sessionId, ProfilePublicKey.Data profilePublicKey) {
      public static RemoteChatSession.Data read(FriendlyByteBuf pBuffer) {
         return new RemoteChatSession.Data(pBuffer.readUUID(), new ProfilePublicKey.Data(pBuffer));
      }

      public static void write(FriendlyByteBuf pBuffer, RemoteChatSession.Data pData) {
         pBuffer.writeUUID(pData.sessionId);
         pData.profilePublicKey.write(pBuffer);
      }

      public RemoteChatSession validate(GameProfile pProfile, SignatureValidator pValidator, Duration pDuration) throws ProfilePublicKey.ValidationException {
         return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(pValidator, pProfile.getId(), this.profilePublicKey, pDuration));
      }
   }
}