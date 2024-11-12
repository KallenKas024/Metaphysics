package net.minecraft.client.resources.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleSoundInstance extends AbstractSoundInstance {
   public SimpleSoundInstance(SoundEvent pSoundEvent, SoundSource pSource, float pVolume, float pPitch, RandomSource pRandom, BlockPos pEntity) {
      this(pSoundEvent, pSource, pVolume, pPitch, pRandom, (double)pEntity.getX() + 0.5D, (double)pEntity.getY() + 0.5D, (double)pEntity.getZ() + 0.5D);
   }

   public static SimpleSoundInstance forUI(SoundEvent pSound, float pPitch) {
      return forUI(pSound, pPitch, 0.25F);
   }

   public static SimpleSoundInstance forUI(Holder<SoundEvent> pSoundHolder, float pPitch) {
      return forUI(pSoundHolder.value(), pPitch);
   }

   public static SimpleSoundInstance forUI(SoundEvent pSound, float pPitch, float pVolume) {
      return new SimpleSoundInstance(pSound.getLocation(), SoundSource.MASTER, pVolume, pPitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
   }

   public static SimpleSoundInstance forMusic(SoundEvent pSound) {
      return new SimpleSoundInstance(pSound.getLocation(), SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
   }

   public static SimpleSoundInstance forRecord(SoundEvent pSound, Vec3 pPos) {
      return new SimpleSoundInstance(pSound, SoundSource.RECORDS, 4.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, pPos.x, pPos.y, pPos.z);
   }

   public static SimpleSoundInstance forLocalAmbience(SoundEvent pSound, float pVolume, float pPitch) {
      return new SimpleSoundInstance(pSound.getLocation(), SoundSource.AMBIENT, pPitch, pVolume, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
   }

   public static SimpleSoundInstance forAmbientAddition(SoundEvent pSound) {
      return forLocalAmbience(pSound, 1.0F, 1.0F);
   }

   public static SimpleSoundInstance forAmbientMood(SoundEvent pSoundEvent, RandomSource pRandom, double pX, double pY, double pZ) {
      return new SimpleSoundInstance(pSoundEvent, SoundSource.AMBIENT, 1.0F, 1.0F, pRandom, false, 0, SoundInstance.Attenuation.LINEAR, pX, pY, pZ);
   }

   public SimpleSoundInstance(SoundEvent pSoundEvent, SoundSource pSource, float pVolume, float pPitch, RandomSource pRandom, double pX, double pY, double pZ) {
      this(pSoundEvent, pSource, pVolume, pPitch, pRandom, false, 0, SoundInstance.Attenuation.LINEAR, pX, pY, pZ);
   }

   private SimpleSoundInstance(SoundEvent pSoundEvent, SoundSource pSource, float pVolume, float pPitch, RandomSource pRandom, boolean pLooping, int pDelay, SoundInstance.Attenuation pAttenuation, double pX, double pY, double pZ) {
      this(pSoundEvent.getLocation(), pSource, pVolume, pPitch, pRandom, pLooping, pDelay, pAttenuation, pX, pY, pZ, false);
   }

   public SimpleSoundInstance(ResourceLocation pLocation, SoundSource pSource, float pVolume, float pPitch, RandomSource pRandom, boolean pLooping, int pDelay, SoundInstance.Attenuation pAttenuation, double pX, double pY, double pZ, boolean pRelative) {
      super(pLocation, pSource, pRandom);
      this.volume = pVolume;
      this.pitch = pPitch;
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.looping = pLooping;
      this.delay = pDelay;
      this.attenuation = pAttenuation;
      this.relative = pRelative;
   }
}