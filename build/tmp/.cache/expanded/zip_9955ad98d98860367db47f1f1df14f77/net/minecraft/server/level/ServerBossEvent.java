package net.minecraft.server.level;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class ServerBossEvent extends BossEvent {
   private final Set<ServerPlayer> players = Sets.newHashSet();
   private final Set<ServerPlayer> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
   private boolean visible = true;

   public ServerBossEvent(Component pName, BossEvent.BossBarColor pColor, BossEvent.BossBarOverlay pOverlay) {
      super(Mth.createInsecureUUID(), pName, pColor, pOverlay);
   }

   public void setProgress(float pProgress) {
      if (pProgress != this.progress) {
         super.setProgress(pProgress);
         this.broadcast(ClientboundBossEventPacket::createUpdateProgressPacket);
      }

   }

   public void setColor(BossEvent.BossBarColor pColor) {
      if (pColor != this.color) {
         super.setColor(pColor);
         this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
      }

   }

   public void setOverlay(BossEvent.BossBarOverlay pOverlay) {
      if (pOverlay != this.overlay) {
         super.setOverlay(pOverlay);
         this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
      }

   }

   public BossEvent setDarkenScreen(boolean pDarkenSky) {
      if (pDarkenSky != this.darkenScreen) {
         super.setDarkenScreen(pDarkenSky);
         this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
      }

      return this;
   }

   public BossEvent setPlayBossMusic(boolean pPlayEndBossMusic) {
      if (pPlayEndBossMusic != this.playBossMusic) {
         super.setPlayBossMusic(pPlayEndBossMusic);
         this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
      }

      return this;
   }

   public BossEvent setCreateWorldFog(boolean pCreateFog) {
      if (pCreateFog != this.createWorldFog) {
         super.setCreateWorldFog(pCreateFog);
         this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
      }

      return this;
   }

   public void setName(Component pName) {
      if (!Objects.equal(pName, this.name)) {
         super.setName(pName);
         this.broadcast(ClientboundBossEventPacket::createUpdateNamePacket);
      }

   }

   private void broadcast(Function<BossEvent, ClientboundBossEventPacket> pPacketGetter) {
      if (this.visible) {
         ClientboundBossEventPacket clientboundbosseventpacket = pPacketGetter.apply(this);

         for(ServerPlayer serverplayer : this.players) {
            serverplayer.connection.send(clientboundbosseventpacket);
         }
      }

   }

   /**
    * Makes the boss visible to the given player.
    */
   public void addPlayer(ServerPlayer pPlayer) {
      if (this.players.add(pPlayer) && this.visible) {
         pPlayer.connection.send(ClientboundBossEventPacket.createAddPacket(this));
      }

   }

   /**
    * Makes the boss non-visible to the given player.
    */
   public void removePlayer(ServerPlayer pPlayer) {
      if (this.players.remove(pPlayer) && this.visible) {
         pPlayer.connection.send(ClientboundBossEventPacket.createRemovePacket(this.getId()));
      }

   }

   public void removeAllPlayers() {
      if (!this.players.isEmpty()) {
         for(ServerPlayer serverplayer : Lists.newArrayList(this.players)) {
            this.removePlayer(serverplayer);
         }
      }

   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean pVisible) {
      if (pVisible != this.visible) {
         this.visible = pVisible;

         for(ServerPlayer serverplayer : this.players) {
            serverplayer.connection.send(pVisible ? ClientboundBossEventPacket.createAddPacket(this) : ClientboundBossEventPacket.createRemovePacket(this.getId()));
         }
      }

   }

   /**
    * The returned collection is unmodifiable
    */
   public Collection<ServerPlayer> getPlayers() {
      return this.unmodifiablePlayers;
   }
}