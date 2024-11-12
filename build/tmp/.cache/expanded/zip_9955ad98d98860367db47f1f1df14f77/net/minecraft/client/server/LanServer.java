package net.minecraft.client.server;

import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanServer {
   private final String motd;
   private final String address;
   private long pingTime;

   public LanServer(String pMotd, String pAddress) {
      this.motd = pMotd;
      this.address = pAddress;
      this.pingTime = Util.getMillis();
   }

   public String getMotd() {
      return this.motd;
   }

   public String getAddress() {
      return this.address;
   }

   /**
    * Updates the time this LanServer was last seen.
    */
   public void updatePingTime() {
      this.pingTime = Util.getMillis();
   }
}