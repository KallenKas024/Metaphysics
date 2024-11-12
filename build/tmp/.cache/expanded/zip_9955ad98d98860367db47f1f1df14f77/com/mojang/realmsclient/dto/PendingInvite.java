package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PendingInvite extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String invitationId;
   public String worldName;
   public String worldOwnerName;
   public String worldOwnerUuid;
   public Date date;

   public static PendingInvite parse(JsonObject pJson) {
      PendingInvite pendinginvite = new PendingInvite();

      try {
         pendinginvite.invitationId = JsonUtils.getStringOr("invitationId", pJson, "");
         pendinginvite.worldName = JsonUtils.getStringOr("worldName", pJson, "");
         pendinginvite.worldOwnerName = JsonUtils.getStringOr("worldOwnerName", pJson, "");
         pendinginvite.worldOwnerUuid = JsonUtils.getStringOr("worldOwnerUuid", pJson, "");
         pendinginvite.date = JsonUtils.getDateOr("date", pJson);
      } catch (Exception exception) {
         LOGGER.error("Could not parse PendingInvite: {}", (Object)exception.getMessage());
      }

      return pendinginvite;
   }
}