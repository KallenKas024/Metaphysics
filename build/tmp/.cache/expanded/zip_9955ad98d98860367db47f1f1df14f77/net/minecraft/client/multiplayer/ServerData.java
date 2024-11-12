package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerData {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String name;
   public String ip;
   public Component status;
   public Component motd;
   @Nullable
   public ServerStatus.Players players;
   public long ping;
   public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
   public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
   public boolean pinged;
   public List<Component> playerList = Collections.emptyList();
   private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
   @Nullable
   private byte[] iconBytes;
   /** True if the server is a LAN server */
   private boolean lan;
   private boolean enforcesSecureChat;
   public net.minecraftforge.client.ExtendedServerListData forgeData = null;

   public ServerData(String pName, String pIp, boolean pLan) {
      this.name = pName;
      this.ip = pIp;
      this.lan = pLan;
   }

   /**
    * Returns an NBTTagCompound with the server's name, IP and maybe acceptTextures.
    */
   public CompoundTag write() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("name", this.name);
      compoundtag.putString("ip", this.ip);
      if (this.iconBytes != null) {
         compoundtag.putString("icon", Base64.getEncoder().encodeToString(this.iconBytes));
      }

      if (this.packStatus == ServerData.ServerPackStatus.ENABLED) {
         compoundtag.putBoolean("acceptTextures", true);
      } else if (this.packStatus == ServerData.ServerPackStatus.DISABLED) {
         compoundtag.putBoolean("acceptTextures", false);
      }

      return compoundtag;
   }

   public ServerData.ServerPackStatus getResourcePackStatus() {
      return this.packStatus;
   }

   public void setResourcePackStatus(ServerData.ServerPackStatus pPackStatus) {
      this.packStatus = pPackStatus;
   }

   /**
    * Takes an NBTTagCompound with 'name' and 'ip' keys, returns a ServerData instance.
    */
   public static ServerData read(CompoundTag pNbtCompound) {
      ServerData serverdata = new ServerData(pNbtCompound.getString("name"), pNbtCompound.getString("ip"), false);
      if (pNbtCompound.contains("icon", 8)) {
         try {
            serverdata.setIconBytes(Base64.getDecoder().decode(pNbtCompound.getString("icon")));
         } catch (IllegalArgumentException illegalargumentexception) {
            LOGGER.warn("Malformed base64 server icon", (Throwable)illegalargumentexception);
         }
      }

      if (pNbtCompound.contains("acceptTextures", 1)) {
         if (pNbtCompound.getBoolean("acceptTextures")) {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
         } else {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
         }
      } else {
         serverdata.setResourcePackStatus(ServerData.ServerPackStatus.PROMPT);
      }

      return serverdata;
   }

   @Nullable
   public byte[] getIconBytes() {
      return this.iconBytes;
   }

   public void setIconBytes(@Nullable byte[] pIconBytes) {
      this.iconBytes = pIconBytes;
   }

   /**
    * Returns {@code true} if the server is a LAN server.
    */
   public boolean isLan() {
      return this.lan;
   }

   public void setEnforcesSecureChat(boolean pEnforcesSecureChat) {
      this.enforcesSecureChat = pEnforcesSecureChat;
   }

   public boolean enforcesSecureChat() {
      return this.enforcesSecureChat;
   }

   public void copyNameIconFrom(ServerData pOther) {
      this.ip = pOther.ip;
      this.name = pOther.name;
      this.iconBytes = pOther.iconBytes;
   }

   public void copyFrom(ServerData pServerData) {
      this.copyNameIconFrom(pServerData);
      this.setResourcePackStatus(pServerData.getResourcePackStatus());
      this.lan = pServerData.lan;
      this.enforcesSecureChat = pServerData.enforcesSecureChat;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum ServerPackStatus {
      ENABLED("enabled"),
      DISABLED("disabled"),
      PROMPT("prompt");

      private final Component name;

      private ServerPackStatus(String pName) {
         this.name = Component.translatable("addServer.resourcePack." + pName);
      }

      public Component getName() {
         return this.name;
      }
   }
}
