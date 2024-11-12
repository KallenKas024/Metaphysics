package net.minecraft.client.quickplay;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuickPlay {
   public static final Component ERROR_TITLE = Component.translatable("quickplay.error.title");
   private static final Component INVALID_IDENTIFIER = Component.translatable("quickplay.error.invalid_identifier");
   private static final Component REALM_CONNECT = Component.translatable("quickplay.error.realm_connect");
   private static final Component REALM_PERMISSION = Component.translatable("quickplay.error.realm_permission");
   private static final Component TO_TITLE = Component.translatable("gui.toTitle");
   private static final Component TO_WORLD_LIST = Component.translatable("gui.toWorld");
   private static final Component TO_REALMS_LIST = Component.translatable("gui.toRealms");

   public static void connect(Minecraft pMinecraft, GameConfig.QuickPlayData pQuickPlayData, ReloadInstance pReloadInstance, RealmsClient pRealmsClient) {
      String s = pQuickPlayData.singleplayer();
      String s1 = pQuickPlayData.multiplayer();
      String s2 = pQuickPlayData.realms();
      pReloadInstance.done().thenRunAsync(() -> {
         if (!Util.isBlank(s)) {
            joinSingleplayerWorld(pMinecraft, s);
         } else if (!Util.isBlank(s1)) {
            joinMultiplayerWorld(pMinecraft, s1);
         } else if (!Util.isBlank(s2)) {
            joinRealmsWorld(pMinecraft, pRealmsClient, s2);
         }

      }, pMinecraft);
   }

   private static void joinSingleplayerWorld(Minecraft pMinecraft, String pLevelName) {
      if (!pMinecraft.getLevelSource().levelExists(pLevelName)) {
         Screen screen = new SelectWorldScreen(new TitleScreen());
         pMinecraft.setScreen(new DisconnectedScreen(screen, ERROR_TITLE, INVALID_IDENTIFIER, TO_WORLD_LIST));
      } else {
         pMinecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
         pMinecraft.createWorldOpenFlows().loadLevel(new TitleScreen(), pLevelName);
      }
   }

   private static void joinMultiplayerWorld(Minecraft pMinecraft, String pIp) {
      ServerList serverlist = new ServerList(pMinecraft);
      serverlist.load();
      ServerData serverdata = serverlist.get(pIp);
      if (serverdata == null) {
         serverdata = new ServerData(I18n.get("selectServer.defaultName"), pIp, false);
         serverlist.add(serverdata, true);
         serverlist.save();
      }

      ServerAddress serveraddress = ServerAddress.parseString(pIp);
      ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), pMinecraft, serveraddress, serverdata, true);
   }

   private static void joinRealmsWorld(Minecraft pMinecraft, RealmsClient pRealmsClient, String pServerId) {
      long i;
      RealmsServerList realmsserverlist;
      try {
         i = Long.parseLong(pServerId);
         realmsserverlist = pRealmsClient.listWorlds();
      } catch (NumberFormatException numberformatexception) {
         Screen screen1 = new RealmsMainScreen(new TitleScreen());
         pMinecraft.setScreen(new DisconnectedScreen(screen1, ERROR_TITLE, INVALID_IDENTIFIER, TO_REALMS_LIST));
         return;
      } catch (RealmsServiceException realmsserviceexception) {
         Screen screen = new TitleScreen();
         pMinecraft.setScreen(new DisconnectedScreen(screen, ERROR_TITLE, REALM_CONNECT, TO_TITLE));
         return;
      }

      RealmsServer realmsserver = realmsserverlist.servers.stream().filter((p_279424_) -> {
         return p_279424_.id == i;
      }).findFirst().orElse((RealmsServer)null);
      if (realmsserver == null) {
         Screen screen2 = new RealmsMainScreen(new TitleScreen());
         pMinecraft.setScreen(new DisconnectedScreen(screen2, ERROR_TITLE, REALM_PERMISSION, TO_REALMS_LIST));
      } else {
         TitleScreen titlescreen = new TitleScreen();
         GetServerDetailsTask getserverdetailstask = new GetServerDetailsTask(new RealmsMainScreen(titlescreen), titlescreen, realmsserver, new ReentrantLock());
         pMinecraft.setScreen(new RealmsLongRunningMcoTaskScreen(titlescreen, getserverdetailstask));
      }
   }
}