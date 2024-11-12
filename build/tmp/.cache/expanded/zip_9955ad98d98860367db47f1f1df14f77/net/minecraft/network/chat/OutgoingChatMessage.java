package net.minecraft.network.chat;

import net.minecraft.server.level.ServerPlayer;

public interface OutgoingChatMessage {
   Component content();

   void sendToPlayer(ServerPlayer pPlayer, boolean pFiltered, ChatType.Bound pBoundType);

   static OutgoingChatMessage create(PlayerChatMessage pMessage) {
      return (OutgoingChatMessage)(pMessage.isSystem() ? new OutgoingChatMessage.Disguised(pMessage.decoratedContent()) : new OutgoingChatMessage.Player(pMessage));
   }

   public static record Disguised(Component content) implements OutgoingChatMessage {
      public Component content() {
         return this.content;
      }

      public void sendToPlayer(ServerPlayer p_249237_, boolean p_249574_, ChatType.Bound p_250880_) {
         p_249237_.connection.sendDisguisedChatMessage(this.content, p_250880_);
      }
   }

   public static record Player(PlayerChatMessage message) implements OutgoingChatMessage {
      public Component content() {
         return this.message.decoratedContent();
      }

      public void sendToPlayer(ServerPlayer p_249642_, boolean p_251123_, ChatType.Bound p_251482_) {
         PlayerChatMessage playerchatmessage = this.message.filter(p_251123_);
         if (!playerchatmessage.isFullyFiltered()) {
            p_249642_.connection.sendPlayerChatMessage(playerchatmessage, p_251482_);
         }

      }
   }
}