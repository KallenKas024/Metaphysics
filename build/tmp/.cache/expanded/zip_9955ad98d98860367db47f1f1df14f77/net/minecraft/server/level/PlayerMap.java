package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;

public final class PlayerMap {
   private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<>();

   public Set<ServerPlayer> getPlayers(long pChunkPos) {
      return this.players.keySet();
   }

   public void addPlayer(long pChunkPos, ServerPlayer pPlayer, boolean pCanGenerateChunks) {
      this.players.put(pPlayer, pCanGenerateChunks);
   }

   public void removePlayer(long pChunkPos, ServerPlayer pPlayer) {
      this.players.removeBoolean(pPlayer);
   }

   public void ignorePlayer(ServerPlayer pPlayer) {
      this.players.replace(pPlayer, true);
   }

   public void unIgnorePlayer(ServerPlayer pPlayer) {
      this.players.replace(pPlayer, false);
   }

   public boolean ignoredOrUnknown(ServerPlayer pPlayer) {
      return this.players.getOrDefault(pPlayer, true);
   }

   public boolean ignored(ServerPlayer pPlayer) {
      return this.players.getBoolean(pPlayer);
   }

   public void updatePlayer(long pOldChunkPos, long pNewChunkPos, ServerPlayer pPlayer) {
   }
}