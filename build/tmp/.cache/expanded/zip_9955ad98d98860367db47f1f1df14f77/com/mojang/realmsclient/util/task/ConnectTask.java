package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsConnect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConnectTask extends LongRunningTask {
   private final RealmsConnect realmsConnect;
   private final RealmsServer server;
   private final RealmsServerAddress address;

   public ConnectTask(Screen pOnlineScreen, RealmsServer pServer, RealmsServerAddress pAddress) {
      this.server = pServer;
      this.address = pAddress;
      this.realmsConnect = new RealmsConnect(pOnlineScreen);
   }

   public void run() {
      this.setTitle(Component.translatable("mco.connect.connecting"));
      this.realmsConnect.connect(this.server, ServerAddress.parseString(this.address.address));
   }

   public void abortTask() {
      this.realmsConnect.abort();
      Minecraft.getInstance().getDownloadedPackSource().clearServerPack();
   }

   public void tick() {
      this.realmsConnect.tick();
   }
}