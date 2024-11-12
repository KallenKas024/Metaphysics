package net.minecraft.network.syncher;

import java.util.Optional;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Handles encoding and decoding of data for {@link SynchedEntityData}.
 * Note that mods cannot add new serializers, because this is not a managed registry and the serializer ID is limited to
 * 16.
 */
public interface EntityDataSerializer<T> {
   void write(FriendlyByteBuf pBuffer, T pValue);

   T read(FriendlyByteBuf pBuffer);

   default EntityDataAccessor<T> createAccessor(int pId) {
      return new EntityDataAccessor<>(pId, this);
   }

   T copy(T pValue);

   static <T> EntityDataSerializer<T> simple(final FriendlyByteBuf.Writer<T> pValueWriter, final FriendlyByteBuf.Reader<T> pValueReader) {
      return new EntityDataSerializer.ForValueType<T>() {
         public void write(FriendlyByteBuf p_238109_, T p_238110_) {
            pValueWriter.accept(p_238109_, p_238110_);
         }

         public T read(FriendlyByteBuf p_238107_) {
            return pValueReader.apply(p_238107_);
         }
      };
   }

   static <T> EntityDataSerializer<Optional<T>> optional(FriendlyByteBuf.Writer<T> pValueWriter, FriendlyByteBuf.Reader<T> pValueReader) {
      return simple(pValueWriter.asOptional(), pValueReader.asOptional());
   }

   static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> pEnumType) {
      return simple(FriendlyByteBuf::writeEnum, (p_238094_) -> {
         return p_238094_.readEnum(pEnumType);
      });
   }

   static <T> EntityDataSerializer<T> simpleId(IdMap<T> pMap) {
      return simple((p_238088_, p_238089_) -> {
         p_238088_.writeId(pMap, (T)p_238089_);
      }, (p_238085_) -> {
         return p_238085_.<T>readById(pMap);
      });
   }

   public interface ForValueType<T> extends EntityDataSerializer<T> {
      default T copy(T p_238112_) {
         return p_238112_;
      }
   }
}