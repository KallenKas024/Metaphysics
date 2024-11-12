package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
   private final Optional<MobEffect> primary;
   private final Optional<MobEffect> secondary;

   public ServerboundSetBeaconPacket(Optional<MobEffect> pPrimary, Optional<MobEffect> pSecondary) {
      this.primary = pPrimary;
      this.secondary = pSecondary;
   }

   public ServerboundSetBeaconPacket(FriendlyByteBuf pBuffer) {
      this.primary = pBuffer.readOptional((p_258214_) -> {
         return p_258214_.readById(BuiltInRegistries.MOB_EFFECT);
      });
      this.secondary = pBuffer.readOptional((p_258215_) -> {
         return p_258215_.readById(BuiltInRegistries.MOB_EFFECT);
      });
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeOptional(this.primary, (p_258216_, p_258217_) -> {
         p_258216_.writeId(BuiltInRegistries.MOB_EFFECT, p_258217_);
      });
      pBuffer.writeOptional(this.secondary, (p_258218_, p_258219_) -> {
         p_258218_.writeId(BuiltInRegistries.MOB_EFFECT, p_258219_);
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleSetBeaconPacket(this);
   }

   public Optional<MobEffect> getPrimary() {
      return this.primary;
   }

   public Optional<MobEffect> getSecondary() {
      return this.secondary;
   }
}