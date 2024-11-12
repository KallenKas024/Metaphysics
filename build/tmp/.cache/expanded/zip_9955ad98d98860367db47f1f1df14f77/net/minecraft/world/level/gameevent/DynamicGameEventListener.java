package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class DynamicGameEventListener<T extends GameEventListener> {
   private final T listener;
   @Nullable
   private SectionPos lastSection;

   public DynamicGameEventListener(T pListener) {
      this.listener = pListener;
   }

   public void add(ServerLevel pLevel) {
      this.move(pLevel);
   }

   public T getListener() {
      return this.listener;
   }

   public void remove(ServerLevel pLevel) {
      ifChunkExists(pLevel, this.lastSection, (p_248453_) -> {
         p_248453_.unregister(this.listener);
      });
   }

   public void move(ServerLevel pLevel) {
      this.listener.getListenerSource().getPosition(pLevel).map(SectionPos::of).ifPresent((p_223621_) -> {
         if (this.lastSection == null || !this.lastSection.equals(p_223621_)) {
            ifChunkExists(pLevel, this.lastSection, (p_248452_) -> {
               p_248452_.unregister(this.listener);
            });
            this.lastSection = p_223621_;
            ifChunkExists(pLevel, this.lastSection, (p_248451_) -> {
               p_248451_.register(this.listener);
            });
         }

      });
   }

   private static void ifChunkExists(LevelReader pLevel, @Nullable SectionPos pSectionPos, Consumer<GameEventListenerRegistry> pDispatcherConsumer) {
      if (pSectionPos != null) {
         ChunkAccess chunkaccess = pLevel.getChunk(pSectionPos.x(), pSectionPos.z(), ChunkStatus.FULL, false);
         if (chunkaccess != null) {
            pDispatcherConsumer.accept(chunkaccess.getListenerRegistry(pSectionPos.y()));
         }

      }
   }
}