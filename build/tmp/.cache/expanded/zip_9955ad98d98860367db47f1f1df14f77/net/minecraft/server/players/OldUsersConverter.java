package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public class OldUsersConverter {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final File OLD_IPBANLIST = new File("banned-ips.txt");
   public static final File OLD_USERBANLIST = new File("banned-players.txt");
   public static final File OLD_OPLIST = new File("ops.txt");
   public static final File OLD_WHITELIST = new File("white-list.txt");

   static List<String> readOldListFormat(File pInFile, Map<String, String[]> pRead) throws IOException {
      List<String> list = Files.readLines(pInFile, StandardCharsets.UTF_8);

      for(String s : list) {
         s = s.trim();
         if (!s.startsWith("#") && s.length() >= 1) {
            String[] astring = s.split("\\|");
            pRead.put(astring[0].toLowerCase(Locale.ROOT), astring);
         }
      }

      return list;
   }

   private static void lookupPlayers(MinecraftServer pServer, Collection<String> pNames, ProfileLookupCallback pCallback) {
      String[] astring = pNames.stream().filter((p_11077_) -> {
         return !StringUtil.isNullOrEmpty(p_11077_);
      }).toArray((p_11070_) -> {
         return new String[p_11070_];
      });
      if (pServer.usesAuthentication()) {
         pServer.getProfileRepository().findProfilesByNames(astring, Agent.MINECRAFT, pCallback);
      } else {
         for(String s : astring) {
            UUID uuid = UUIDUtil.getOrCreatePlayerUUID(new GameProfile((UUID)null, s));
            GameProfile gameprofile = new GameProfile(uuid, s);
            pCallback.onProfileLookupSucceeded(gameprofile);
         }
      }

   }

   public static boolean convertUserBanlist(final MinecraftServer pServer) {
      final UserBanList userbanlist = new UserBanList(PlayerList.USERBANLIST_FILE);
      if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
         if (userbanlist.getFile().exists()) {
            try {
               userbanlist.load();
            } catch (IOException ioexception1) {
               LOGGER.warn("Could not load existing file {}", userbanlist.getFile().getName(), ioexception1);
            }
         }

         try {
            final Map<String, String[]> map = Maps.newHashMap();
            readOldListFormat(OLD_USERBANLIST, map);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile p_11123_) {
                  pServer.getProfileCache().add(p_11123_);
                  String[] astring = map.get(p_11123_.getName().toLowerCase(Locale.ROOT));
                  if (astring == null) {
                     OldUsersConverter.LOGGER.warn("Could not convert user banlist entry for {}", (Object)p_11123_.getName());
                     throw new OldUsersConverter.ConversionError("Profile not in the conversionlist");
                  } else {
                     Date date = astring.length > 1 ? OldUsersConverter.parseDate(astring[1], (Date)null) : null;
                     String s = astring.length > 2 ? astring[2] : null;
                     Date date1 = astring.length > 3 ? OldUsersConverter.parseDate(astring[3], (Date)null) : null;
                     String s1 = astring.length > 4 ? astring[4] : null;
                     userbanlist.add(new UserBanListEntry(p_11123_, date, s, date1, s1));
                  }
               }

               public void onProfileLookupFailed(GameProfile p_11120_, Exception p_11121_) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user banlist entry for {}", p_11120_.getName(), p_11121_);
                  if (!(p_11121_ instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + p_11120_.getName() + " from backend systems", p_11121_);
                  }
               }
            };
            lookupPlayers(pServer, map.keySet(), profilelookupcallback);
            userbanlist.save();
            renameOldFile(OLD_USERBANLIST);
            return true;
         } catch (IOException ioexception) {
            LOGGER.warn("Could not read old user banlist to convert it!", (Throwable)ioexception);
            return false;
         } catch (OldUsersConverter.ConversionError oldusersconverter$conversionerror) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)oldusersconverter$conversionerror);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertIpBanlist(MinecraftServer pServer) {
      IpBanList ipbanlist = new IpBanList(PlayerList.IPBANLIST_FILE);
      if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
         if (ipbanlist.getFile().exists()) {
            try {
               ipbanlist.load();
            } catch (IOException ioexception1) {
               LOGGER.warn("Could not load existing file {}", ipbanlist.getFile().getName(), ioexception1);
            }
         }

         try {
            Map<String, String[]> map = Maps.newHashMap();
            readOldListFormat(OLD_IPBANLIST, map);

            for(String s : map.keySet()) {
               String[] astring = map.get(s);
               Date date = astring.length > 1 ? parseDate(astring[1], (Date)null) : null;
               String s1 = astring.length > 2 ? astring[2] : null;
               Date date1 = astring.length > 3 ? parseDate(astring[3], (Date)null) : null;
               String s2 = astring.length > 4 ? astring[4] : null;
               ipbanlist.add(new IpBanListEntry(s, date, s1, date1, s2));
            }

            ipbanlist.save();
            renameOldFile(OLD_IPBANLIST);
            return true;
         } catch (IOException ioexception) {
            LOGGER.warn("Could not parse old ip banlist to convert it!", (Throwable)ioexception);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertOpsList(final MinecraftServer pServer) {
      final ServerOpList serveroplist = new ServerOpList(PlayerList.OPLIST_FILE);
      if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
         if (serveroplist.getFile().exists()) {
            try {
               serveroplist.load();
            } catch (IOException ioexception1) {
               LOGGER.warn("Could not load existing file {}", serveroplist.getFile().getName(), ioexception1);
            }
         }

         try {
            List<String> list = Files.readLines(OLD_OPLIST, StandardCharsets.UTF_8);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile p_11133_) {
                  pServer.getProfileCache().add(p_11133_);
                  serveroplist.add(new ServerOpListEntry(p_11133_, pServer.getOperatorUserPermissionLevel(), false));
               }

               public void onProfileLookupFailed(GameProfile p_11130_, Exception p_11131_) {
                  OldUsersConverter.LOGGER.warn("Could not lookup oplist entry for {}", p_11130_.getName(), p_11131_);
                  if (!(p_11131_ instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + p_11130_.getName() + " from backend systems", p_11131_);
                  }
               }
            };
            lookupPlayers(pServer, list, profilelookupcallback);
            serveroplist.save();
            renameOldFile(OLD_OPLIST);
            return true;
         } catch (IOException ioexception) {
            LOGGER.warn("Could not read old oplist to convert it!", (Throwable)ioexception);
            return false;
         } catch (OldUsersConverter.ConversionError oldusersconverter$conversionerror) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)oldusersconverter$conversionerror);
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean convertWhiteList(final MinecraftServer pServer) {
      final UserWhiteList userwhitelist = new UserWhiteList(PlayerList.WHITELIST_FILE);
      if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
         if (userwhitelist.getFile().exists()) {
            try {
               userwhitelist.load();
            } catch (IOException ioexception1) {
               LOGGER.warn("Could not load existing file {}", userwhitelist.getFile().getName(), ioexception1);
            }
         }

         try {
            List<String> list = Files.readLines(OLD_WHITELIST, StandardCharsets.UTF_8);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile p_11143_) {
                  pServer.getProfileCache().add(p_11143_);
                  userwhitelist.add(new UserWhiteListEntry(p_11143_));
               }

               public void onProfileLookupFailed(GameProfile p_11140_, Exception p_11141_) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", p_11140_.getName(), p_11141_);
                  if (!(p_11141_ instanceof ProfileNotFoundException)) {
                     throw new OldUsersConverter.ConversionError("Could not request user " + p_11140_.getName() + " from backend systems", p_11141_);
                  }
               }
            };
            lookupPlayers(pServer, list, profilelookupcallback);
            userwhitelist.save();
            renameOldFile(OLD_WHITELIST);
            return true;
         } catch (IOException ioexception) {
            LOGGER.warn("Could not read old whitelist to convert it!", (Throwable)ioexception);
            return false;
         } catch (OldUsersConverter.ConversionError oldusersconverter$conversionerror) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)oldusersconverter$conversionerror);
            return false;
         }
      } else {
         return true;
      }
   }

   @Nullable
   public static UUID convertMobOwnerIfNecessary(final MinecraftServer pServer, String pUsername) {
      if (!StringUtil.isNullOrEmpty(pUsername) && pUsername.length() <= 16) {
         Optional<UUID> optional = pServer.getProfileCache().get(pUsername).map(GameProfile::getId);
         if (optional.isPresent()) {
            return optional.get();
         } else if (!pServer.isSingleplayer() && pServer.usesAuthentication()) {
            final List<GameProfile> list = Lists.newArrayList();
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile p_11153_) {
                  pServer.getProfileCache().add(p_11153_);
                  list.add(p_11153_);
               }

               public void onProfileLookupFailed(GameProfile p_11150_, Exception p_11151_) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", p_11150_.getName(), p_11151_);
               }
            };
            lookupPlayers(pServer, Lists.newArrayList(pUsername), profilelookupcallback);
            return !list.isEmpty() && list.get(0).getId() != null ? list.get(0).getId() : null;
         } else {
            return UUIDUtil.getOrCreatePlayerUUID(new GameProfile((UUID)null, pUsername));
         }
      } else {
         try {
            return UUID.fromString(pUsername);
         } catch (IllegalArgumentException illegalargumentexception) {
            return null;
         }
      }
   }

   public static boolean convertPlayers(final DedicatedServer pServer) {
      final File file1 = getWorldPlayersDirectory(pServer);
      final File file2 = new File(file1.getParentFile(), "playerdata");
      final File file3 = new File(file1.getParentFile(), "unknownplayers");
      if (file1.exists() && file1.isDirectory()) {
         File[] afile = file1.listFiles();
         List<String> list = Lists.newArrayList();

         for(File file4 : afile) {
            String s = file4.getName();
            if (s.toLowerCase(Locale.ROOT).endsWith(".dat")) {
               String s1 = s.substring(0, s.length() - ".dat".length());
               if (!s1.isEmpty()) {
                  list.add(s1);
               }
            }
         }

         try {
            final String[] astring = list.toArray(new String[list.size()]);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile p_11175_) {
                  pServer.getProfileCache().add(p_11175_);
                  UUID uuid = p_11175_.getId();
                  if (uuid == null) {
                     throw new OldUsersConverter.ConversionError("Missing UUID for user profile " + p_11175_.getName());
                  } else {
                     this.movePlayerFile(file2, this.getFileNameForProfile(p_11175_), uuid.toString());
                  }
               }

               public void onProfileLookupFailed(GameProfile p_11172_, Exception p_11173_) {
                  OldUsersConverter.LOGGER.warn("Could not lookup user uuid for {}", p_11172_.getName(), p_11173_);
                  if (p_11173_ instanceof ProfileNotFoundException) {
                     String s2 = this.getFileNameForProfile(p_11172_);
                     this.movePlayerFile(file3, s2, s2);
                  } else {
                     throw new OldUsersConverter.ConversionError("Could not request user " + p_11172_.getName() + " from backend systems", p_11173_);
                  }
               }

               private void movePlayerFile(File p_11168_, String p_11169_, String p_11170_) {
                  File file5 = new File(file1, p_11169_ + ".dat");
                  File file6 = new File(p_11168_, p_11170_ + ".dat");
                  OldUsersConverter.ensureDirectoryExists(p_11168_);
                  if (!file5.renameTo(file6)) {
                     throw new OldUsersConverter.ConversionError("Could not convert file for " + p_11169_);
                  }
               }

               private String getFileNameForProfile(GameProfile p_11166_) {
                  String s2 = null;

                  for(String s3 : astring) {
                     if (s3 != null && s3.equalsIgnoreCase(p_11166_.getName())) {
                        s2 = s3;
                        break;
                     }
                  }

                  if (s2 == null) {
                     throw new OldUsersConverter.ConversionError("Could not find the filename for " + p_11166_.getName() + " anymore");
                  } else {
                     return s2;
                  }
               }
            };
            lookupPlayers(pServer, Lists.newArrayList(astring), profilelookupcallback);
            return true;
         } catch (OldUsersConverter.ConversionError oldusersconverter$conversionerror) {
            LOGGER.error("Conversion failed, please try again later", (Throwable)oldusersconverter$conversionerror);
            return false;
         }
      } else {
         return true;
      }
   }

   static void ensureDirectoryExists(File pDir) {
      if (pDir.exists()) {
         if (!pDir.isDirectory()) {
            throw new OldUsersConverter.ConversionError("Can't create directory " + pDir.getName() + " in world save directory.");
         }
      } else if (!pDir.mkdirs()) {
         throw new OldUsersConverter.ConversionError("Can't create directory " + pDir.getName() + " in world save directory.");
      }
   }

   public static boolean serverReadyAfterUserconversion(MinecraftServer pServer) {
      boolean flag = areOldUserlistsRemoved();
      return flag && areOldPlayersConverted(pServer);
   }

   private static boolean areOldUserlistsRemoved() {
      boolean flag = false;
      if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
         flag = true;
      }

      boolean flag1 = false;
      if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
         flag1 = true;
      }

      boolean flag2 = false;
      if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
         flag2 = true;
      }

      boolean flag3 = false;
      if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
         flag3 = true;
      }

      if (!flag && !flag1 && !flag2 && !flag3) {
         return true;
      } else {
         LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
         LOGGER.warn("** please remove the following files and restart the server:");
         if (flag) {
            LOGGER.warn("* {}", (Object)OLD_USERBANLIST.getName());
         }

         if (flag1) {
            LOGGER.warn("* {}", (Object)OLD_IPBANLIST.getName());
         }

         if (flag2) {
            LOGGER.warn("* {}", (Object)OLD_OPLIST.getName());
         }

         if (flag3) {
            LOGGER.warn("* {}", (Object)OLD_WHITELIST.getName());
         }

         return false;
      }
   }

   private static boolean areOldPlayersConverted(MinecraftServer pServer) {
      File file1 = getWorldPlayersDirectory(pServer);
      if (!file1.exists() || !file1.isDirectory() || file1.list().length <= 0 && file1.delete()) {
         return true;
      } else {
         LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
         LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
         LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", (Object)file1.getPath());
         return false;
      }
   }

   private static File getWorldPlayersDirectory(MinecraftServer pServer) {
      return pServer.getWorldPath(LevelResource.PLAYER_OLD_DATA_DIR).toFile();
   }

   private static void renameOldFile(File pConvertedFile) {
      File file1 = new File(pConvertedFile.getName() + ".converted");
      pConvertedFile.renameTo(file1);
   }

   static Date parseDate(String pInput, Date pDefaultValue) {
      Date date;
      try {
         date = BanListEntry.DATE_FORMAT.parse(pInput);
      } catch (ParseException parseexception) {
         date = pDefaultValue;
      }

      return date;
   }

   static class ConversionError extends RuntimeException {
      ConversionError(String pMessage, Throwable pCause) {
         super(pMessage, pCause);
      }

      ConversionError(String pMessage) {
         super(pMessage);
      }
   }
}