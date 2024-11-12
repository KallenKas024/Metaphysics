package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public abstract class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener> {
   protected final double x;
   protected final double y;
   protected final double z;
   protected final float yRot;
   protected final float xRot;
   protected final boolean onGround;
   protected final boolean hasPos;
   protected final boolean hasRot;

   protected ServerboundMovePlayerPacket(double pX, double pY, double pZ, float pYRot, float pXRot, boolean pOnGround, boolean pHasPos, boolean pHasRot) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.yRot = pYRot;
      this.xRot = pXRot;
      this.onGround = pOnGround;
      this.hasPos = pHasPos;
      this.hasRot = pHasRot;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleMovePlayer(this);
   }

   public double getX(double pDefaultValue) {
      return this.hasPos ? this.x : pDefaultValue;
   }

   public double getY(double pDefaultValue) {
      return this.hasPos ? this.y : pDefaultValue;
   }

   public double getZ(double pDefaultValue) {
      return this.hasPos ? this.z : pDefaultValue;
   }

   public float getYRot(float pDefaultValue) {
      return this.hasRot ? this.yRot : pDefaultValue;
   }

   public float getXRot(float pDefaultValue) {
      return this.hasRot ? this.xRot : pDefaultValue;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public boolean hasPosition() {
      return this.hasPos;
   }

   public boolean hasRotation() {
      return this.hasRot;
   }

   public static class Pos extends ServerboundMovePlayerPacket {
      public Pos(double pX, double pY, double pZ, boolean pOnGround) {
         super(pX, pY, pZ, 0.0F, 0.0F, pOnGround, true, false);
      }

      public static ServerboundMovePlayerPacket.Pos read(FriendlyByteBuf pBuffer) {
         double d0 = pBuffer.readDouble();
         double d1 = pBuffer.readDouble();
         double d2 = pBuffer.readDouble();
         boolean flag = pBuffer.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.Pos(d0, d1, d2, flag);
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeDouble(this.x);
         pBuffer.writeDouble(this.y);
         pBuffer.writeDouble(this.z);
         pBuffer.writeByte(this.onGround ? 1 : 0);
      }
   }

   public static class PosRot extends ServerboundMovePlayerPacket {
      public PosRot(double pX, double pY, double pZ, float pYRot, float pXRot, boolean pOnGround) {
         super(pX, pY, pZ, pYRot, pXRot, pOnGround, true, true);
      }

      public static ServerboundMovePlayerPacket.PosRot read(FriendlyByteBuf pBuffer) {
         double d0 = pBuffer.readDouble();
         double d1 = pBuffer.readDouble();
         double d2 = pBuffer.readDouble();
         float f = pBuffer.readFloat();
         float f1 = pBuffer.readFloat();
         boolean flag = pBuffer.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.PosRot(d0, d1, d2, f, f1, flag);
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
         pBuffer.writeByte(this.onGround ? 1 : 0);
      }
   }

   public static class Rot extends ServerboundMovePlayerPacket {
      public Rot(float pYRot, float pXRot, boolean pOnGround) {
         super(0.0D, 0.0D, 0.0D, pYRot, pXRot, pOnGround, false, true);
      }

      public static ServerboundMovePlayerPacket.Rot read(FriendlyByteBuf pBuffer) {
         float f = pBuffer.readFloat();
         float f1 = pBuffer.readFloat();
         boolean flag = pBuffer.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.Rot(f, f1, flag);
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeFloat(this.yRot);
         pBuffer.writeFloat(this.xRot);
         pBuffer.writeByte(this.onGround ? 1 : 0);
      }
   }

   public static class StatusOnly extends ServerboundMovePlayerPacket {
      public StatusOnly(boolean pOnGround) {
         super(0.0D, 0.0D, 0.0D, 0.0F, 0.0F, pOnGround, false, false);
      }

      public static ServerboundMovePlayerPacket.StatusOnly read(FriendlyByteBuf pBuffer) {
         boolean flag = pBuffer.readUnsignedByte() != 0;
         return new ServerboundMovePlayerPacket.StatusOnly(flag);
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeByte(this.onGround ? 1 : 0);
      }
   }
}