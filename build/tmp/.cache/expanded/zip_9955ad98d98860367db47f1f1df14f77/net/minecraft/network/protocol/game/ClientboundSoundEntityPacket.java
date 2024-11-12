package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class ClientboundSoundEntityPacket implements Packet<ClientGamePacketListener> {
   private final Holder<SoundEvent> sound;
   private final SoundSource source;
   private final int id;
   private final float volume;
   private final float pitch;
   private final long seed;

   public ClientboundSoundEntityPacket(Holder<SoundEvent> pSound, SoundSource pSource, Entity pEntity, float pVolume, float pPitch, long pSeed) {
      this.sound = pSound;
      this.source = pSource;
      this.id = pEntity.getId();
      this.volume = pVolume;
      this.pitch = pPitch;
      this.seed = pSeed;
   }

   public ClientboundSoundEntityPacket(FriendlyByteBuf pBuffer) {
      this.sound = pBuffer.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork);
      this.source = pBuffer.readEnum(SoundSource.class);
      this.id = pBuffer.readVarInt();
      this.volume = pBuffer.readFloat();
      this.pitch = pBuffer.readFloat();
      this.seed = pBuffer.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (p_263534_, p_263498_) -> {
         p_263498_.writeToNetwork(p_263534_);
      });
      pBuffer.writeEnum(this.source);
      pBuffer.writeVarInt(this.id);
      pBuffer.writeFloat(this.volume);
      pBuffer.writeFloat(this.pitch);
      pBuffer.writeLong(this.seed);
   }

   public Holder<SoundEvent> getSound() {
      return this.sound;
   }

   public SoundSource getSource() {
      return this.source;
   }

   public int getId() {
      return this.id;
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public long getSeed() {
      return this.seed;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSoundEntityEvent(this);
   }
}