package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;
import javax.annotation.Nullable;

public class IpBanList extends StoredUserList<String, IpBanListEntry> {
   public IpBanList(File pFile) {
      super(pFile);
   }

   protected StoredUserEntry<String> createEntry(JsonObject pEntryData) {
      return new IpBanListEntry(pEntryData);
   }

   public boolean isBanned(SocketAddress pAddress) {
      String s = this.getIpFromAddress(pAddress);
      return this.contains(s);
   }

   public boolean isBanned(String pAddress) {
      return this.contains(pAddress);
   }

   @Nullable
   public IpBanListEntry get(SocketAddress pAddress) {
      String s = this.getIpFromAddress(pAddress);
      return this.get(s);
   }

   private String getIpFromAddress(SocketAddress pAddress) {
      String s = pAddress.toString();
      if (s.contains("/")) {
         s = s.substring(s.indexOf(47) + 1);
      }

      if (s.contains(":")) {
         s = s.substring(0, s.indexOf(58));
      }

      return s;
   }
}