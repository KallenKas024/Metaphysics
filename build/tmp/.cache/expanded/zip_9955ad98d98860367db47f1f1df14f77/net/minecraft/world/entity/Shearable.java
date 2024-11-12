package net.minecraft.world.entity;

import net.minecraft.sounds.SoundSource;

@Deprecated // Forge: Use IForgeShearable
public interface Shearable {
   @Deprecated // Forge: Use IForgeShearable
   void shear(SoundSource pSource);

   @Deprecated // Forge: Use IForgeShearable
   boolean readyForShearing();
}
