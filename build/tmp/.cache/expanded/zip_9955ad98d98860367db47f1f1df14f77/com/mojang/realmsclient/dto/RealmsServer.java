package com.mojang.realmsclient.dto;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServer extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public long id;
   public String remoteSubscriptionId;
   public String name;
   public String motd;
   public RealmsServer.State state;
   public String owner;
   public String ownerUUID;
   public List<PlayerInfo> players;
   public Map<Integer, RealmsWorldOptions> slots;
   public boolean expired;
   public boolean expiredTrial;
   public int daysLeft;
   public RealmsServer.WorldType worldType;
   public int activeSlot;
   public String minigameName;
   public int minigameId;
   public String minigameImage;
   public RealmsServerPing serverPing = new RealmsServerPing();

   public String getDescription() {
      return this.motd;
   }

   public String getName() {
      return this.name;
   }

   public String getMinigameName() {
      return this.minigameName;
   }

   public void setName(String pName) {
      this.name = pName;
   }

   public void setDescription(String pMotd) {
      this.motd = pMotd;
   }

   public void updateServerPing(RealmsServerPlayerList pRealmsServerPlayerList) {
      List<String> list = Lists.newArrayList();
      int i = 0;

      for(String s : pRealmsServerPlayerList.players) {
         if (!s.equals(Minecraft.getInstance().getUser().getUuid())) {
            String s1 = "";

            try {
               s1 = RealmsUtil.uuidToName(s);
            } catch (Exception exception) {
               LOGGER.error("Could not get name for {}", s, exception);
               continue;
            }

            list.add(s1);
            ++i;
         }
      }

      this.serverPing.nrOfPlayers = String.valueOf(i);
      this.serverPing.playerList = Joiner.on('\n').join(list);
   }

   public static RealmsServer parse(JsonObject pJson) {
      RealmsServer realmsserver = new RealmsServer();

      try {
         realmsserver.id = JsonUtils.getLongOr("id", pJson, -1L);
         realmsserver.remoteSubscriptionId = JsonUtils.getStringOr("remoteSubscriptionId", pJson, (String)null);
         realmsserver.name = JsonUtils.getStringOr("name", pJson, (String)null);
         realmsserver.motd = JsonUtils.getStringOr("motd", pJson, (String)null);
         realmsserver.state = getState(JsonUtils.getStringOr("state", pJson, RealmsServer.State.CLOSED.name()));
         realmsserver.owner = JsonUtils.getStringOr("owner", pJson, (String)null);
         if (pJson.get("players") != null && pJson.get("players").isJsonArray()) {
            realmsserver.players = parseInvited(pJson.get("players").getAsJsonArray());
            sortInvited(realmsserver);
         } else {
            realmsserver.players = Lists.newArrayList();
         }

         realmsserver.daysLeft = JsonUtils.getIntOr("daysLeft", pJson, 0);
         realmsserver.expired = JsonUtils.getBooleanOr("expired", pJson, false);
         realmsserver.expiredTrial = JsonUtils.getBooleanOr("expiredTrial", pJson, false);
         realmsserver.worldType = getWorldType(JsonUtils.getStringOr("worldType", pJson, RealmsServer.WorldType.NORMAL.name()));
         realmsserver.ownerUUID = JsonUtils.getStringOr("ownerUUID", pJson, "");
         if (pJson.get("slots") != null && pJson.get("slots").isJsonArray()) {
            realmsserver.slots = parseSlots(pJson.get("slots").getAsJsonArray());
         } else {
            realmsserver.slots = createEmptySlots();
         }

         realmsserver.minigameName = JsonUtils.getStringOr("minigameName", pJson, (String)null);
         realmsserver.activeSlot = JsonUtils.getIntOr("activeSlot", pJson, -1);
         realmsserver.minigameId = JsonUtils.getIntOr("minigameId", pJson, -1);
         realmsserver.minigameImage = JsonUtils.getStringOr("minigameImage", pJson, (String)null);
      } catch (Exception exception) {
         LOGGER.error("Could not parse McoServer: {}", (Object)exception.getMessage());
      }

      return realmsserver;
   }

   private static void sortInvited(RealmsServer pRealmsServer) {
      pRealmsServer.players.sort((p_87502_, p_87503_) -> {
         return ComparisonChain.start().compareFalseFirst(p_87503_.getAccepted(), p_87502_.getAccepted()).compare(p_87502_.getName().toLowerCase(Locale.ROOT), p_87503_.getName().toLowerCase(Locale.ROOT)).result();
      });
   }

   private static List<PlayerInfo> parseInvited(JsonArray pJsonArray) {
      List<PlayerInfo> list = Lists.newArrayList();

      for(JsonElement jsonelement : pJsonArray) {
         try {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            PlayerInfo playerinfo = new PlayerInfo();
            playerinfo.setName(JsonUtils.getStringOr("name", jsonobject, (String)null));
            playerinfo.setUuid(JsonUtils.getStringOr("uuid", jsonobject, (String)null));
            playerinfo.setOperator(JsonUtils.getBooleanOr("operator", jsonobject, false));
            playerinfo.setAccepted(JsonUtils.getBooleanOr("accepted", jsonobject, false));
            playerinfo.setOnline(JsonUtils.getBooleanOr("online", jsonobject, false));
            list.add(playerinfo);
         } catch (Exception exception) {
         }
      }

      return list;
   }

   private static Map<Integer, RealmsWorldOptions> parseSlots(JsonArray pJsonArray) {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();

      for(JsonElement jsonelement : pJsonArray) {
         try {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            JsonParser jsonparser = new JsonParser();
            JsonElement jsonelement1 = jsonparser.parse(jsonobject.get("options").getAsString());
            RealmsWorldOptions realmsworldoptions;
            if (jsonelement1 == null) {
               realmsworldoptions = RealmsWorldOptions.createDefaults();
            } else {
               realmsworldoptions = RealmsWorldOptions.parse(jsonelement1.getAsJsonObject());
            }

            int i = JsonUtils.getIntOr("slotId", jsonobject, -1);
            map.put(i, realmsworldoptions);
         } catch (Exception exception) {
         }
      }

      for(int j = 1; j <= 3; ++j) {
         if (!map.containsKey(j)) {
            map.put(j, RealmsWorldOptions.createEmptyDefaults());
         }
      }

      return map;
   }

   private static Map<Integer, RealmsWorldOptions> createEmptySlots() {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();
      map.put(1, RealmsWorldOptions.createEmptyDefaults());
      map.put(2, RealmsWorldOptions.createEmptyDefaults());
      map.put(3, RealmsWorldOptions.createEmptyDefaults());
      return map;
   }

   public static RealmsServer parse(String pJson) {
      try {
         return parse((new JsonParser()).parse(pJson).getAsJsonObject());
      } catch (Exception exception) {
         LOGGER.error("Could not parse McoServer: {}", (Object)exception.getMessage());
         return new RealmsServer();
      }
   }

   private static RealmsServer.State getState(String pName) {
      try {
         return RealmsServer.State.valueOf(pName);
      } catch (Exception exception) {
         return RealmsServer.State.CLOSED;
      }
   }

   private static RealmsServer.WorldType getWorldType(String pName) {
      try {
         return RealmsServer.WorldType.valueOf(pName);
      } catch (Exception exception) {
         return RealmsServer.WorldType.NORMAL;
      }
   }

   public int hashCode() {
      return Objects.hash(this.id, this.name, this.motd, this.state, this.owner, this.expired);
   }

   public boolean equals(Object pOther) {
      if (pOther == null) {
         return false;
      } else if (pOther == this) {
         return true;
      } else if (pOther.getClass() != this.getClass()) {
         return false;
      } else {
         RealmsServer realmsserver = (RealmsServer)pOther;
         return (new EqualsBuilder()).append(this.id, realmsserver.id).append((Object)this.name, (Object)realmsserver.name).append((Object)this.motd, (Object)realmsserver.motd).append((Object)this.state, (Object)realmsserver.state).append((Object)this.owner, (Object)realmsserver.owner).append(this.expired, realmsserver.expired).append((Object)this.worldType, (Object)this.worldType).isEquals();
      }
   }

   public RealmsServer clone() {
      RealmsServer realmsserver = new RealmsServer();
      realmsserver.id = this.id;
      realmsserver.remoteSubscriptionId = this.remoteSubscriptionId;
      realmsserver.name = this.name;
      realmsserver.motd = this.motd;
      realmsserver.state = this.state;
      realmsserver.owner = this.owner;
      realmsserver.players = this.players;
      realmsserver.slots = this.cloneSlots(this.slots);
      realmsserver.expired = this.expired;
      realmsserver.expiredTrial = this.expiredTrial;
      realmsserver.daysLeft = this.daysLeft;
      realmsserver.serverPing = new RealmsServerPing();
      realmsserver.serverPing.nrOfPlayers = this.serverPing.nrOfPlayers;
      realmsserver.serverPing.playerList = this.serverPing.playerList;
      realmsserver.worldType = this.worldType;
      realmsserver.ownerUUID = this.ownerUUID;
      realmsserver.minigameName = this.minigameName;
      realmsserver.activeSlot = this.activeSlot;
      realmsserver.minigameId = this.minigameId;
      realmsserver.minigameImage = this.minigameImage;
      return realmsserver;
   }

   public Map<Integer, RealmsWorldOptions> cloneSlots(Map<Integer, RealmsWorldOptions> pSlots) {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();

      for(Map.Entry<Integer, RealmsWorldOptions> entry : pSlots.entrySet()) {
         map.put(entry.getKey(), entry.getValue().clone());
      }

      return map;
   }

   public String getWorldName(int pSlot) {
      return this.name + " (" + this.slots.get(pSlot).getSlotName(pSlot) + ")";
   }

   public ServerData toServerData(String pIp) {
      return new ServerData(this.name, pIp, false);
   }

   @OnlyIn(Dist.CLIENT)
   public static class McoServerComparator implements Comparator<RealmsServer> {
      private final String refOwner;

      public McoServerComparator(String pRefOwner) {
         this.refOwner = pRefOwner;
      }

      public int compare(RealmsServer pFirst, RealmsServer pSecond) {
         return ComparisonChain.start().compareTrueFirst(pFirst.state == RealmsServer.State.UNINITIALIZED, pSecond.state == RealmsServer.State.UNINITIALIZED).compareTrueFirst(pFirst.expiredTrial, pSecond.expiredTrial).compareTrueFirst(pFirst.owner.equals(this.refOwner), pSecond.owner.equals(this.refOwner)).compareFalseFirst(pFirst.expired, pSecond.expired).compareTrueFirst(pFirst.state == RealmsServer.State.OPEN, pSecond.state == RealmsServer.State.OPEN).compare(pFirst.id, pSecond.id).result();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum State {
      CLOSED,
      OPEN,
      UNINITIALIZED;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum WorldType {
      NORMAL,
      MINIGAME,
      ADVENTUREMAP,
      EXPERIENCE,
      INSPIRATION;
   }
}