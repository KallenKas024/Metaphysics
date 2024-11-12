package net.minecraft.world.level.gameevent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface GameEventListener {
   /**
    * Gets the position of the listener itself.
    */
   PositionSource getListenerSource();

   /**
    * Gets the listening radius of the listener. Events within this radius will notify the listener when broadcasted.
    */
   int getListenerRadius();

   boolean handleGameEvent(ServerLevel pLevel, GameEvent pGameEvent, GameEvent.Context pContext, Vec3 pPos);

   default GameEventListener.DeliveryMode getDeliveryMode() {
      return GameEventListener.DeliveryMode.UNSPECIFIED;
   }

   public static enum DeliveryMode {
      UNSPECIFIED,
      BY_DISTANCE;
   }

   public interface Holder<T extends GameEventListener> {
      T getListener();
   }
}