package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SystemToast implements Toast {
   private static final int MAX_LINE_SIZE = 200;
   private static final int LINE_SPACING = 12;
   private static final int MARGIN = 10;
   private final SystemToast.SystemToastIds id;
   private Component title;
   private List<FormattedCharSequence> messageLines;
   private long lastChanged;
   private boolean changed;
   private final int width;

   public SystemToast(SystemToast.SystemToastIds pId, Component pTitle, @Nullable Component pMessage) {
      this(pId, pTitle, nullToEmpty(pMessage), Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(pTitle), pMessage == null ? 0 : Minecraft.getInstance().font.width(pMessage))));
   }

   public static SystemToast multiline(Minecraft pMinecraft, SystemToast.SystemToastIds pId, Component pTitle, Component pMessage) {
      Font font = pMinecraft.font;
      List<FormattedCharSequence> list = font.split(pMessage, 200);
      int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
      return new SystemToast(pId, pTitle, list, i + 30);
   }

   private SystemToast(SystemToast.SystemToastIds pId, Component pTitle, List<FormattedCharSequence> pMessageLines, int pWidth) {
      this.id = pId;
      this.title = pTitle;
      this.messageLines = pMessageLines;
      this.width = pWidth;
   }

   private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component pMessage) {
      return pMessage == null ? ImmutableList.of() : ImmutableList.of(pMessage.getVisualOrderText());
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return 20 + Math.max(this.messageLines.size(), 1) * 12;
   }

   public Toast.Visibility render(GuiGraphics pGuiGraphics, ToastComponent pToastComponent, long pTimeSinceLastVisible) {
      if (this.changed) {
         this.lastChanged = pTimeSinceLastVisible;
         this.changed = false;
      }

      int i = this.width();
      if (i == 160 && this.messageLines.size() <= 1) {
         pGuiGraphics.blit(TEXTURE, 0, 0, 0, 64, i, this.height());
      } else {
         int j = this.height();
         int k = 28;
         int l = Math.min(4, j - 28);
         this.renderBackgroundRow(pGuiGraphics, pToastComponent, i, 0, 0, 28);

         for(int i1 = 28; i1 < j - l; i1 += 10) {
            this.renderBackgroundRow(pGuiGraphics, pToastComponent, i, 16, i1, Math.min(16, j - i1 - l));
         }

         this.renderBackgroundRow(pGuiGraphics, pToastComponent, i, 32 - l, j - l, l);
      }

      if (this.messageLines == null) {
         pGuiGraphics.drawString(pToastComponent.getMinecraft().font, this.title, 18, 12, -256, false);
      } else {
         pGuiGraphics.drawString(pToastComponent.getMinecraft().font, this.title, 18, 7, -256, false);

         for(int j1 = 0; j1 < this.messageLines.size(); ++j1) {
            pGuiGraphics.drawString(pToastComponent.getMinecraft().font, this.messageLines.get(j1), 18, 18 + j1 * 12, -1, false);
         }
      }

      return (double)(pTimeSinceLastVisible - this.lastChanged) < (double)this.id.displayTime * pToastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
   }

   private void renderBackgroundRow(GuiGraphics pGuiGraphics, ToastComponent pToastComponent, int pWidth, int p_282371_, int p_283613_, int p_282880_) {
      int i = p_282371_ == 0 ? 20 : 5;
      int j = Math.min(60, pWidth - i);
      pGuiGraphics.blit(TEXTURE, 0, p_283613_, 0, 64 + p_282371_, i, p_282880_);

      for(int k = i; k < pWidth - j; k += 64) {
         pGuiGraphics.blit(TEXTURE, k, p_283613_, 32, 64 + p_282371_, Math.min(64, pWidth - k - j), p_282880_);
      }

      pGuiGraphics.blit(TEXTURE, pWidth - j, p_283613_, 160 - j, 64 + p_282371_, j, p_282880_);
   }

   public void reset(Component pTitle, @Nullable Component pMessage) {
      this.title = pTitle;
      this.messageLines = nullToEmpty(pMessage);
      this.changed = true;
   }

   public SystemToast.SystemToastIds getToken() {
      return this.id;
   }

   public static void add(ToastComponent pToastComponent, SystemToast.SystemToastIds pId, Component pTitle, @Nullable Component pMessage) {
      pToastComponent.addToast(new SystemToast(pId, pTitle, pMessage));
   }

   public static void addOrUpdate(ToastComponent pToastComponent, SystemToast.SystemToastIds pId, Component pTitle, @Nullable Component pMessage) {
      SystemToast systemtoast = pToastComponent.getToast(SystemToast.class, pId);
      if (systemtoast == null) {
         add(pToastComponent, pId, pTitle, pMessage);
      } else {
         systemtoast.reset(pTitle, pMessage);
      }

   }

   public static void onWorldAccessFailure(Minecraft pMinecraft, String pMessage) {
      add(pMinecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.access_failure"), Component.literal(pMessage));
   }

   public static void onWorldDeleteFailure(Minecraft pMinecraft, String pMessage) {
      add(pMinecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.delete_failure"), Component.literal(pMessage));
   }

   public static void onPackCopyFailure(Minecraft pMinecraft, String pMessage) {
      add(pMinecraft.getToasts(), SystemToast.SystemToastIds.PACK_COPY_FAILURE, Component.translatable("pack.copyFailure"), Component.literal(pMessage));
   }

   @OnlyIn(Dist.CLIENT)
   public static enum SystemToastIds {
      TUTORIAL_HINT,
      NARRATOR_TOGGLE,
      WORLD_BACKUP,
      PACK_LOAD_FAILURE,
      WORLD_ACCESS_FAILURE,
      PACK_COPY_FAILURE,
      PERIODIC_NOTIFICATION,
      UNSECURE_SERVER_WARNING(10000L);

      final long displayTime;

      private SystemToastIds(long pDisplayTime) {
         this.displayTime = pDisplayTime;
      }

      private SystemToastIds() {
         this(5000L);
      }
   }
}