package net.minecraft.client.resources.sounds;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

@OnlyIn(Dist.CLIENT)
public class SoundEventRegistrationSerializer implements JsonDeserializer<SoundEventRegistration> {
   private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);

   public SoundEventRegistration deserialize(JsonElement p_119827_, Type p_119828_, JsonDeserializationContext p_119829_) throws JsonParseException {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(p_119827_, "entry");
      boolean flag = GsonHelper.getAsBoolean(jsonobject, "replace", false);
      String s = GsonHelper.getAsString(jsonobject, "subtitle", (String)null);
      List<Sound> list = this.getSounds(jsonobject);
      return new SoundEventRegistration(list, flag, s);
   }

   private List<Sound> getSounds(JsonObject pObject) {
      List<Sound> list = Lists.newArrayList();
      if (pObject.has("sounds")) {
         JsonArray jsonarray = GsonHelper.getAsJsonArray(pObject, "sounds");

         for(int i = 0; i < jsonarray.size(); ++i) {
            JsonElement jsonelement = jsonarray.get(i);
            if (GsonHelper.isStringValue(jsonelement)) {
               String s = GsonHelper.convertToString(jsonelement, "sound");
               list.add(new Sound(s, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, false, false, 16));
            } else {
               list.add(this.getSound(GsonHelper.convertToJsonObject(jsonelement, "sound")));
            }
         }
      }

      return list;
   }

   private Sound getSound(JsonObject pObject) {
      String s = GsonHelper.getAsString(pObject, "name");
      Sound.Type sound$type = this.getType(pObject, Sound.Type.FILE);
      float f = GsonHelper.getAsFloat(pObject, "volume", 1.0F);
      Validate.isTrue(f > 0.0F, "Invalid volume");
      float f1 = GsonHelper.getAsFloat(pObject, "pitch", 1.0F);
      Validate.isTrue(f1 > 0.0F, "Invalid pitch");
      int i = GsonHelper.getAsInt(pObject, "weight", 1);
      Validate.isTrue(i > 0, "Invalid weight");
      boolean flag = GsonHelper.getAsBoolean(pObject, "preload", false);
      boolean flag1 = GsonHelper.getAsBoolean(pObject, "stream", false);
      int j = GsonHelper.getAsInt(pObject, "attenuation_distance", 16);
      return new Sound(s, ConstantFloat.of(f), ConstantFloat.of(f1), i, sound$type, flag1, flag, j);
   }

   private Sound.Type getType(JsonObject pObject, Sound.Type pDefaultValue) {
      Sound.Type sound$type = pDefaultValue;
      if (pObject.has("type")) {
         sound$type = Sound.Type.getByName(GsonHelper.getAsString(pObject, "type"));
         Validate.notNull(sound$type, "Invalid type");
      }

      return sound$type;
   }
}