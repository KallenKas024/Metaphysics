package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
   public static final Codec<ChatType> CODEC = RecordCodecBuilder.create((p_240514_) -> {
      return p_240514_.group(ChatTypeDecoration.CODEC.fieldOf("chat").forGetter(ChatType::chat), ChatTypeDecoration.CODEC.fieldOf("narration").forGetter(ChatType::narration)).apply(p_240514_, ChatType::new);
   });
   public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
   public static final ResourceKey<ChatType> CHAT = create("chat");
   public static final ResourceKey<ChatType> SAY_COMMAND = create("say_command");
   public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = create("msg_command_incoming");
   public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = create("msg_command_outgoing");
   public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = create("team_msg_command_incoming");
   public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = create("team_msg_command_outgoing");
   public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");

   private static ResourceKey<ChatType> create(String pKey) {
      return ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation(pKey));
   }

   public static void bootstrap(BootstapContext<ChatType> pContext) {
      pContext.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
      pContext.register(SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      pContext.register(MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      pContext.register(MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      pContext.register(TEAM_MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      pContext.register(TEAM_MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      pContext.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
   }

   public static ChatType.Bound bind(ResourceKey<ChatType> pChatTypeKey, Entity pEntity) {
      return bind(pChatTypeKey, pEntity.level().registryAccess(), pEntity.getDisplayName());
   }

   public static ChatType.Bound bind(ResourceKey<ChatType> pChatTypeKey, CommandSourceStack pSource) {
      return bind(pChatTypeKey, pSource.registryAccess(), pSource.getDisplayName());
   }

   public static ChatType.Bound bind(ResourceKey<ChatType> pChatTypeKey, RegistryAccess pRegistryAccess, Component pName) {
      Registry<ChatType> registry = pRegistryAccess.registryOrThrow(Registries.CHAT_TYPE);
      return registry.getOrThrow(pChatTypeKey).bind(pName);
   }

   public ChatType.Bound bind(Component pName) {
      return new ChatType.Bound(this, pName);
   }

   public static record Bound(ChatType chatType, Component name, @Nullable Component targetName) {
      Bound(ChatType pChatType, Component pName) {
         this(pChatType, pName, (Component)null);
      }

      public Component decorate(Component pContent) {
         return this.chatType.chat().decorate(pContent, this);
      }

      public Component decorateNarration(Component pContent) {
         return this.chatType.narration().decorate(pContent, this);
      }

      public ChatType.Bound withTargetName(Component pTargetName) {
         return new ChatType.Bound(this.chatType, this.name, pTargetName);
      }

      public ChatType.BoundNetwork toNetwork(RegistryAccess pRegistryAccess) {
         Registry<ChatType> registry = pRegistryAccess.registryOrThrow(Registries.CHAT_TYPE);
         return new ChatType.BoundNetwork(registry.getId(this.chatType), this.name, this.targetName);
      }
   }

   public static record BoundNetwork(int chatType, Component name, @Nullable Component targetName) {
      public BoundNetwork(FriendlyByteBuf pBuffer) {
         this(pBuffer.readVarInt(), pBuffer.readComponent(), pBuffer.readNullable(FriendlyByteBuf::readComponent));
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeVarInt(this.chatType);
         pBuffer.writeComponent(this.name);
         pBuffer.writeNullable(this.targetName, FriendlyByteBuf::writeComponent);
      }

      public Optional<ChatType.Bound> resolve(RegistryAccess pRegistryAccess) {
         Registry<ChatType> registry = pRegistryAccess.registryOrThrow(Registries.CHAT_TYPE);
         ChatType chattype = registry.byId(this.chatType);
         return Optional.ofNullable(chattype).map((p_242929_) -> {
            return new ChatType.Bound(p_242929_, this.name, this.targetName);
         });
      }
   }
}