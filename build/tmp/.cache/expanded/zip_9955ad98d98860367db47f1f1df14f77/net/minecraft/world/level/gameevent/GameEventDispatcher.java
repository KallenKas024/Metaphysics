package net.minecraft.world.level.gameevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

public class GameEventDispatcher {
   private final ServerLevel level;

   public GameEventDispatcher(ServerLevel pLevel) {
      this.level = pLevel;
   }

   public void post(GameEvent pEvent, Vec3 pPos, GameEvent.Context pContext) {
      int i = pEvent.getNotificationRadius();
      BlockPos blockpos = BlockPos.containing(pPos);
      int j = SectionPos.blockToSectionCoord(blockpos.getX() - i);
      int k = SectionPos.blockToSectionCoord(blockpos.getY() - i);
      int l = SectionPos.blockToSectionCoord(blockpos.getZ() - i);
      int i1 = SectionPos.blockToSectionCoord(blockpos.getX() + i);
      int j1 = SectionPos.blockToSectionCoord(blockpos.getY() + i);
      int k1 = SectionPos.blockToSectionCoord(blockpos.getZ() + i);
      List<GameEvent.ListenerInfo> list = new ArrayList<>();
      GameEventListenerRegistry.ListenerVisitor gameeventlistenerregistry$listenervisitor = (p_251272_, p_248685_) -> {
         if (p_251272_.getDeliveryMode() == GameEventListener.DeliveryMode.BY_DISTANCE) {
            list.add(new GameEvent.ListenerInfo(pEvent, pPos, pContext, p_251272_, p_248685_));
         } else {
            p_251272_.handleGameEvent(this.level, pEvent, pContext, pPos);
         }

      };
      boolean flag = false;

      for(int l1 = j; l1 <= i1; ++l1) {
         for(int i2 = l; i2 <= k1; ++i2) {
            ChunkAccess chunkaccess = this.level.getChunkSource().getChunkNow(l1, i2);
            if (chunkaccess != null) {
               for(int j2 = k; j2 <= j1; ++j2) {
                  flag |= chunkaccess.getListenerRegistry(j2).visitInRangeListeners(pEvent, pPos, pContext, gameeventlistenerregistry$listenervisitor);
               }
            }
         }
      }

      if (!list.isEmpty()) {
         this.handleGameEventMessagesInQueue(list);
      }

      if (flag) {
         DebugPackets.sendGameEventInfo(this.level, pEvent, pPos);
      }

   }

   private void handleGameEventMessagesInQueue(List<GameEvent.ListenerInfo> pListenerInfos) {
      Collections.sort(pListenerInfos);

      for(GameEvent.ListenerInfo gameevent$listenerinfo : pListenerInfos) {
         GameEventListener gameeventlistener = gameevent$listenerinfo.recipient();
         gameeventlistener.handleGameEvent(this.level, gameevent$listenerinfo.gameEvent(), gameevent$listenerinfo.context(), gameevent$listenerinfo.source());
      }

   }
}