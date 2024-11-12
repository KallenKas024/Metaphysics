package net.minecraft.client.sounds;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The SoundEventListener interface defines a listener for sound events.
 * Classes implementing this interface can be registered as listeners to receive notifications when a sound is played.
 */
@OnlyIn(Dist.CLIENT)
public interface SoundEventListener {
   /**
    * Called when a sound is played.
    * @param pSound the sound instance being played
    * @param pAccessor the accessor for weighed sound events associated with the sound
    */
   void onPlaySound(SoundInstance pSound, WeighedSoundEvents pAccessor);
}