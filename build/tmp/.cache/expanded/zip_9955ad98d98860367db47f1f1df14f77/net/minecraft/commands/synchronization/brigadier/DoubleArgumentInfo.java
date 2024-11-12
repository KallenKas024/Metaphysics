package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo implements ArgumentTypeInfo<DoubleArgumentType, DoubleArgumentInfo.Template> {
   public void serializeToNetwork(DoubleArgumentInfo.Template pTemplate, FriendlyByteBuf pBuffer) {
      boolean flag = pTemplate.min != -Double.MAX_VALUE;
      boolean flag1 = pTemplate.max != Double.MAX_VALUE;
      pBuffer.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
      if (flag) {
         pBuffer.writeDouble(pTemplate.min);
      }

      if (flag1) {
         pBuffer.writeDouble(pTemplate.max);
      }

   }

   public DoubleArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
      byte b0 = pBuffer.readByte();
      double d0 = ArgumentUtils.numberHasMin(b0) ? pBuffer.readDouble() : -Double.MAX_VALUE;
      double d1 = ArgumentUtils.numberHasMax(b0) ? pBuffer.readDouble() : Double.MAX_VALUE;
      return new DoubleArgumentInfo.Template(d0, d1);
   }

   public void serializeToJson(DoubleArgumentInfo.Template pTemplate, JsonObject pJson) {
      if (pTemplate.min != -Double.MAX_VALUE) {
         pJson.addProperty("min", pTemplate.min);
      }

      if (pTemplate.max != Double.MAX_VALUE) {
         pJson.addProperty("max", pTemplate.max);
      }

   }

   public DoubleArgumentInfo.Template unpack(DoubleArgumentType pArgument) {
      return new DoubleArgumentInfo.Template(pArgument.getMinimum(), pArgument.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<DoubleArgumentType> {
      final double min;
      final double max;

      Template(double pMin, double pMax) {
         this.min = pMin;
         this.max = pMax;
      }

      public DoubleArgumentType instantiate(CommandBuildContext pContext) {
         return DoubleArgumentType.doubleArg(this.min, this.max);
      }

      public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
         return DoubleArgumentInfo.this;
      }
   }
}