package net.minecraft.world.level.gameevent;

import net.minecraft.world.phys.Vec3;

public interface GameEventListenerRegistry {
   GameEventListenerRegistry NOOP = new GameEventListenerRegistry() {
      public boolean isEmpty() {
         return true;
      }

      public void register(GameEventListener p_251092_) {
      }

      public void unregister(GameEventListener p_251937_) {
      }

      public boolean visitInRangeListeners(GameEvent p_251260_, Vec3 p_249086_, GameEvent.Context p_249012_, GameEventListenerRegistry.ListenerVisitor p_252106_) {
         return false;
      }
   };

   boolean isEmpty();

   void register(GameEventListener pListener);

   void unregister(GameEventListener pListener);

   boolean visitInRangeListeners(GameEvent pEvent, Vec3 pPos, GameEvent.Context pContext, GameEventListenerRegistry.ListenerVisitor pListenerVisitor);

   @FunctionalInterface
   public interface ListenerVisitor {
      void visit(GameEventListener pListener, Vec3 pPos);
   }
}