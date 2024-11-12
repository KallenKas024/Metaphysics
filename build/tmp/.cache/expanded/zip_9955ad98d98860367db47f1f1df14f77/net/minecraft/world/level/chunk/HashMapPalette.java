package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette<T> implements Palette<T> {
   private final IdMap<T> registry;
   private final CrudeIncrementalIntIdentityHashBiMap<T> values;
   private final PaletteResize<T> resizeHandler;
   private final int bits;

   public HashMapPalette(IdMap<T> pRegistry, int pBits, PaletteResize<T> pResizeHandler, List<T> pValues) {
      this(pRegistry, pBits, pResizeHandler);
      pValues.forEach(this.values::add);
   }

   public HashMapPalette(IdMap<T> pRegistry, int pBits, PaletteResize<T> pResizeHandler) {
      this(pRegistry, pBits, pResizeHandler, CrudeIncrementalIntIdentityHashBiMap.create(1 << pBits));
   }

   private HashMapPalette(IdMap<T> pRegistry, int pBits, PaletteResize<T> pResizeHandler, CrudeIncrementalIntIdentityHashBiMap<T> pValues) {
      this.registry = pRegistry;
      this.bits = pBits;
      this.resizeHandler = pResizeHandler;
      this.values = pValues;
   }

   public static <A> Palette<A> create(int pBits, IdMap<A> pRegistry, PaletteResize<A> pResizeHandler, List<A> pValues) {
      return new HashMapPalette<>(pRegistry, pBits, pResizeHandler, pValues);
   }

   public int idFor(T pState) {
      int i = this.values.getId(pState);
      if (i == -1) {
         i = this.values.add(pState);
         if (i >= 1 << this.bits) {
            i = this.resizeHandler.onResize(this.bits + 1, pState);
         }
      }

      return i;
   }

   public boolean maybeHas(Predicate<T> pFilter) {
      for(int i = 0; i < this.getSize(); ++i) {
         if (pFilter.test(this.values.byId(i))) {
            return true;
         }
      }

      return false;
   }

   public T valueFor(int pId) {
      T t = this.values.byId(pId);
      if (t == null) {
         throw new MissingPaletteEntryException(pId);
      } else {
         return t;
      }
   }

   public void read(FriendlyByteBuf pBuffer) {
      this.values.clear();
      int i = pBuffer.readVarInt();

      for(int j = 0; j < i; ++j) {
         this.values.add(this.registry.byIdOrThrow(pBuffer.readVarInt()));
      }

   }

   public void write(FriendlyByteBuf pBuffer) {
      int i = this.getSize();
      pBuffer.writeVarInt(i);

      for(int j = 0; j < i; ++j) {
         pBuffer.writeVarInt(this.registry.getId(this.values.byId(j)));
      }

   }

   public int getSerializedSize() {
      int i = FriendlyByteBuf.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(j)));
      }

      return i;
   }

   public List<T> getEntries() {
      ArrayList<T> arraylist = new ArrayList<>();
      this.values.iterator().forEachRemaining(arraylist::add);
      return arraylist;
   }

   public int getSize() {
      return this.values.size();
   }

   public Palette<T> copy() {
      return new HashMapPalette<>(this.registry, this.bits, this.resizeHandler, this.values.copy());
   }
}