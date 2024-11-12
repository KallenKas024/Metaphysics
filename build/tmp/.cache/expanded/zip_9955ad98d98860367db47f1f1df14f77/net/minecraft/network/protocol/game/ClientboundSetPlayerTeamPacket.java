package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.PlayerTeam;

public class ClientboundSetPlayerTeamPacket implements Packet<ClientGamePacketListener> {
   private static final int METHOD_ADD = 0;
   private static final int METHOD_REMOVE = 1;
   private static final int METHOD_CHANGE = 2;
   private static final int METHOD_JOIN = 3;
   private static final int METHOD_LEAVE = 4;
   private static final int MAX_VISIBILITY_LENGTH = 40;
   private static final int MAX_COLLISION_LENGTH = 40;
   private final int method;
   private final String name;
   private final Collection<String> players;
   private final Optional<ClientboundSetPlayerTeamPacket.Parameters> parameters;

   private ClientboundSetPlayerTeamPacket(String pName, int pMethod, Optional<ClientboundSetPlayerTeamPacket.Parameters> pParameters, Collection<String> pPlayers) {
      this.name = pName;
      this.method = pMethod;
      this.parameters = pParameters;
      this.players = ImmutableList.copyOf(pPlayers);
   }

   public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam pTeam, boolean pUseAdd) {
      return new ClientboundSetPlayerTeamPacket(pTeam.getName(), pUseAdd ? 0 : 2, Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(pTeam)), (Collection<String>)(pUseAdd ? pTeam.getPlayers() : ImmutableList.of()));
   }

   public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam pTeam) {
      return new ClientboundSetPlayerTeamPacket(pTeam.getName(), 1, Optional.empty(), ImmutableList.of());
   }

   public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam pTeam, String pPlayerName, ClientboundSetPlayerTeamPacket.Action pAction) {
      return new ClientboundSetPlayerTeamPacket(pTeam.getName(), pAction == ClientboundSetPlayerTeamPacket.Action.ADD ? 3 : 4, Optional.empty(), ImmutableList.of(pPlayerName));
   }

   public ClientboundSetPlayerTeamPacket(FriendlyByteBuf pBuffer) {
      this.name = pBuffer.readUtf();
      this.method = pBuffer.readByte();
      if (shouldHaveParameters(this.method)) {
         this.parameters = Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(pBuffer));
      } else {
         this.parameters = Optional.empty();
      }

      if (shouldHavePlayerList(this.method)) {
         this.players = pBuffer.readList(FriendlyByteBuf::readUtf);
      } else {
         this.players = ImmutableList.of();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.name);
      pBuffer.writeByte(this.method);
      if (shouldHaveParameters(this.method)) {
         this.parameters.orElseThrow(() -> {
            return new IllegalStateException("Parameters not present, but method is" + this.method);
         }).write(pBuffer);
      }

      if (shouldHavePlayerList(this.method)) {
         pBuffer.writeCollection(this.players, FriendlyByteBuf::writeUtf);
      }

   }

   private static boolean shouldHavePlayerList(int pMethod) {
      return pMethod == 0 || pMethod == 3 || pMethod == 4;
   }

   private static boolean shouldHaveParameters(int pMethod) {
      return pMethod == 0 || pMethod == 2;
   }

   @Nullable
   public ClientboundSetPlayerTeamPacket.Action getPlayerAction() {
      switch (this.method) {
         case 0:
         case 3:
            return ClientboundSetPlayerTeamPacket.Action.ADD;
         case 1:
         case 2:
         default:
            return null;
         case 4:
            return ClientboundSetPlayerTeamPacket.Action.REMOVE;
      }
   }

   @Nullable
   public ClientboundSetPlayerTeamPacket.Action getTeamAction() {
      switch (this.method) {
         case 0:
            return ClientboundSetPlayerTeamPacket.Action.ADD;
         case 1:
            return ClientboundSetPlayerTeamPacket.Action.REMOVE;
         default:
            return null;
      }
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetPlayerTeamPacket(this);
   }

   public String getName() {
      return this.name;
   }

   public Collection<String> getPlayers() {
      return this.players;
   }

   public Optional<ClientboundSetPlayerTeamPacket.Parameters> getParameters() {
      return this.parameters;
   }

   public static enum Action {
      ADD,
      REMOVE;
   }

   public static class Parameters {
      private final Component displayName;
      private final Component playerPrefix;
      private final Component playerSuffix;
      private final String nametagVisibility;
      private final String collisionRule;
      private final ChatFormatting color;
      private final int options;

      public Parameters(PlayerTeam pTeam) {
         this.displayName = pTeam.getDisplayName();
         this.options = pTeam.packOptions();
         this.nametagVisibility = pTeam.getNameTagVisibility().name;
         this.collisionRule = pTeam.getCollisionRule().name;
         this.color = pTeam.getColor();
         this.playerPrefix = pTeam.getPlayerPrefix();
         this.playerSuffix = pTeam.getPlayerSuffix();
      }

      public Parameters(FriendlyByteBuf pBuffer) {
         this.displayName = pBuffer.readComponent();
         this.options = pBuffer.readByte();
         this.nametagVisibility = pBuffer.readUtf(40);
         this.collisionRule = pBuffer.readUtf(40);
         this.color = pBuffer.readEnum(ChatFormatting.class);
         this.playerPrefix = pBuffer.readComponent();
         this.playerSuffix = pBuffer.readComponent();
      }

      public Component getDisplayName() {
         return this.displayName;
      }

      public int getOptions() {
         return this.options;
      }

      public ChatFormatting getColor() {
         return this.color;
      }

      public String getNametagVisibility() {
         return this.nametagVisibility;
      }

      public String getCollisionRule() {
         return this.collisionRule;
      }

      public Component getPlayerPrefix() {
         return this.playerPrefix;
      }

      public Component getPlayerSuffix() {
         return this.playerSuffix;
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeComponent(this.displayName);
         pBuffer.writeByte(this.options);
         pBuffer.writeUtf(this.nametagVisibility);
         pBuffer.writeUtf(this.collisionRule);
         pBuffer.writeEnum(this.color);
         pBuffer.writeComponent(this.playerPrefix);
         pBuffer.writeComponent(this.playerSuffix);
      }
   }
}