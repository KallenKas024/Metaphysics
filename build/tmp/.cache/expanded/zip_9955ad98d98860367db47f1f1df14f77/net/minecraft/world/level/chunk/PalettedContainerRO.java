package net.minecraft.world.level.chunk;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface PalettedContainerRO<T> {
   T get(int pX, int pY, int pZ);

   void getAll(Consumer<T> pConsumer);

   void write(FriendlyByteBuf pBuffer);

   int getSerializedSize();

   boolean maybeHas(Predicate<T> pFilter);

   /**
    * Counts the number of instances of each state in the container.
    * The provided consumer is invoked for each state with the number of instances.
    */
   void count(PalettedContainer.CountConsumer<T> pCountConsumer);

   PalettedContainer<T> recreate();

   PalettedContainerRO.PackedData<T> pack(IdMap<T> pRegistry, PalettedContainer.Strategy pStrategy);

   public static record PackedData<T>(List<T> paletteEntries, Optional<LongStream> storage) {
   }

   public interface Unpacker<T, C extends PalettedContainerRO<T>> {
      DataResult<C> read(IdMap<T> pRegistry, PalettedContainer.Strategy pStrategy, PalettedContainerRO.PackedData<T> pPackedData);
   }
}