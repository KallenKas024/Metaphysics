package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float power;
   private final List<BlockPos> toBlow;
   private final float knockbackX;
   private final float knockbackY;
   private final float knockbackZ;

   public ClientboundExplodePacket(double pX, double pY, double pZ, float pPower, List<BlockPos> pToBlow, @Nullable Vec3 pKnockback) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.power = pPower;
      this.toBlow = Lists.newArrayList(pToBlow);
      if (pKnockback != null) {
         this.knockbackX = (float)pKnockback.x;
         this.knockbackY = (float)pKnockback.y;
         this.knockbackZ = (float)pKnockback.z;
      } else {
         this.knockbackX = 0.0F;
         this.knockbackY = 0.0F;
         this.knockbackZ = 0.0F;
      }

   }

   public ClientboundExplodePacket(FriendlyByteBuf pBuffer) {
      this.x = pBuffer.readDouble();
      this.y = pBuffer.readDouble();
      this.z = pBuffer.readDouble();
      this.power = pBuffer.readFloat();
      int i = Mth.floor(this.x);
      int j = Mth.floor(this.y);
      int k = Mth.floor(this.z);
      this.toBlow = pBuffer.readList((p_178850_) -> {
         int l = p_178850_.readByte() + i;
         int i1 = p_178850_.readByte() + j;
         int j1 = p_178850_.readByte() + k;
         return new BlockPos(l, i1, j1);
      });
      this.knockbackX = pBuffer.readFloat();
      this.knockbackY = pBuffer.readFloat();
      this.knockbackZ = pBuffer.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeFloat(this.power);
      int i = Mth.floor(this.x);
      int j = Mth.floor(this.y);
      int k = Mth.floor(this.z);
      pBuffer.writeCollection(this.toBlow, (p_178855_, p_178856_) -> {
         int l = p_178856_.getX() - i;
         int i1 = p_178856_.getY() - j;
         int j1 = p_178856_.getZ() - k;
         p_178855_.writeByte(l);
         p_178855_.writeByte(i1);
         p_178855_.writeByte(j1);
      });
      pBuffer.writeFloat(this.knockbackX);
      pBuffer.writeFloat(this.knockbackY);
      pBuffer.writeFloat(this.knockbackZ);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleExplosion(this);
   }

   public float getKnockbackX() {
      return this.knockbackX;
   }

   public float getKnockbackY() {
      return this.knockbackY;
   }

   public float getKnockbackZ() {
      return this.knockbackZ;
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

   public float getPower() {
      return this.power;
   }

   public List<BlockPos> getToBlow() {
      return this.toBlow;
   }
}