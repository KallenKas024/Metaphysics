package net.minecraft.world.level.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.AbortableIterationConsumer;
import org.slf4j.Logger;

public class EntityLookup<T extends EntityAccess> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Int2ObjectMap<T> byId = new Int2ObjectLinkedOpenHashMap<>();
   private final Map<UUID, T> byUuid = Maps.newHashMap();

   public <U extends T> void getEntities(EntityTypeTest<T, U> pTest, AbortableIterationConsumer<U> pConsumer) {
      for(T t : this.byId.values()) {
         U u = (U)((EntityAccess)pTest.tryCast(t));
         if (u != null && pConsumer.accept(u).shouldAbort()) {
            return;
         }
      }

   }

   public Iterable<T> getAllEntities() {
      return Iterables.unmodifiableIterable(this.byId.values());
   }

   public void add(T pEntity) {
      UUID uuid = pEntity.getUUID();
      if (this.byUuid.containsKey(uuid)) {
         LOGGER.warn("Duplicate entity UUID {}: {}", uuid, pEntity);
      } else {
         this.byUuid.put(uuid, pEntity);
         this.byId.put(pEntity.getId(), pEntity);
      }
   }

   public void remove(T pEntity) {
      this.byUuid.remove(pEntity.getUUID());
      this.byId.remove(pEntity.getId());
   }

   @Nullable
   public T getEntity(int pId) {
      return this.byId.get(pId);
   }

   @Nullable
   public T getEntity(UUID pUuid) {
      return this.byUuid.get(pUuid);
   }

   public int count() {
      return this.byUuid.size();
   }
}