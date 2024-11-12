package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class PlayerList {
   public static final File USERBANLIST_FILE = new File("banned-players.json");
   public static final File IPBANLIST_FILE = new File("banned-ips.json");
   public static final File OPLIST_FILE = new File("ops.json");
   public static final File WHITELIST_FILE = new File("whitelist.json");
   public static final Component CHAT_FILTERED_FULL = Component.translatable("chat.filtered_full");
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SEND_PLAYER_INFO_INTERVAL = 600;
   private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private final MinecraftServer server;
   private final List<ServerPlayer> players = Lists.newArrayList();
   /** A map containing the key-value pairs for UUIDs and their EntityPlayerMP objects. */
   private final Map<UUID, ServerPlayer> playersByUUID = Maps.newHashMap();
   private final UserBanList bans = new UserBanList(USERBANLIST_FILE);
   private final IpBanList ipBans = new IpBanList(IPBANLIST_FILE);
   private final ServerOpList ops = new ServerOpList(OPLIST_FILE);
   private final UserWhiteList whitelist = new UserWhiteList(WHITELIST_FILE);
   private final Map<UUID, ServerStatsCounter> stats = Maps.newHashMap();
   private final Map<UUID, PlayerAdvancements> advancements = Maps.newHashMap();
   private final PlayerDataStorage playerIo;
   private boolean doWhiteList;
   private final LayeredRegistryAccess<RegistryLayer> registries;
   private final RegistryAccess.Frozen synchronizedRegistries;
   protected final int maxPlayers;
   private int viewDistance;
   private int simulationDistance;
   private boolean allowCheatsForAllPlayers;
   private static final boolean ALLOW_LOGOUTIVATOR = false;
   private int sendAllPlayerInfoIn;
   private final List<ServerPlayer> playersView = java.util.Collections.unmodifiableList(players);

   public PlayerList(MinecraftServer pServer, LayeredRegistryAccess<RegistryLayer> pRegistries, PlayerDataStorage pPlayerIo, int pMaxPlayers) {
      this.server = pServer;
      this.registries = pRegistries;
      this.synchronizedRegistries = (new RegistryAccess.ImmutableRegistryAccess(RegistrySynchronization.networkedRegistries(pRegistries))).freeze();
      this.maxPlayers = pMaxPlayers;
      this.playerIo = pPlayerIo;
   }

   public void placeNewPlayer(Connection pNetManager, ServerPlayer pPlayer) {
      GameProfile gameprofile = pPlayer.getGameProfile();
      GameProfileCache gameprofilecache = this.server.getProfileCache();
      String s;
      if (gameprofilecache != null) {
         Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
         s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
         gameprofilecache.add(gameprofile);
      } else {
         s = gameprofile.getName();
      }

      CompoundTag compoundtag = this.load(pPlayer);
      ResourceKey<Level> resourcekey = compoundtag != null ? DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Dimension"))).resultOrPartial(LOGGER::error).orElse(Level.OVERWORLD) : Level.OVERWORLD;
      ServerLevel serverlevel = this.server.getLevel(resourcekey);
      ServerLevel serverlevel1;
      if (serverlevel == null) {
         LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", (Object)resourcekey);
         serverlevel1 = this.server.overworld();
      } else {
         serverlevel1 = serverlevel;
      }

      pPlayer.setServerLevel(serverlevel1);
      String s1 = "local";
      if (pNetManager.getRemoteAddress() != null) {
         s1 = net.minecraftforge.network.DualStackUtils.getAddressString(pNetManager.getRemoteAddress());
      }

      LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", pPlayer.getName().getString(), s1, pPlayer.getId(), pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
      LevelData leveldata = serverlevel1.getLevelData();
      pPlayer.loadGameTypes(compoundtag);
      ServerGamePacketListenerImpl servergamepacketlistenerimpl = new ServerGamePacketListenerImpl(this.server, pNetManager, pPlayer);
      net.minecraftforge.network.NetworkHooks.sendMCRegistryPackets(pNetManager, "PLAY_TO_CLIENT");
      GameRules gamerules = serverlevel1.getGameRules();
      boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
      boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
      servergamepacketlistenerimpl.send(new ClientboundLoginPacket(pPlayer.getId(), leveldata.isHardcore(), pPlayer.gameMode.getGameModeForPlayer(), pPlayer.gameMode.getPreviousGameModeForPlayer(), this.server.levelKeys(), this.synchronizedRegistries, serverlevel1.dimensionTypeId(), serverlevel1.dimension(), BiomeManager.obfuscateSeed(serverlevel1.getSeed()), this.getMaxPlayers(), this.viewDistance, this.simulationDistance, flag1, !flag, serverlevel1.isDebug(), serverlevel1.isFlat(), pPlayer.getLastDeathLocation(), pPlayer.getPortalCooldown()));
      servergamepacketlistenerimpl.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(serverlevel1.enabledFeatures())));
      servergamepacketlistenerimpl.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(this.getServer().getServerModName())));
      servergamepacketlistenerimpl.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
      servergamepacketlistenerimpl.send(new ClientboundPlayerAbilitiesPacket(pPlayer.getAbilities()));
      servergamepacketlistenerimpl.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.OnDatapackSyncEvent(this, pPlayer));
      servergamepacketlistenerimpl.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
      servergamepacketlistenerimpl.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
      this.sendPlayerPermissionLevel(pPlayer);
      pPlayer.getStats().markAllDirty();
      pPlayer.getRecipeBook().sendInitialRecipeBook(pPlayer);
      this.updateEntireScoreboard(serverlevel1.getScoreboard(), pPlayer);
      this.server.invalidateStatus();
      MutableComponent mutablecomponent;
      if (pPlayer.getGameProfile().getName().equalsIgnoreCase(s)) {
         mutablecomponent = Component.translatable("multiplayer.player.joined", pPlayer.getDisplayName());
      } else {
         mutablecomponent = Component.translatable("multiplayer.player.joined.renamed", pPlayer.getDisplayName(), s);
      }

      this.broadcastSystemMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), false);
      servergamepacketlistenerimpl.teleport(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.getYRot(), pPlayer.getXRot());
      ServerStatus serverstatus = this.server.getStatus();
      if (serverstatus != null) {
         pPlayer.sendServerStatus(serverstatus);
      }

      pPlayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(this.players));
      this.players.add(pPlayer);
      this.playersByUUID.put(pPlayer.getUUID(), pPlayer);
      this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(pPlayer)));
      this.sendLevelInfo(pPlayer, serverlevel1);
      serverlevel1.addNewPlayer(pPlayer);
      this.server.getCustomBossEvents().onPlayerConnect(pPlayer);
      this.server.getServerResourcePack().ifPresent((p_215606_) -> {
         pPlayer.sendTexturePack(p_215606_.url(), p_215606_.hash(), p_215606_.isRequired(), p_215606_.prompt());
      });

      for(MobEffectInstance mobeffectinstance : pPlayer.getActiveEffects()) {
         servergamepacketlistenerimpl.send(new ClientboundUpdateMobEffectPacket(pPlayer.getId(), mobeffectinstance));
      }

      if (compoundtag != null && compoundtag.contains("RootVehicle", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("RootVehicle");
         Entity entity1 = EntityType.loadEntityRecursive(compoundtag1.getCompound("Entity"), serverlevel1, (p_215603_) -> {
            return !serverlevel1.addWithUUID(p_215603_) ? null : p_215603_;
         });
         if (entity1 != null) {
            UUID uuid;
            if (compoundtag1.hasUUID("Attach")) {
               uuid = compoundtag1.getUUID("Attach");
            } else {
               uuid = null;
            }

            if (entity1.getUUID().equals(uuid)) {
               pPlayer.startRiding(entity1, true);
            } else {
               for(Entity entity : entity1.getIndirectPassengers()) {
                  if (entity.getUUID().equals(uuid)) {
                     pPlayer.startRiding(entity, true);
                     break;
                  }
               }
            }

            if (!pPlayer.isPassenger()) {
               LOGGER.warn("Couldn't reattach entity to player");
               entity1.discard();

               for(Entity entity2 : entity1.getIndirectPassengers()) {
                  entity2.discard();
               }
            }
         }
      }

      pPlayer.initInventoryMenu();
      net.minecraftforge.event.ForgeEventFactory.firePlayerLoggedIn( pPlayer );
   }

   protected void updateEntireScoreboard(ServerScoreboard pScoreboard, ServerPlayer pPlayer) {
      Set<Objective> set = Sets.newHashSet();

      for(PlayerTeam playerteam : pScoreboard.getPlayerTeams()) {
         pPlayer.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerteam, true));
      }

      for(int i = 0; i < 19; ++i) {
         Objective objective = pScoreboard.getDisplayObjective(i);
         if (objective != null && !set.contains(objective)) {
            for(Packet<?> packet : pScoreboard.getStartTrackingPackets(objective)) {
               pPlayer.connection.send(packet);
            }

            set.add(objective);
         }
      }

   }

   public void addWorldborderListener(ServerLevel pLevel) {
      pLevel.getWorldBorder().addListener(new BorderChangeListener() {
         public void onBorderSizeSet(WorldBorder p_11321_, double p_11322_) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket(p_11321_));
         }

         public void onBorderSizeLerping(WorldBorder p_11328_, double p_11329_, double p_11330_, long p_11331_) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket(p_11328_));
         }

         public void onBorderCenterSet(WorldBorder p_11324_, double p_11325_, double p_11326_) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket(p_11324_));
         }

         public void onBorderSetWarningTime(WorldBorder p_11333_, int p_11334_) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket(p_11333_));
         }

         public void onBorderSetWarningBlocks(WorldBorder p_11339_, int p_11340_) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket(p_11339_));
         }

         public void onBorderSetDamagePerBlock(WorldBorder p_11336_, double p_11337_) {
         }

         public void onBorderSetDamageSafeZOne(WorldBorder p_11342_, double p_11343_) {
         }
      });
   }

   /**
    * Called during player login. Reads the player information from disk.
    */
   @Nullable
   public CompoundTag load(ServerPlayer pPlayer) {
      CompoundTag compoundtag = this.server.getWorldData().getLoadedPlayerTag();
      CompoundTag compoundtag1;
      if (this.server.isSingleplayerOwner(pPlayer.getGameProfile()) && compoundtag != null) {
         compoundtag1 = compoundtag;
         pPlayer.load(compoundtag);
         LOGGER.debug("loading single player");
         net.minecraftforge.event.ForgeEventFactory.firePlayerLoadingEvent(pPlayer, this.playerIo, pPlayer.getUUID().toString());
      } else {
         compoundtag1 = this.playerIo.load(pPlayer);
      }

      return compoundtag1;
   }

   /**
    * Also stores the NBTTags if this is an IntegratedPlayerList.
    */
   protected void save(ServerPlayer pPlayer) {
      if (pPlayer.connection == null) return;
      this.playerIo.save(pPlayer);
      ServerStatsCounter serverstatscounter = this.stats.get(pPlayer.getUUID());
      if (serverstatscounter != null) {
         serverstatscounter.save();
      }

      PlayerAdvancements playeradvancements = this.advancements.get(pPlayer.getUUID());
      if (playeradvancements != null) {
         playeradvancements.save();
      }

   }

   /**
    * Called when a player disconnects from the game. Writes player data to disk and removes them from the world.
    */
   public void remove(ServerPlayer pPlayer) {
      net.minecraftforge.event.ForgeEventFactory.firePlayerLoggedOut(pPlayer);
      ServerLevel serverlevel = pPlayer.serverLevel();
      pPlayer.awardStat(Stats.LEAVE_GAME);
      this.save(pPlayer);
      if (pPlayer.isPassenger()) {
         Entity entity = pPlayer.getRootVehicle();
         if (entity.hasExactlyOnePlayerPassenger()) {
            LOGGER.debug("Removing player mount");
            pPlayer.stopRiding();
            entity.getPassengersAndSelf().forEach((p_215620_) -> {
               p_215620_.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            });
         }
      }

      pPlayer.unRide();
      serverlevel.removePlayerImmediately(pPlayer, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
      pPlayer.getAdvancements().stopListening();
      this.players.remove(pPlayer);
      this.server.getCustomBossEvents().onPlayerDisconnect(pPlayer);
      UUID uuid = pPlayer.getUUID();
      ServerPlayer serverplayer = this.playersByUUID.get(uuid);
      if (serverplayer == pPlayer) {
         this.playersByUUID.remove(uuid);
         this.stats.remove(uuid);
         this.advancements.remove(uuid);
      }

      this.broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(pPlayer.getUUID())));
   }

   @Nullable
   public Component canPlayerLogin(SocketAddress pSocketAddress, GameProfile pGameProfile) {
      if (this.bans.isBanned(pGameProfile)) {
         UserBanListEntry userbanlistentry = this.bans.get(pGameProfile);
         MutableComponent mutablecomponent1 = Component.translatable("multiplayer.disconnect.banned.reason", userbanlistentry.getReason());
         if (userbanlistentry.getExpires() != null) {
            mutablecomponent1.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userbanlistentry.getExpires())));
         }

         return mutablecomponent1;
      } else if (!this.isWhiteListed(pGameProfile)) {
         return Component.translatable("multiplayer.disconnect.not_whitelisted");
      } else if (this.ipBans.isBanned(pSocketAddress)) {
         IpBanListEntry ipbanlistentry = this.ipBans.get(pSocketAddress);
         MutableComponent mutablecomponent = Component.translatable("multiplayer.disconnect.banned_ip.reason", ipbanlistentry.getReason());
         if (ipbanlistentry.getExpires() != null) {
            mutablecomponent.append(Component.translatable("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanlistentry.getExpires())));
         }

         return mutablecomponent;
      } else {
         return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(pGameProfile) ? Component.translatable("multiplayer.disconnect.server_full") : null;
      }
   }

   public ServerPlayer getPlayerForLogin(GameProfile pProfile) {
      UUID uuid = UUIDUtil.getOrCreatePlayerUUID(pProfile);
      List<ServerPlayer> list = Lists.newArrayList();

      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayer serverplayer = this.players.get(i);
         if (serverplayer.getUUID().equals(uuid)) {
            list.add(serverplayer);
         }
      }

      ServerPlayer serverplayer2 = this.playersByUUID.get(pProfile.getId());
      if (serverplayer2 != null && !list.contains(serverplayer2)) {
         list.add(serverplayer2);
      }

      for(ServerPlayer serverplayer1 : list) {
         serverplayer1.connection.disconnect(Component.translatable("multiplayer.disconnect.duplicate_login"));
      }

      return new ServerPlayer(this.server, this.server.overworld(), pProfile);
   }

   public ServerPlayer respawn(ServerPlayer pPlayer, boolean pKeepEverything) {
      this.players.remove(pPlayer);
      pPlayer.serverLevel().removePlayerImmediately(pPlayer, Entity.RemovalReason.DISCARDED);
      BlockPos blockpos = pPlayer.getRespawnPosition();
      float f = pPlayer.getRespawnAngle();
      boolean flag = pPlayer.isRespawnForced();
      ServerLevel serverlevel = this.server.getLevel(pPlayer.getRespawnDimension());
      Optional<Vec3> optional;
      if (serverlevel != null && blockpos != null) {
         optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag, pKeepEverything);
      } else {
         optional = Optional.empty();
      }

      ServerLevel serverlevel1 = serverlevel != null && optional.isPresent() ? serverlevel : this.server.overworld();
      ServerPlayer serverplayer = new ServerPlayer(this.server, serverlevel1, pPlayer.getGameProfile());
      serverplayer.connection = pPlayer.connection;
      serverplayer.restoreFrom(pPlayer, pKeepEverything);
      serverplayer.setId(pPlayer.getId());
      serverplayer.setMainArm(pPlayer.getMainArm());

      for(String s : pPlayer.getTags()) {
         serverplayer.addTag(s);
      }

      boolean flag2 = false;
      if (optional.isPresent()) {
         BlockState blockstate = serverlevel1.getBlockState(blockpos);
         boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
         Vec3 vec3 = optional.get();
         float f1;
         if (!blockstate.is(BlockTags.BEDS) && !flag1) {
            f1 = f;
         } else {
            Vec3 vec31 = Vec3.atBottomCenterOf(blockpos).subtract(vec3).normalize();
            f1 = (float)Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double)(180F / (float)Math.PI) - 90.0D);
         }

         serverplayer.moveTo(vec3.x, vec3.y, vec3.z, f1, 0.0F);
         serverplayer.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag, false);
         flag2 = !pKeepEverything && flag1;
      } else if (blockpos != null) {
         serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
      }

      while(!serverlevel1.noCollision(serverplayer) && serverplayer.getY() < (double)serverlevel1.getMaxBuildHeight()) {
         serverplayer.setPos(serverplayer.getX(), serverplayer.getY() + 1.0D, serverplayer.getZ());
      }

      byte b0 = (byte)(pKeepEverything ? 1 : 0);
      LevelData leveldata = serverplayer.level().getLevelData();
      serverplayer.connection.send(new ClientboundRespawnPacket(serverplayer.level().dimensionTypeId(), serverplayer.level().dimension(), BiomeManager.obfuscateSeed(serverplayer.serverLevel().getSeed()), serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer(), serverplayer.level().isDebug(), serverplayer.serverLevel().isFlat(), b0, serverplayer.getLastDeathLocation(), serverplayer.getPortalCooldown()));
      serverplayer.connection.teleport(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), serverplayer.getYRot(), serverplayer.getXRot());
      serverplayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverlevel1.getSharedSpawnPos(), serverlevel1.getSharedSpawnAngle()));
      serverplayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
      serverplayer.connection.send(new ClientboundSetExperiencePacket(serverplayer.experienceProgress, serverplayer.totalExperience, serverplayer.experienceLevel));
      this.sendLevelInfo(serverplayer, serverlevel1);
      this.sendPlayerPermissionLevel(serverplayer);
      serverlevel1.addRespawnedPlayer(serverplayer);
      this.players.add(serverplayer);
      this.playersByUUID.put(serverplayer.getUUID(), serverplayer);
      serverplayer.initInventoryMenu();
      serverplayer.setHealth(serverplayer.getHealth());
      net.minecraftforge.event.ForgeEventFactory.firePlayerRespawnEvent(serverplayer, pKeepEverything);
      if (flag2) {
         serverplayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0F, 1.0F, serverlevel1.getRandom().nextLong()));
      }

      return serverplayer;
   }

   public void sendPlayerPermissionLevel(ServerPlayer pPlayer) {
      GameProfile gameprofile = pPlayer.getGameProfile();
      int i = this.server.getProfilePermissions(gameprofile);
      this.sendPlayerPermissionLevel(pPlayer, i);
   }

   public void tick() {
      if (++this.sendAllPlayerInfoIn > 600) {
         this.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY), this.players));
         this.sendAllPlayerInfoIn = 0;
      }

   }

   public void broadcastAll(Packet<?> pPacket) {
      for(ServerPlayer serverplayer : this.players) {
         serverplayer.connection.send(pPacket);
      }

   }

   public void broadcastAll(Packet<?> pPacket, ResourceKey<Level> pDimension) {
      for(ServerPlayer serverplayer : this.players) {
         if (serverplayer.level().dimension() == pDimension) {
            serverplayer.connection.send(pPacket);
         }
      }

   }

   public void broadcastSystemToTeam(Player pPlayer, Component pMessage) {
      Team team = pPlayer.getTeam();
      if (team != null) {
         for(String s : team.getPlayers()) {
            ServerPlayer serverplayer = this.getPlayerByName(s);
            if (serverplayer != null && serverplayer != pPlayer) {
               serverplayer.sendSystemMessage(pMessage);
            }
         }

      }
   }

   public void broadcastSystemToAllExceptTeam(Player pPlayer, Component pMessage) {
      Team team = pPlayer.getTeam();
      if (team == null) {
         this.broadcastSystemMessage(pMessage, false);
      } else {
         for(int i = 0; i < this.players.size(); ++i) {
            ServerPlayer serverplayer = this.players.get(i);
            if (serverplayer.getTeam() != team) {
               serverplayer.sendSystemMessage(pMessage);
            }
         }

      }
   }

   /**
    * Returns an array of the usernames of all the connected players.
    */
   public String[] getPlayerNamesArray() {
      String[] astring = new String[this.players.size()];

      for(int i = 0; i < this.players.size(); ++i) {
         astring[i] = this.players.get(i).getGameProfile().getName();
      }

      return astring;
   }

   public UserBanList getBans() {
      return this.bans;
   }

   public IpBanList getIpBans() {
      return this.ipBans;
   }

   public void op(GameProfile pProfile) {
      if (net.minecraftforge.event.ForgeEventFactory.onPermissionChanged(pProfile, this.server.getOperatorUserPermissionLevel(), this)) return;
      this.ops.add(new ServerOpListEntry(pProfile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(pProfile)));
      ServerPlayer serverplayer = this.getPlayer(pProfile.getId());
      if (serverplayer != null) {
         this.sendPlayerPermissionLevel(serverplayer);
      }

   }

   public void deop(GameProfile pProfile) {
      if (net.minecraftforge.event.ForgeEventFactory.onPermissionChanged(pProfile, 0, this)) return;
      this.ops.remove(pProfile);
      ServerPlayer serverplayer = this.getPlayer(pProfile.getId());
      if (serverplayer != null) {
         this.sendPlayerPermissionLevel(serverplayer);
      }

   }

   private void sendPlayerPermissionLevel(ServerPlayer pPlayer, int pPermLevel) {
      if (pPlayer.connection != null) {
         byte b0;
         if (pPermLevel <= 0) {
            b0 = 24;
         } else if (pPermLevel >= 4) {
            b0 = 28;
         } else {
            b0 = (byte)(24 + pPermLevel);
         }

         pPlayer.connection.send(new ClientboundEntityEventPacket(pPlayer, b0));
      }

      this.server.getCommands().sendCommands(pPlayer);
   }

   public boolean isWhiteListed(GameProfile pProfile) {
      return !this.doWhiteList || this.ops.contains(pProfile) || this.whitelist.contains(pProfile);
   }

   public boolean isOp(GameProfile pProfile) {
      return this.ops.contains(pProfile) || this.server.isSingleplayerOwner(pProfile) && this.server.getWorldData().getAllowCommands() || this.allowCheatsForAllPlayers;
   }

   @Nullable
   public ServerPlayer getPlayerByName(String pUsername) {
      for(ServerPlayer serverplayer : this.players) {
         if (serverplayer.getGameProfile().getName().equalsIgnoreCase(pUsername)) {
            return serverplayer;
         }
      }

      return null;
   }

   public void broadcast(@Nullable Player pExcept, double pX, double pY, double pZ, double pRadius, ResourceKey<Level> pDimension, Packet<?> pPacket) {
      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayer serverplayer = this.players.get(i);
         if (serverplayer != pExcept && serverplayer.level().dimension() == pDimension) {
            double d0 = pX - serverplayer.getX();
            double d1 = pY - serverplayer.getY();
            double d2 = pZ - serverplayer.getZ();
            if (d0 * d0 + d1 * d1 + d2 * d2 < pRadius * pRadius) {
               serverplayer.connection.send(pPacket);
            }
         }
      }

   }

   /**
    * Saves all of the players' current states.
    */
   public void saveAll() {
      for(int i = 0; i < this.players.size(); ++i) {
         this.save(this.players.get(i));
      }

   }

   public UserWhiteList getWhiteList() {
      return this.whitelist;
   }

   public String[] getWhiteListNames() {
      return this.whitelist.getUserList();
   }

   public ServerOpList getOps() {
      return this.ops;
   }

   public String[] getOpNames() {
      return this.ops.getUserList();
   }

   public void reloadWhiteList() {
   }

   /**
    * Updates the time and weather for the given player to those of the given world
    */
   public void sendLevelInfo(ServerPlayer pPlayer, ServerLevel pLevel) {
      WorldBorder worldborder = this.server.overworld().getWorldBorder();
      pPlayer.connection.send(new ClientboundInitializeBorderPacket(worldborder));
      pPlayer.connection.send(new ClientboundSetTimePacket(pLevel.getGameTime(), pLevel.getDayTime(), pLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
      pPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(pLevel.getSharedSpawnPos(), pLevel.getSharedSpawnAngle()));
      if (pLevel.isRaining()) {
         pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
         pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, pLevel.getRainLevel(1.0F)));
         pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, pLevel.getThunderLevel(1.0F)));
      }

   }

   /**
    * Sends the players inventory to himself.
    */
   public void sendAllPlayerInfo(ServerPlayer pPlayer) {
      pPlayer.inventoryMenu.sendAllDataToRemote();
      pPlayer.resetSentInfo();
      pPlayer.connection.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));
   }

   /**
    * Returns the number of players currently on the server.
    */
   public int getPlayerCount() {
      return this.players.size();
   }

   /**
    * Returns the maximum number of players allowed on the server.
    */
   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public boolean isUsingWhitelist() {
      return this.doWhiteList;
   }

   public void setUsingWhiteList(boolean pWhitelistEnabled) {
      this.doWhiteList = pWhitelistEnabled;
   }

   public List<ServerPlayer> getPlayersWithAddress(String pAddress) {
      List<ServerPlayer> list = Lists.newArrayList();

      for(ServerPlayer serverplayer : this.players) {
         if (serverplayer.getIpAddress().equals(pAddress)) {
            list.add(serverplayer);
         }
      }

      return list;
   }

   /**
    * Gets the view distance, in chunks.
    */
   public int getViewDistance() {
      return this.viewDistance;
   }

   public int getSimulationDistance() {
      return this.simulationDistance;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   /**
    * On integrated servers, returns the host's player data to be written to level.dat.
    */
   @Nullable
   public CompoundTag getSingleplayerData() {
      return null;
   }

   /**
    * Sets whether all players are allowed to use commands (cheats) on the server.
    */
   public void setAllowCheatsForAllPlayers(boolean pAllowCheatsForAllPlayers) {
      this.allowCheatsForAllPlayers = pAllowCheatsForAllPlayers;
   }

   /**
    * Kicks everyone with "Server closed" as reason.
    */
   public void removeAll() {
      for(int i = 0; i < this.players.size(); ++i) {
         (this.players.get(i)).connection.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
      }

   }

   public void broadcastSystemMessage(Component pMessage, boolean pBypassHiddenChat) {
      this.broadcastSystemMessage(pMessage, (p_215639_) -> {
         return pMessage;
      }, pBypassHiddenChat);
   }

   public void broadcastSystemMessage(Component pServerMessage, Function<ServerPlayer, Component> pPlayerMessageFactory, boolean pBypassHiddenChat) {
      this.server.sendSystemMessage(pServerMessage);

      for(ServerPlayer serverplayer : this.players) {
         Component component = pPlayerMessageFactory.apply(serverplayer);
         if (component != null) {
            serverplayer.sendSystemMessage(component, pBypassHiddenChat);
         }
      }

   }

   public void broadcastChatMessage(PlayerChatMessage pMessage, CommandSourceStack pSender, ChatType.Bound pBoundChatType) {
      this.broadcastChatMessage(pMessage, pSender::shouldFilterMessageTo, pSender.getPlayer(), pBoundChatType);
   }

   public void broadcastChatMessage(PlayerChatMessage pMessage, ServerPlayer pSender, ChatType.Bound pBoundChatType) {
      this.broadcastChatMessage(pMessage, pSender::shouldFilterMessageTo, pSender, pBoundChatType);
   }

   private void broadcastChatMessage(PlayerChatMessage pMessage, Predicate<ServerPlayer> pShouldFilterMessageTo, @Nullable ServerPlayer pSender, ChatType.Bound pBoundChatType) {
      boolean flag = this.verifyChatTrusted(pMessage);
      this.server.logChatMessage(pMessage.decoratedContent(), pBoundChatType, flag ? null : "Not Secure");
      OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(pMessage);
      boolean flag1 = false;

      for(ServerPlayer serverplayer : this.players) {
         boolean flag2 = pShouldFilterMessageTo.test(serverplayer);
         serverplayer.sendChatMessage(outgoingchatmessage, flag2, pBoundChatType);
         flag1 |= flag2 && pMessage.isFullyFiltered();
      }

      if (flag1 && pSender != null) {
         pSender.sendSystemMessage(CHAT_FILTERED_FULL);
      }

   }

   private boolean verifyChatTrusted(PlayerChatMessage pMessage) {
      return pMessage.hasSignature() && !pMessage.hasExpiredServer(Instant.now());
   }

   public ServerStatsCounter getPlayerStats(Player pPlayer) {
      UUID uuid = pPlayer.getUUID();
      ServerStatsCounter serverstatscounter = this.stats.get(uuid);
      if (serverstatscounter == null) {
         File file1 = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
         File file2 = new File(file1, uuid + ".json");

         serverstatscounter = new ServerStatsCounter(this.server, file2);
         this.stats.put(uuid, serverstatscounter);
      }

      return serverstatscounter;
   }

   public PlayerAdvancements getPlayerAdvancements(ServerPlayer pPlayer) {
      UUID uuid = pPlayer.getUUID();
      PlayerAdvancements playeradvancements = this.advancements.get(uuid);
      if (playeradvancements == null) {
         Path path = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(uuid + ".json");
         playeradvancements = new PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), path, pPlayer);
         this.advancements.put(uuid, playeradvancements);
      }

      // Forge: don't overwrite active player with a fake one.
      if (!(pPlayer instanceof net.minecraftforge.common.util.FakePlayer))
      playeradvancements.setPlayer(pPlayer);
      return playeradvancements;
   }

   public void setViewDistance(int pViewDistance) {
      this.viewDistance = pViewDistance;
      this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(pViewDistance));

      for(ServerLevel serverlevel : this.server.getAllLevels()) {
         if (serverlevel != null) {
            serverlevel.getChunkSource().setViewDistance(pViewDistance);
         }
      }

   }

   public void setSimulationDistance(int pSimulationDistance) {
      this.simulationDistance = pSimulationDistance;
      this.broadcastAll(new ClientboundSetSimulationDistancePacket(pSimulationDistance));

      for(ServerLevel serverlevel : this.server.getAllLevels()) {
         if (serverlevel != null) {
            serverlevel.getChunkSource().setSimulationDistance(pSimulationDistance);
         }
      }

   }

   public List<ServerPlayer> getPlayers() {
      return this.playersView; //Unmodifiable view, we don't want people removing things without us knowing.
   }

   /**
    * Gets the ServerPlayer object representing the player with the UUID.
    */
   @Nullable
   public ServerPlayer getPlayer(UUID pPlayerUUID) {
      return this.playersByUUID.get(pPlayerUUID);
   }

   public boolean canBypassPlayerLimit(GameProfile pProfile) {
      return false;
   }

   public void reloadResources() {
      for(PlayerAdvancements playeradvancements : this.advancements.values()) {
         playeradvancements.reload(this.server.getAdvancements());
      }

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.OnDatapackSyncEvent(this, null));
      this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
      ClientboundUpdateRecipesPacket clientboundupdaterecipespacket = new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes());

      for(ServerPlayer serverplayer : this.players) {
         serverplayer.connection.send(clientboundupdaterecipespacket);
         serverplayer.getRecipeBook().sendInitialRecipeBook(serverplayer);
      }

   }

   public boolean isAllowCheatsForAllPlayers() {
      return this.allowCheatsForAllPlayers;
   }
}
