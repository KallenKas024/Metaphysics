package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientboundPlayerLookAtPacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final int entity;
   private final EntityAnchorArgument.Anchor fromAnchor;
   private final EntityAnchorArgument.Anchor toAnchor;
   private final boolean atEntity;

   public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor pFromAnchor, double pX, double pY, double pZ) {
      this.fromAnchor = pFromAnchor;
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.entity = 0;
      this.atEntity = false;
      this.toAnchor = null;
   }

   public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor pFromAnchor, Entity pEntity, EntityAnchorArgument.Anchor pToAnchor) {
      this.fromAnchor = pFromAnchor;
      this.entity = pEntity.getId();
      this.toAnchor = pToAnchor;
      Vec3 vec3 = pToAnchor.apply(pEntity);
      this.x = vec3.x;
      this.y = vec3.y;
      this.z = vec3.z;
      this.atEntity = true;
   }

   public ClientboundPlayerLookAtPacket(FriendlyByteBuf pBuffer) {
      this.fromAnchor = pBuffer.readEnum(EntityAnchorArgument.Anchor.class);
      this.x = pBuffer.readDouble();
      this.y = pBuffer.readDouble();
      this.z = pBuffer.readDouble();
      this.atEntity = pBuffer.readBoolean();
      if (this.atEntity) {
         this.entity = pBuffer.readVarInt();
         this.toAnchor = pBuffer.readEnum(EntityAnchorArgument.Anchor.class);
      } else {
         this.entity = 0;
         this.toAnchor = null;
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.fromAnchor);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeBoolean(this.atEntity);
      if (this.atEntity) {
         pBuffer.writeVarInt(this.entity);
         pBuffer.writeEnum(this.toAnchor);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleLookAt(this);
   }

   public EntityAnchorArgument.Anchor getFromAnchor() {
      return this.fromAnchor;
   }

   @Nullable
   public Vec3 getPosition(Level pLevel) {
      if (this.atEntity) {
         Entity entity = pLevel.getEntity(this.entity);
         return entity == null ? new Vec3(this.x, this.y, this.z) : this.toAnchor.apply(entity);
      } else {
         return new Vec3(this.x, this.y, this.z);
      }
   }
}