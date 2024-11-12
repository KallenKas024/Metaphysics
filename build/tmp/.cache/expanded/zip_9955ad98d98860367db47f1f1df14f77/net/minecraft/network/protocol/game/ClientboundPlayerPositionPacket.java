package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.RelativeMovement;

public class ClientboundPlayerPositionPacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float yRot;
   private final float xRot;
   private final Set<RelativeMovement> relativeArguments;
   private final int id;

   public ClientboundPlayerPositionPacket(double pX, double pY, double pZ, float pYRot, float pXRot, Set<RelativeMovement> pRelativeArguments, int pId) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.yRot = pYRot;
      this.xRot = pXRot;
      this.relativeArguments = pRelativeArguments;
      this.id = pId;
   }

   public ClientboundPlayerPositionPacket(FriendlyByteBuf pBuffer) {
      this.x = pBuffer.readDouble();
      this.y = pBuffer.readDouble();
      this.z = pBuffer.readDouble();
      this.yRot = pBuffer.readFloat();
      this.xRot = pBuffer.readFloat();
      this.relativeArguments = RelativeMovement.unpack(pBuffer.readUnsignedByte());
      this.id = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeFloat(this.yRot);
      pBuffer.writeFloat(this.xRot);
      pBuffer.writeByte(RelativeMovement.pack(this.relativeArguments));
      pBuffer.writeVarInt(this.id);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleMovePlayer(this);
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getYRot() {
      return this.yRot;
   }

   public float getXRot() {
      return this.xRot;
   }

   public int getId() {
      return this.id;
   }

   /**
    * Returns a set of which fields are relative. Items in this set indicate that the value is a relative change applied
    * to the player's position, rather than an exact value.
    */
   public Set<RelativeMovement> getRelativeArguments() {
      return this.relativeArguments;
   }
}