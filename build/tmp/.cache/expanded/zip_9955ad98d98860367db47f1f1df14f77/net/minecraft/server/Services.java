package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.SignatureValidator;

public record Services(MinecraftSessionService sessionService, ServicesKeySet servicesKeySet, GameProfileRepository profileRepository, GameProfileCache profileCache) {
   private static final String USERID_CACHE_FILE = "usercache.json";

   public static Services create(YggdrasilAuthenticationService pAuthenticationService, File pProfileRepository) {
      MinecraftSessionService minecraftsessionservice = pAuthenticationService.createMinecraftSessionService();
      GameProfileRepository gameprofilerepository = pAuthenticationService.createProfileRepository();
      GameProfileCache gameprofilecache = new GameProfileCache(gameprofilerepository, new File(pProfileRepository, "usercache.json"));
      return new Services(minecraftsessionservice, pAuthenticationService.getServicesKeySet(), gameprofilerepository, gameprofilecache);
   }

   @Nullable
   public SignatureValidator profileKeySignatureValidator() {
      return SignatureValidator.from(this.servicesKeySet, ServicesKeyType.PROFILE_KEY);
   }
}