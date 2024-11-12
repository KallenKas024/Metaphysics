package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventListenerRegistry implements GameEventListenerRegistry {
   private final List<GameEventListener> listeners = Lists.newArrayList();
   private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
   private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
   private boolean processing;
   private final ServerLevel level;
   private final int sectionY;
   private final EuclideanGameEventListenerRegistry.OnEmptyAction onEmptyAction;

   public EuclideanGameEventListenerRegistry(ServerLevel pLevel, int pSectionY, EuclideanGameEventListenerRegistry.OnEmptyAction pOnEmptyAction) {
      this.level = pLevel;
      this.sectionY = pSectionY;
      this.onEmptyAction = pOnEmptyAction;
   }

   public boolean isEmpty() {
      return this.listeners.isEmpty();
   }

   public void register(GameEventListener pListener) {
      if (this.processing) {
         this.listenersToAdd.add(pListener);
      } else {
         this.listeners.add(pListener);
      }

      DebugPackets.sendGameEventListenerInfo(this.level, pListener);
   }

   public void unregister(GameEventListener pListener) {
      if (this.processing) {
         this.listenersToRemove.add(pListener);
      } else {
         this.listeners.remove(pListener);
      }

      if (this.listeners.isEmpty()) {
         this.onEmptyAction.apply(this.sectionY);
      }

   }

   public boolean visitInRangeListeners(GameEvent pEvent, Vec3 pPos, GameEvent.Context pContext, GameEventListenerRegistry.ListenerVisitor pListenerVisitor) {
      this.processing = true;
      boolean flag = false;

      try {
         Iterator<GameEventListener> iterator = this.listeners.iterator();

         while(iterator.hasNext()) {
            GameEventListener gameeventlistener = iterator.next();
            if (this.listenersToRemove.remove(gameeventlistener)) {
               iterator.remove();
            } else {
               Optional<Vec3> optional = getPostableListenerPosition(this.level, pPos, gameeventlistener);
               if (optional.isPresent()) {
                  pListenerVisitor.visit(gameeventlistener, optional.get());
                  flag = true;
               }
            }
         }
      } finally {
         this.processing = false;
      }

      if (!this.listenersToAdd.isEmpty()) {
         this.listeners.addAll(this.listenersToAdd);
         this.listenersToAdd.clear();
      }

      if (!this.listenersToRemove.isEmpty()) {
         this.listeners.removeAll(this.listenersToRemove);
         this.listenersToRemove.clear();
      }

      return flag;
   }

   private static Optional<Vec3> getPostableListenerPosition(ServerLevel pLevel, Vec3 pPos, GameEventListener pListener) {
      Optional<Vec3> optional = pListener.getListenerSource().getPosition(pLevel);
      if (optional.isEmpty()) {
         return Optional.empty();
      } else {
         double d0 = optional.get().distanceToSqr(pPos);
         int i = pListener.getListenerRadius() * pListener.getListenerRadius();
         return d0 > (double)i ? Optional.empty() : optional;
      }
   }

   @FunctionalInterface
   public interface OnEmptyAction {
      void apply(int pSectionY);
   }
}