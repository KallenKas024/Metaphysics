package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsNotification {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String NOTIFICATION_UUID = "notificationUuid";
   private static final String DISMISSABLE = "dismissable";
   private static final String SEEN = "seen";
   private static final String TYPE = "type";
   private static final String VISIT_URL = "visitUrl";
   final UUID uuid;
   final boolean dismissable;
   final boolean seen;
   final String type;

   RealmsNotification(UUID pUuid, boolean pDismissable, boolean pSeen, String pType) {
      this.uuid = pUuid;
      this.dismissable = pDismissable;
      this.seen = pSeen;
      this.type = pType;
   }

   public boolean seen() {
      return this.seen;
   }

   public boolean dismissable() {
      return this.dismissable;
   }

   public UUID uuid() {
      return this.uuid;
   }

   public static List<RealmsNotification> parseList(String pJson) {
      List<RealmsNotification> list = new ArrayList<>();

      try {
         for(JsonElement jsonelement : JsonParser.parseString(pJson).getAsJsonObject().get("notifications").getAsJsonArray()) {
            list.add(parse(jsonelement.getAsJsonObject()));
         }
      } catch (Exception exception) {
         LOGGER.error("Could not parse list of RealmsNotifications", (Throwable)exception);
      }

      return list;
   }

   private static RealmsNotification parse(JsonObject pJson) {
      UUID uuid = JsonUtils.getUuidOr("notificationUuid", pJson, (UUID)null);
      if (uuid == null) {
         throw new IllegalStateException("Missing required property notificationUuid");
      } else {
         boolean flag = JsonUtils.getBooleanOr("dismissable", pJson, true);
         boolean flag1 = JsonUtils.getBooleanOr("seen", pJson, false);
         String s = JsonUtils.getRequiredString("type", pJson);
         RealmsNotification realmsnotification = new RealmsNotification(uuid, flag, flag1, s);
         return (RealmsNotification)("visitUrl".equals(s) ? RealmsNotification.VisitUrl.parse(realmsnotification, pJson) : realmsnotification);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class VisitUrl extends RealmsNotification {
      private static final String URL = "url";
      private static final String BUTTON_TEXT = "buttonText";
      private static final String MESSAGE = "message";
      private final String url;
      private final RealmsText buttonText;
      private final RealmsText message;

      private VisitUrl(RealmsNotification pNotification, String pUrl, RealmsText pButtonText, RealmsText pMessage) {
         super(pNotification.uuid, pNotification.dismissable, pNotification.seen, pNotification.type);
         this.url = pUrl;
         this.buttonText = pButtonText;
         this.message = pMessage;
      }

      public static RealmsNotification.VisitUrl parse(RealmsNotification pNotification, JsonObject pJson) {
         String s = JsonUtils.getRequiredString("url", pJson);
         RealmsText realmstext = JsonUtils.getRequired("buttonText", pJson, RealmsText::parse);
         RealmsText realmstext1 = JsonUtils.getRequired("message", pJson, RealmsText::parse);
         return new RealmsNotification.VisitUrl(pNotification, s, realmstext, realmstext1);
      }

      public Component getMessage() {
         return this.message.createComponent(Component.translatable("mco.notification.visitUrl.message.default"));
      }

      public Button buildOpenLinkButton(Screen pLastScreen) {
         Component component = this.buttonText.createComponent(Component.translatable("mco.notification.visitUrl.buttonText.default"));
         return Button.builder(component, ConfirmLinkScreen.confirmLink(this.url, pLastScreen, true)).build();
      }
   }
}