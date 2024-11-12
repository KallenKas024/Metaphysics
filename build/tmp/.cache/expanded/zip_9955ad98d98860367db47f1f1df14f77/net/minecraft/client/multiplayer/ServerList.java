package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerList {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ProcessorMailbox<Runnable> IO_MAILBOX = ProcessorMailbox.create(Util.backgroundExecutor(), "server-list-io");
   private static final int MAX_HIDDEN_SERVERS = 16;
   private final Minecraft minecraft;
   private final List<ServerData> serverList = Lists.newArrayList();
   private final List<ServerData> hiddenServerList = Lists.newArrayList();

   public ServerList(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   /**
    * Loads a list of servers from servers.dat, by running ServerData.getServerDataFromNBTCompound on each NBT compound
    * found in the "servers" tag list.
    */
   public void load() {
      try {
         this.serverList.clear();
         this.hiddenServerList.clear();
         CompoundTag compoundtag = NbtIo.read(new File(this.minecraft.gameDirectory, "servers.dat"));
         if (compoundtag == null) {
            return;
         }

         ListTag listtag = compoundtag.getList("servers", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            ServerData serverdata = ServerData.read(compoundtag1);
            if (compoundtag1.getBoolean("hidden")) {
               this.hiddenServerList.add(serverdata);
            } else {
               this.serverList.add(serverdata);
            }
         }
      } catch (Exception exception) {
         LOGGER.error("Couldn't load server list", (Throwable)exception);
      }

   }

   /**
    * Runs getNBTCompound on each ServerData instance, puts everything into a "servers" NBT list and writes it to
    * servers.dat.
    */
   public void save() {
      try {
         ListTag listtag = new ListTag();

         for(ServerData serverdata : this.serverList) {
            CompoundTag compoundtag = serverdata.write();
            compoundtag.putBoolean("hidden", false);
            listtag.add(compoundtag);
         }

         for(ServerData serverdata1 : this.hiddenServerList) {
            CompoundTag compoundtag2 = serverdata1.write();
            compoundtag2.putBoolean("hidden", true);
            listtag.add(compoundtag2);
         }

         CompoundTag compoundtag1 = new CompoundTag();
         compoundtag1.put("servers", listtag);
         File file2 = File.createTempFile("servers", ".dat", this.minecraft.gameDirectory);
         NbtIo.write(compoundtag1, file2);
         File file3 = new File(this.minecraft.gameDirectory, "servers.dat_old");
         File file1 = new File(this.minecraft.gameDirectory, "servers.dat");
         Util.safeReplaceFile(file1, file2, file3);
      } catch (Exception exception) {
         LOGGER.error("Couldn't save server list", (Throwable)exception);
      }

   }

   /**
    * Gets the ServerData instance stored for the given index in the list.
    */
   public ServerData get(int pIndex) {
      return this.serverList.get(pIndex);
   }

   @Nullable
   public ServerData get(String pIp) {
      for(ServerData serverdata : this.serverList) {
         if (serverdata.ip.equals(pIp)) {
            return serverdata;
         }
      }

      for(ServerData serverdata1 : this.hiddenServerList) {
         if (serverdata1.ip.equals(pIp)) {
            return serverdata1;
         }
      }

      return null;
   }

   @Nullable
   public ServerData unhide(String pIp) {
      for(int i = 0; i < this.hiddenServerList.size(); ++i) {
         ServerData serverdata = this.hiddenServerList.get(i);
         if (serverdata.ip.equals(pIp)) {
            this.hiddenServerList.remove(i);
            this.serverList.add(serverdata);
            return serverdata;
         }
      }

      return null;
   }

   public void remove(ServerData pServerData) {
      if (!this.serverList.remove(pServerData)) {
         this.hiddenServerList.remove(pServerData);
      }

   }

   public void add(ServerData pServer, boolean pHidden) {
      if (pHidden) {
         this.hiddenServerList.add(0, pServer);

         while(this.hiddenServerList.size() > 16) {
            this.hiddenServerList.remove(this.hiddenServerList.size() - 1);
         }
      } else {
         this.serverList.add(pServer);
      }

   }

   /**
    * Counts the number of ServerData instances in the list.
    */
   public int size() {
      return this.serverList.size();
   }

   /**
    * Takes two list indexes, and swaps their order around.
    */
   public void swap(int pPos1, int pPos2) {
      ServerData serverdata = this.get(pPos1);
      this.serverList.set(pPos1, this.get(pPos2));
      this.serverList.set(pPos2, serverdata);
      this.save();
   }

   public void replace(int pIndex, ServerData pServer) {
      this.serverList.set(pIndex, pServer);
   }

   private static boolean set(ServerData pServer, List<ServerData> pServerList) {
      for(int i = 0; i < pServerList.size(); ++i) {
         ServerData serverdata = pServerList.get(i);
         if (serverdata.name.equals(pServer.name) && serverdata.ip.equals(pServer.ip)) {
            pServerList.set(i, pServer);
            return true;
         }
      }

      return false;
   }

   public static void saveSingleServer(ServerData pServer) {
      IO_MAILBOX.tell(() -> {
         ServerList serverlist = new ServerList(Minecraft.getInstance());
         serverlist.load();
         if (!set(pServer, serverlist.serverList)) {
            set(pServer, serverlist.hiddenServerList);
         }

         serverlist.save();
      });
   }
}