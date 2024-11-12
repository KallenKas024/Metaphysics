package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoUpdatePacket implements Packet<ClientGamePacketListener> {
   private final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions;
   private final List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

   public ClientboundPlayerInfoUpdatePacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> pActions, Collection<ServerPlayer> pPlayers) {
      this.actions = pActions;
      this.entries = pPlayers.stream().map(ClientboundPlayerInfoUpdatePacket.Entry::new).toList();
   }

   public ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action pAction, ServerPlayer pPlayer) {
      this.actions = EnumSet.of(pAction);
      this.entries = List.of(new ClientboundPlayerInfoUpdatePacket.Entry(pPlayer));
   }

   public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> pPlayers) {
      EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumset = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
      return new ClientboundPlayerInfoUpdatePacket(enumset, pPlayers);
   }

   public ClientboundPlayerInfoUpdatePacket(FriendlyByteBuf pBuffer) {
      this.actions = pBuffer.readEnumSet(ClientboundPlayerInfoUpdatePacket.Action.class);
      this.entries = pBuffer.readList((p_249950_) -> {
         ClientboundPlayerInfoUpdatePacket.EntryBuilder clientboundplayerinfoupdatepacket$entrybuilder = new ClientboundPlayerInfoUpdatePacket.EntryBuilder(p_249950_.readUUID());

         for(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket$action : this.actions) {
            clientboundplayerinfoupdatepacket$action.reader.read(clientboundplayerinfoupdatepacket$entrybuilder, p_249950_);
         }

         return clientboundplayerinfoupdatepacket$entrybuilder.build();
      });
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnumSet(this.actions, ClientboundPlayerInfoUpdatePacket.Action.class);
      pBuffer.writeCollection(this.entries, (p_251434_, p_252303_) -> {
         p_251434_.writeUUID(p_252303_.profileId());

         for(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket$action : this.actions) {
            clientboundplayerinfoupdatepacket$action.writer.write(p_251434_, p_252303_);
         }

      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerInfoUpdate(this);
   }

   public EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions() {
      return this.actions;
   }

   public List<ClientboundPlayerInfoUpdatePacket.Entry> entries() {
      return this.entries;
   }

   public List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries() {
      return this.actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) ? this.entries : List.of();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
   }

   public static enum Action {
      ADD_PLAYER((p_251116_, p_251884_) -> {
         GameProfile gameprofile = new GameProfile(p_251116_.profileId, p_251884_.readUtf(16));
         gameprofile.getProperties().putAll(p_251884_.readGameProfileProperties());
         p_251116_.profile = gameprofile;
      }, (p_252022_, p_250357_) -> {
         p_252022_.writeUtf(p_250357_.profile().getName(), 16);
         p_252022_.writeGameProfileProperties(p_250357_.profile().getProperties());
      }),
      INITIALIZE_CHAT((p_253468_, p_253469_) -> {
         p_253468_.chatSession = p_253469_.readNullable(RemoteChatSession.Data::read);
      }, (p_253470_, p_253471_) -> {
         p_253470_.writeNullable(p_253471_.chatSession, RemoteChatSession.Data::write);
      }),
      UPDATE_GAME_MODE((p_251118_, p_248955_) -> {
         p_251118_.gameMode = GameType.byId(p_248955_.readVarInt());
      }, (p_249222_, p_250996_) -> {
         p_249222_.writeVarInt(p_250996_.gameMode().getId());
      }),
      UPDATE_LISTED((p_248777_, p_248837_) -> {
         p_248777_.listed = p_248837_.readBoolean();
      }, (p_249355_, p_251658_) -> {
         p_249355_.writeBoolean(p_251658_.listed());
      }),
      UPDATE_LATENCY((p_252263_, p_248964_) -> {
         p_252263_.latency = p_248964_.readVarInt();
      }, (p_248830_, p_251312_) -> {
         p_248830_.writeVarInt(p_251312_.latency());
      }),
      UPDATE_DISPLAY_NAME((p_248840_, p_251000_) -> {
         p_248840_.displayName = p_251000_.readNullable(FriendlyByteBuf::readComponent);
      }, (p_251723_, p_251870_) -> {
         p_251723_.writeNullable(p_251870_.displayName(), FriendlyByteBuf::writeComponent);
      });

      final ClientboundPlayerInfoUpdatePacket.Action.Reader reader;
      final ClientboundPlayerInfoUpdatePacket.Action.Writer writer;

      private Action(ClientboundPlayerInfoUpdatePacket.Action.Reader pReader, ClientboundPlayerInfoUpdatePacket.Action.Writer pWriter) {
         this.reader = pReader;
         this.writer = pWriter;
      }

      public interface Reader {
         void read(ClientboundPlayerInfoUpdatePacket.EntryBuilder pEntryBuilder, FriendlyByteBuf pBuffer);
      }

      public interface Writer {
         void write(FriendlyByteBuf pBuffer, ClientboundPlayerInfoUpdatePacket.Entry pEntry);
      }
   }

   public static record Entry(UUID profileId, GameProfile profile, boolean listed, int latency, GameType gameMode, @Nullable Component displayName, @Nullable RemoteChatSession.Data chatSession) {
      Entry(ServerPlayer pPlayer) {
         this(pPlayer.getUUID(), pPlayer.getGameProfile(), true, pPlayer.latency, pPlayer.gameMode.getGameModeForPlayer(), pPlayer.getTabListDisplayName(), Optionull.map(pPlayer.getChatSession(), RemoteChatSession::asData));
      }
   }

   static class EntryBuilder {
      final UUID profileId;
      GameProfile profile;
      boolean listed;
      int latency;
      GameType gameMode = GameType.DEFAULT_MODE;
      @Nullable
      Component displayName;
      @Nullable
      RemoteChatSession.Data chatSession;

      EntryBuilder(UUID pProfileId) {
         this.profileId = pProfileId;
         this.profile = new GameProfile(pProfileId, (String)null);
      }

      ClientboundPlayerInfoUpdatePacket.Entry build() {
         return new ClientboundPlayerInfoUpdatePacket.Entry(this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession);
      }
   }
}