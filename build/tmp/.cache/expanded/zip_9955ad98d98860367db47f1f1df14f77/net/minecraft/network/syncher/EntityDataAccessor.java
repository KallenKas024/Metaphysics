package net.minecraft.network.syncher;

/**
 * A Key for {@link SynchedEntityData}.
 */
public class EntityDataAccessor<T> {
   private final int id;
   private final EntityDataSerializer<T> serializer;

   public EntityDataAccessor(int pId, EntityDataSerializer<T> pSerializer) {
      this.id = pId;
      this.serializer = pSerializer;
   }

   public int getId() {
      return this.id;
   }

   public EntityDataSerializer<T> getSerializer() {
      return this.serializer;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         EntityDataAccessor<?> entitydataaccessor = (EntityDataAccessor)pOther;
         return this.id == entitydataaccessor.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id;
   }

   public String toString() {
      return "<entity data: " + this.id + ">";
   }
}