package net.minecraft.commands.synchronization;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

public class ArgumentUtils {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final byte NUMBER_FLAG_MIN = 1;
   private static final byte NUMBER_FLAG_MAX = 2;

   public static int createNumberFlags(boolean pMin, boolean pMax) {
      int i = 0;
      if (pMin) {
         i |= 1;
      }

      if (pMax) {
         i |= 2;
      }

      return i;
   }

   public static boolean numberHasMin(byte pNumber) {
      return (pNumber & 1) != 0;
   }

   public static boolean numberHasMax(byte pNumber) {
      return (pNumber & 2) != 0;
   }

   private static <A extends ArgumentType<?>> void serializeCap(JsonObject pJson, ArgumentTypeInfo.Template<A> pTemplate) {
      serializeCap(pJson, pTemplate.type(), pTemplate);
   }

   private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(JsonObject pJson, ArgumentTypeInfo<A, T> pArgumentTypeInfo, ArgumentTypeInfo.Template<A> pTemplate) {
      pArgumentTypeInfo.serializeToJson((T)pTemplate, pJson);
   }

   private static <T extends ArgumentType<?>> void serializeArgumentToJson(JsonObject pJson, T pType) {
      ArgumentTypeInfo.Template<T> template = ArgumentTypeInfos.unpack(pType);
      pJson.addProperty("type", "argument");
      pJson.addProperty("parser", BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(template.type()).toString());
      JsonObject jsonobject = new JsonObject();
      serializeCap(jsonobject, template);
      if (jsonobject.size() > 0) {
         pJson.add("properties", jsonobject);
      }

   }

   public static <S> JsonObject serializeNodeToJson(CommandDispatcher<S> pDispatcher, CommandNode<S> pNode) {
      JsonObject jsonobject = new JsonObject();
      if (pNode instanceof RootCommandNode) {
         jsonobject.addProperty("type", "root");
      } else if (pNode instanceof LiteralCommandNode) {
         jsonobject.addProperty("type", "literal");
      } else if (pNode instanceof ArgumentCommandNode) {
         ArgumentCommandNode<?, ?> argumentcommandnode = (ArgumentCommandNode)pNode;
         serializeArgumentToJson(jsonobject, argumentcommandnode.getType());
      } else {
         LOGGER.error("Could not serialize node {} ({})!", pNode, pNode.getClass());
         jsonobject.addProperty("type", "unknown");
      }

      JsonObject jsonobject1 = new JsonObject();

      for(CommandNode<S> commandnode : pNode.getChildren()) {
         jsonobject1.add(commandnode.getName(), serializeNodeToJson(pDispatcher, commandnode));
      }

      if (jsonobject1.size() > 0) {
         jsonobject.add("children", jsonobject1);
      }

      if (pNode.getCommand() != null) {
         jsonobject.addProperty("executable", true);
      }

      if (pNode.getRedirect() != null) {
         Collection<String> collection = pDispatcher.getPath(pNode.getRedirect());
         if (!collection.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(String s : collection) {
               jsonarray.add(s);
            }

            jsonobject.add("redirect", jsonarray);
         }
      }

      return jsonobject;
   }

   public static <T> Set<ArgumentType<?>> findUsedArgumentTypes(CommandNode<T> pNode) {
      Set<CommandNode<T>> set = Sets.newIdentityHashSet();
      Set<ArgumentType<?>> set1 = Sets.newHashSet();
      findUsedArgumentTypes(pNode, set1, set);
      return set1;
   }

   private static <T> void findUsedArgumentTypes(CommandNode<T> pNode, Set<ArgumentType<?>> pTypes, Set<CommandNode<T>> pNodes) {
      if (pNodes.add(pNode)) {
         if (pNode instanceof ArgumentCommandNode) {
            ArgumentCommandNode<?, ?> argumentcommandnode = (ArgumentCommandNode)pNode;
            pTypes.add(argumentcommandnode.getType());
         }

         pNode.getChildren().forEach((p_235426_) -> {
            findUsedArgumentTypes(p_235426_, pTypes, pNodes);
         });
         CommandNode<T> commandnode = pNode.getRedirect();
         if (commandnode != null) {
            findUsedArgumentTypes(commandnode, pTypes, pNodes);
         }

      }
   }
}