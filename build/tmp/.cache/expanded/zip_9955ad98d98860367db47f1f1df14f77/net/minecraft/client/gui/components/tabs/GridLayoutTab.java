package net.minecraft.client.gui.components.tabs;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GridLayoutTab implements Tab {
   private final Component title;
   protected final GridLayout layout = new GridLayout();

   public GridLayoutTab(Component pTitle) {
      this.title = pTitle;
   }

   public Component getTabTitle() {
      return this.title;
   }

   public void visitChildren(Consumer<AbstractWidget> pConsumer) {
      this.layout.visitWidgets(pConsumer);
   }

   public void doLayout(ScreenRectangle pRectangle) {
      this.layout.arrangeElements();
      FrameLayout.alignInRectangle(this.layout, pRectangle, 0.5F, 0.16666667F);
   }
}