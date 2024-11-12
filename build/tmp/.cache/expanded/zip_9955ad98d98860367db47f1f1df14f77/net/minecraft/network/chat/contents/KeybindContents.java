package net.minecraft.network.chat.contents;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class KeybindContents implements ComponentContents {
   private final String name;
   @Nullable
   private Supplier<Component> nameResolver;

   public KeybindContents(String pName) {
      this.name = pName;
   }

   private Component getNestedComponent() {
      if (this.nameResolver == null) {
         this.nameResolver = KeybindResolver.keyResolver.apply(this.name);
      }

      return this.nameResolver.get();
   }

   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pContentConsumer) {
      return this.getNestedComponent().visit(pContentConsumer);
   }

   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pStyledContentConsumer, Style pStyle) {
      return this.getNestedComponent().visit(pStyledContentConsumer, pStyle);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof KeybindContents) {
            KeybindContents keybindcontents = (KeybindContents)pOther;
            if (this.name.equals(keybindcontents.name)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public String toString() {
      return "keybind{" + this.name + "}";
   }

   public String getName() {
      return this.name;
   }
}