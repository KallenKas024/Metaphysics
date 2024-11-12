package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class VirtualScreen implements AutoCloseable {
   private final Minecraft minecraft;
   private final ScreenManager screenManager;

   public VirtualScreen(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
      this.screenManager = new ScreenManager(Monitor::new);
   }

   public Window newWindow(DisplayData pScreenSize, @Nullable String pVideoModeName, String pTitle) {
      return new Window(this.minecraft, this.screenManager, pScreenSize, pVideoModeName, pTitle);
   }

   public void close() {
      this.screenManager.shutdown();
   }
}