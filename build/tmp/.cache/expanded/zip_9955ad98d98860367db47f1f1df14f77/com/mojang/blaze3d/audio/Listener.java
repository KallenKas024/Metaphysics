package com.mojang.blaze3d.audio;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

/**
 * The Listener class represents the listener in a 3D audio environment.
 * 
 * The listener's position and orientation determine how sounds are perceived by the listener.
 */
@OnlyIn(Dist.CLIENT)
public class Listener {
   private float gain = 1.0F;
   private Vec3 position = Vec3.ZERO;

   /**
    * Sets the listener's position.
    * @param pPosition A Vec3 representing the listener's position in 3D space.
    */
   public void setListenerPosition(Vec3 pPosition) {
      this.position = pPosition;
      AL10.alListener3f(4100, (float)pPosition.x, (float)pPosition.y, (float)pPosition.z);
   }

   /**
    * {@return the current position of the listener in a 3D space}
    */
   public Vec3 getListenerPosition() {
      return this.position;
   }

   /**
    * Sets the listener's orientation.
    * @param pClientViewVector The view vector indicating the direction the listener is facing.
    * @param pViewVectorRaised The up vector indicating the listener's "up" direction.
    */
   public void setListenerOrientation(Vector3f pClientViewVector, Vector3f pViewVectorRaised) {
      AL10.alListenerfv(4111, new float[]{pClientViewVector.x(), pClientViewVector.y(), pClientViewVector.z(), pViewVectorRaised.x(), pViewVectorRaised.y(), pViewVectorRaised.z()});
   }

   /**
    * Sets the listener's gain.
    * @param pGain The gain to set for the listener.
    */
   public void setGain(float pGain) {
      AL10.alListenerf(4106, pGain);
      this.gain = pGain;
   }

   /**
    * {@return the current gain value of the listener}
    */
   public float getGain() {
      return this.gain;
   }

   /**
    * Resets the listener's position and orientation to default values.
    */
   public void reset() {
      this.setListenerPosition(Vec3.ZERO);
      this.setListenerOrientation(new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(0.0F, 1.0F, 0.0F));
   }
}