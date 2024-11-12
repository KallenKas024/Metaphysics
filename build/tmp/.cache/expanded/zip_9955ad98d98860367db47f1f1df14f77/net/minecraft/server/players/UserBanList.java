package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class UserBanList extends StoredUserList<GameProfile, UserBanListEntry> {
   public UserBanList(File pFile) {
      super(pFile);
   }

   protected StoredUserEntry<GameProfile> createEntry(JsonObject pEntryData) {
      return new UserBanListEntry(pEntryData);
   }

   public boolean isBanned(GameProfile pProfile) {
      return this.contains(pProfile);
   }

   public String[] getUserList() {
      return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray((p_144013_) -> {
         return new String[p_144013_];
      });
   }

   /**
    * Gets the key value for the given object
    */
   protected String getKeyForUser(GameProfile pObj) {
      return pObj.getId().toString();
   }
}