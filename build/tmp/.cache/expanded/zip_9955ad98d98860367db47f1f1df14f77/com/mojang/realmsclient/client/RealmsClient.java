package com.mojang.realmsclient.client;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.ServerActivityList;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsClient {
   public static RealmsClient.Environment currentEnvironment = RealmsClient.Environment.PRODUCTION;
   private static boolean initialized;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String sessionId;
   private final String username;
   private final Minecraft minecraft;
   private static final String WORLDS_RESOURCE_PATH = "worlds";
   private static final String INVITES_RESOURCE_PATH = "invites";
   private static final String MCO_RESOURCE_PATH = "mco";
   private static final String SUBSCRIPTION_RESOURCE = "subscriptions";
   private static final String ACTIVITIES_RESOURCE = "activities";
   private static final String OPS_RESOURCE = "ops";
   private static final String REGIONS_RESOURCE = "regions/ping/stat";
   private static final String TRIALS_RESOURCE = "trial";
   private static final String NOTIFICATIONS_RESOURCE = "notifications";
   private static final String PATH_INITIALIZE = "/$WORLD_ID/initialize";
   private static final String PATH_GET_ACTIVTIES = "/$WORLD_ID";
   private static final String PATH_GET_LIVESTATS = "/liveplayerlist";
   private static final String PATH_GET_SUBSCRIPTION = "/$WORLD_ID";
   private static final String PATH_OP = "/$WORLD_ID/$PROFILE_UUID";
   private static final String PATH_PUT_INTO_MINIGAMES_MODE = "/minigames/$MINIGAME_ID/$WORLD_ID";
   private static final String PATH_AVAILABLE = "/available";
   private static final String PATH_TEMPLATES = "/templates/$WORLD_TYPE";
   private static final String PATH_WORLD_JOIN = "/v1/$ID/join/pc";
   private static final String PATH_WORLD_GET = "/$ID";
   private static final String PATH_WORLD_INVITES = "/$WORLD_ID";
   private static final String PATH_WORLD_UNINVITE = "/$WORLD_ID/invite/$UUID";
   private static final String PATH_PENDING_INVITES_COUNT = "/count/pending";
   private static final String PATH_PENDING_INVITES = "/pending";
   private static final String PATH_ACCEPT_INVITE = "/accept/$INVITATION_ID";
   private static final String PATH_REJECT_INVITE = "/reject/$INVITATION_ID";
   private static final String PATH_UNINVITE_MYSELF = "/$WORLD_ID";
   private static final String PATH_WORLD_UPDATE = "/$WORLD_ID";
   private static final String PATH_SLOT = "/$WORLD_ID/slot/$SLOT_ID";
   private static final String PATH_WORLD_OPEN = "/$WORLD_ID/open";
   private static final String PATH_WORLD_CLOSE = "/$WORLD_ID/close";
   private static final String PATH_WORLD_RESET = "/$WORLD_ID/reset";
   private static final String PATH_DELETE_WORLD = "/$WORLD_ID";
   private static final String PATH_WORLD_BACKUPS = "/$WORLD_ID/backups";
   private static final String PATH_WORLD_DOWNLOAD = "/$WORLD_ID/slot/$SLOT_ID/download";
   private static final String PATH_WORLD_UPLOAD = "/$WORLD_ID/backups/upload";
   private static final String PATH_CLIENT_COMPATIBLE = "/client/compatible";
   private static final String PATH_TOS_AGREED = "/tos/agreed";
   private static final String PATH_NEWS = "/v1/news";
   private static final String PATH_MARK_NOTIFICATIONS_SEEN = "/seen";
   private static final String PATH_DISMISS_NOTIFICATIONS = "/dismiss";
   private static final String PATH_STAGE_AVAILABLE = "/stageAvailable";
   private static final GuardedSerializer GSON = new GuardedSerializer();

   public static RealmsClient create() {
      Minecraft minecraft = Minecraft.getInstance();
      return create(minecraft);
   }

   public static RealmsClient create(Minecraft pMinecraft) {
      String s = pMinecraft.getUser().getName();
      String s1 = pMinecraft.getUser().getSessionId();
      if (!initialized) {
         initialized = true;
         Optional<String> optional = Optional.ofNullable(System.getenv("realms.environment")).or(() -> {
            return Optional.ofNullable(System.getProperty("realms.environment"));
         });
         optional.flatMap(RealmsClient.Environment::byName).ifPresent((p_289648_) -> {
            currentEnvironment = p_289648_;
         });
      }

      return new RealmsClient(s1, s, pMinecraft);
   }

   public static void switchToStage() {
      currentEnvironment = RealmsClient.Environment.STAGE;
   }

   public static void switchToProd() {
      currentEnvironment = RealmsClient.Environment.PRODUCTION;
   }

   public static void switchToLocal() {
      currentEnvironment = RealmsClient.Environment.LOCAL;
   }

   public RealmsClient(String pSessionId, String pUsername, Minecraft pMinecraft) {
      this.sessionId = pSessionId;
      this.username = pUsername;
      this.minecraft = pMinecraft;
      RealmsClientConfig.setProxy(pMinecraft.getProxy());
   }

   public RealmsServerList listWorlds() throws RealmsServiceException {
      String s = this.url("worlds");
      String s1 = this.execute(Request.get(s));
      return RealmsServerList.parse(s1);
   }

   public List<RealmsNotification> getNotifications() throws RealmsServiceException {
      String s = this.url("notifications");
      String s1 = this.execute(Request.get(s));
      List<RealmsNotification> list = RealmsNotification.parseList(s1);
      return list.size() > 1 ? List.of(list.get(0)) : list;
   }

   private static JsonArray uuidListToJsonArray(List<UUID> pUuidList) {
      JsonArray jsonarray = new JsonArray();

      for(UUID uuid : pUuidList) {
         if (uuid != null) {
            jsonarray.add(uuid.toString());
         }
      }

      return jsonarray;
   }

   public void notificationsSeen(List<UUID> pUuidList) throws RealmsServiceException {
      String s = this.url("notifications/seen");
      this.execute(Request.post(s, GSON.toJson(uuidListToJsonArray(pUuidList))));
   }

   public void notificationsDismiss(List<UUID> pUuidList) throws RealmsServiceException {
      String s = this.url("notifications/dismiss");
      this.execute(Request.post(s, GSON.toJson(uuidListToJsonArray(pUuidList))));
   }

   public RealmsServer getOwnWorld(long pServerId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$ID".replace("$ID", String.valueOf(pServerId)));
      String s1 = this.execute(Request.get(s));
      return RealmsServer.parse(s1);
   }

   public ServerActivityList getActivity(long pWorldId) throws RealmsServiceException {
      String s = this.url("activities" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.get(s));
      return ServerActivityList.parse(s1);
   }

   public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
      String s = this.url("activities/liveplayerlist");
      String s1 = this.execute(Request.get(s));
      return RealmsServerPlayerLists.parse(s1);
   }

   public RealmsServerAddress join(long pServerId) throws RealmsServiceException {
      String s = this.url("worlds" + "/v1/$ID/join/pc".replace("$ID", "" + pServerId));
      String s1 = this.execute(Request.get(s, 5000, 30000));
      return RealmsServerAddress.parse(s1);
   }

   public void initializeWorld(long pWorldId, String pName, String pDescription) throws RealmsServiceException {
      RealmsDescriptionDto realmsdescriptiondto = new RealmsDescriptionDto(pName, pDescription);
      String s = this.url("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = GSON.toJson(realmsdescriptiondto);
      this.execute(Request.post(s, s1, 5000, 10000));
   }

   public Boolean mcoEnabled() throws RealmsServiceException {
      String s = this.url("mco/available");
      String s1 = this.execute(Request.get(s));
      return Boolean.valueOf(s1);
   }

   public Boolean stageAvailable() throws RealmsServiceException {
      String s = this.url("mco/stageAvailable");
      String s1 = this.execute(Request.get(s));
      return Boolean.valueOf(s1);
   }

   public RealmsClient.CompatibleVersionResponse clientCompatible() throws RealmsServiceException {
      String s = this.url("mco/client/compatible");
      String s1 = this.execute(Request.get(s));

      try {
         return RealmsClient.CompatibleVersionResponse.valueOf(s1);
      } catch (IllegalArgumentException illegalargumentexception) {
         throw new RealmsServiceException(500, "Could not check compatible version, got response: " + s1);
      }
   }

   public void uninvite(long pWorldId, String pUuid) throws RealmsServiceException {
      String s = this.url("invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(pWorldId)).replace("$UUID", pUuid));
      this.execute(Request.delete(s));
   }

   public void uninviteMyselfFrom(long pWorldId) throws RealmsServiceException {
      String s = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(pWorldId)));
      this.execute(Request.delete(s));
   }

   public RealmsServer invite(long pWorldId, String pPlayerName) throws RealmsServiceException {
      PlayerInfo playerinfo = new PlayerInfo();
      playerinfo.setName(pPlayerName);
      String s = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.post(s, GSON.toJson(playerinfo)));
      return RealmsServer.parse(s1);
   }

   public BackupList backupsFor(long pWorldId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.get(s));
      return BackupList.parse(s1);
   }

   public void update(long pWorldId, String pName, String pDescription) throws RealmsServiceException {
      RealmsDescriptionDto realmsdescriptiondto = new RealmsDescriptionDto(pName, pDescription);
      String s = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(pWorldId)));
      this.execute(Request.post(s, GSON.toJson(realmsdescriptiondto)));
   }

   public void updateSlot(long pWorldId, int pSlotId, RealmsWorldOptions pWorldOptions) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(pWorldId)).replace("$SLOT_ID", String.valueOf(pSlotId)));
      String s1 = pWorldOptions.toJson();
      this.execute(Request.post(s, s1));
   }

   public boolean switchSlot(long pWorldId, int pSlotId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(pWorldId)).replace("$SLOT_ID", String.valueOf(pSlotId)));
      String s1 = this.execute(Request.put(s, ""));
      return Boolean.valueOf(s1);
   }

   public void restoreWorld(long pWorldId, String pBackupId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(pWorldId)), "backupId=" + pBackupId);
      this.execute(Request.put(s, "", 40000, 600000));
   }

   public WorldTemplatePaginatedList fetchWorldTemplates(int pPage, int pPageSize, RealmsServer.WorldType pWorldType) throws RealmsServiceException {
      String s = this.url("worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", pWorldType.toString()), String.format(Locale.ROOT, "page=%d&pageSize=%d", pPage, pPageSize));
      String s1 = this.execute(Request.get(s));
      return WorldTemplatePaginatedList.parse(s1);
   }

   public Boolean putIntoMinigameMode(long pWorldId, String pMinigameId) throws RealmsServiceException {
      String s = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", pMinigameId).replace("$WORLD_ID", String.valueOf(pWorldId));
      String s1 = this.url("worlds" + s);
      return Boolean.valueOf(this.execute(Request.put(s1, "")));
   }

   public Ops op(long pWorldId, String pProfileUuid) throws RealmsServiceException {
      String s = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(pWorldId)).replace("$PROFILE_UUID", pProfileUuid);
      String s1 = this.url("ops" + s);
      return Ops.parse(this.execute(Request.post(s1, "")));
   }

   public Ops deop(long pWorldId, String pProfileUuid) throws RealmsServiceException {
      String s = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(pWorldId)).replace("$PROFILE_UUID", pProfileUuid);
      String s1 = this.url("ops" + s);
      return Ops.parse(this.execute(Request.delete(s1)));
   }

   public Boolean open(long pWorldId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/open".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.put(s, ""));
      return Boolean.valueOf(s1);
   }

   public Boolean close(long pWorldId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/close".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.put(s, ""));
      return Boolean.valueOf(s1);
   }

   public Boolean resetWorldWithSeed(long pWorldId, WorldGenerationInfo pGenerationInfo) throws RealmsServiceException {
      RealmsWorldResetDto realmsworldresetdto = new RealmsWorldResetDto(pGenerationInfo.getSeed(), -1L, pGenerationInfo.getLevelType().getDtoIndex(), pGenerationInfo.shouldGenerateStructures());
      String s = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.post(s, GSON.toJson(realmsworldresetdto), 30000, 80000));
      return Boolean.valueOf(s1);
   }

   public Boolean resetWorldWithTemplate(long pWorldId, String pWorldTemplateId) throws RealmsServiceException {
      RealmsWorldResetDto realmsworldresetdto = new RealmsWorldResetDto((String)null, Long.valueOf(pWorldTemplateId), -1, false);
      String s = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.post(s, GSON.toJson(realmsworldresetdto), 30000, 80000));
      return Boolean.valueOf(s1);
   }

   public Subscription subscriptionFor(long pWorldId) throws RealmsServiceException {
      String s = this.url("subscriptions" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(pWorldId)));
      String s1 = this.execute(Request.get(s));
      return Subscription.parse(s1);
   }

   public int pendingInvitesCount() throws RealmsServiceException {
      return this.pendingInvites().pendingInvites.size();
   }

   public PendingInvitesList pendingInvites() throws RealmsServiceException {
      String s = this.url("invites/pending");
      String s1 = this.execute(Request.get(s));
      PendingInvitesList pendinginviteslist = PendingInvitesList.parse(s1);
      pendinginviteslist.pendingInvites.removeIf(this::isBlocked);
      return pendinginviteslist;
   }

   private boolean isBlocked(PendingInvite p_87198_) {
      try {
         UUID uuid = UUID.fromString(p_87198_.worldOwnerUuid);
         return this.minecraft.getPlayerSocialManager().isBlocked(uuid);
      } catch (IllegalArgumentException illegalargumentexception) {
         return false;
      }
   }

   public void acceptInvitation(String pInviteId) throws RealmsServiceException {
      String s = this.url("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", pInviteId));
      this.execute(Request.put(s, ""));
   }

   public WorldDownload requestDownloadInfo(long pWorldId, int pSlotId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(pWorldId)).replace("$SLOT_ID", String.valueOf(pSlotId)));
      String s1 = this.execute(Request.get(s));
      return WorldDownload.parse(s1);
   }

   @Nullable
   public UploadInfo requestUploadInfo(long pWorldId, @Nullable String pToken) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(pWorldId)));
      return UploadInfo.parse(this.execute(Request.put(s, UploadInfo.createRequest(pToken))));
   }

   public void rejectInvitation(String pInviteId) throws RealmsServiceException {
      String s = this.url("invites" + "/reject/$INVITATION_ID".replace("$INVITATION_ID", pInviteId));
      this.execute(Request.put(s, ""));
   }

   public void agreeToTos() throws RealmsServiceException {
      String s = this.url("mco/tos/agreed");
      this.execute(Request.post(s, ""));
   }

   public RealmsNews getNews() throws RealmsServiceException {
      String s = this.url("mco/v1/news");
      String s1 = this.execute(Request.get(s, 5000, 10000));
      return RealmsNews.parse(s1);
   }

   public void sendPingResults(PingResult pPingResult) throws RealmsServiceException {
      String s = this.url("regions/ping/stat");
      this.execute(Request.post(s, GSON.toJson(pPingResult)));
   }

   public Boolean trialAvailable() throws RealmsServiceException {
      String s = this.url("trial");
      String s1 = this.execute(Request.get(s));
      return Boolean.valueOf(s1);
   }

   public void deleteWorld(long pWorldId) throws RealmsServiceException {
      String s = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(pWorldId)));
      this.execute(Request.delete(s));
   }

   private String url(String pPath) {
      return this.url(pPath, (String)null);
   }

   private String url(String pPath, @Nullable String pQuery) {
      try {
         return (new URI(currentEnvironment.protocol, currentEnvironment.baseUrl, "/" + pPath, pQuery, (String)null)).toASCIIString();
      } catch (URISyntaxException urisyntaxexception) {
         throw new IllegalArgumentException(pPath, urisyntaxexception);
      }
   }

   private String execute(Request<?> pRequest) throws RealmsServiceException {
      pRequest.cookie("sid", this.sessionId);
      pRequest.cookie("user", this.username);
      pRequest.cookie("version", SharedConstants.getCurrentVersion().getName());

      try {
         int i = pRequest.responseCode();
         if (i != 503 && i != 277) {
            String s1 = pRequest.text();
            if (i >= 200 && i < 300) {
               return s1;
            } else if (i == 401) {
               String s2 = pRequest.getHeader("WWW-Authenticate");
               LOGGER.info("Could not authorize you against Realms server: {}", (Object)s2);
               throw new RealmsServiceException(i, s2);
            } else {
               RealmsError realmserror = RealmsError.parse(s1);
               if (realmserror != null) {
                  LOGGER.error("Realms http code: {} -  error code: {} -  message: {} - raw body: {}", i, realmserror.getErrorCode(), realmserror.getErrorMessage(), s1);
                  throw new RealmsServiceException(i, s1, realmserror);
               } else {
                  LOGGER.error("Realms http code: {} - raw body (message failed to parse): {}", i, s1);
                  String s = getHttpCodeDescription(i);
                  throw new RealmsServiceException(i, s);
               }
            }
         } else {
            int j = pRequest.getRetryAfterHeader();
            throw new RetryCallException(j, i);
         }
      } catch (RealmsHttpException realmshttpexception) {
         throw new RealmsServiceException(500, "Could not connect to Realms: " + realmshttpexception.getMessage());
      }
   }

   private static String getHttpCodeDescription(int pHttpCode) {
      String s;
      switch (pHttpCode) {
         case 429:
            s = I18n.get("mco.errorMessage.serviceBusy");
            break;
         default:
            s = "Unknown error";
      }

      return s;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum CompatibleVersionResponse {
      COMPATIBLE,
      OUTDATED,
      OTHER;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Environment {
      PRODUCTION("pc.realms.minecraft.net", "https"),
      STAGE("pc-stage.realms.minecraft.net", "https"),
      LOCAL("localhost:8080", "http");

      public String baseUrl;
      public String protocol;

      private Environment(String pBaseUrl, String pProtocol) {
         this.baseUrl = pBaseUrl;
         this.protocol = pProtocol;
      }

      public static Optional<RealmsClient.Environment> byName(String pName) {
         Optional optional;
         switch (pName.toLowerCase(Locale.ROOT)) {
            case "production":
               optional = Optional.of(PRODUCTION);
               break;
            case "local":
               optional = Optional.of(LOCAL);
               break;
            case "stage":
               optional = Optional.of(STAGE);
               break;
            default:
               optional = Optional.empty();
         }

         return optional;
      }
   }
}