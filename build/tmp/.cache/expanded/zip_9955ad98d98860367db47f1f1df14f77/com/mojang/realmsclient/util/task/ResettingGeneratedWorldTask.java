package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResettingGeneratedWorldTask extends ResettingWorldTask {
   private final WorldGenerationInfo generationInfo;

   public ResettingGeneratedWorldTask(WorldGenerationInfo pGenerationInfo, long pServerId, Component pTitle, Runnable pCallback) {
      super(pServerId, pTitle, pCallback);
      this.generationInfo = pGenerationInfo;
   }

   protected void sendResetRequest(RealmsClient pClient, long pServerId) throws RealmsServiceException {
      pClient.resetWorldWithSeed(pServerId, this.generationInfo);
   }
}