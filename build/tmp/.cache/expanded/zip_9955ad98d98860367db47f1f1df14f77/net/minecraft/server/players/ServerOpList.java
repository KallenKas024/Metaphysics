package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class ServerOpList extends StoredUserList<GameProfile, ServerOpListEntry> {
   public ServerOpList(File pFile) {
      super(pFile);
   }

   protected StoredUserEntry<GameProfile> createEntry(JsonObject pEntryData) {
      return new ServerOpListEntry(pEntryData);
   }

   public String[] getUserList() {
      return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray((p_143997_) -> {
         return new String[p_143997_];
      });
   }

   public boolean canBypassPlayerLimit(GameProfile pProfile) {
      ServerOpListEntry serveroplistentry = this.get(pProfile);
      return serveroplistentry != null ? serveroplistentry.getBypassesPlayerLimit() : false;
   }

   /**
    * Gets the key value for the given object
    */
   protected String getKeyForUser(GameProfile pObj) {
      return pObj.getId().toString();
   }
}