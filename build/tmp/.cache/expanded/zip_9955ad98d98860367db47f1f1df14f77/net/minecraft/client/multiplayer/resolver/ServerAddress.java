package net.minecraft.client.multiplayer.resolver;

import com.google.common.net.HostAndPort;
import com.mojang.logging.LogUtils;
import java.net.IDN;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public final class ServerAddress {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final HostAndPort hostAndPort;
   private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

   public ServerAddress(String pHost, int pPort) {
      this(HostAndPort.fromParts(pHost, pPort));
   }

   private ServerAddress(HostAndPort pHostAndPort) {
      this.hostAndPort = pHostAndPort;
   }

   public String getHost() {
      try {
         return IDN.toASCII(this.hostAndPort.getHost());
      } catch (IllegalArgumentException illegalargumentexception) {
         return "";
      }
   }

   public int getPort() {
      return this.hostAndPort.getPort();
   }

   public static ServerAddress parseString(String pIp) {
      if (pIp == null) {
         return INVALID;
      } else {
         try {
            HostAndPort hostandport = HostAndPort.fromString(pIp).withDefaultPort(25565);
            return hostandport.getHost().isEmpty() ? INVALID : new ServerAddress(hostandport);
         } catch (IllegalArgumentException illegalargumentexception) {
            LOGGER.info("Failed to parse URL {}", pIp, illegalargumentexception);
            return INVALID;
         }
      }
   }

   public static boolean isValidAddress(String pHostAndPort) {
      try {
         HostAndPort hostandport = HostAndPort.fromString(pHostAndPort);
         String s = hostandport.getHost();
         if (!s.isEmpty()) {
            IDN.toASCII(s);
            return true;
         }
      } catch (IllegalArgumentException illegalargumentexception) {
      }

      return false;
   }

   static int parsePort(String pPort) {
      try {
         return Integer.parseInt(pPort.trim());
      } catch (Exception exception) {
         return 25565;
      }
   }

   public String toString() {
      return this.hostAndPort.toString();
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof ServerAddress ? this.hostAndPort.equals(((ServerAddress)pOther).hostAndPort) : false;
      }
   }

   public int hashCode() {
      return this.hostAndPort.hashCode();
   }
}