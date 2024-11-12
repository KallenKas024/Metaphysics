package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class InventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
   private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
   /** The old x position of the mouse pointer */
   private float xMouse;
   /** The old y position of the mouse pointer */
   private float yMouse;
   private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
   private boolean widthTooNarrow;
   private boolean buttonClicked;

   public InventoryScreen(Player pPlayer) {
      super(pPlayer.inventoryMenu, pPlayer.getInventory(), Component.translatable("container.crafting"));
      this.titleLabelX = 97;
   }

   public void containerTick() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
      } else {
         this.recipeBookComponent.tick();
      }
   }

   protected void init() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
      } else {
         super.init();
         this.widthTooNarrow = this.width < 379;
         this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
         this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_289631_) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            p_289631_.setPosition(this.leftPos + 104, this.height / 2 - 22);
            this.buttonClicked = true;
         }));
         this.addWidget(this.recipeBookComponent);
         this.setInitialFocus(this.recipeBookComponent);
      }
   }

   protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
      pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
         this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
         this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      } else {
         this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         this.recipeBookComponent.renderGhostRecipe(pGuiGraphics, this.leftPos, this.topPos, false, pPartialTick);
      }

      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
      this.recipeBookComponent.renderTooltip(pGuiGraphics, this.leftPos, this.topPos, pMouseX, pMouseY);
      this.xMouse = (float)pMouseX;
      this.yMouse = (float)pMouseY;
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      int i = this.leftPos;
      int j = this.topPos;
      pGuiGraphics.blit(INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
      renderEntityInInventoryFollowsMouse(pGuiGraphics, i + 51, j + 75, 30, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.minecraft.player);
   }

   public static void renderEntityInInventoryFollowsMouse(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float pMouseX, float pMouseY, LivingEntity pEntity) {
      float f = (float)Math.atan((double)(pMouseX / 40.0F));
      float f1 = (float)Math.atan((double)(pMouseY / 40.0F));
      // Forge: Allow passing in direct angle components instead of mouse position
      renderEntityInInventoryFollowsAngle(pGuiGraphics, pX, pY, pScale, f, f1, pEntity);
   }

   public static void renderEntityInInventoryFollowsAngle(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float angleXComponent, float angleYComponent, LivingEntity pEntity) {
      float f = angleXComponent;
      float f1 = angleYComponent;
      Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
      Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F));
      quaternionf.mul(quaternionf1);
      float f2 = pEntity.yBodyRot;
      float f3 = pEntity.getYRot();
      float f4 = pEntity.getXRot();
      float f5 = pEntity.yHeadRotO;
      float f6 = pEntity.yHeadRot;
      pEntity.yBodyRot = 180.0F + f * 20.0F;
      pEntity.setYRot(180.0F + f * 40.0F);
      pEntity.setXRot(-f1 * 20.0F);
      pEntity.yHeadRot = pEntity.getYRot();
      pEntity.yHeadRotO = pEntity.getYRot();
      renderEntityInInventory(pGuiGraphics, pX, pY, pScale, quaternionf, quaternionf1, pEntity);
      pEntity.yBodyRot = f2;
      pEntity.setYRot(f3);
      pEntity.setXRot(f4);
      pEntity.yHeadRotO = f5;
      pEntity.yHeadRot = f6;
   }

   public static void renderEntityInInventory(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, Quaternionf pPose, @Nullable Quaternionf pCameraOrientation, LivingEntity pEntity) {
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate((double)pX, (double)pY, 50.0D);
      pGuiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)pScale, (float)pScale, (float)(-pScale)));
      pGuiGraphics.pose().mulPose(pPose);
      Lighting.setupForEntityInInventory();
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      if (pCameraOrientation != null) {
         pCameraOrientation.conjugate();
         entityrenderdispatcher.overrideCameraOrientation(pCameraOrientation);
      }

      entityrenderdispatcher.setRenderShadow(false);
      RenderSystem.runAsFancy(() -> {
         entityrenderdispatcher.render(pEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, pGuiGraphics.pose(), pGuiGraphics.bufferSource(), 15728880);
      });
      pGuiGraphics.flush();
      entityrenderdispatcher.setRenderShadow(true);
      pGuiGraphics.pose().popPose();
      Lighting.setupFor3DItems();
   }

   protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
      return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.recipeBookComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   /**
    * Called when a mouse button is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was released.
    */
   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      if (this.buttonClicked) {
         this.buttonClicked = false;
         return true;
      } else {
         return super.mouseReleased(pMouseX, pMouseY, pButton);
      }
   }

   protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
      boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
      return this.recipeBookComponent.hasClickedOutside(pMouseX, pMouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pMouseButton) && flag;
   }

   /**
    * Called when the mouse is clicked over a slot or outside the gui.
    */
   protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
      super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
      this.recipeBookComponent.slotClicked(pSlot);
   }

   public void recipesUpdated() {
      this.recipeBookComponent.recipesUpdated();
   }

   public RecipeBookComponent getRecipeBookComponent() {
      return this.recipeBookComponent;
   }
}
