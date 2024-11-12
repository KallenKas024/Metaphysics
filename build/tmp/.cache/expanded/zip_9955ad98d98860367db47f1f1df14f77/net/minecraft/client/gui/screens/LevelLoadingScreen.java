package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelLoadingScreen extends Screen {
   private static final long NARRATION_DELAY_MS = 2000L;
   private final StoringChunkProgressListener progressListener;
   private long lastNarration = -1L;
   private boolean done;
   private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap<>(), (p_280803_) -> {
      p_280803_.defaultReturnValue(0);
      p_280803_.put(ChunkStatus.EMPTY, 5526612);
      p_280803_.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
      p_280803_.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
      p_280803_.put(ChunkStatus.BIOMES, 8434258);
      p_280803_.put(ChunkStatus.NOISE, 13750737);
      p_280803_.put(ChunkStatus.SURFACE, 7497737);
      p_280803_.put(ChunkStatus.CARVERS, 3159410);
      p_280803_.put(ChunkStatus.FEATURES, 2213376);
      p_280803_.put(ChunkStatus.INITIALIZE_LIGHT, 13421772);
      p_280803_.put(ChunkStatus.LIGHT, 16769184);
      p_280803_.put(ChunkStatus.SPAWN, 15884384);
      p_280803_.put(ChunkStatus.FULL, 16777215);
   });

   public LevelLoadingScreen(StoringChunkProgressListener pProgressListener) {
      super(GameNarrator.NO_TITLE);
      this.progressListener = pProgressListener;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected boolean shouldNarrateNavigation() {
      return false;
   }

   public void removed() {
      this.done = true;
      this.triggerImmediateNarration(true);
   }

   protected void updateNarratedWidget(NarrationElementOutput pNarrationElementOutput) {
      if (this.done) {
         pNarrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narrator.loading.done"));
      } else {
         String s = this.getFormattedProgress();
         pNarrationElementOutput.add(NarratedElementType.TITLE, s);
      }

   }

   private String getFormattedProgress() {
      return Mth.clamp(this.progressListener.getProgress(), 0, 100) + "%";
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
      long i = Util.getMillis();
      if (i - this.lastNarration > 2000L) {
         this.lastNarration = i;
         this.triggerImmediateNarration(true);
      }

      int j = this.width / 2;
      int k = this.height / 2;
      int l = 30;
      renderChunks(pGuiGraphics, this.progressListener, j, k + 30, 2, 0);
      pGuiGraphics.drawCenteredString(this.font, this.getFormattedProgress(), j, k - 9 / 2 - 30, 16777215);
   }

   public static void renderChunks(GuiGraphics pGuiGraphics, StoringChunkProgressListener pProgressListener, int pX, int pY, int p_96154_, int p_96155_) {
      int i = p_96154_ + p_96155_;
      int j = pProgressListener.getFullDiameter();
      int k = j * i - p_96155_;
      int l = pProgressListener.getDiameter();
      int i1 = l * i - p_96155_;
      int j1 = pX - i1 / 2;
      int k1 = pY - i1 / 2;
      int l1 = k / 2 + 1;
      int i2 = -16772609;
      pGuiGraphics.drawManaged(() -> {
         if (p_96155_ != 0) {
            pGuiGraphics.fill(pX - l1, pY - l1, pX - l1 + 1, pY + l1, -16772609);
            pGuiGraphics.fill(pX + l1 - 1, pY - l1, pX + l1, pY + l1, -16772609);
            pGuiGraphics.fill(pX - l1, pY - l1, pX + l1, pY - l1 + 1, -16772609);
            pGuiGraphics.fill(pX - l1, pY + l1 - 1, pX + l1, pY + l1, -16772609);
         }

         for(int j2 = 0; j2 < l; ++j2) {
            for(int k2 = 0; k2 < l; ++k2) {
               ChunkStatus chunkstatus = pProgressListener.getStatus(j2, k2);
               int l2 = j1 + j2 * i;
               int i3 = k1 + k2 * i;
               pGuiGraphics.fill(l2, i3, l2 + p_96154_, i3 + p_96154_, COLORS.getInt(chunkstatus) | -16777216);
            }
         }

      });
   }
}