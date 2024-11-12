package net.minecraft.world.effect;

public class InstantenousMobEffect extends MobEffect {
   public InstantenousMobEffect(MobEffectCategory pCategory, int pColor) {
      super(pCategory, pColor);
   }

   /**
    * Returns {@code true} if the potion has an instant effect instead of a continuous one (e.g. Harming)
    */
   public boolean isInstantenous() {
      return true;
   }

   /**
    * Checks whether the effect is ready to be applied this tick.
    */
   public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
      return pDuration >= 1;
   }
}