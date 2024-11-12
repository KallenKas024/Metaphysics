package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRemoveMobEffectPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final MobEffect effect;

   public ClientboundRemoveMobEffectPacket(int pEntityId, MobEffect pEffect) {
      this.entityId = pEntityId;
      this.effect = pEffect;
   }

   public ClientboundRemoveMobEffectPacket(FriendlyByteBuf pBuffer) {
      this.entityId = pBuffer.readVarInt();
      this.effect = pBuffer.readById(BuiltInRegistries.MOB_EFFECT);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleRemoveMobEffect(this);
   }

   @Nullable
   public Entity getEntity(Level pLevel) {
      return pLevel.getEntity(this.entityId);
   }

   @Nullable
   public MobEffect getEffect() {
      return this.effect;
   }
}