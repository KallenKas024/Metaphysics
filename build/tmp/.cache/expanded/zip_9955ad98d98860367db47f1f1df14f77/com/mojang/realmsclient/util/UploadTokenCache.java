package com.mojang.realmsclient.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UploadTokenCache {
   private static final Long2ObjectMap<String> TOKEN_CACHE = new Long2ObjectOpenHashMap<>();

   public static String get(long pWorldId) {
      return TOKEN_CACHE.get(pWorldId);
   }

   public static void invalidate(long pWorldId) {
      TOKEN_CACHE.remove(pWorldId);
   }

   public static void put(long pWorldId, String pToken) {
      TOKEN_CACHE.put(pWorldId, pToken);
   }
}