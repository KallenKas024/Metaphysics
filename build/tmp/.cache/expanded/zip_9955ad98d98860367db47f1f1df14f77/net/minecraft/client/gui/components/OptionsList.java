package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
   public OptionsList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
      super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
      this.centerListVertically = false;
   }

   public int addBig(OptionInstance<?> pOption) {
      return this.addEntry(OptionsList.Entry.big(this.minecraft.options, this.width, pOption));
   }

   public void addSmall(OptionInstance<?> pLeftOption, @Nullable OptionInstance<?> pRightOption) {
      this.addEntry(OptionsList.Entry.small(this.minecraft.options, this.width, pLeftOption, pRightOption));
   }

   public void addSmall(OptionInstance<?>[] pOptions) {
      for(int i = 0; i < pOptions.length; i += 2) {
         this.addSmall(pOptions[i], i < pOptions.length - 1 ? pOptions[i + 1] : null);
      }

   }

   public int getRowWidth() {
      return 400;
   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 32;
   }

   @Nullable
   public AbstractWidget findOption(OptionInstance<?> pOption) {
      for(OptionsList.Entry optionslist$entry : this.children()) {
         AbstractWidget abstractwidget = optionslist$entry.options.get(pOption);
         if (abstractwidget != null) {
            return abstractwidget;
         }
      }

      return null;
   }

   public Optional<AbstractWidget> getMouseOver(double pMouseX, double pMouseY) {
      for(OptionsList.Entry optionslist$entry : this.children()) {
         for(AbstractWidget abstractwidget : optionslist$entry.children) {
            if (abstractwidget.isMouseOver(pMouseX, pMouseY)) {
               return Optional.of(abstractwidget);
            }
         }
      }

      return Optional.empty();
   }

   @OnlyIn(Dist.CLIENT)
   protected static class Entry extends ContainerObjectSelectionList.Entry<OptionsList.Entry> {
      final Map<OptionInstance<?>, AbstractWidget> options;
      final List<AbstractWidget> children;

      private Entry(Map<OptionInstance<?>, AbstractWidget> pOptions) {
         this.options = pOptions;
         this.children = ImmutableList.copyOf(pOptions.values());
      }

      public static OptionsList.Entry big(Options pOptions, int pGuiWidth, OptionInstance<?> pOption) {
         return new OptionsList.Entry(ImmutableMap.of(pOption, pOption.createButton(pOptions, pGuiWidth / 2 - 155, 0, 310)));
      }

      public static OptionsList.Entry small(Options pOptions, int pGuiWidth, OptionInstance<?> pLeftOption, @Nullable OptionInstance<?> pRightOption) {
         AbstractWidget abstractwidget = pLeftOption.createButton(pOptions, pGuiWidth / 2 - 155, 0, 150);
         return pRightOption == null ? new OptionsList.Entry(ImmutableMap.of(pLeftOption, abstractwidget)) : new OptionsList.Entry(ImmutableMap.of(pLeftOption, abstractwidget, pRightOption, pRightOption.createButton(pOptions, pGuiWidth / 2 - 155 + 160, 0, 150)));
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.children.forEach((p_280776_) -> {
            p_280776_.setY(pTop);
            p_280776_.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         });
      }

      /**
       * {@return a List containing all GUI element children of this GUI element}
       */
      public List<? extends GuiEventListener> children() {
         return this.children;
      }

      public List<? extends NarratableEntry> narratables() {
         return this.children;
      }
   }
}