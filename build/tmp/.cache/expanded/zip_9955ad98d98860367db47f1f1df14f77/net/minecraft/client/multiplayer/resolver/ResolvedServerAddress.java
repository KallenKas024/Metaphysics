package net.minecraft.client.multiplayer.resolver;

import java.net.InetSocketAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ResolvedServerAddress {
   String getHostName();

   String getHostIp();

   int getPort();

   InetSocketAddress asInetSocketAddress();

   static ResolvedServerAddress from(final InetSocketAddress pInetSocketAddress) {
      return new ResolvedServerAddress() {
         public String getHostName() {
            return pInetSocketAddress.getAddress().getHostName();
         }

         public String getHostIp() {
            return pInetSocketAddress.getAddress().getHostAddress();
         }

         public int getPort() {
            return pInetSocketAddress.getPort();
         }

         public InetSocketAddress asInetSocketAddress() {
            return pInetSocketAddress;
         }
      };
   }
}