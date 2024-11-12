package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;

public class ClientboundSetDisplayObjectivePacket implements Packet<ClientGamePacketListener> {
   private final int slot;
   private final String objectiveName;

   public ClientboundSetDisplayObjectivePacket(int pSlot, @Nullable Objective pObjective) {
      this.slot = pSlot;
      if (pObjective == null) {
         this.objectiveName = "";
      } else {
         this.objectiveName = pObjective.getName();
      }

   }

   public ClientboundSetDisplayObjectivePacket(FriendlyByteBuf pBuffer) {
      this.slot = pBuffer.readByte();
      this.objectiveName = pBuffer.readUtf();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.slot);
      pBuffer.writeUtf(this.objectiveName);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetDisplayObjective(this);
   }

   public int getSlot() {
      return this.slot;
   }

   @Nullable
   public String getObjectiveName() {
      return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
   }
}