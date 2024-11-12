package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class UserWhiteList extends StoredUserList<GameProfile, UserWhiteListEntry> {
   public UserWhiteList(File pFile) {
      super(pFile);
   }

   protected StoredUserEntry<GameProfile> createEntry(JsonObject pEntryData) {
      return new UserWhiteListEntry(pEntryData);
   }

   /**
    * Returns {@code true} if the profile is in the whitelist.
    */
   public boolean isWhiteListed(GameProfile pProfile) {
      return this.contains(pProfile);
   }

   public String[] getUserList() {
      return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray((p_144015_) -> {
         return new String[p_144015_];
      });
   }

   /**
    * Gets the key value for the given object
    */
   protected String getKeyForUser(GameProfile pObj) {
      return pObj.getId().toString();
   }
}