package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.Validate;

public class LinearPalette<T> implements Palette<T> {
   private final IdMap<T> registry;
   private final T[] values;
   private final PaletteResize<T> resizeHandler;
   private final int bits;
   private int size;

   private LinearPalette(IdMap<T> pRegistry, int pBits, PaletteResize<T> pResizeHandler, List<T> pValues) {
      this.registry = pRegistry;
      this.values = (T[])(new Object[1 << pBits]);
      this.bits = pBits;
      this.resizeHandler = pResizeHandler;
      Validate.isTrue(pValues.size() <= this.values.length, "Can't initialize LinearPalette of size %d with %d entries", this.values.length, pValues.size());

      for(int i = 0; i < pValues.size(); ++i) {
         this.values[i] = pValues.get(i);
      }

      this.size = pValues.size();
   }

   private LinearPalette(IdMap<T> pRegistry, T[] pValues, PaletteResize<T> pResizeHandler, int pBits, int pSize) {
      this.registry = pRegistry;
      this.values = pValues;
      this.resizeHandler = pResizeHandler;
      this.bits = pBits;
      this.size = pSize;
   }

   public static <A> Palette<A> create(int pBits, IdMap<A> pRegistry, PaletteResize<A> pResizeHandler, List<A> pValues) {
      return new LinearPalette<>(pRegistry, pBits, pResizeHandler, pValues);
   }

   public int idFor(T pState) {
      for(int i = 0; i < this.size; ++i) {
         if (this.values[i] == pState) {
            return i;
         }
      }

      int j = this.size;
      if (j < this.values.length) {
         this.values[j] = pState;
         ++this.size;
         return j;
      } else {
         return this.resizeHandler.onResize(this.bits + 1, pState);
      }
   }

   public boolean maybeHas(Predicate<T> pFilter) {
      for(int i = 0; i < this.size; ++i) {
         if (pFilter.test(this.values[i])) {
            return true;
         }
      }

      return false;
   }

   public T valueFor(int pId) {
      if (pId >= 0 && pId < this.size) {
         return this.values[pId];
      } else {
         throw new MissingPaletteEntryException(pId);
      }
   }

   public void read(FriendlyByteBuf pBuffer) {
      this.size = pBuffer.readVarInt();

      for(int i = 0; i < this.size; ++i) {
         this.values[i] = this.registry.byIdOrThrow(pBuffer.readVarInt());
      }

   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.size);

      for(int i = 0; i < this.size; ++i) {
         pBuffer.writeVarInt(this.registry.getId(this.values[i]));
      }

   }

   public int getSerializedSize() {
      int i = FriendlyByteBuf.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values[j]));
      }

      return i;
   }

   public int getSize() {
      return this.size;
   }

   public Palette<T> copy() {
      return new LinearPalette<>(this.registry, (T[])((Object[])this.values.clone()), this.resizeHandler, this.bits, this.size);
   }
}