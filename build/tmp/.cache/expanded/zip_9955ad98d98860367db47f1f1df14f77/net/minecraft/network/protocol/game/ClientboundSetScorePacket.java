package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;

public class ClientboundSetScorePacket implements Packet<ClientGamePacketListener> {
   private final String owner;
   @Nullable
   private final String objectiveName;
   private final int score;
   private final ServerScoreboard.Method method;

   public ClientboundSetScorePacket(ServerScoreboard.Method pMethod, @Nullable String pObjectiveName, String pOwner, int pScore) {
      if (pMethod != ServerScoreboard.Method.REMOVE && pObjectiveName == null) {
         throw new IllegalArgumentException("Need an objective name");
      } else {
         this.owner = pOwner;
         this.objectiveName = pObjectiveName;
         this.score = pScore;
         this.method = pMethod;
      }
   }

   public ClientboundSetScorePacket(FriendlyByteBuf pBuffer) {
      this.owner = pBuffer.readUtf();
      this.method = pBuffer.readEnum(ServerScoreboard.Method.class);
      String s = pBuffer.readUtf();
      this.objectiveName = Objects.equals(s, "") ? null : s;
      if (this.method != ServerScoreboard.Method.REMOVE) {
         this.score = pBuffer.readVarInt();
      } else {
         this.score = 0;
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.owner);
      pBuffer.writeEnum(this.method);
      pBuffer.writeUtf(this.objectiveName == null ? "" : this.objectiveName);
      if (this.method != ServerScoreboard.Method.REMOVE) {
         pBuffer.writeVarInt(this.score);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetScore(this);
   }

   public String getOwner() {
      return this.owner;
   }

   @Nullable
   public String getObjectiveName() {
      return this.objectiveName;
   }

   public int getScore() {
      return this.score;
   }

   public ServerScoreboard.Method getMethod() {
      return this.method;
   }
}