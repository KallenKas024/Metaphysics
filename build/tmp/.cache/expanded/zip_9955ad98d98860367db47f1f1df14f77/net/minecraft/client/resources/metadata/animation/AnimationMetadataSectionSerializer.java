package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

@OnlyIn(Dist.CLIENT)
public class AnimationMetadataSectionSerializer implements MetadataSectionSerializer<AnimationMetadataSection> {
   public AnimationMetadataSection fromJson(JsonObject pJson) {
      ImmutableList.Builder<AnimationFrame> builder = ImmutableList.builder();
      int i = GsonHelper.getAsInt(pJson, "frametime", 1);
      if (i != 1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid default frame time");
      }

      if (pJson.has("frames")) {
         try {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, "frames");

            for(int j = 0; j < jsonarray.size(); ++j) {
               JsonElement jsonelement = jsonarray.get(j);
               AnimationFrame animationframe = this.getFrame(j, jsonelement);
               if (animationframe != null) {
                  builder.add(animationframe);
               }
            }
         } catch (ClassCastException classcastexception) {
            throw new JsonParseException("Invalid animation->frames: expected array, was " + pJson.get("frames"), classcastexception);
         }
      }

      int k = GsonHelper.getAsInt(pJson, "width", -1);
      int l = GsonHelper.getAsInt(pJson, "height", -1);
      if (k != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)k, "Invalid width");
      }

      if (l != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)l, "Invalid height");
      }

      boolean flag = GsonHelper.getAsBoolean(pJson, "interpolate", false);
      return new AnimationMetadataSection(builder.build(), k, l, i, flag);
   }

   @Nullable
   private AnimationFrame getFrame(int pFrame, JsonElement pElement) {
      if (pElement.isJsonPrimitive()) {
         return new AnimationFrame(GsonHelper.convertToInt(pElement, "frames[" + pFrame + "]"));
      } else if (pElement.isJsonObject()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pElement, "frames[" + pFrame + "]");
         int i = GsonHelper.getAsInt(jsonobject, "time", -1);
         if (jsonobject.has("time")) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid frame time");
         }

         int j = GsonHelper.getAsInt(jsonobject, "index");
         Validate.inclusiveBetween(0L, 2147483647L, (long)j, "Invalid frame index");
         return new AnimationFrame(j, i);
      } else {
         return null;
      }
   }

   /**
    * The name of this section type as it appears in JSON.
    */
   public String getMetadataSectionName() {
      return "animation";
   }
}