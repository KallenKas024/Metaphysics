package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ServerboundInteractPacket implements Packet<ServerGamePacketListener> {
   private final int entityId;
   private final ServerboundInteractPacket.Action action;
   private final boolean usingSecondaryAction;
   static final ServerboundInteractPacket.Action ATTACK_ACTION = new ServerboundInteractPacket.Action() {
      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.ATTACK;
      }

      public void dispatch(ServerboundInteractPacket.Handler p_179624_) {
         p_179624_.onAttack();
      }

      public void write(FriendlyByteBuf p_179622_) {
      }
   };

   private ServerboundInteractPacket(int pEntityId, boolean pUsingSecondaryAction, ServerboundInteractPacket.Action pAction) {
      this.entityId = pEntityId;
      this.action = pAction;
      this.usingSecondaryAction = pUsingSecondaryAction;
   }

   public static ServerboundInteractPacket createAttackPacket(Entity pEntity, boolean pUsingSecondaryAction) {
      return new ServerboundInteractPacket(pEntity.getId(), pUsingSecondaryAction, ATTACK_ACTION);
   }

   public static ServerboundInteractPacket createInteractionPacket(Entity pEntity, boolean pUsingSecondaryAction, InteractionHand pHand) {
      return new ServerboundInteractPacket(pEntity.getId(), pUsingSecondaryAction, new ServerboundInteractPacket.InteractionAction(pHand));
   }

   public static ServerboundInteractPacket createInteractionPacket(Entity pEntity, boolean pUsingSecondaryAction, InteractionHand pHand, Vec3 pInteractionLocation) {
      return new ServerboundInteractPacket(pEntity.getId(), pUsingSecondaryAction, new ServerboundInteractPacket.InteractionAtLocationAction(pHand, pInteractionLocation));
   }

   public ServerboundInteractPacket(FriendlyByteBuf pBuffer) {
      this.entityId = pBuffer.readVarInt();
      ServerboundInteractPacket.ActionType serverboundinteractpacket$actiontype = pBuffer.readEnum(ServerboundInteractPacket.ActionType.class);
      this.action = serverboundinteractpacket$actiontype.reader.apply(pBuffer);
      this.usingSecondaryAction = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeEnum(this.action.getType());
      this.action.write(pBuffer);
      pBuffer.writeBoolean(this.usingSecondaryAction);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleInteract(this);
   }

   @Nullable
   public Entity getTarget(ServerLevel pLevel) {
      return pLevel.getEntityOrPart(this.entityId);
   }

   public boolean isUsingSecondaryAction() {
      return this.usingSecondaryAction;
   }

   public void dispatch(ServerboundInteractPacket.Handler pHandler) {
      this.action.dispatch(pHandler);
   }

   interface Action {
      ServerboundInteractPacket.ActionType getType();

      void dispatch(ServerboundInteractPacket.Handler pHandler);

      void write(FriendlyByteBuf pBuffer);
   }

   static enum ActionType {
      INTERACT(ServerboundInteractPacket.InteractionAction::new),
      ATTACK((p_179639_) -> {
         return ServerboundInteractPacket.ATTACK_ACTION;
      }),
      INTERACT_AT(ServerboundInteractPacket.InteractionAtLocationAction::new);

      final Function<FriendlyByteBuf, ServerboundInteractPacket.Action> reader;

      private ActionType(Function<FriendlyByteBuf, ServerboundInteractPacket.Action> pReader) {
         this.reader = pReader;
      }
   }

   public interface Handler {
      void onInteraction(InteractionHand pHand);

      void onInteraction(InteractionHand pHand, Vec3 pInteractionLocation);

      void onAttack();
   }

   static class InteractionAction implements ServerboundInteractPacket.Action {
      private final InteractionHand hand;

      InteractionAction(InteractionHand pHand) {
         this.hand = pHand;
      }

      private InteractionAction(FriendlyByteBuf pBuffer) {
         this.hand = pBuffer.readEnum(InteractionHand.class);
      }

      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.INTERACT;
      }

      public void dispatch(ServerboundInteractPacket.Handler pHandler) {
         pHandler.onInteraction(this.hand);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeEnum(this.hand);
      }
   }

   static class InteractionAtLocationAction implements ServerboundInteractPacket.Action {
      private final InteractionHand hand;
      private final Vec3 location;

      InteractionAtLocationAction(InteractionHand pHand, Vec3 pLocation) {
         this.hand = pHand;
         this.location = pLocation;
      }

      private InteractionAtLocationAction(FriendlyByteBuf pBuffer) {
         this.location = new Vec3((double)pBuffer.readFloat(), (double)pBuffer.readFloat(), (double)pBuffer.readFloat());
         this.hand = pBuffer.readEnum(InteractionHand.class);
      }

      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.INTERACT_AT;
      }

      public void dispatch(ServerboundInteractPacket.Handler pHandler) {
         pHandler.onInteraction(this.hand, this.location);
      }

      public void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeFloat((float)this.location.x);
         pBuffer.writeFloat((float)this.location.y);
         pBuffer.writeFloat((float)this.location.z);
         pBuffer.writeEnum(this.hand);
      }
   }
}