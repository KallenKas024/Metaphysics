package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardSaveData;

public class ServerScoreboard extends Scoreboard {
   private final MinecraftServer server;
   private final Set<Objective> trackedObjectives = Sets.newHashSet();
   private final List<Runnable> dirtyListeners = Lists.newArrayList();

   public ServerScoreboard(MinecraftServer pServer) {
      this.server = pServer;
   }

   public void onScoreChanged(Score pScore) {
      super.onScoreChanged(pScore);
      if (this.trackedObjectives.contains(pScore.getObjective())) {
         this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, pScore.getObjective().getName(), pScore.getOwner(), pScore.getScore()));
      }

      this.setDirty();
   }

   public void onPlayerRemoved(String pScoreName) {
      super.onPlayerRemoved(pScoreName);
      this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, (String)null, pScoreName, 0));
      this.setDirty();
   }

   public void onPlayerScoreRemoved(String pScoreName, Objective pObjective) {
      super.onPlayerScoreRemoved(pScoreName, pObjective);
      if (this.trackedObjectives.contains(pObjective)) {
         this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, pObjective.getName(), pScoreName, 0));
      }

      this.setDirty();
   }

   /**
    * 0 is tab menu, 1 is sidebar, 2 is below name
    */
   public void setDisplayObjective(int pObjectiveSlot, @Nullable Objective pObjective) {
      Objective objective = this.getDisplayObjective(pObjectiveSlot);
      super.setDisplayObjective(pObjectiveSlot, pObjective);
      if (objective != pObjective && objective != null) {
         if (this.getObjectiveDisplaySlotCount(objective) > 0) {
            this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(pObjectiveSlot, pObjective));
         } else {
            this.stopTrackingObjective(objective);
         }
      }

      if (pObjective != null) {
         if (this.trackedObjectives.contains(pObjective)) {
            this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(pObjectiveSlot, pObjective));
         } else {
            this.startTrackingObjective(pObjective);
         }
      }

      this.setDirty();
   }

   public boolean addPlayerToTeam(String pPlayerName, PlayerTeam pTeam) {
      if (super.addPlayerToTeam(pPlayerName, pTeam)) {
         this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(pTeam, pPlayerName, ClientboundSetPlayerTeamPacket.Action.ADD));
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   /**
    * Removes the given username from the given ScorePlayerTeam. If the player is not on the team then an
    * IllegalStateException is thrown.
    */
   public void removePlayerFromTeam(String pUsername, PlayerTeam pPlayerTeam) {
      super.removePlayerFromTeam(pUsername, pPlayerTeam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(pPlayerTeam, pUsername, ClientboundSetPlayerTeamPacket.Action.REMOVE));
      this.setDirty();
   }

   public void onObjectiveAdded(Objective pObjective) {
      super.onObjectiveAdded(pObjective);
      this.setDirty();
   }

   public void onObjectiveChanged(Objective pObjective) {
      super.onObjectiveChanged(pObjective);
      if (this.trackedObjectives.contains(pObjective)) {
         this.server.getPlayerList().broadcastAll(new ClientboundSetObjectivePacket(pObjective, 2));
      }

      this.setDirty();
   }

   public void onObjectiveRemoved(Objective pObjective) {
      super.onObjectiveRemoved(pObjective);
      if (this.trackedObjectives.contains(pObjective)) {
         this.stopTrackingObjective(pObjective);
      }

      this.setDirty();
   }

   public void onTeamAdded(PlayerTeam pPlayerTeam) {
      super.onTeamAdded(pPlayerTeam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(pPlayerTeam, true));
      this.setDirty();
   }

   public void onTeamChanged(PlayerTeam pPlayerTeam) {
      super.onTeamChanged(pPlayerTeam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(pPlayerTeam, false));
      this.setDirty();
   }

   public void onTeamRemoved(PlayerTeam pPlayerTeam) {
      super.onTeamRemoved(pPlayerTeam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createRemovePacket(pPlayerTeam));
      this.setDirty();
   }

   public void addDirtyListener(Runnable pRunnable) {
      this.dirtyListeners.add(pRunnable);
   }

   protected void setDirty() {
      for(Runnable runnable : this.dirtyListeners) {
         runnable.run();
      }

   }

   public List<Packet<?>> getStartTrackingPackets(Objective pObjective) {
      List<Packet<?>> list = Lists.newArrayList();
      list.add(new ClientboundSetObjectivePacket(pObjective, 0));

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == pObjective) {
            list.add(new ClientboundSetDisplayObjectivePacket(i, pObjective));
         }
      }

      for(Score score : this.getPlayerScores(pObjective)) {
         list.add(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, score.getObjective().getName(), score.getOwner(), score.getScore()));
      }

      return list;
   }

   public void startTrackingObjective(Objective pObjective) {
      List<Packet<?>> list = this.getStartTrackingPackets(pObjective);

      for(ServerPlayer serverplayer : this.server.getPlayerList().getPlayers()) {
         for(Packet<?> packet : list) {
            serverplayer.connection.send(packet);
         }
      }

      this.trackedObjectives.add(pObjective);
   }

   public List<Packet<?>> getStopTrackingPackets(Objective pObjective) {
      List<Packet<?>> list = Lists.newArrayList();
      list.add(new ClientboundSetObjectivePacket(pObjective, 1));

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == pObjective) {
            list.add(new ClientboundSetDisplayObjectivePacket(i, pObjective));
         }
      }

      return list;
   }

   public void stopTrackingObjective(Objective pObjective) {
      List<Packet<?>> list = this.getStopTrackingPackets(pObjective);

      for(ServerPlayer serverplayer : this.server.getPlayerList().getPlayers()) {
         for(Packet<?> packet : list) {
            serverplayer.connection.send(packet);
         }
      }

      this.trackedObjectives.remove(pObjective);
   }

   public int getObjectiveDisplaySlotCount(Objective pObjective) {
      int i = 0;

      for(int j = 0; j < 19; ++j) {
         if (this.getDisplayObjective(j) == pObjective) {
            ++i;
         }
      }

      return i;
   }

   public ScoreboardSaveData createData() {
      ScoreboardSaveData scoreboardsavedata = new ScoreboardSaveData(this);
      this.addDirtyListener(scoreboardsavedata::setDirty);
      return scoreboardsavedata;
   }

   public ScoreboardSaveData createData(CompoundTag pTag) {
      return this.createData().load(pTag);
   }

   public static enum Method {
      CHANGE,
      REMOVE;
   }
}