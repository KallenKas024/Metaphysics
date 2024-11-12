package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WinScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
   private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
   private static final String NAME_PREFIX = "           ";
   private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
   private static final float SPEEDUP_FACTOR = 5.0F;
   private static final float SPEEDUP_FACTOR_FAST = 15.0F;
   private final boolean poem;
   private final Runnable onFinished;
   private float scroll;
   private List<FormattedCharSequence> lines;
   private IntSet centeredLines;
   private int totalScrollLength;
   private boolean speedupActive;
   private final IntSet speedupModifiers = new IntOpenHashSet();
   private float scrollSpeed;
   private final float unmodifiedScrollSpeed;
   private int direction;
   private final LogoRenderer logoRenderer = new LogoRenderer(false);

   public WinScreen(boolean pPoem, Runnable pOnFinished) {
      super(GameNarrator.NO_TITLE);
      this.poem = pPoem;
      this.onFinished = pOnFinished;
      if (!pPoem) {
         this.unmodifiedScrollSpeed = 0.75F;
      } else {
         this.unmodifiedScrollSpeed = 0.5F;
      }

      this.direction = 1;
      this.scrollSpeed = this.unmodifiedScrollSpeed;
   }

   private float calculateScrollSpeed() {
      return this.speedupActive ? this.unmodifiedScrollSpeed * (5.0F + (float)this.speedupModifiers.size() * 15.0F) * (float)this.direction : this.unmodifiedScrollSpeed * (float)this.direction;
   }

   public void tick() {
      this.minecraft.getMusicManager().tick();
      this.minecraft.getSoundManager().tick(false);
      float f = (float)(this.totalScrollLength + this.height + this.height + 24);
      if (this.scroll > f) {
         this.respawn();
      }

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
         this.direction = -1;
      } else if (pKeyCode != 341 && pKeyCode != 345) {
         if (pKeyCode == 32) {
            this.speedupActive = true;
         }
      } else {
         this.speedupModifiers.add(pKeyCode);
      }

      this.scrollSpeed = this.calculateScrollSpeed();
      return super.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   /**
    * Called when a keyboard key is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the released key.
    * @param pScanCode the scan code of the released key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 265) {
         this.direction = 1;
      }

      if (pKeyCode == 32) {
         this.speedupActive = false;
      } else if (pKeyCode == 341 || pKeyCode == 345) {
         this.speedupModifiers.remove(pKeyCode);
      }

      this.scrollSpeed = this.calculateScrollSpeed();
      return super.keyReleased(pKeyCode, pScanCode, pModifiers);
   }

   public void onClose() {
      this.respawn();
   }

   private void respawn() {
      this.onFinished.run();
   }

   protected void init() {
      if (this.lines == null) {
         this.lines = Lists.newArrayList();
         this.centeredLines = new IntOpenHashSet();
         if (this.poem) {
            this.wrapCreditsIO("texts/end.txt", this::addPoemFile);
         }

         this.wrapCreditsIO("texts/credits.json", this::addCreditsFile);
         if (this.poem) {
            this.wrapCreditsIO("texts/postcredits.txt", this::addPoemFile);
         }

         this.totalScrollLength = this.lines.size() * 12;
      }
   }

   private void wrapCreditsIO(String pCreditsLocation, WinScreen.CreditsReader pReader) {
      try (Reader reader = this.minecraft.getResourceManager().openAsReader(new ResourceLocation(pCreditsLocation))) {
         pReader.read(reader);
      } catch (Exception exception) {
         LOGGER.error("Couldn't load credits", (Throwable)exception);
      }

   }

   private void addPoemFile(Reader p_232818_) throws IOException {
      BufferedReader bufferedreader = new BufferedReader(p_232818_);
      RandomSource randomsource = RandomSource.create(8124371L);

      String s;
      while((s = bufferedreader.readLine()) != null) {
         int i;
         String s1;
         String s2;
         for(s = s.replaceAll("PLAYERNAME", this.minecraft.getUser().getName()); (i = s.indexOf(OBFUSCATE_TOKEN)) != -1; s = s1 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, randomsource.nextInt(4) + 3) + s2) {
            s1 = s.substring(0, i);
            s2 = s.substring(i + OBFUSCATE_TOKEN.length());
         }

         this.addPoemLines(s);
         this.addEmptyLine();
      }

      for(int j = 0; j < 8; ++j) {
         this.addEmptyLine();
      }

   }

   private void addCreditsFile(Reader p_232820_) {
      for(JsonElement jsonelement : GsonHelper.parseArray(p_232820_)) {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         String s = jsonobject.get("section").getAsString();
         this.addCreditsLine(SECTION_HEADING, true);
         this.addCreditsLine(Component.literal(s).withStyle(ChatFormatting.YELLOW), true);
         this.addCreditsLine(SECTION_HEADING, true);
         this.addEmptyLine();
         this.addEmptyLine();

         for(JsonElement jsonelement1 : jsonobject.getAsJsonArray("disciplines")) {
            JsonObject jsonobject1 = jsonelement1.getAsJsonObject();
            String s1 = jsonobject1.get("discipline").getAsString();
            if (StringUtils.isNotEmpty(s1)) {
               this.addCreditsLine(Component.literal(s1).withStyle(ChatFormatting.YELLOW), true);
               this.addEmptyLine();
               this.addEmptyLine();
            }

            for(JsonElement jsonelement2 : jsonobject1.getAsJsonArray("titles")) {
               JsonObject jsonobject2 = jsonelement2.getAsJsonObject();
               String s2 = jsonobject2.get("title").getAsString();
               JsonArray jsonarray = jsonobject2.getAsJsonArray("names");
               this.addCreditsLine(Component.literal(s2).withStyle(ChatFormatting.GRAY), false);

               for(JsonElement jsonelement3 : jsonarray) {
                  String s3 = jsonelement3.getAsString();
                  this.addCreditsLine(Component.literal("           ").append(s3).withStyle(ChatFormatting.WHITE), false);
               }

               this.addEmptyLine();
               this.addEmptyLine();
            }
         }
      }

   }

   private void addEmptyLine() {
      this.lines.add(FormattedCharSequence.EMPTY);
   }

   private void addPoemLines(String pText) {
      this.lines.addAll(this.minecraft.font.split(Component.literal(pText), 256));
   }

   private void addCreditsLine(Component pCreditsLine, boolean pCentered) {
      if (pCentered) {
         this.centeredLines.add(this.lines.size());
      }

      this.lines.add(pCreditsLine.getVisualOrderText());
   }

   private void renderBg(GuiGraphics pGuiGraphics) {
      int i = this.width;
      float f = this.scroll * 0.5F;
      int j = 64;
      float f1 = this.scroll / this.unmodifiedScrollSpeed;
      float f2 = f1 * 0.02F;
      float f3 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
      float f4 = (f3 - 20.0F - f1) * 0.005F;
      if (f4 < f2) {
         f2 = f4;
      }

      if (f2 > 1.0F) {
         f2 = 1.0F;
      }

      f2 *= f2;
      f2 = f2 * 96.0F / 255.0F;
      pGuiGraphics.setColor(f2, f2, f2, 1.0F);
      pGuiGraphics.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, f, i, this.height, 64, 64);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.scroll = Math.max(0.0F, this.scroll + pPartialTick * this.scrollSpeed);
      this.renderBg(pGuiGraphics);
      int i = this.width / 2 - 128;
      int j = this.height + 50;
      float f = -this.scroll;
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate(0.0F, f, 0.0F);
      this.logoRenderer.renderLogo(pGuiGraphics, this.width, 1.0F, j);
      int k = j + 100;

      for(int l = 0; l < this.lines.size(); ++l) {
         if (l == this.lines.size() - 1) {
            float f1 = (float)k + f - (float)(this.height / 2 - 6);
            if (f1 < 0.0F) {
               pGuiGraphics.pose().translate(0.0F, -f1, 0.0F);
            }
         }

         if ((float)k + f + 12.0F + 8.0F > 0.0F && (float)k + f < (float)this.height) {
            FormattedCharSequence formattedcharsequence = this.lines.get(l);
            if (this.centeredLines.contains(l)) {
               pGuiGraphics.drawCenteredString(this.font, formattedcharsequence, i + 128, k, 16777215);
            } else {
               pGuiGraphics.drawString(this.font, formattedcharsequence, i, k, 16777215);
            }
         }

         k += 12;
      }

      pGuiGraphics.pose().popPose();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
      pGuiGraphics.blit(VIGNETTE_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void removed() {
      this.minecraft.getMusicManager().stopPlaying(Musics.CREDITS);
   }

   public Music getBackgroundMusic() {
      return Musics.CREDITS;
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   interface CreditsReader {
      void read(Reader pReader) throws IOException;
   }
}