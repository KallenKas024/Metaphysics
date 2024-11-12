package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatListener {
   private final Minecraft minecraft;
   private final Deque<ChatListener.Message> delayedMessageQueue = Queues.newArrayDeque();
   private long messageDelay;
   private long previousMessageTime;

   public ChatListener(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void tick() {
      if (this.messageDelay != 0L) {
         if (Util.getMillis() >= this.previousMessageTime + this.messageDelay) {
            for(ChatListener.Message chatlistener$message = this.delayedMessageQueue.poll(); chatlistener$message != null && !chatlistener$message.accept(); chatlistener$message = this.delayedMessageQueue.poll()) {
            }
         }

      }
   }

   public void setMessageDelay(double pDelaySeconds) {
      long i = (long)(pDelaySeconds * 1000.0D);
      if (i == 0L && this.messageDelay > 0L) {
         this.delayedMessageQueue.forEach(ChatListener.Message::accept);
         this.delayedMessageQueue.clear();
      }

      this.messageDelay = i;
   }

   public void acceptNextDelayedMessage() {
      this.delayedMessageQueue.remove().accept();
   }

   public long queueSize() {
      return (long)this.delayedMessageQueue.size();
   }

   public void clearQueue() {
      this.delayedMessageQueue.forEach(ChatListener.Message::accept);
      this.delayedMessageQueue.clear();
   }

   public boolean removeFromDelayedMessageQueue(MessageSignature pSignature) {
      return this.delayedMessageQueue.removeIf((p_247887_) -> {
         return pSignature.equals(p_247887_.signature());
      });
   }

   private boolean willDelayMessages() {
      return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
   }

   private void handleMessage(@Nullable MessageSignature pSignature, BooleanSupplier pHandler) {
      if (this.willDelayMessages()) {
         this.delayedMessageQueue.add(new ChatListener.Message(pSignature, pHandler));
      } else {
         pHandler.getAsBoolean();
      }

   }

   public void handlePlayerChatMessage(PlayerChatMessage pChatMessage, GameProfile pGameProfile, ChatType.Bound pBoundChatType) {
      boolean flag = this.minecraft.options.onlyShowSecureChat().get();
      PlayerChatMessage playerchatmessage = flag ? pChatMessage.removeUnsignedContent() : pChatMessage;
      Component component = pBoundChatType.decorate(playerchatmessage.decoratedContent());
      Instant instant = Instant.now();
      this.handleMessage(pChatMessage.signature(), () -> {
         boolean flag1 = this.showMessageToPlayer(pBoundChatType, pChatMessage, component, pGameProfile, flag, instant);
         ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
         if (clientpacketlistener != null) {
            clientpacketlistener.markMessageAsProcessed(pChatMessage, flag1);
         }

         return flag1;
      });
   }

   public void handleDisguisedChatMessage(Component pMessage, ChatType.Bound pBoundChatType) {
      Instant instant = Instant.now();
      this.handleMessage((MessageSignature)null, () -> {
         Component component = pBoundChatType.decorate(pMessage);
         Component forgeComponent = net.minecraftforge.client.ForgeHooksClient.onClientChat(pBoundChatType, component, Util.NIL_UUID);
         if (forgeComponent == null) return false;
         this.minecraft.gui.getChat().addMessage(forgeComponent);
         this.narrateChatMessage(pBoundChatType, pMessage);
         this.logSystemMessage(component, instant);
         this.previousMessageTime = Util.getMillis();
         return true;
      });
   }

   private boolean showMessageToPlayer(ChatType.Bound pBoundChatType, PlayerChatMessage pChatMessage, Component pDecoratedServerContent, GameProfile pGameProfile, boolean pOnlyShowSecureChat, Instant pTimestamp) {
      ChatTrustLevel chattrustlevel = this.evaluateTrustLevel(pChatMessage, pDecoratedServerContent, pTimestamp);
      if (pOnlyShowSecureChat && chattrustlevel.isNotSecure()) {
         return false;
      } else if (!this.minecraft.isBlocked(pChatMessage.sender()) && !pChatMessage.isFullyFiltered()) {
         GuiMessageTag guimessagetag = chattrustlevel.createTag(pChatMessage);
         MessageSignature messagesignature = pChatMessage.signature();
         FilterMask filtermask = pChatMessage.filterMask();
         if (filtermask.isEmpty()) {
            Component forgeComponent = net.minecraftforge.client.ForgeHooksClient.onClientPlayerChat(pBoundChatType, pDecoratedServerContent, pChatMessage, pChatMessage.sender());
            if (forgeComponent == null) return false;
            this.minecraft.gui.getChat().addMessage(forgeComponent, messagesignature, guimessagetag);
            this.narrateChatMessage(pBoundChatType, pChatMessage.decoratedContent());
         } else {
            Component component = filtermask.applyWithFormatting(pChatMessage.signedContent());
            if (component != null) {
               Component forgeComponent = net.minecraftforge.client.ForgeHooksClient.onClientPlayerChat(pBoundChatType, pBoundChatType.decorate(component), pChatMessage, pChatMessage.sender());
               if (forgeComponent == null) return false;
               this.minecraft.gui.getChat().addMessage(forgeComponent, messagesignature, guimessagetag);
               this.narrateChatMessage(pBoundChatType, component);
            }
         }

         this.logPlayerMessage(pChatMessage, pBoundChatType, pGameProfile, chattrustlevel);
         this.previousMessageTime = Util.getMillis();
         return true;
      } else {
         return false;
      }
   }

   private void narrateChatMessage(ChatType.Bound pBoundChatType, Component pMessage) {
      this.minecraft.getNarrator().sayChat(pBoundChatType.decorateNarration(pMessage));
   }

   private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage pChatMessage, Component pDecoratedServerContent, Instant pTimestamp) {
      return this.isSenderLocalPlayer(pChatMessage.sender()) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(pChatMessage, pDecoratedServerContent, pTimestamp);
   }

   private void logPlayerMessage(PlayerChatMessage pMessage, ChatType.Bound pBoundChatType, GameProfile pGameProfile, ChatTrustLevel pTrustLevel) {
      ChatLog chatlog = this.minecraft.getReportingContext().chatLog();
      chatlog.push(LoggedChatMessage.player(pGameProfile, pMessage, pTrustLevel));
   }

   private void logSystemMessage(Component pMessage, Instant pTimestamp) {
      ChatLog chatlog = this.minecraft.getReportingContext().chatLog();
      chatlog.push(LoggedChatMessage.system(pMessage, pTimestamp));
   }

   public void handleSystemMessage(Component pMessage, boolean pIsOverlay) {
      if (!this.minecraft.options.hideMatchedNames().get() || !this.minecraft.isBlocked(this.guessChatUUID(pMessage))) {
         pMessage = net.minecraftforge.client.ForgeHooksClient.onClientSystemChat(pMessage, pIsOverlay);
         if (pMessage == null) return;
         if (pIsOverlay) {
            this.minecraft.gui.setOverlayMessage(pMessage, false);
         } else {
            this.minecraft.gui.getChat().addMessage(pMessage);
            this.logSystemMessage(pMessage, Instant.now());
         }

         this.minecraft.getNarrator().say(pMessage);
      }
   }

   private UUID guessChatUUID(Component pMessage) {
      String s = StringDecomposer.getPlainText(pMessage);
      String s1 = StringUtils.substringBetween(s, "<", ">");
      return s1 == null ? Util.NIL_UUID : this.minecraft.getPlayerSocialManager().getDiscoveredUUID(s1);
   }

   private boolean isSenderLocalPlayer(UUID pSender) {
      if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
         UUID uuid = this.minecraft.player.getGameProfile().getId();
         return uuid.equals(pSender);
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static record Message(@Nullable MessageSignature signature, BooleanSupplier handler) {
      public boolean accept() {
         return this.handler.getAsBoolean();
      }
   }
}
