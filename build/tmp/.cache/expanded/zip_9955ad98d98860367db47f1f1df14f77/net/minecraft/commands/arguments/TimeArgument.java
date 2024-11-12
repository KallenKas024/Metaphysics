package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class TimeArgument implements ArgumentType<Integer> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0d", "0s", "0t", "0");
   private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = new SimpleCommandExceptionType(Component.translatable("argument.time.invalid_unit"));
   private static final Dynamic2CommandExceptionType ERROR_TICK_COUNT_TOO_LOW = new Dynamic2CommandExceptionType((p_264715_, p_264716_) -> {
      return Component.translatable("argument.time.tick_count_too_low", p_264716_, p_264715_);
   });
   private static final Object2IntMap<String> UNITS = new Object2IntOpenHashMap<>();
   final int minimum;

   private TimeArgument(int pMinimum) {
      this.minimum = pMinimum;
   }

   public static TimeArgument time() {
      return new TimeArgument(0);
   }

   public static TimeArgument time(int pMinimum) {
      return new TimeArgument(pMinimum);
   }

   public Integer parse(StringReader pReader) throws CommandSyntaxException {
      float f = pReader.readFloat();
      String s = pReader.readUnquotedString();
      int i = UNITS.getOrDefault(s, 0);
      if (i == 0) {
         throw ERROR_INVALID_UNIT.create();
      } else {
         int j = Math.round(f * (float)i);
         if (j < this.minimum) {
            throw ERROR_TICK_COUNT_TOO_LOW.create(j, this.minimum);
         } else {
            return j;
         }
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      StringReader stringreader = new StringReader(pBuilder.getRemaining());

      try {
         stringreader.readFloat();
      } catch (CommandSyntaxException commandsyntaxexception) {
         return pBuilder.buildFuture();
      }

      return SharedSuggestionProvider.suggest(UNITS.keySet(), pBuilder.createOffset(pBuilder.getStart() + stringreader.getCursor()));
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static {
      UNITS.put("d", 24000);
      UNITS.put("s", 20);
      UNITS.put("t", 1);
      UNITS.put("", 1);
   }

   public static class Info implements ArgumentTypeInfo<TimeArgument, TimeArgument.Info.Template> {
      public void serializeToNetwork(TimeArgument.Info.Template pTemplate, FriendlyByteBuf pBuffer) {
         pBuffer.writeInt(pTemplate.min);
      }

      public TimeArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
         int i = pBuffer.readInt();
         return new TimeArgument.Info.Template(i);
      }

      public void serializeToJson(TimeArgument.Info.Template pTemplate, JsonObject pJson) {
         pJson.addProperty("min", pTemplate.min);
      }

      public TimeArgument.Info.Template unpack(TimeArgument pArgument) {
         return new TimeArgument.Info.Template(pArgument.minimum);
      }

      public final class Template implements ArgumentTypeInfo.Template<TimeArgument> {
         final int min;

         Template(int pMin) {
            this.min = pMin;
         }

         public TimeArgument instantiate(CommandBuildContext pContext) {
            return TimeArgument.time(this.min);
         }

         public ArgumentTypeInfo<TimeArgument, ?> type() {
            return Info.this;
         }
      }
   }
}