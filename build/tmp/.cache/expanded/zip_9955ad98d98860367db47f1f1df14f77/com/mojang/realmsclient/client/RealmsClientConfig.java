package com.mojang.realmsclient.client;

import java.net.Proxy;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsClientConfig {
   @Nullable
   private static Proxy proxy;

   @Nullable
   public static Proxy getProxy() {
      return proxy;
   }

   public static void setProxy(Proxy pProxy) {
      if (proxy == null) {
         proxy = pProxy;
      }

   }
}