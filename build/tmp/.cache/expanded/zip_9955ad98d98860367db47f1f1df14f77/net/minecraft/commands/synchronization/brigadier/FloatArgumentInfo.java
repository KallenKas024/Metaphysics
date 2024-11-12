package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentInfo implements ArgumentTypeInfo<FloatArgumentType, FloatArgumentInfo.Template> {
   public void serializeToNetwork(FloatArgumentInfo.Template pTemplate, FriendlyByteBuf pBuffer) {
      boolean flag = pTemplate.min != -Float.MAX_VALUE;
      boolean flag1 = pTemplate.max != Float.MAX_VALUE;
      pBuffer.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
      if (flag) {
         pBuffer.writeFloat(pTemplate.min);
      }

      if (flag1) {
         pBuffer.writeFloat(pTemplate.max);
      }

   }

   public FloatArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
      byte b0 = pBuffer.readByte();
      float f = ArgumentUtils.numberHasMin(b0) ? pBuffer.readFloat() : -Float.MAX_VALUE;
      float f1 = ArgumentUtils.numberHasMax(b0) ? pBuffer.readFloat() : Float.MAX_VALUE;
      return new FloatArgumentInfo.Template(f, f1);
   }

   public void serializeToJson(FloatArgumentInfo.Template pTemplate, JsonObject pJson) {
      if (pTemplate.min != -Float.MAX_VALUE) {
         pJson.addProperty("min", pTemplate.min);
      }

      if (pTemplate.max != Float.MAX_VALUE) {
         pJson.addProperty("max", pTemplate.max);
      }

   }

   public FloatArgumentInfo.Template unpack(FloatArgumentType pArgument) {
      return new FloatArgumentInfo.Template(pArgument.getMinimum(), pArgument.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<FloatArgumentType> {
      final float min;
      final float max;

      Template(float pMin, float pMax) {
         this.min = pMin;
         this.max = pMax;
      }

      public FloatArgumentType instantiate(CommandBuildContext pContext) {
         return FloatArgumentType.floatArg(this.min, this.max);
      }

      public ArgumentTypeInfo<FloatArgumentType, ?> type() {
         return FloatArgumentInfo.this;
      }
   }
}