package net.minecraft.network.protocol.game;

import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener> {
   private static final byte MASK_TYPE = 3;
   private static final byte FLAG_EXECUTABLE = 4;
   private static final byte FLAG_REDIRECT = 8;
   private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
   private static final byte TYPE_ROOT = 0;
   private static final byte TYPE_LITERAL = 1;
   private static final byte TYPE_ARGUMENT = 2;
   private final int rootIndex;
   private final List<ClientboundCommandsPacket.Entry> entries;

   public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> pRoot) {
      Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap = enumerateNodes(pRoot);
      this.entries = createEntries(object2intmap);
      this.rootIndex = object2intmap.getInt(pRoot);
   }

   public ClientboundCommandsPacket(FriendlyByteBuf pBuffer) {
      this.entries = pBuffer.readList(ClientboundCommandsPacket::readNode);
      this.rootIndex = pBuffer.readVarInt();
      validateEntries(this.entries);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeCollection(this.entries, (p_237642_, p_237643_) -> {
         p_237643_.write(p_237642_);
      });
      pBuffer.writeVarInt(this.rootIndex);
   }

   private static void validateEntries(List<ClientboundCommandsPacket.Entry> pEntries, BiPredicate<ClientboundCommandsPacket.Entry, IntSet> pValidator) {
      IntSet intset = new IntOpenHashSet(IntSets.fromTo(0, pEntries.size()));

      while(!intset.isEmpty()) {
         boolean flag = intset.removeIf((p_237637_) -> {
            return pValidator.test(pEntries.get(p_237637_), intset);
         });
         if (!flag) {
            throw new IllegalStateException("Server sent an impossible command tree");
         }
      }

   }

   private static void validateEntries(List<ClientboundCommandsPacket.Entry> pEntries) {
      validateEntries(pEntries, ClientboundCommandsPacket.Entry::canBuild);
      validateEntries(pEntries, ClientboundCommandsPacket.Entry::canResolve);
   }

   private static Object2IntMap<CommandNode<SharedSuggestionProvider>> enumerateNodes(RootCommandNode<SharedSuggestionProvider> pRootNode) {
      Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap = new Object2IntOpenHashMap<>();
      Queue<CommandNode<SharedSuggestionProvider>> queue = Queues.newArrayDeque();
      queue.add(pRootNode);

      CommandNode<SharedSuggestionProvider> commandnode;
      while((commandnode = queue.poll()) != null) {
         if (!object2intmap.containsKey(commandnode)) {
            int i = object2intmap.size();
            object2intmap.put(commandnode, i);
            queue.addAll(commandnode.getChildren());
            if (commandnode.getRedirect() != null) {
               queue.add(commandnode.getRedirect());
            }
         }
      }

      return object2intmap;
   }

   private static List<ClientboundCommandsPacket.Entry> createEntries(Object2IntMap<CommandNode<SharedSuggestionProvider>> pNodes) {
      ObjectArrayList<ClientboundCommandsPacket.Entry> objectarraylist = new ObjectArrayList<>(pNodes.size());
      objectarraylist.size(pNodes.size());

      for(Object2IntMap.Entry<CommandNode<SharedSuggestionProvider>> entry : Object2IntMaps.fastIterable(pNodes)) {
         objectarraylist.set(entry.getIntValue(), createEntry(entry.getKey(), pNodes));
      }

      return objectarraylist;
   }

   private static ClientboundCommandsPacket.Entry readNode(FriendlyByteBuf p_131888_) {
      byte b0 = p_131888_.readByte();
      int[] aint = p_131888_.readVarIntArray();
      int i = (b0 & 8) != 0 ? p_131888_.readVarInt() : 0;
      ClientboundCommandsPacket.NodeStub clientboundcommandspacket$nodestub = read(p_131888_, b0);
      return new ClientboundCommandsPacket.Entry(clientboundcommandspacket$nodestub, b0, i, aint);
   }

   @Nullable
   private static ClientboundCommandsPacket.NodeStub read(FriendlyByteBuf pBuffer, byte p_237640_) {
      int i = p_237640_ & 3;
      if (i == 2) {
         String s1 = pBuffer.readUtf();
         int j = pBuffer.readVarInt();
         ArgumentTypeInfo<?, ?> argumenttypeinfo = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId(j);
         if (argumenttypeinfo == null) {
            return null;
         } else {
            ArgumentTypeInfo.Template<?> template = argumenttypeinfo.deserializeFromNetwork(pBuffer);
            ResourceLocation resourcelocation = (p_237640_ & 16) != 0 ? pBuffer.readResourceLocation() : null;
            return new ClientboundCommandsPacket.ArgumentNodeStub(s1, template, resourcelocation);
         }
      } else if (i == 1) {
         String s = pBuffer.readUtf();
         return new ClientboundCommandsPacket.LiteralNodeStub(s);
      } else {
         return null;
      }
   }

   private static ClientboundCommandsPacket.Entry createEntry(CommandNode<SharedSuggestionProvider> pNode, Object2IntMap<CommandNode<SharedSuggestionProvider>> pNodes) {
      int i = 0;
      int j;
      if (pNode.getRedirect() != null) {
         i |= 8;
         j = pNodes.getInt(pNode.getRedirect());
      } else {
         j = 0;
      }

      if (pNode.getCommand() != null) {
         i |= 4;
      }

      ClientboundCommandsPacket.NodeStub clientboundcommandspacket$nodestub;
      if (pNode instanceof RootCommandNode) {
         i |= 0;
         clientboundcommandspacket$nodestub = null;
      } else if (pNode instanceof ArgumentCommandNode) {
         ArgumentCommandNode<SharedSuggestionProvider, ?> argumentcommandnode = (ArgumentCommandNode)pNode;
         clientboundcommandspacket$nodestub = new ClientboundCommandsPacket.ArgumentNodeStub(argumentcommandnode);
         i |= 2;
         if (argumentcommandnode.getCustomSuggestions() != null) {
            i |= 16;
         }
      } else {
         if (!(pNode instanceof LiteralCommandNode)) {
            throw new UnsupportedOperationException("Unknown node type " + pNode);
         }

         LiteralCommandNode literalcommandnode = (LiteralCommandNode)pNode;
         clientboundcommandspacket$nodestub = new ClientboundCommandsPacket.LiteralNodeStub(literalcommandnode.getLiteral());
         i |= 1;
      }

      int[] aint = pNode.getChildren().stream().mapToInt(pNodes::getInt).toArray();
      return new ClientboundCommandsPacket.Entry(clientboundcommandspacket$nodestub, i, j, aint);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleCommands(this);
   }

   public RootCommandNode<SharedSuggestionProvider> getRoot(CommandBuildContext pContext) {
      return (RootCommandNode)(new ClientboundCommandsPacket.NodeResolver(pContext, this.entries)).resolve(this.rootIndex);
   }

   static class ArgumentNodeStub implements ClientboundCommandsPacket.NodeStub {
      private final String id;
      private final ArgumentTypeInfo.Template<?> argumentType;
      @Nullable
      private final ResourceLocation suggestionId;

      @Nullable
      private static ResourceLocation getSuggestionId(@Nullable SuggestionProvider<SharedSuggestionProvider> pProvider) {
         return pProvider != null ? SuggestionProviders.getName(pProvider) : null;
      }

      ArgumentNodeStub(String pId, ArgumentTypeInfo.Template<?> pArgumentType, @Nullable ResourceLocation pSuggestionId) {
         this.id = pId;
         this.argumentType = pArgumentType;
         this.suggestionId = pSuggestionId;
      }

      public ArgumentNodeStub(ArgumentCommandNode<SharedSuggestionProvider, ?> pArgumentNode) {
         this(pArgumentNode.getName(), ArgumentTypeInfos.unpack(pArgumentNode.getType()), getSuggestionId(pArgumentNode.getCustomSuggestions()));
      }

      public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext pContext) {
         ArgumentType<?> argumenttype = this.argumentType.instantiate(pContext);
         RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredargumentbuilder = RequiredArgumentBuilder.argument(this.id, argumenttype);
         if (this.suggestionId != null) {
            requiredargumentbuilder.suggests(SuggestionProviders.getProvider(this.suggestionId));
         }

         return requiredargumentbuilder;
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeUtf(this.id);
         serializeCap(pBuffer, this.argumentType);
         if (this.suggestionId != null) {
            pBuffer.writeResourceLocation(this.suggestionId);
         }

      }

      private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf pBuffer, ArgumentTypeInfo.Template<A> pArgumentInfoTemplate) {
         serializeCap(pBuffer, pArgumentInfoTemplate.type(), pArgumentInfoTemplate);
      }

      private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(FriendlyByteBuf pBuffer, ArgumentTypeInfo<A, T> pArgumentInfo, ArgumentTypeInfo.Template<A> pArgumentInfoTemplate) {
         pBuffer.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(pArgumentInfo));
         pArgumentInfo.serializeToNetwork((T)pArgumentInfoTemplate, pBuffer);
      }
   }

   static class Entry {
      @Nullable
      final ClientboundCommandsPacket.NodeStub stub;
      final int flags;
      final int redirect;
      final int[] children;

      Entry(@Nullable ClientboundCommandsPacket.NodeStub pStub, int pFlags, int pRedirect, int[] pChildren) {
         this.stub = pStub;
         this.flags = pFlags;
         this.redirect = pRedirect;
         this.children = pChildren;
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeByte(this.flags);
         pBuffer.writeVarIntArray(this.children);
         if ((this.flags & 8) != 0) {
            pBuffer.writeVarInt(this.redirect);
         }

         if (this.stub != null) {
            this.stub.write(pBuffer);
         }

      }

      public boolean canBuild(IntSet p_237673_) {
         if ((this.flags & 8) != 0) {
            return !p_237673_.contains(this.redirect);
         } else {
            return true;
         }
      }

      public boolean canResolve(IntSet p_237677_) {
         for(int i : this.children) {
            if (p_237677_.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   static class LiteralNodeStub implements ClientboundCommandsPacket.NodeStub {
      private final String id;

      LiteralNodeStub(String pId) {
         this.id = pId;
      }

      public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext pContext) {
         return LiteralArgumentBuilder.literal(this.id);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeUtf(this.id);
      }
   }

   static class NodeResolver {
      private final CommandBuildContext context;
      private final List<ClientboundCommandsPacket.Entry> entries;
      private final List<CommandNode<SharedSuggestionProvider>> nodes;

      NodeResolver(CommandBuildContext pContext, List<ClientboundCommandsPacket.Entry> pEntries) {
         this.context = pContext;
         this.entries = pEntries;
         ObjectArrayList<CommandNode<SharedSuggestionProvider>> objectarraylist = new ObjectArrayList<>();
         objectarraylist.size(pEntries.size());
         this.nodes = objectarraylist;
      }

      public CommandNode<SharedSuggestionProvider> resolve(int pIndex) {
         CommandNode<SharedSuggestionProvider> commandnode = this.nodes.get(pIndex);
         if (commandnode != null) {
            return commandnode;
         } else {
            ClientboundCommandsPacket.Entry clientboundcommandspacket$entry = this.entries.get(pIndex);
            CommandNode<SharedSuggestionProvider> commandnode1;
            if (clientboundcommandspacket$entry.stub == null) {
               commandnode1 = new RootCommandNode<>();
            } else {
               ArgumentBuilder<SharedSuggestionProvider, ?> argumentbuilder = clientboundcommandspacket$entry.stub.build(this.context);
               if ((clientboundcommandspacket$entry.flags & 8) != 0) {
                  argumentbuilder.redirect(this.resolve(clientboundcommandspacket$entry.redirect));
               }

               if ((clientboundcommandspacket$entry.flags & 4) != 0) {
                  argumentbuilder.executes((p_237694_) -> {
                     return 0;
                  });
               }

               commandnode1 = argumentbuilder.build();
            }

            this.nodes.set(pIndex, commandnode1);

            for(int i : clientboundcommandspacket$entry.children) {
               CommandNode<SharedSuggestionProvider> commandnode2 = this.resolve(i);
               if (!(commandnode2 instanceof RootCommandNode)) {
                  commandnode1.addChild(commandnode2);
               }
            }

            return commandnode1;
         }
      }
   }

   interface NodeStub {
      ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext pContext);

      void write(FriendlyByteBuf pBuffer);
   }
}