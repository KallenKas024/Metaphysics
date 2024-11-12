package net.minecraft.server.players;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class SleepStatus {
   private int activePlayers;
   private int sleepingPlayers;

   public boolean areEnoughSleeping(int pRequiredSleepPercentage) {
      return this.sleepingPlayers >= this.sleepersNeeded(pRequiredSleepPercentage);
   }

   public boolean areEnoughDeepSleeping(int pRequiredSleepPercentage, List<ServerPlayer> pSleepingPlayers) {
      int i = (int)pSleepingPlayers.stream().filter(Player::isSleepingLongEnough).count();
      return i >= this.sleepersNeeded(pRequiredSleepPercentage);
   }

   public int sleepersNeeded(int pRequiredSleepPercentage) {
      return Math.max(1, Mth.ceil((float)(this.activePlayers * pRequiredSleepPercentage) / 100.0F));
   }

   public void removeAllSleepers() {
      this.sleepingPlayers = 0;
   }

   public int amountSleeping() {
      return this.sleepingPlayers;
   }

   public boolean update(List<ServerPlayer> pPlayers) {
      int i = this.activePlayers;
      int j = this.sleepingPlayers;
      this.activePlayers = 0;
      this.sleepingPlayers = 0;

      for(ServerPlayer serverplayer : pPlayers) {
         if (!serverplayer.isSpectator()) {
            ++this.activePlayers;
            if (serverplayer.isSleeping()) {
               ++this.sleepingPlayers;
            }
         }
      }

      return (j > 0 || this.sleepingPlayers > 0) && (i != this.activePlayers || j != this.sleepingPlayers);
   }
}