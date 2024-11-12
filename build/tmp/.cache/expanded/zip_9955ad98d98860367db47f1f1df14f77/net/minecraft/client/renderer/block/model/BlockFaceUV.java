package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockFaceUV {
   public float[] uvs;
   public final int rotation;

   public BlockFaceUV(@Nullable float[] pUvs, int pRotation) {
      this.uvs = pUvs;
      this.rotation = pRotation;
   }

   public float getU(int pIndex) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int i = this.getShiftedIndex(pIndex);
         return this.uvs[i != 0 && i != 1 ? 2 : 0];
      }
   }

   public float getV(int pIndex) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int i = this.getShiftedIndex(pIndex);
         return this.uvs[i != 0 && i != 3 ? 3 : 1];
      }
   }

   private int getShiftedIndex(int pIndex) {
      return (pIndex + this.rotation / 90) % 4;
   }

   public int getReverseIndex(int pIndex) {
      return (pIndex + 4 - this.rotation / 90) % 4;
   }

   public void setMissingUv(float[] pUvs) {
      if (this.uvs == null) {
         this.uvs = pUvs;
      }

   }

   @OnlyIn(Dist.CLIENT)
   public static class Deserializer implements JsonDeserializer<BlockFaceUV> {
      private static final int DEFAULT_ROTATION = 0;

      public BlockFaceUV deserialize(JsonElement pJson, Type pType, JsonDeserializationContext pContext) throws JsonParseException {
         JsonObject jsonobject = pJson.getAsJsonObject();
         float[] afloat = this.getUVs(jsonobject);
         int i = this.getRotation(jsonobject);
         return new BlockFaceUV(afloat, i);
      }

      protected int getRotation(JsonObject pJson) {
         int i = GsonHelper.getAsInt(pJson, "rotation", 0);
         if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
            return i;
         } else {
            throw new JsonParseException("Invalid rotation " + i + " found, only 0/90/180/270 allowed");
         }
      }

      @Nullable
      private float[] getUVs(JsonObject pJson) {
         if (!pJson.has("uv")) {
            return null;
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, "uv");
            if (jsonarray.size() != 4) {
               throw new JsonParseException("Expected 4 uv values, found: " + jsonarray.size());
            } else {
               float[] afloat = new float[4];

               for(int i = 0; i < afloat.length; ++i) {
                  afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), "uv[" + i + "]");
               }

               return afloat;
            }
         }
      }
   }
}