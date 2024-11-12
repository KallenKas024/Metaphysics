package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class UserWhiteListEntry extends StoredUserEntry<GameProfile> {
   public UserWhiteListEntry(GameProfile pUser) {
      super(pUser);
   }

   public UserWhiteListEntry(JsonObject pEntryData) {
      super(createGameProfile(pEntryData));
   }

   protected void serialize(JsonObject pData) {
      if (this.getUser() != null) {
         pData.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
         pData.addProperty("name", this.getUser().getName());
      }
   }

   private static GameProfile createGameProfile(JsonObject pJson) {
      if (pJson.has("uuid") && pJson.has("name")) {
         String s = pJson.get("uuid").getAsString();

         UUID uuid;
         try {
            uuid = UUID.fromString(s);
         } catch (Throwable throwable) {
            return null;
         }

         return new GameProfile(uuid, pJson.get("name").getAsString());
      } else {
         return null;
      }
   }
}