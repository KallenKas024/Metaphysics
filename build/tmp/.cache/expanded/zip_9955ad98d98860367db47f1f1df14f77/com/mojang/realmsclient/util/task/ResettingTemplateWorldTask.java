package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResettingTemplateWorldTask extends ResettingWorldTask {
   private final WorldTemplate template;

   public ResettingTemplateWorldTask(WorldTemplate pTemplate, long pServerId, Component pTitle, Runnable pCallback) {
      super(pServerId, pTitle, pCallback);
      this.template = pTemplate;
   }

   protected void sendResetRequest(RealmsClient pClient, long pServerId) throws RealmsServiceException {
      pClient.resetWorldWithTemplate(pServerId, this.template.id);
   }
}