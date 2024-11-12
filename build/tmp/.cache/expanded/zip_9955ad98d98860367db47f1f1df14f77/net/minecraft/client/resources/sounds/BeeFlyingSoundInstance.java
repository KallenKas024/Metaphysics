package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Bee;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeFlyingSoundInstance extends BeeSoundInstance {
   public BeeFlyingSoundInstance(Bee pBee) {
      super(pBee, SoundEvents.BEE_LOOP, SoundSource.NEUTRAL);
   }

   protected AbstractTickableSoundInstance getAlternativeSoundInstance() {
      return new BeeAggressiveSoundInstance(this.bee);
   }

   protected boolean shouldSwitchSounds() {
      return this.bee.isAngry();
   }
}