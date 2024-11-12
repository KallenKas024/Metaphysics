package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public class GlobalPalette<T> implements Palette<T> {
   private final IdMap<T> registry;

   public GlobalPalette(IdMap<T> pRegistry) {
      this.registry = pRegistry;
   }

   public static <A> Palette<A> create(int pBits, IdMap<A> pRegistry, PaletteResize<A> pResizeHandler, List<A> pValues) {
      return new GlobalPalette<>(pRegistry);
   }

   public int idFor(T pState) {
      int i = this.registry.getId(pState);
      return i == -1 ? 0 : i;
   }

   public boolean maybeHas(Predicate<T> pFilter) {
      return true;
   }

   public T valueFor(int pId) {
      T t = this.registry.byId(pId);
      if (t == null) {
         throw new MissingPaletteEntryException(pId);
      } else {
         return t;
      }
   }

   public void read(FriendlyByteBuf pBuffer) {
   }

   public void write(FriendlyByteBuf pBuffer) {
   }

   public int getSerializedSize() {
      return FriendlyByteBuf.getVarIntSize(0);
   }

   public int getSize() {
      return this.registry.size();
   }

   public Palette<T> copy() {
      return this;
   }
}