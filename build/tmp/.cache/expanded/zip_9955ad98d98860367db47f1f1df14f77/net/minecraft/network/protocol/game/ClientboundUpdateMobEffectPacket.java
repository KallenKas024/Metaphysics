package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
   private static final int FLAG_AMBIENT = 1;
   private static final int FLAG_VISIBLE = 2;
   private static final int FLAG_SHOW_ICON = 4;
   private final int entityId;
   private final MobEffect effect;
   private final byte effectAmplifier;
   private final int effectDurationTicks;
   private final byte flags;
   @Nullable
   private final MobEffectInstance.FactorData factorData;

   public ClientboundUpdateMobEffectPacket(int pEntityId, MobEffectInstance pEffectInstance) {
      this.entityId = pEntityId;
      this.effect = pEffectInstance.getEffect();
      this.effectAmplifier = (byte)(pEffectInstance.getAmplifier() & 255);
      this.effectDurationTicks = pEffectInstance.getDuration();
      byte b0 = 0;
      if (pEffectInstance.isAmbient()) {
         b0 = (byte)(b0 | 1);
      }

      if (pEffectInstance.isVisible()) {
         b0 = (byte)(b0 | 2);
      }

      if (pEffectInstance.showIcon()) {
         b0 = (byte)(b0 | 4);
      }

      this.flags = b0;
      this.factorData = pEffectInstance.getFactorData().orElse((MobEffectInstance.FactorData)null);
   }

   public ClientboundUpdateMobEffectPacket(FriendlyByteBuf pBuffer) {
      this.entityId = pBuffer.readVarInt();
      this.effect = pBuffer.readById(BuiltInRegistries.MOB_EFFECT);
      this.effectAmplifier = pBuffer.readByte();
      this.effectDurationTicks = pBuffer.readVarInt();
      this.flags = pBuffer.readByte();
      this.factorData = pBuffer.readNullable((p_266628_) -> {
         return p_266628_.readWithCodec(NbtOps.INSTANCE, MobEffectInstance.FactorData.CODEC);
      });
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
      pBuffer.writeByte(this.effectAmplifier);
      pBuffer.writeVarInt(this.effectDurationTicks);
      pBuffer.writeByte(this.flags);
      pBuffer.writeNullable(this.factorData, (p_266629_, p_266630_) -> {
         p_266629_.writeWithCodec(NbtOps.INSTANCE, MobEffectInstance.FactorData.CODEC, p_266630_);
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleUpdateMobEffect(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public MobEffect getEffect() {
      return this.effect;
   }

   public byte getEffectAmplifier() {
      return this.effectAmplifier;
   }

   public int getEffectDurationTicks() {
      return this.effectDurationTicks;
   }

   public boolean isEffectVisible() {
      return (this.flags & 2) == 2;
   }

   public boolean isEffectAmbient() {
      return (this.flags & 1) == 1;
   }

   public boolean effectShowsIcon() {
      return (this.flags & 4) == 4;
   }

   @Nullable
   public MobEffectInstance.FactorData getFactorData() {
      return this.factorData;
   }
}