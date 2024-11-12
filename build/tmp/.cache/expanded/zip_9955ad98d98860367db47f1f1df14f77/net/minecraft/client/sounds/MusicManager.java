package net.minecraft.client.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The MusicManager class manages the playing of music in Minecraft.
 */
@OnlyIn(Dist.CLIENT)
public class MusicManager {
   /** The delay before starting to play the next song. */
   private static final int STARTING_DELAY = 100;
   private final RandomSource random = RandomSource.create();
   private final Minecraft minecraft;
   @Nullable
   private SoundInstance currentMusic;
   /** The delay until the next song starts. */
   private int nextSongDelay = 100;

   public MusicManager(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   /**
    * Called every tick to manage the playing of music.
    */
   public void tick() {
      Music music = this.minecraft.getSituationalMusic();
      if (this.currentMusic != null) {
         if (!music.getEvent().value().getLocation().equals(this.currentMusic.getLocation()) && music.replaceCurrentMusic()) {
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.nextSongDelay = Mth.nextInt(this.random, 0, music.getMinDelay() / 2);
         }

         if (!this.minecraft.getSoundManager().isActive(this.currentMusic)) {
            this.currentMusic = null;
            this.nextSongDelay = Math.min(this.nextSongDelay, Mth.nextInt(this.random, music.getMinDelay(), music.getMaxDelay()));
         }
      }

      this.nextSongDelay = Math.min(this.nextSongDelay, music.getMaxDelay());
      if (this.currentMusic == null && this.nextSongDelay-- <= 0) {
         this.startPlaying(music);
      }

   }

   /**
    * Starts playing the specified {@linkplain Music} selector.
    * @param pSelector the {@linkplain Music} selector to play
    */
   public void startPlaying(Music pSelector) {
      this.currentMusic = SimpleSoundInstance.forMusic(pSelector.getEvent().value());
      if (this.currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
         this.minecraft.getSoundManager().play(this.currentMusic);
      }

      this.nextSongDelay = Integer.MAX_VALUE;
   }

   /**
    * Stops playing the specified {@linkplain Music} selector.
    * @param pMusic the {@linkplain Music} selector to stop playing
    */
   public void stopPlaying(Music pMusic) {
      if (this.isPlayingMusic(pMusic)) {
         this.stopPlaying();
      }

   }

   /**
    * Stops playing the current {@linkplain Music} selector.
    */
   public void stopPlaying() {
      if (this.currentMusic != null) {
         this.minecraft.getSoundManager().stop(this.currentMusic);
         this.currentMusic = null;
      }

      this.nextSongDelay += 100;
   }

   /**
    * {@return {@code true} if the {@linkplain Music} selector is currently playing, {@code false} otherwise}
    * @param pSelector the {@linkplain Music} selector to check for
    */
   public boolean isPlayingMusic(Music pSelector) {
      return this.currentMusic == null ? false : pSelector.getEvent().value().getLocation().equals(this.currentMusic.getLocation());
   }
}