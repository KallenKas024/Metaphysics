package net.minecraft.network.protocol.handshake;

import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientIntentionPacket implements Packet<ServerHandshakePacketListener> {
   private static final int MAX_HOST_LENGTH = 255;
   private final int protocolVersion;
   private final String hostName;
   private final int port;
   private final ConnectionProtocol intention;
   private String fmlVersion = net.minecraftforge.network.NetworkConstants.NETVERSION;

   public ClientIntentionPacket(String pHostName, int pPort, ConnectionProtocol pIntention) {
      this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
      this.hostName = pHostName;
      this.port = pPort;
      this.intention = pIntention;
   }

   public ClientIntentionPacket(FriendlyByteBuf pBuffer) {
      this.protocolVersion = pBuffer.readVarInt();
      String hostName = pBuffer.readUtf(255);
      this.port = pBuffer.readUnsignedShort();
      this.intention = ConnectionProtocol.getById(pBuffer.readVarInt());
      this.fmlVersion = net.minecraftforge.network.NetworkHooks.getFMLVersion(hostName);
      this.hostName = hostName.split("\0")[0];
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.protocolVersion);
      pBuffer.writeUtf(this.hostName + "\0"+ net.minecraftforge.network.NetworkConstants.NETVERSION+"\0");
      pBuffer.writeShort(this.port);
      pBuffer.writeVarInt(this.intention.getId());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerHandshakePacketListener pHandler) {
      pHandler.handleIntention(this);
   }

   public ConnectionProtocol getIntention() {
      return this.intention;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public String getHostName() {
      return this.hostName;
   }

   public int getPort() {
      return this.port;
   }

   public String getFMLVersion() {
      return this.fmlVersion;
   }
}
