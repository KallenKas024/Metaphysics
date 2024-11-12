package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatTrustLevel implements StringRepresentable {
   SECURE("secure"),
   MODIFIED("modified"),
   NOT_SECURE("not_secure");

   public static final Codec<ChatTrustLevel> CODEC = StringRepresentable.fromEnum(ChatTrustLevel::values);
   private final String serializedName;

   private ChatTrustLevel(String pSerializedName) {
      this.serializedName = pSerializedName;
   }

   public static ChatTrustLevel evaluate(PlayerChatMessage pChatMessage, Component pDecoratedServerContent, Instant pTimestamp) {
      if (pChatMessage.hasSignature() && !pChatMessage.hasExpiredClient(pTimestamp)) {
         return isModified(pChatMessage, pDecoratedServerContent) ? MODIFIED : SECURE;
      } else {
         return NOT_SECURE;
      }
   }

   private static boolean isModified(PlayerChatMessage pChatMessage, Component pDecoratedServerContent) {
      if (!pDecoratedServerContent.getString().contains(pChatMessage.signedContent())) {
         return true;
      } else {
         Component component = pChatMessage.unsignedContent();
         return component == null ? false : containsModifiedStyle(component);
      }
   }

   private static boolean containsModifiedStyle(Component pChatMessage) {
      return pChatMessage.visit((p_251711_, p_250844_) -> {
         return isModifiedStyle(p_251711_) ? Optional.of(true) : Optional.empty();
      }, Style.EMPTY).orElse(false);
   }

   private static boolean isModifiedStyle(Style pStyle) {
      return !pStyle.getFont().equals(Style.DEFAULT_FONT);
   }

   public boolean isNotSecure() {
      return this == NOT_SECURE;
   }

   @Nullable
   public GuiMessageTag createTag(PlayerChatMessage pChatMessage) {
      GuiMessageTag guimessagetag;
      switch (this) {
         case MODIFIED:
            guimessagetag = GuiMessageTag.chatModified(pChatMessage.signedContent());
            break;
         case NOT_SECURE:
            guimessagetag = GuiMessageTag.chatNotSecure();
            break;
         default:
            guimessagetag = null;
      }

      return guimessagetag;
   }

   public String getSerializedName() {
      return this.serializedName;
   }
}