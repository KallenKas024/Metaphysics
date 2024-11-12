package net.minecraft.network.protocol.game;

import java.util.UUID;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket implements Packet<ClientGamePacketListener> {
   private static final int FLAG_DARKEN = 1;
   private static final int FLAG_MUSIC = 2;
   private static final int FLAG_FOG = 4;
   private final UUID id;
   private final ClientboundBossEventPacket.Operation operation;
   static final ClientboundBossEventPacket.Operation REMOVE_OPERATION = new ClientboundBossEventPacket.Operation() {
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.REMOVE;
      }

      public void dispatch(UUID p_178660_, ClientboundBossEventPacket.Handler p_178661_) {
         p_178661_.remove(p_178660_);
      }

      public void write(FriendlyByteBuf p_178663_) {
      }
   };

   private ClientboundBossEventPacket(UUID pId, ClientboundBossEventPacket.Operation pOperation) {
      this.id = pId;
      this.operation = pOperation;
   }

   public ClientboundBossEventPacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readUUID();
      ClientboundBossEventPacket.OperationType clientboundbosseventpacket$operationtype = pBuffer.readEnum(ClientboundBossEventPacket.OperationType.class);
      this.operation = clientboundbosseventpacket$operationtype.reader.apply(pBuffer);
   }

   public static ClientboundBossEventPacket createAddPacket(BossEvent pEvent) {
      return new ClientboundBossEventPacket(pEvent.getId(), new ClientboundBossEventPacket.AddOperation(pEvent));
   }

   public static ClientboundBossEventPacket createRemovePacket(UUID pId) {
      return new ClientboundBossEventPacket(pId, REMOVE_OPERATION);
   }

   public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent pEvent) {
      return new ClientboundBossEventPacket(pEvent.getId(), new ClientboundBossEventPacket.UpdateProgressOperation(pEvent.getProgress()));
   }

   public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent pEvent) {
      return new ClientboundBossEventPacket(pEvent.getId(), new ClientboundBossEventPacket.UpdateNameOperation(pEvent.getName()));
   }

   public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent pEvent) {
      return new ClientboundBossEventPacket(pEvent.getId(), new ClientboundBossEventPacket.UpdateStyleOperation(pEvent.getColor(), pEvent.getOverlay()));
   }

   public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent pEvent) {
      return new ClientboundBossEventPacket(pEvent.getId(), new ClientboundBossEventPacket.UpdatePropertiesOperation(pEvent.shouldDarkenScreen(), pEvent.shouldPlayBossMusic(), pEvent.shouldCreateWorldFog()));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUUID(this.id);
      pBuffer.writeEnum(this.operation.getType());
      this.operation.write(pBuffer);
   }

   static int encodeProperties(boolean pDarkenScreen, boolean pPlayMusic, boolean pCreateWorldFog) {
      int i = 0;
      if (pDarkenScreen) {
         i |= 1;
      }

      if (pPlayMusic) {
         i |= 2;
      }

      if (pCreateWorldFog) {
         i |= 4;
      }

      return i;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleBossUpdate(this);
   }

   public void dispatch(ClientboundBossEventPacket.Handler pHandler) {
      this.operation.dispatch(this.id, pHandler);
   }

   static class AddOperation implements ClientboundBossEventPacket.Operation {
      private final Component name;
      private final float progress;
      private final BossEvent.BossBarColor color;
      private final BossEvent.BossBarOverlay overlay;
      private final boolean darkenScreen;
      private final boolean playMusic;
      private final boolean createWorldFog;

      AddOperation(BossEvent pEvent) {
         this.name = pEvent.getName();
         this.progress = pEvent.getProgress();
         this.color = pEvent.getColor();
         this.overlay = pEvent.getOverlay();
         this.darkenScreen = pEvent.shouldDarkenScreen();
         this.playMusic = pEvent.shouldPlayBossMusic();
         this.createWorldFog = pEvent.shouldCreateWorldFog();
      }

      private AddOperation(FriendlyByteBuf pBuffer) {
         this.name = pBuffer.readComponent();
         this.progress = pBuffer.readFloat();
         this.color = pBuffer.readEnum(BossEvent.BossBarColor.class);
         this.overlay = pBuffer.readEnum(BossEvent.BossBarOverlay.class);
         int i = pBuffer.readUnsignedByte();
         this.darkenScreen = (i & 1) > 0;
         this.playMusic = (i & 2) > 0;
         this.createWorldFog = (i & 4) > 0;
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.ADD;
      }

      public void dispatch(UUID pId, ClientboundBossEventPacket.Handler pHandler) {
         pHandler.add(pId, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeComponent(this.name);
         pBuffer.writeFloat(this.progress);
         pBuffer.writeEnum(this.color);
         pBuffer.writeEnum(this.overlay);
         pBuffer.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
      }
   }

   public interface Handler {
      default void add(UUID pId, Component pName, float pProgress, BossEvent.BossBarColor pColor, BossEvent.BossBarOverlay pOverlay, boolean pDarkenScreen, boolean pPlayMusic, boolean pCreateWorldFog) {
      }

      default void remove(UUID pId) {
      }

      default void updateProgress(UUID pId, float pProgress) {
      }

      default void updateName(UUID pId, Component pName) {
      }

      default void updateStyle(UUID pId, BossEvent.BossBarColor pColor, BossEvent.BossBarOverlay pOverlay) {
      }

      default void updateProperties(UUID pId, boolean pDarkenScreen, boolean pPlayMusic, boolean pCreateWorldFog) {
      }
   }

   interface Operation {
      ClientboundBossEventPacket.OperationType getType();

      void dispatch(UUID pId, ClientboundBossEventPacket.Handler pHandler);

      void write(FriendlyByteBuf pBuffer);
   }

   static enum OperationType {
      ADD(ClientboundBossEventPacket.AddOperation::new),
      REMOVE((p_178719_) -> {
         return ClientboundBossEventPacket.REMOVE_OPERATION;
      }),
      UPDATE_PROGRESS(ClientboundBossEventPacket.UpdateProgressOperation::new),
      UPDATE_NAME(ClientboundBossEventPacket.UpdateNameOperation::new),
      UPDATE_STYLE(ClientboundBossEventPacket.UpdateStyleOperation::new),
      UPDATE_PROPERTIES(ClientboundBossEventPacket.UpdatePropertiesOperation::new);

      final Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> reader;

      private OperationType(Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> pReader) {
         this.reader = pReader;
      }
   }

   static class UpdateNameOperation implements ClientboundBossEventPacket.Operation {
      private final Component name;

      UpdateNameOperation(Component pName) {
         this.name = pName;
      }

      private UpdateNameOperation(FriendlyByteBuf pBuffer) {
         this.name = pBuffer.readComponent();
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_NAME;
      }

      public void dispatch(UUID pId, ClientboundBossEventPacket.Handler pHandler) {
         pHandler.updateName(pId, this.name);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeComponent(this.name);
      }
   }

   static class UpdateProgressOperation implements ClientboundBossEventPacket.Operation {
      private final float progress;

      UpdateProgressOperation(float pProgress) {
         this.progress = pProgress;
      }

      private UpdateProgressOperation(FriendlyByteBuf pBuffer) {
         this.progress = pBuffer.readFloat();
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_PROGRESS;
      }

      public void dispatch(UUID pId, ClientboundBossEventPacket.Handler pHandler) {
         pHandler.updateProgress(pId, this.progress);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeFloat(this.progress);
      }
   }

   static class UpdatePropertiesOperation implements ClientboundBossEventPacket.Operation {
      private final boolean darkenScreen;
      private final boolean playMusic;
      private final boolean createWorldFog;

      UpdatePropertiesOperation(boolean pDarkenScreen, boolean pPlayMusic, boolean pCreateWorldFog) {
         this.darkenScreen = pDarkenScreen;
         this.playMusic = pPlayMusic;
         this.createWorldFog = pCreateWorldFog;
      }

      private UpdatePropertiesOperation(FriendlyByteBuf pBuffer) {
         int i = pBuffer.readUnsignedByte();
         this.darkenScreen = (i & 1) > 0;
         this.playMusic = (i & 2) > 0;
         this.createWorldFog = (i & 4) > 0;
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_PROPERTIES;
      }

      public void dispatch(UUID pId, ClientboundBossEventPacket.Handler pHandler) {
         pHandler.updateProperties(pId, this.darkenScreen, this.playMusic, this.createWorldFog);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
      }
   }

   static class UpdateStyleOperation implements ClientboundBossEventPacket.Operation {
      private final BossEvent.BossBarColor color;
      private final BossEvent.BossBarOverlay overlay;

      UpdateStyleOperation(BossEvent.BossBarColor pColor, BossEvent.BossBarOverlay pOverlay) {
         this.color = pColor;
         this.overlay = pOverlay;
      }

      private UpdateStyleOperation(FriendlyByteBuf pBuffer) {
         this.color = pBuffer.readEnum(BossEvent.BossBarColor.class);
         this.overlay = pBuffer.readEnum(BossEvent.BossBarOverlay.class);
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_STYLE;
      }

      public void dispatch(UUID pId, ClientboundBossEventPacket.Handler pHandler) {
         pHandler.updateStyle(pId, this.color, this.overlay);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeEnum(this.color);
         pBuffer.writeEnum(this.overlay);
      }
   }
}