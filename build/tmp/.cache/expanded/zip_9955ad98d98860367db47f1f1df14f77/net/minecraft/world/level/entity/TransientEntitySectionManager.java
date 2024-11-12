package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class TransientEntitySectionManager<T extends EntityAccess> {
   static final Logger LOGGER = LogUtils.getLogger();
   final LevelCallback<T> callbacks;
   final EntityLookup<T> entityStorage;
   final EntitySectionStorage<T> sectionStorage;
   private final LongSet tickingChunks = new LongOpenHashSet();
   private final LevelEntityGetter<T> entityGetter;

   public TransientEntitySectionManager(Class<T> pClazz, LevelCallback<T> pCallbacks) {
      this.entityStorage = new EntityLookup<>();
      this.sectionStorage = new EntitySectionStorage<>(pClazz, (p_157647_) -> {
         return this.tickingChunks.contains(p_157647_) ? Visibility.TICKING : Visibility.TRACKED;
      });
      this.callbacks = pCallbacks;
      this.entityGetter = new LevelEntityGetterAdapter<>(this.entityStorage, this.sectionStorage);
   }

   public void startTicking(ChunkPos pPos) {
      long i = pPos.toLong();
      this.tickingChunks.add(i);
      this.sectionStorage.getExistingSectionsInChunk(i).forEach((p_157663_) -> {
         Visibility visibility = p_157663_.updateChunkStatus(Visibility.TICKING);
         if (!visibility.isTicking()) {
            p_157663_.getEntities().filter((p_157666_) -> {
               return !p_157666_.isAlwaysTicking();
            }).forEach(this.callbacks::onTickingStart);
         }

      });
   }

   public void stopTicking(ChunkPos pPos) {
      long i = pPos.toLong();
      this.tickingChunks.remove(i);
      this.sectionStorage.getExistingSectionsInChunk(i).forEach((p_157656_) -> {
         Visibility visibility = p_157656_.updateChunkStatus(Visibility.TRACKED);
         if (visibility.isTicking()) {
            p_157656_.getEntities().filter((p_157661_) -> {
               return !p_157661_.isAlwaysTicking();
            }).forEach(this.callbacks::onTickingEnd);
         }

      });
   }

   public LevelEntityGetter<T> getEntityGetter() {
      return this.entityGetter;
   }

   public void addEntity(T pEntity) {
      this.entityStorage.add(pEntity);
      long i = SectionPos.asLong(pEntity.blockPosition());
      EntitySection<T> entitysection = this.sectionStorage.getOrCreateSection(i);
      entitysection.add(pEntity);
      pEntity.setLevelCallback(new TransientEntitySectionManager.Callback(pEntity, i, entitysection));
      this.callbacks.onCreated(pEntity);
      this.callbacks.onTrackingStart(pEntity);
      if (pEntity.isAlwaysTicking() || entitysection.getStatus().isTicking()) {
         this.callbacks.onTickingStart(pEntity);
      }

   }

   @VisibleForDebug
   public int count() {
      return this.entityStorage.count();
   }

   void removeSectionIfEmpty(long pSection, EntitySection<T> pEntitySection) {
      if (pEntitySection.isEmpty()) {
         this.sectionStorage.remove(pSection);
      }

   }

   @VisibleForDebug
   public String gatherStats() {
      return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
   }

   class Callback implements EntityInLevelCallback {
      private final T entity;
      private final Entity realEntity;
      private long currentSectionKey;
      private EntitySection<T> currentSection;

      Callback(T pEntity, long pSection, EntitySection<T> pCurrentSection) {
         this.entity = pEntity;
         this.realEntity = pEntity instanceof Entity ? (Entity) pEntity : null;
         this.currentSectionKey = pSection;
         this.currentSection = pCurrentSection;
      }

      public void onMove() {
         BlockPos blockpos = this.entity.blockPosition();
         long i = SectionPos.asLong(blockpos);
         if (i != this.currentSectionKey) {
            Visibility visibility = this.currentSection.getStatus();
            if (!this.currentSection.remove(this.entity)) {
               TransientEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", this.entity, SectionPos.of(this.currentSectionKey), i);
            }

            TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
            EntitySection<T> entitysection = TransientEntitySectionManager.this.sectionStorage.getOrCreateSection(i);
            entitysection.add(this.entity);
            long oldSectionKey = currentSectionKey;
            this.currentSection = entitysection;
            this.currentSectionKey = i;
            TransientEntitySectionManager.this.callbacks.onSectionChange(this.entity);
            if (!this.entity.isAlwaysTicking()) {
               boolean flag = visibility.isTicking();
               boolean flag1 = entitysection.getStatus().isTicking();
               if (flag && !flag1) {
                  TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
               } else if (!flag && flag1) {
                  TransientEntitySectionManager.this.callbacks.onTickingStart(this.entity);
               }
            }
            if (this.realEntity != null) net.minecraftforge.common.ForgeHooks.onEntityEnterSection(this.realEntity, oldSectionKey, i);
         }

      }

      public void onRemove(Entity.RemovalReason pReason) {
         if (!this.currentSection.remove(this.entity)) {
            TransientEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, SectionPos.of(this.currentSectionKey), pReason);
         }

         Visibility visibility = this.currentSection.getStatus();
         if (visibility.isTicking() || this.entity.isAlwaysTicking()) {
            TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
         }

         TransientEntitySectionManager.this.callbacks.onTrackingEnd(this.entity);
         TransientEntitySectionManager.this.callbacks.onDestroyed(this.entity);
         TransientEntitySectionManager.this.entityStorage.remove(this.entity);
         this.entity.setLevelCallback(NULL);
         TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
      }
   }
}
