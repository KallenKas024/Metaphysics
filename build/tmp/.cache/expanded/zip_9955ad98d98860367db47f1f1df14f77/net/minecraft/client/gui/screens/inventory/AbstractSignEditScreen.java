package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSignEditScreen extends Screen {
   /** Reference to the sign object. */
   private final SignBlockEntity sign;
   private SignText text;
   private final String[] messages;
   private final boolean isFrontText;
   protected final WoodType woodType;
   /** Counts the number of screen updates. */
   private int frame;
   /** The index of the line that is being edited. */
   private int line;
   @Nullable
   private TextFieldHelper signField;

   public AbstractSignEditScreen(SignBlockEntity pSign, boolean pIsFrontText, boolean pIsFiltered) {
      this(pSign, pIsFrontText, pIsFiltered, Component.translatable("sign.edit"));
   }

   public AbstractSignEditScreen(SignBlockEntity pSign, boolean pIsFrontText, boolean pIsFiltered, Component pTitle) {
      super(pTitle);
      this.sign = pSign;
      this.text = pSign.getText(pIsFrontText);
      this.isFrontText = pIsFrontText;
      this.woodType = SignBlock.getWoodType(pSign.getBlockState().getBlock());
      this.messages = IntStream.range(0, 4).mapToObj((p_277214_) -> {
         return this.text.getMessage(p_277214_, pIsFiltered);
      }).map(Component::getString).toArray((p_249111_) -> {
         return new String[p_249111_];
      });
   }

   protected void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_251194_) -> {
         this.onDone();
      }).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
      this.signField = new TextFieldHelper(() -> {
         return this.messages[this.line];
      }, this::setMessage, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (p_280850_) -> {
         return this.minecraft.font.width(p_280850_) <= this.sign.getMaxTextLineWidth();
      });
   }

   public void tick() {
      ++this.frame;
      if (!this.isValid()) {
         this.onDone();
      }

   }

   private boolean isValid() {
      return this.minecraft != null && this.minecraft.player != null && !this.sign.isRemoved() && !this.sign.playerIsTooFarAwayToEdit(this.minecraft.player.getUUID());
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 265) {
         this.line = this.line - 1 & 3;
         this.signField.setCursorToEnd();
         return true;
      } else if (pKeyCode != 264 && pKeyCode != 257 && pKeyCode != 335) {
         return this.signField.keyPressed(pKeyCode) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
      } else {
         this.line = this.line + 1 & 3;
         this.signField.setCursorToEnd();
         return true;
      }
   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean charTyped(char pCodePoint, int pModifiers) {
      this.signField.charTyped(pCodePoint);
      return true;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      Lighting.setupForFlatItems();
      this.renderBackground(pGuiGraphics);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
      this.renderSign(pGuiGraphics);
      Lighting.setupFor3DItems();
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void onClose() {
      this.onDone();
   }

   public void removed() {
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null) {
         clientpacketlistener.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.isFrontText, this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
      }

   }

   public boolean isPauseScreen() {
      return false;
   }

   protected abstract void renderSignBackground(GuiGraphics pGuiGraphics, BlockState pState);

   protected abstract Vector3f getSignTextScale();

   protected void offsetSign(GuiGraphics pGuiGraphics, BlockState pState) {
      pGuiGraphics.pose().translate((float)this.width / 2.0F, 90.0F, 50.0F);
   }

   private void renderSign(GuiGraphics pGuiGraphics) {
      BlockState blockstate = this.sign.getBlockState();
      pGuiGraphics.pose().pushPose();
      this.offsetSign(pGuiGraphics, blockstate);
      pGuiGraphics.pose().pushPose();
      this.renderSignBackground(pGuiGraphics, blockstate);
      pGuiGraphics.pose().popPose();
      this.renderSignText(pGuiGraphics);
      pGuiGraphics.pose().popPose();
   }

   private void renderSignText(GuiGraphics pGuiGraphics) {
      pGuiGraphics.pose().translate(0.0F, 0.0F, 4.0F);
      Vector3f vector3f = this.getSignTextScale();
      pGuiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());
      int i = this.text.getColor().getTextColor();
      boolean flag = this.frame / 6 % 2 == 0;
      int j = this.signField.getCursorPos();
      int k = this.signField.getSelectionPos();
      int l = 4 * this.sign.getTextLineHeight() / 2;
      int i1 = this.line * this.sign.getTextLineHeight() - l;

      for(int j1 = 0; j1 < this.messages.length; ++j1) {
         String s = this.messages[j1];
         if (s != null) {
            if (this.font.isBidirectional()) {
               s = this.font.bidirectionalShaping(s);
            }

            int k1 = -this.font.width(s) / 2;
            pGuiGraphics.drawString(this.font, s, k1, j1 * this.sign.getTextLineHeight() - l, i, false);
            if (j1 == this.line && j >= 0 && flag) {
               int l1 = this.font.width(s.substring(0, Math.max(Math.min(j, s.length()), 0)));
               int i2 = l1 - this.font.width(s) / 2;
               if (j >= s.length()) {
                  pGuiGraphics.drawString(this.font, "_", i2, i1, i, false);
               }
            }
         }
      }

      for(int k3 = 0; k3 < this.messages.length; ++k3) {
         String s1 = this.messages[k3];
         if (s1 != null && k3 == this.line && j >= 0) {
            int l3 = this.font.width(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
            int i4 = l3 - this.font.width(s1) / 2;
            if (flag && j < s1.length()) {
               pGuiGraphics.fill(i4, i1 - 1, i4 + 1, i1 + this.sign.getTextLineHeight(), -16777216 | i);
            }

            if (k != j) {
               int j4 = Math.min(j, k);
               int j2 = Math.max(j, k);
               int k2 = this.font.width(s1.substring(0, j4)) - this.font.width(s1) / 2;
               int l2 = this.font.width(s1.substring(0, j2)) - this.font.width(s1) / 2;
               int i3 = Math.min(k2, l2);
               int j3 = Math.max(k2, l2);
               pGuiGraphics.fill(RenderType.guiTextHighlight(), i3, i1, j3, i1 + this.sign.getTextLineHeight(), -16776961);
            }
         }
      }

   }

   private void setMessage(String p_277913_) {
      this.messages[this.line] = p_277913_;
      this.text = this.text.setMessage(this.line, Component.literal(p_277913_));
      this.sign.setText(this.text, this.isFrontText);
   }

   private void onDone() {
      this.minecraft.setScreen((Screen)null);
   }
}