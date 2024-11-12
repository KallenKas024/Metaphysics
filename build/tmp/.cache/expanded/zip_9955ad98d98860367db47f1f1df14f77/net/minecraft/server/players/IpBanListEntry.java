package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public class IpBanListEntry extends BanListEntry<String> {
   public IpBanListEntry(String pIp) {
      this(pIp, (Date)null, (String)null, (Date)null, (String)null);
   }

   public IpBanListEntry(String pIp, @Nullable Date pCreated, @Nullable String pSource, @Nullable Date pExpires, @Nullable String pReason) {
      super(pIp, pCreated, pSource, pExpires, pReason);
   }

   public Component getDisplayName() {
      return Component.literal(String.valueOf(this.getUser()));
   }

   public IpBanListEntry(JsonObject pEntryData) {
      super(createIpInfo(pEntryData), pEntryData);
   }

   private static String createIpInfo(JsonObject pJson) {
      return pJson.has("ip") ? pJson.get("ip").getAsString() : null;
   }

   protected void serialize(JsonObject pData) {
      if (this.getUser() != null) {
         pData.addProperty("ip", this.getUser());
         super.serialize(pData);
      }
   }
}