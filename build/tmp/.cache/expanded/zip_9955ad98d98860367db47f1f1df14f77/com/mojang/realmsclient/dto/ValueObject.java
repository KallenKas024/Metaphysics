package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ValueObject {
   public String toString() {
      StringBuilder stringbuilder = new StringBuilder("{");

      for(Field field : this.getClass().getFields()) {
         if (!isStatic(field)) {
            try {
               stringbuilder.append(getName(field)).append("=").append(field.get(this)).append(" ");
            } catch (IllegalAccessException illegalaccessexception) {
            }
         }
      }

      stringbuilder.deleteCharAt(stringbuilder.length() - 1);
      stringbuilder.append('}');
      return stringbuilder.toString();
   }

   private static String getName(Field pField) {
      SerializedName serializedname = pField.getAnnotation(SerializedName.class);
      return serializedname != null ? serializedname.value() : pField.getName();
   }

   private static boolean isStatic(Field pField) {
      return Modifier.isStatic(pField.getModifiers());
   }
}