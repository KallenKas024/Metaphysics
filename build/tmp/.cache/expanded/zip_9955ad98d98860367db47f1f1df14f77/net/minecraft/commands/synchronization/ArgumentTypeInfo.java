package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.FriendlyByteBuf;

public interface ArgumentTypeInfo<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> {
   void serializeToNetwork(T pTemplate, FriendlyByteBuf pBuffer);

   T deserializeFromNetwork(FriendlyByteBuf pBuffer);

   void serializeToJson(T pTemplate, JsonObject pJson);

   T unpack(A pArgument);

   public interface Template<A extends ArgumentType<?>> {
      A instantiate(CommandBuildContext pContext);

      ArgumentTypeInfo<A, ?> type();
   }
}