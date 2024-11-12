package net.minecraft.util.profiling.jfr;

import net.minecraft.server.MinecraftServer;

public enum Environment {
   CLIENT("client"),
   SERVER("server");

   private final String description;

   private Environment(String pDescription) {
      this.description = pDescription;
   }

   public static Environment from(MinecraftServer pServer) {
      return pServer.isDedicatedServer() ? SERVER : CLIENT;
   }

   public String getDescription() {
      return this.description;
   }
}