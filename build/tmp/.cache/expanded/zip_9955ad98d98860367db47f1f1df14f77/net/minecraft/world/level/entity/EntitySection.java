package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EntitySection<T extends EntityAccess> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ClassInstanceMultiMap<T> storage;
   private Visibility chunkStatus;

   public EntitySection(Class<T> pEntityClazz, Visibility pChunkStatus) {
      this.chunkStatus = pChunkStatus;
      this.storage = new ClassInstanceMultiMap<>(pEntityClazz);
   }

   public void add(T pEntity) {
      this.storage.add(pEntity);
   }

   public boolean remove(T pEntity) {
      return this.storage.remove(pEntity);
   }

   public AbortableIterationConsumer.Continuation getEntities(AABB pBounds, AbortableIterationConsumer<T> pConsumer) {
      for(T t : this.storage) {
         if (t.getBoundingBox().intersects(pBounds) && pConsumer.accept(t).shouldAbort()) {
            return AbortableIterationConsumer.Continuation.ABORT;
         }
      }

      return AbortableIterationConsumer.Continuation.CONTINUE;
   }

   public <U extends T> AbortableIterationConsumer.Continuation getEntities(EntityTypeTest<T, U> pTest, AABB pBounds, AbortableIterationConsumer<? super U> pConsumer) {
      Collection<? extends T> collection = this.storage.find(pTest.getBaseClass());
      if (collection.isEmpty()) {
         return AbortableIterationConsumer.Continuation.CONTINUE;
      } else {
         for(T t : collection) {
            U u = (U)((EntityAccess)pTest.tryCast(t));
            if (u != null && t.getBoundingBox().intersects(pBounds) && pConsumer.accept(u).shouldAbort()) {
               return AbortableIterationConsumer.Continuation.ABORT;
            }
         }

         return AbortableIterationConsumer.Continuation.CONTINUE;
      }
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public Stream<T> getEntities() {
      return this.storage.stream();
   }

   public Visibility getStatus() {
      return this.chunkStatus;
   }

   public Visibility updateChunkStatus(Visibility pChunkStatus) {
      Visibility visibility = this.chunkStatus;
      this.chunkStatus = pChunkStatus;
      return visibility;
   }

   @VisibleForDebug
   public int size() {
      return this.storage.size();
   }
}