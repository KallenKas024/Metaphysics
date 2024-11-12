package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Screenshot {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String SCREENSHOT_DIR = "screenshots";
   private int rowHeight;
   private final DataOutputStream outputStream;
   private final byte[] bytes;
   private final int width;
   private final int height;
   private File file;

   /**
    * Saves a screenshot in the game directory with a time-stamped filename.
    */
   public static void grab(File pGameDirectory, RenderTarget pBuffer, Consumer<Component> pMessageConsumer) {
      grab(pGameDirectory, (String)null, pBuffer, pMessageConsumer);
   }

   /**
    * Saves a screenshot in the game directory with the given file name (or null to generate a time-stamped name).
    */
   public static void grab(File pGameDirectory, @Nullable String pScreenshotName, RenderTarget pBuffer, Consumer<Component> pMessageConsumer) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            _grab(pGameDirectory, pScreenshotName, pBuffer, pMessageConsumer);
         });
      } else {
         _grab(pGameDirectory, pScreenshotName, pBuffer, pMessageConsumer);
      }

   }

   private static void _grab(File pGameDirectory, @Nullable String pScreenshotName, RenderTarget pBuffer, Consumer<Component> pMessageConsumer) {
      NativeImage nativeimage = takeScreenshot(pBuffer);
      File file1 = new File(pGameDirectory, "screenshots");
      file1.mkdir();
      File file2;
      if (pScreenshotName == null) {
         file2 = getFile(file1);
      } else {
         file2 = new File(file1, pScreenshotName);
      }

      net.minecraftforge.client.event.ScreenshotEvent event = net.minecraftforge.client.ForgeHooksClient.onScreenshot(nativeimage, file2);
      if (event.isCanceled()) {
         pMessageConsumer.accept(event.getCancelMessage());
         return;
      }
      final File target = event.getScreenshotFile();

      Util.ioPool().execute(() -> {
         try {
            nativeimage.writeToFile(target);
            Component component = Component.literal(file2.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((p_168608_) -> {
               return p_168608_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, target.getAbsolutePath()));
            });
            if (event.getResultMessage() != null)
               pMessageConsumer.accept(event.getResultMessage());
            else
               pMessageConsumer.accept(Component.translatable("screenshot.success", component));
         } catch (Exception exception) {
            LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
            pMessageConsumer.accept(Component.translatable("screenshot.failure", exception.getMessage()));
         } finally {
            nativeimage.close();
         }

      });
   }

   public static NativeImage takeScreenshot(RenderTarget pFramebuffer) {
      int i = pFramebuffer.width;
      int j = pFramebuffer.height;
      NativeImage nativeimage = new NativeImage(i, j, false);
      RenderSystem.bindTexture(pFramebuffer.getColorTextureId());
      nativeimage.downloadTexture(0, true);
      nativeimage.flipY();
      return nativeimage;
   }

   /**
    * Creates a unique PNG file in the given directory named by a timestamp. Handles cases where the timestamp alone is
    * not enough to create a uniquely named file, though it still might suffer from an unlikely race condition where the
    * filename was unique when this method was called, but another process or thread created a file at the same path
    * immediately after this method returned.
    */
   private static File getFile(File pGameDirectory) {
      String s = Util.getFilenameFormattedDateTime();
      int i = 1;

      while(true) {
         File file1 = new File(pGameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");
         if (!file1.exists()) {
            return file1;
         }

         ++i;
      }
   }

   public Screenshot(File pGameDirectory, int pWidth, int pHeight, int pRowHeight) throws IOException {
      this.width = pWidth;
      this.height = pHeight;
      this.rowHeight = pRowHeight;
      File file1 = new File(pGameDirectory, "screenshots");
      file1.mkdir();
      String s = "huge_" + Util.getFilenameFormattedDateTime();

      for(int i = 1; (this.file = new File(file1, s + (i == 1 ? "" : "_" + i) + ".tga")).exists(); ++i) {
      }

      byte[] abyte = new byte[18];
      abyte[2] = 2;
      abyte[12] = (byte)(pWidth % 256);
      abyte[13] = (byte)(pWidth / 256);
      abyte[14] = (byte)(pHeight % 256);
      abyte[15] = (byte)(pHeight / 256);
      abyte[16] = 24;
      this.bytes = new byte[pWidth * pRowHeight * 3];
      this.outputStream = new DataOutputStream(new FileOutputStream(this.file));
      this.outputStream.write(abyte);
   }

   public void addRegion(ByteBuffer pBuffer, int pWidth, int pHeight, int pRowWidth, int pRowHeight) {
      int i = pRowWidth;
      int j = pRowHeight;
      if (pRowWidth > this.width - pWidth) {
         i = this.width - pWidth;
      }

      if (pRowHeight > this.height - pHeight) {
         j = this.height - pHeight;
      }

      this.rowHeight = j;

      for(int k = 0; k < j; ++k) {
         pBuffer.position((pRowHeight - j) * pRowWidth * 3 + k * pRowWidth * 3);
         int l = (pWidth + k * this.width) * 3;
         pBuffer.get(this.bytes, l, i * 3);
      }

   }

   public void saveRow() throws IOException {
      this.outputStream.write(this.bytes, 0, this.width * 3 * this.rowHeight);
   }

   public File close() throws IOException {
      this.outputStream.close();
      return this.file;
   }
}
