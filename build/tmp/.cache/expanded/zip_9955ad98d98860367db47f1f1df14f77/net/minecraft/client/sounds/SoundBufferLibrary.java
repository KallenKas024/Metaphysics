package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The {@linkplain SoundBufferLibrary} class provides a cache containing instances of {@linkplain SoundBuffer} and
 * {@linkplain AudioStream} for use in Minecraft sound handling.
 */
@OnlyIn(Dist.CLIENT)
public class SoundBufferLibrary {
   /** The {@linkplain ResourceProvider} used for loading sound resources. */
   private final ResourceProvider resourceManager;
   private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

   public SoundBufferLibrary(ResourceProvider pResourceManager) {
      this.resourceManager = pResourceManager;
   }

   /**
    * {@return Returns a {@linkplain CompletableFuture} containing the complete {@linkplain SoundBuffer}. The
    * {@linkplain SoundBuffer} is loaded asynchronously and cached.}
    * @param pSoundID the {@linkplain ResourceLocation} of the sound
    */
   public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation pSoundID) {
      return this.cache.computeIfAbsent(pSoundID, (p_120208_) -> {
         return CompletableFuture.supplyAsync(() -> {
            try (
               InputStream inputstream = this.resourceManager.open(p_120208_);
               OggAudioStream oggaudiostream = new OggAudioStream(inputstream);
            ) {
               ByteBuffer bytebuffer = oggaudiostream.readAll();
               return new SoundBuffer(bytebuffer, oggaudiostream.getFormat());
            } catch (IOException ioexception) {
               throw new CompletionException(ioexception);
            }
         }, Util.backgroundExecutor());
      });
   }

   /**
    * {@return Returns a {@linkplain CompletableFuture} containing the {@linkplain AudioStream}. The {@linkplain
    * AudioStream} is loaded asynchronously.}
    * @param pResourceLocation the {@linkplain ResourceLocation} of the sound
    * @param pIsWrapper whether the {@linkplain AudioStream} should be a {@linkplain LoopingAudioStream}
    */
   public CompletableFuture<AudioStream> getStream(ResourceLocation pResourceLocation, boolean pIsWrapper) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            InputStream inputstream = this.resourceManager.open(pResourceLocation);
            return (AudioStream)(pIsWrapper ? new LoopingAudioStream(OggAudioStream::new, inputstream) : new OggAudioStream(inputstream));
         } catch (IOException ioexception) {
            throw new CompletionException(ioexception);
         }
      }, Util.backgroundExecutor());
   }

   /**
    * Clears the cache of all {@linkplain SoundBuffer} instances.
    */
   public void clear() {
      this.cache.values().forEach((p_120201_) -> {
         p_120201_.thenAccept(SoundBuffer::discardAlBuffer);
      });
      this.cache.clear();
   }

   /**
    * Preloads the {@linkplain SoundBuffer} objects for the specified collection of sounds.
    * <p>
    * @return a {@linkplain CompletableFuture} representing the completion of the preload operation
    * @param pSounds the collection of sounds to preload
    */
   public CompletableFuture<?> preload(Collection<Sound> pSounds) {
      return CompletableFuture.allOf(pSounds.stream().map((p_120197_) -> {
         return this.getCompleteBuffer(p_120197_.getPath());
      }).toArray((p_120195_) -> {
         return new CompletableFuture[p_120195_];
      }));
   }
}