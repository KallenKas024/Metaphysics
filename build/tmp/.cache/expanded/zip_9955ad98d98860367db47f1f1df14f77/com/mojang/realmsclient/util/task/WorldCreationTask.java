package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldCreationTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String name;
   private final String motd;
   private final long worldId;
   private final Screen lastScreen;

   public WorldCreationTask(long pWorldId, String pName, String pMotd, Screen pLastScreen) {
      this.worldId = pWorldId;
      this.name = pName;
      this.motd = pMotd;
      this.lastScreen = pLastScreen;
   }

   public void run() {
      this.setTitle(Component.translatable("mco.create.world.wait"));
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.initializeWorld(this.worldId, this.name, this.motd);
         setScreen(this.lastScreen);
      } catch (RealmsServiceException realmsserviceexception) {
         LOGGER.error("Couldn't create world");
         this.error(realmsserviceexception.toString());
      } catch (Exception exception) {
         LOGGER.error("Could not create world");
         this.error(exception.getLocalizedMessage());
      }

   }
}