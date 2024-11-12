package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentInfo implements ArgumentTypeInfo<LongArgumentType, LongArgumentInfo.Template> {
   public void serializeToNetwork(LongArgumentInfo.Template pTemplate, FriendlyByteBuf pBuffer) {
      boolean flag = pTemplate.min != Long.MIN_VALUE;
      boolean flag1 = pTemplate.max != Long.MAX_VALUE;
      pBuffer.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
      if (flag) {
         pBuffer.writeLong(pTemplate.min);
      }

      if (flag1) {
         pBuffer.writeLong(pTemplate.max);
      }

   }

   public LongArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
      byte b0 = pBuffer.readByte();
      long i = ArgumentUtils.numberHasMin(b0) ? pBuffer.readLong() : Long.MIN_VALUE;
      long j = ArgumentUtils.numberHasMax(b0) ? pBuffer.readLong() : Long.MAX_VALUE;
      return new LongArgumentInfo.Template(i, j);
   }

   public void serializeToJson(LongArgumentInfo.Template pTemplate, JsonObject pJson) {
      if (pTemplate.min != Long.MIN_VALUE) {
         pJson.addProperty("min", pTemplate.min);
      }

      if (pTemplate.max != Long.MAX_VALUE) {
         pJson.addProperty("max", pTemplate.max);
      }

   }

   public LongArgumentInfo.Template unpack(LongArgumentType pArgument) {
      return new LongArgumentInfo.Template(pArgument.getMinimum(), pArgument.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<LongArgumentType> {
      final long min;
      final long max;

      Template(long pMin, long pMax) {
         this.min = pMin;
         this.max = pMax;
      }

      public LongArgumentType instantiate(CommandBuildContext pContext) {
         return LongArgumentType.longArg(this.min, this.max);
      }

      public ArgumentTypeInfo<LongArgumentType, ?> type() {
         return LongArgumentInfo.this;
      }
   }
}