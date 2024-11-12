package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class ResettingWorldTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final long serverId;
   private final Component title;
   private final Runnable callback;

   public ResettingWorldTask(long pServerId, Component pTitle, Runnable pCallback) {
      this.serverId = pServerId;
      this.title = pTitle;
      this.callback = pCallback;
   }

   protected abstract void sendResetRequest(RealmsClient pClient, long pServerId) throws RealmsServiceException;

   public void run() {
      RealmsClient realmsclient = RealmsClient.create();
      this.setTitle(this.title);
      int i = 0;

      while(i < 25) {
         try {
            if (this.aborted()) {
               return;
            }

            this.sendResetRequest(realmsclient, this.serverId);
            if (this.aborted()) {
               return;
            }

            this.callback.run();
            return;
         } catch (RetryCallException retrycallexception) {
            if (this.aborted()) {
               return;
            }

            pause((long)retrycallexception.delaySeconds);
            ++i;
         } catch (Exception exception) {
            if (this.aborted()) {
               return;
            }

            LOGGER.error("Couldn't reset world");
            this.error(exception.toString());
            return;
         }
      }

   }
}