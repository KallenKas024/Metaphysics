package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
   private final int duration;

   public ClientboundPlayerCombatEndPacket(CombatTracker pCombatTracker) {
      this(pCombatTracker.getCombatDuration());
   }

   public ClientboundPlayerCombatEndPacket(int pDuration) {
      this.duration = pDuration;
   }

   public ClientboundPlayerCombatEndPacket(FriendlyByteBuf pBuffer) {
      this.duration = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.duration);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerCombatEnd(this);
   }
}