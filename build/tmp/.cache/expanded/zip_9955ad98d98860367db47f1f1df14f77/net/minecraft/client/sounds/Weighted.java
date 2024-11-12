package net.minecraft.client.sounds;

import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The Weighted interface represents an element with a weight in a weighted collection.
 * It is used to provide weighted selection and retrieval of elements.
 * 
 * @param <T> The type of the element
 */
@OnlyIn(Dist.CLIENT)
public interface Weighted<T> {
   /**
    * Retrieves the total weight of the sound events.
    * The weight is calculated as the sum of the weights of all the individual sound events.
    * <p>
    * @return The total weight of the sound events
    */
   int getWeight();

   /**
    * Retrieves a randomly selected sound from the sound events based on their weights.
    * The selection is performed using the provided random source.
    * <p>
    * @return A randomly selected sound from the sound events
    * The random source used for sound selection
    * @param pRandomSource the random source used for sound selection
    */
   T getSound(RandomSource pRandomSource);

   /**
    * Preloads the sound events into the sound engine if required.
    * This method is called to preload the sounds associated with the sound events into the sound engine, ensuring they
    * are ready for playback.
    * @param pEngine The sound engine used for sound preloading
    */
   void preloadIfRequired(SoundEngine pEngine);
}