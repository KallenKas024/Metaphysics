package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import javax.annotation.Nullable;

public class ServerOpListEntry extends StoredUserEntry<GameProfile> {
   private final int level;
   private final boolean bypassesPlayerLimit;

   public ServerOpListEntry(GameProfile pUser, int pLevel, boolean pBypassesPlayerLimit) {
      super(pUser);
      this.level = pLevel;
      this.bypassesPlayerLimit = pBypassesPlayerLimit;
   }

   public ServerOpListEntry(JsonObject pEntryData) {
      super(createGameProfile(pEntryData));
      this.level = pEntryData.has("level") ? pEntryData.get("level").getAsInt() : 0;
      this.bypassesPlayerLimit = pEntryData.has("bypassesPlayerLimit") && pEntryData.get("bypassesPlayerLimit").getAsBoolean();
   }

   /**
    * Gets the permission level of the user, as defined in the "level" attribute of the ops.json file
    */
   public int getLevel() {
      return this.level;
   }

   public boolean getBypassesPlayerLimit() {
      return this.bypassesPlayerLimit;
   }

   protected void serialize(JsonObject pData) {
      if (this.getUser() != null) {
         pData.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
         pData.addProperty("name", this.getUser().getName());
         pData.addProperty("level", this.level);
         pData.addProperty("bypassesPlayerLimit", this.bypassesPlayerLimit);
      }
   }

   @Nullable
   private static GameProfile createGameProfile(JsonObject pProfileData) {
      if (pProfileData.has("uuid") && pProfileData.has("name")) {
         String s = pProfileData.get("uuid").getAsString();

         UUID uuid;
         try {
            uuid = UUID.fromString(s);
         } catch (Throwable throwable) {
            return null;
         }

         return new GameProfile(uuid, pProfileData.get("name").getAsString());
      } else {
         return null;
      }
   }
}