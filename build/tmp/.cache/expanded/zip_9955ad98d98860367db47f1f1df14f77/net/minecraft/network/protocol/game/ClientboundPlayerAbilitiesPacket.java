package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ClientboundPlayerAbilitiesPacket implements Packet<ClientGamePacketListener> {
   private static final int FLAG_INVULNERABLE = 1;
   private static final int FLAG_FLYING = 2;
   private static final int FLAG_CAN_FLY = 4;
   private static final int FLAG_INSTABUILD = 8;
   private final boolean invulnerable;
   private final boolean isFlying;
   private final boolean canFly;
   private final boolean instabuild;
   private final float flyingSpeed;
   private final float walkingSpeed;

   public ClientboundPlayerAbilitiesPacket(Abilities pAbilities) {
      this.invulnerable = pAbilities.invulnerable;
      this.isFlying = pAbilities.flying;
      this.canFly = pAbilities.mayfly;
      this.instabuild = pAbilities.instabuild;
      this.flyingSpeed = pAbilities.getFlyingSpeed();
      this.walkingSpeed = pAbilities.getWalkingSpeed();
   }

   public ClientboundPlayerAbilitiesPacket(FriendlyByteBuf pBuffer) {
      byte b0 = pBuffer.readByte();
      this.invulnerable = (b0 & 1) != 0;
      this.isFlying = (b0 & 2) != 0;
      this.canFly = (b0 & 4) != 0;
      this.instabuild = (b0 & 8) != 0;
      this.flyingSpeed = pBuffer.readFloat();
      this.walkingSpeed = pBuffer.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      byte b0 = 0;
      if (this.invulnerable) {
         b0 = (byte)(b0 | 1);
      }

      if (this.isFlying) {
         b0 = (byte)(b0 | 2);
      }

      if (this.canFly) {
         b0 = (byte)(b0 | 4);
      }

      if (this.instabuild) {
         b0 = (byte)(b0 | 8);
      }

      pBuffer.writeByte(b0);
      pBuffer.writeFloat(this.flyingSpeed);
      pBuffer.writeFloat(this.walkingSpeed);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerAbilities(this);
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public boolean isFlying() {
      return this.isFlying;
   }

   public boolean canFly() {
      return this.canFly;
   }

   public boolean canInstabuild() {
      return this.instabuild;
   }

   public float getFlyingSpeed() {
      return this.flyingSpeed;
   }

   public float getWalkingSpeed() {
      return this.walkingSpeed;
   }
}