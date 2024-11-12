package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfileKeyPair;

public record LocalChatSession(UUID sessionId, ProfileKeyPair keyPair) {
   public static LocalChatSession create(ProfileKeyPair pKeyPair) {
      return new LocalChatSession(UUID.randomUUID(), pKeyPair);
   }

   public SignedMessageChain.Encoder createMessageEncoder(UUID pSender) {
      return (new SignedMessageChain(pSender, this.sessionId)).encoder(Signer.from(this.keyPair.privateKey(), "SHA256withRSA"));
   }

   public RemoteChatSession asRemote() {
      return new RemoteChatSession(this.sessionId, this.keyPair.publicKey());
   }
}