package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Type;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@OnlyIn(Dist.CLIENT)
public class ItemTransform {
   public static final ItemTransform NO_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
   public final Vector3f rotation;
   public final Vector3f translation;
   public final Vector3f scale;
   public final Vector3f rightRotation;

   public ItemTransform(Vector3f pRotation, Vector3f pTranslation, Vector3f pScale) {
      this(pRotation, pTranslation, pScale, new Vector3f());
   }

   public ItemTransform(Vector3f pRotation, Vector3f pTranslation, Vector3f pScale, Vector3f rightRotation) {
      this.rotation = new Vector3f((Vector3fc)pRotation);
      this.translation = new Vector3f((Vector3fc)pTranslation);
      this.scale = new Vector3f((Vector3fc)pScale);
      this.rightRotation = new Vector3f(rightRotation);
   }

   public void apply(boolean pLeftHand, PoseStack pPoseStack) {
      if (this != NO_TRANSFORM) {
         float f = this.rotation.x();
         float f1 = this.rotation.y();
         float f2 = this.rotation.z();
         if (pLeftHand) {
            f1 = -f1;
            f2 = -f2;
         }

         int i = pLeftHand ? -1 : 1;
         pPoseStack.translate((float)i * this.translation.x(), this.translation.y(), this.translation.z());
         pPoseStack.mulPose((new Quaternionf()).rotationXYZ(f * ((float)Math.PI / 180F), f1 * ((float)Math.PI / 180F), f2 * ((float)Math.PI / 180F)));
         pPoseStack.scale(this.scale.x(), this.scale.y(), this.scale.z());
         pPoseStack.mulPose(net.minecraftforge.common.util.TransformationHelper.quatFromXYZ(rightRotation.x(), rightRotation.y() * (pLeftHand ? -1 : 1), rightRotation.z() * (pLeftHand ? -1 : 1), true));
      }
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (this.getClass() != pOther.getClass()) {
         return false;
      } else {
         ItemTransform itemtransform = (ItemTransform)pOther;
         return this.rotation.equals(itemtransform.rotation) && this.scale.equals(itemtransform.scale) && this.translation.equals(itemtransform.translation);
      }
   }

   public int hashCode() {
      int i = this.rotation.hashCode();
      i = 31 * i + this.translation.hashCode();
      return 31 * i + this.scale.hashCode();
   }

   @OnlyIn(Dist.CLIENT)
   public static class Deserializer implements JsonDeserializer<ItemTransform> {
      public static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
      public static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
      public static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
      public static final float MAX_TRANSLATION = 5.0F;
      public static final float MAX_SCALE = 4.0F;

      public ItemTransform deserialize(JsonElement pJson, Type pType, JsonDeserializationContext pContext) throws JsonParseException {
         JsonObject jsonobject = pJson.getAsJsonObject();
         Vector3f vector3f = this.getVector3f(jsonobject, "rotation", DEFAULT_ROTATION);
         Vector3f vector3f1 = this.getVector3f(jsonobject, "translation", DEFAULT_TRANSLATION);
         vector3f1.mul(0.0625F);
         vector3f1.set(Mth.clamp(vector3f1.x, -5.0F, 5.0F), Mth.clamp(vector3f1.y, -5.0F, 5.0F), Mth.clamp(vector3f1.z, -5.0F, 5.0F));
         Vector3f vector3f2 = this.getVector3f(jsonobject, "scale", DEFAULT_SCALE);
         vector3f2.set(Mth.clamp(vector3f2.x, -4.0F, 4.0F), Mth.clamp(vector3f2.y, -4.0F, 4.0F), Mth.clamp(vector3f2.z, -4.0F, 4.0F));
         Vector3f rightRotation = this.getVector3f(jsonobject, "right_rotation", DEFAULT_ROTATION);
         return new ItemTransform(vector3f, vector3f1, vector3f2, rightRotation);
      }

      private Vector3f getVector3f(JsonObject pJson, String pKey, Vector3f pFallback) {
         if (!pJson.has(pKey)) {
            return pFallback;
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, pKey);
            if (jsonarray.size() != 3) {
               throw new JsonParseException("Expected 3 " + pKey + " values, found: " + jsonarray.size());
            } else {
               float[] afloat = new float[3];

               for(int i = 0; i < afloat.length; ++i) {
                  afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), pKey + "[" + i + "]");
               }

               return new Vector3f(afloat[0], afloat[1], afloat[2]);
            }
         }
      }
   }
}
