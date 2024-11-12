package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookPage {
   public static final int ITEMS_PER_PAGE = 20;
   private final List<RecipeButton> buttons = Lists.newArrayListWithCapacity(20);
   @Nullable
   private RecipeButton hoveredButton;
   private final OverlayRecipeComponent overlay = new OverlayRecipeComponent();
   private Minecraft minecraft;
   private final List<RecipeShownListener> showListeners = Lists.newArrayList();
   private List<RecipeCollection> recipeCollections = ImmutableList.of();
   private StateSwitchingButton forwardButton;
   private StateSwitchingButton backButton;
   private int totalPages;
   private int currentPage;
   private RecipeBook recipeBook;
   @Nullable
   private Recipe<?> lastClickedRecipe;
   @Nullable
   private RecipeCollection lastClickedRecipeCollection;

   public RecipeBookPage() {
      for(int i = 0; i < 20; ++i) {
         this.buttons.add(new RecipeButton());
      }

   }

   public void init(Minecraft pMinecraft, int pX, int pY) {
      this.minecraft = pMinecraft;
      this.recipeBook = pMinecraft.player.getRecipeBook();

      for(int i = 0; i < this.buttons.size(); ++i) {
         this.buttons.get(i).setPosition(pX + 11 + 25 * (i % 5), pY + 31 + 25 * (i / 5));
      }

      this.forwardButton = new StateSwitchingButton(pX + 93, pY + 137, 12, 17, false);
      this.forwardButton.initTextureValues(1, 208, 13, 18, RecipeBookComponent.RECIPE_BOOK_LOCATION);
      this.backButton = new StateSwitchingButton(pX + 38, pY + 137, 12, 17, true);
      this.backButton.initTextureValues(1, 208, 13, 18, RecipeBookComponent.RECIPE_BOOK_LOCATION);
   }

   public void addListener(RecipeBookComponent pListener) {
      this.showListeners.remove(pListener);
      this.showListeners.add(pListener);
   }

   public void updateCollections(List<RecipeCollection> pRecipeCollections, boolean pResetPageNumber) {
      this.recipeCollections = pRecipeCollections;
      this.totalPages = (int)Math.ceil((double)pRecipeCollections.size() / 20.0D);
      if (this.totalPages <= this.currentPage || pResetPageNumber) {
         this.currentPage = 0;
      }

      this.updateButtonsForPage();
   }

   private void updateButtonsForPage() {
      int i = 20 * this.currentPage;

      for(int j = 0; j < this.buttons.size(); ++j) {
         RecipeButton recipebutton = this.buttons.get(j);
         if (i + j < this.recipeCollections.size()) {
            RecipeCollection recipecollection = this.recipeCollections.get(i + j);
            recipebutton.init(recipecollection, this);
            recipebutton.visible = true;
         } else {
            recipebutton.visible = false;
         }
      }

      this.updateArrowButtons();
   }

   private void updateArrowButtons() {
      this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
      this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
   }

   public void render(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.totalPages > 1) {
         String s = this.currentPage + 1 + "/" + this.totalPages;
         int i = this.minecraft.font.width(s);
         pGuiGraphics.drawString(this.minecraft.font, s, pX - i / 2 + 73, pY + 141, -1, false);
      }

      this.hoveredButton = null;

      for(RecipeButton recipebutton : this.buttons) {
         recipebutton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         if (recipebutton.visible && recipebutton.isHoveredOrFocused()) {
            this.hoveredButton = recipebutton;
         }
      }

      this.backButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.forwardButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.overlay.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
      if (this.minecraft.screen != null && this.hoveredButton != null && !this.overlay.isVisible()) {
         pGuiGraphics.renderComponentTooltip(this.minecraft.font, this.hoveredButton.getTooltipText(), pX, pY);
      }

   }

   @Nullable
   public Recipe<?> getLastClickedRecipe() {
      return this.lastClickedRecipe;
   }

   @Nullable
   public RecipeCollection getLastClickedRecipeCollection() {
      return this.lastClickedRecipeCollection;
   }

   public void setInvisible() {
      this.overlay.setVisible(false);
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton, int p_100413_, int p_100414_, int p_100415_, int p_100416_) {
      this.lastClickedRecipe = null;
      this.lastClickedRecipeCollection = null;
      if (this.overlay.isVisible()) {
         if (this.overlay.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.lastClickedRecipe = this.overlay.getLastRecipeClicked();
            this.lastClickedRecipeCollection = this.overlay.getRecipeCollection();
         } else {
            this.overlay.setVisible(false);
         }

         return true;
      } else if (this.forwardButton.mouseClicked(pMouseX, pMouseY, pButton)) {
         ++this.currentPage;
         this.updateButtonsForPage();
         return true;
      } else if (this.backButton.mouseClicked(pMouseX, pMouseY, pButton)) {
         --this.currentPage;
         this.updateButtonsForPage();
         return true;
      } else {
         for(RecipeButton recipebutton : this.buttons) {
            if (recipebutton.mouseClicked(pMouseX, pMouseY, pButton)) {
               if (pButton == 0) {
                  this.lastClickedRecipe = recipebutton.getRecipe();
                  this.lastClickedRecipeCollection = recipebutton.getCollection();
               } else if (pButton == 1 && !this.overlay.isVisible() && !recipebutton.isOnlyOption()) {
                  this.overlay.init(this.minecraft, recipebutton.getCollection(), recipebutton.getX(), recipebutton.getY(), p_100413_ + p_100415_ / 2, p_100414_ + 13 + p_100416_ / 2, (float)recipebutton.getWidth());
               }

               return true;
            }
         }

         return false;
      }
   }

   public void recipesShown(List<Recipe<?>> pRecipes) {
      for(RecipeShownListener recipeshownlistener : this.showListeners) {
         recipeshownlistener.recipesShown(pRecipes);
      }

   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public RecipeBook getRecipeBook() {
      return this.recipeBook;
   }

   protected void listButtons(Consumer<AbstractWidget> pConsumer) {
      pConsumer.accept(this.forwardButton);
      pConsumer.accept(this.backButton);
      this.buttons.forEach(pConsumer);
   }
}