package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public abstract class DustParticleOptionsBase implements ParticleOptions {
   public static final float MIN_SCALE = 0.01F;
   public static final float MAX_SCALE = 4.0F;
   protected final Vector3f color;
   protected final float scale;

   public DustParticleOptionsBase(Vector3f pColor, float pScale) {
      this.color = pColor;
      this.scale = Mth.clamp(pScale, 0.01F, 4.0F);
   }

   public static Vector3f readVector3f(StringReader pReader) throws CommandSyntaxException {
      pReader.expect(' ');
      float f = pReader.readFloat();
      pReader.expect(' ');
      float f1 = pReader.readFloat();
      pReader.expect(' ');
      float f2 = pReader.readFloat();
      return new Vector3f(f, f1, f2);
   }

   public static Vector3f readVector3f(FriendlyByteBuf pBuffer) {
      return new Vector3f(pBuffer.readFloat(), pBuffer.readFloat(), pBuffer.readFloat());
   }

   public void writeToNetwork(FriendlyByteBuf pBuffer) {
      pBuffer.writeFloat(this.color.x());
      pBuffer.writeFloat(this.color.y());
      pBuffer.writeFloat(this.color.z());
      pBuffer.writeFloat(this.scale);
   }

   public String writeToString() {
      return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.scale);
   }

   public Vector3f getColor() {
      return this.color;
   }

   public float getScale() {
      return this.scale;
   }
}