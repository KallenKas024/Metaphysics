package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, int sourceTypeId, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition) implements Packet<ClientGamePacketListener> {
   public ClientboundDamageEventPacket(Entity pEntity, DamageSource pDamageSource) {
      this(pEntity.getId(), pEntity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getId(pDamageSource.type()), pDamageSource.getEntity() != null ? pDamageSource.getEntity().getId() : -1, pDamageSource.getDirectEntity() != null ? pDamageSource.getDirectEntity().getId() : -1, Optional.ofNullable(pDamageSource.sourcePositionRaw()));
   }

   public ClientboundDamageEventPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readVarInt(), pBuffer.readVarInt(), readOptionalEntityId(pBuffer), readOptionalEntityId(pBuffer), pBuffer.readOptional((p_270813_) -> {
         return new Vec3(p_270813_.readDouble(), p_270813_.readDouble(), p_270813_.readDouble());
      }));
   }

   private static void writeOptionalEntityId(FriendlyByteBuf pBuffer, int pOptionalEntityId) {
      pBuffer.writeVarInt(pOptionalEntityId + 1);
   }

   private static int readOptionalEntityId(FriendlyByteBuf pBuffer) {
      return pBuffer.readVarInt() - 1;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeVarInt(this.sourceTypeId);
      writeOptionalEntityId(pBuffer, this.sourceCauseId);
      writeOptionalEntityId(pBuffer, this.sourceDirectId);
      pBuffer.writeOptional(this.sourcePosition, (p_270788_, p_270196_) -> {
         p_270788_.writeDouble(p_270196_.x());
         p_270788_.writeDouble(p_270196_.y());
         p_270788_.writeDouble(p_270196_.z());
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleDamageEvent(this);
   }

   public DamageSource getSource(Level pLevel) {
      Holder<DamageType> holder = pLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(this.sourceTypeId).get();
      if (this.sourcePosition.isPresent()) {
         return new DamageSource(holder, this.sourcePosition.get());
      } else {
         Entity entity = pLevel.getEntity(this.sourceCauseId);
         Entity entity1 = pLevel.getEntity(this.sourceDirectId);
         return new DamageSource(holder, entity1, entity);
      }
   }
}