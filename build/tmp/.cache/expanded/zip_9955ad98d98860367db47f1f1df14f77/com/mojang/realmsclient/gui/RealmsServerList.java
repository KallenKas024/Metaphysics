package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServerList {
   private final Minecraft minecraft;
   private final Set<RealmsServer> removedServers = Sets.newHashSet();
   private List<RealmsServer> servers = Lists.newArrayList();

   public RealmsServerList(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public List<RealmsServer> updateServersList(List<RealmsServer> pServers) {
      List<RealmsServer> list = new ArrayList<>(pServers);
      list.sort(new RealmsServer.McoServerComparator(this.minecraft.getUser().getName()));
      boolean flag = list.removeAll(this.removedServers);
      if (!flag) {
         this.removedServers.clear();
      }

      this.servers = list;
      return List.copyOf(this.servers);
   }

   public synchronized List<RealmsServer> removeItem(RealmsServer pServer) {
      this.servers.remove(pServer);
      this.removedServers.add(pServer);
      return List.copyOf(this.servers);
   }
}