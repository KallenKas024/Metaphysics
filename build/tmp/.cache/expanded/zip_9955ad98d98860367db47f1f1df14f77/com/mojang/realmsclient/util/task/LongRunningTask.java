package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LongRunningTask implements ErrorCallback, Runnable {
   protected static final int NUMBER_OF_RETRIES = 25;
   private static final Logger LOGGER = LogUtils.getLogger();
   protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

   protected static void pause(long pSeconds) {
      try {
         Thread.sleep(pSeconds * 1000L);
      } catch (InterruptedException interruptedexception) {
         Thread.currentThread().interrupt();
         LOGGER.error("", (Throwable)interruptedexception);
      }

   }

   public static void setScreen(Screen pScreen) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.execute(() -> {
         minecraft.setScreen(pScreen);
      });
   }

   public void setScreen(RealmsLongRunningMcoTaskScreen pLongRunningMcoTaskScreen) {
      this.longRunningMcoTaskScreen = pLongRunningMcoTaskScreen;
   }

   public void error(Component pError) {
      this.longRunningMcoTaskScreen.error(pError);
   }

   public void setTitle(Component pTitle) {
      this.longRunningMcoTaskScreen.setTitle(pTitle);
   }

   public boolean aborted() {
      return this.longRunningMcoTaskScreen.aborted();
   }

   public void tick() {
   }

   public void init() {
   }

   public void abortTask() {
   }
}