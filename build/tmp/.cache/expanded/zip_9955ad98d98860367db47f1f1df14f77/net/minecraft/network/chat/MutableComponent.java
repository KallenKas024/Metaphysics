package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;

/**
 * A Component which can have its Style and siblings modified.
 */
public class MutableComponent implements Component {
   private final ComponentContents contents;
   private final List<Component> siblings;
   private Style style;
   private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
   @Nullable
   private Language decomposedWith;

   MutableComponent(ComponentContents pContents, List<Component> pSiblings, Style pStyle) {
      this.contents = pContents;
      this.siblings = pSiblings;
      this.style = pStyle;
   }

   public static MutableComponent create(ComponentContents pContents) {
      return new MutableComponent(pContents, Lists.newArrayList(), Style.EMPTY);
   }

   public ComponentContents getContents() {
      return this.contents;
   }

   /**
    * Gets the sibling components of this one.
    */
   public List<Component> getSiblings() {
      return this.siblings;
   }

   /**
    * Sets the style for this component and returns the component itself.
    */
   public MutableComponent setStyle(Style pStyle) {
      this.style = pStyle;
      return this;
   }

   /**
    * Gets the style of this component.
    */
   public Style getStyle() {
      return this.style;
   }

   /**
    * Add the given text to this component's siblings.
    * 
    * Note: If this component turns the text bold, that will apply to all the siblings until a later sibling turns the
    * text something else.
    */
   public MutableComponent append(String pString) {
      return this.append(Component.literal(pString));
   }

   /**
    * Add the given component to this component's siblings.
    * 
    * Note: If this component turns the text bold, that will apply to all the siblings until a later sibling turns the
    * text something else.
    */
   public MutableComponent append(Component pSibling) {
      this.siblings.add(pSibling);
      return this;
   }

   public MutableComponent withStyle(UnaryOperator<Style> pModifyFunc) {
      this.setStyle(pModifyFunc.apply(this.getStyle()));
      return this;
   }

   public MutableComponent withStyle(Style pStyle) {
      this.setStyle(pStyle.applyTo(this.getStyle()));
      return this;
   }

   public MutableComponent withStyle(ChatFormatting... pFormats) {
      this.setStyle(this.getStyle().applyFormats(pFormats));
      return this;
   }

   public MutableComponent withStyle(ChatFormatting pFormat) {
      this.setStyle(this.getStyle().applyFormat(pFormat));
      return this;
   }

   public FormattedCharSequence getVisualOrderText() {
      Language language = Language.getInstance();
      if (this.decomposedWith != language) {
         this.visualOrderText = language.getVisualOrder(this);
         this.decomposedWith = language;
      }

      return this.visualOrderText;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof MutableComponent)) {
         return false;
      } else {
         MutableComponent mutablecomponent = (MutableComponent)pOther;
         return this.contents.equals(mutablecomponent.contents) && this.style.equals(mutablecomponent.style) && this.siblings.equals(mutablecomponent.siblings);
      }
   }

   public int hashCode() {
      return Objects.hash(this.contents, this.style, this.siblings);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder(this.contents.toString());
      boolean flag = !this.style.isEmpty();
      boolean flag1 = !this.siblings.isEmpty();
      if (flag || flag1) {
         stringbuilder.append('[');
         if (flag) {
            stringbuilder.append("style=");
            stringbuilder.append((Object)this.style);
         }

         if (flag && flag1) {
            stringbuilder.append(", ");
         }

         if (flag1) {
            stringbuilder.append("siblings=");
            stringbuilder.append((Object)this.siblings);
         }

         stringbuilder.append(']');
      }

      return stringbuilder.toString();
   }
}