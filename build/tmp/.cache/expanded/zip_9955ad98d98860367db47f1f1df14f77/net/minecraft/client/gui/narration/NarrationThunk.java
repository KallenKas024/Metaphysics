package net.minecraft.client.gui.narration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NarrationThunk<T> {
   private final T contents;
   private final BiConsumer<Consumer<String>, T> converter;
   public static final NarrationThunk<?> EMPTY = new NarrationThunk<>(Unit.INSTANCE, (p_169171_, p_169172_) -> {
   });

   private NarrationThunk(T pContents, BiConsumer<Consumer<String>, T> pConverter) {
      this.contents = pContents;
      this.converter = pConverter;
   }

   public static NarrationThunk<?> from(String pText) {
      return new NarrationThunk<>(pText, Consumer::accept);
   }

   public static NarrationThunk<?> from(Component pComponent) {
      return new NarrationThunk<>(pComponent, (p_169174_, p_169175_) -> {
         p_169174_.accept(p_169175_.getString());
      });
   }

   public static NarrationThunk<?> from(List<Component> pComponents) {
      return new NarrationThunk<>(pComponents, (p_169166_, p_169167_) -> {
         pComponents.stream().map(Component::getString).forEach(p_169166_);
      });
   }

   public void getText(Consumer<String> pConsumer) {
      this.converter.accept(pConsumer, this.contents);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof NarrationThunk)) {
         return false;
      } else {
         NarrationThunk<?> narrationthunk = (NarrationThunk)pOther;
         return narrationthunk.converter == this.converter && narrationthunk.contents.equals(this.contents);
      }
   }

   public int hashCode() {
      int i = this.contents.hashCode();
      return 31 * i + this.converter.hashCode();
   }
}