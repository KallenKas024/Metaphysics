package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ServerboundMoveVehiclePacket implements Packet<ServerGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float yRot;
   private final float xRot;

   public ServerboundMoveVehiclePacket(Entity pVehicle) {
      this.x = pVehicle.getX();
      this.y = pVehicle.getY();
      this.z = pVehicle.getZ();
      this.yRot = pVehicle.getYRot();
      this.xRot = pVehicle.getXRot();
   }

   public ServerboundMoveVehiclePacket(FriendlyByteBuf pBuffer) {
      this.x = pBuffer.readDouble();
      this.y = pBuffer.readDouble();
      this.z = pBuffer.readDouble();
      this.yRot = pBuffer.readFloat();
      this.xRot = pBuffer.readFloat();
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
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleMoveVehicle(this);
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
}