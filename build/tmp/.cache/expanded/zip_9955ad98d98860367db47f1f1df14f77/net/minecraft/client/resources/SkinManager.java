package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinManager {
   public static final String PROPERTY_TEXTURES = "textures";
   private final TextureManager textureManager;
   private final File skinsDirectory;
   private final MinecraftSessionService sessionService;
   private final LoadingCache<String, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> insecureSkinCache;

   public SkinManager(TextureManager pTextureManager, File pSkinsDirectory, final MinecraftSessionService pSessionService) {
      this.textureManager = pTextureManager;
      this.skinsDirectory = pSkinsDirectory;
      this.sessionService = pSessionService;
      this.insecureSkinCache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<String, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() {
         public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(String p_118853_) {
            GameProfile gameprofile = new GameProfile((UUID)null, "dummy_mcdummyface");
            gameprofile.getProperties().put("textures", new Property("textures", p_118853_, ""));

            try {
               return pSessionService.getTextures(gameprofile, false);
            } catch (Throwable throwable) {
               return ImmutableMap.of();
            }
         }
      });
   }

   /**
    * Used in the Skull renderer to fetch a skin. May download the skin if it's not in the cache
    */
   public ResourceLocation registerTexture(MinecraftProfileTexture pProfileTexture, MinecraftProfileTexture.Type pTextureType) {
      return this.registerTexture(pProfileTexture, pTextureType, (SkinManager.SkinTextureCallback)null);
   }

   /**
    * May download the skin if it's not in the cache, can be passed a SkinManager#SkinAvailableCallback for handling
    */
   private ResourceLocation registerTexture(MinecraftProfileTexture pProfileTexture, MinecraftProfileTexture.Type pTextureType, @Nullable SkinManager.SkinTextureCallback pSkinAvailableCallback) {
      String s = Hashing.sha1().hashUnencodedChars(pProfileTexture.getHash()).toString();
      ResourceLocation resourcelocation = getTextureLocation(pTextureType, s);
      AbstractTexture abstracttexture = this.textureManager.getTexture(resourcelocation, MissingTextureAtlasSprite.getTexture());
      if (abstracttexture == MissingTextureAtlasSprite.getTexture()) {
         File file1 = new File(this.skinsDirectory, s.length() > 2 ? s.substring(0, 2) : "xx");
         File file2 = new File(file1, s);
         HttpTexture httptexture = new HttpTexture(file2, pProfileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), pTextureType == Type.SKIN, () -> {
            if (pSkinAvailableCallback != null) {
               pSkinAvailableCallback.onSkinTextureAvailable(pTextureType, resourcelocation, pProfileTexture);
            }

         });
         this.textureManager.register(resourcelocation, httptexture);
      } else if (pSkinAvailableCallback != null) {
         pSkinAvailableCallback.onSkinTextureAvailable(pTextureType, resourcelocation, pProfileTexture);
      }

      return resourcelocation;
   }

   private static ResourceLocation getTextureLocation(MinecraftProfileTexture.Type pType, String pId) {
      String s1;
      switch (pType) {
         case SKIN:
            s1 = "skins";
            break;
         case CAPE:
            s1 = "capes";
            break;
         case ELYTRA:
            s1 = "elytra";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      String s = s1;
      return new ResourceLocation(s + "/" + pId);
   }

   public void registerSkins(GameProfile pProfile, SkinManager.SkinTextureCallback pSkinAvailableCallback, boolean pRequireSecure) {
      Runnable runnable = () -> {
         Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();

         try {
            map.putAll(this.sessionService.getTextures(pProfile, pRequireSecure));
         } catch (InsecurePublicKeyException insecurepublickeyexception1) {
         }

         if (map.isEmpty()) {
            pProfile.getProperties().clear();
            if (pProfile.getId().equals(Minecraft.getInstance().getUser().getGameProfile().getId())) {
               pProfile.getProperties().putAll(Minecraft.getInstance().getProfileProperties());
               map.putAll(this.sessionService.getTextures(pProfile, false));
            } else {
               this.sessionService.fillProfileProperties(pProfile, pRequireSecure);

               try {
                  map.putAll(this.sessionService.getTextures(pProfile, pRequireSecure));
               } catch (InsecurePublicKeyException insecurepublickeyexception) {
               }
            }
         }

         Minecraft.getInstance().execute(() -> {
            RenderSystem.recordRenderCall(() -> {
               ImmutableList.of(Type.SKIN, Type.CAPE).forEach((p_174848_) -> {
                  if (map.containsKey(p_174848_)) {
                     this.registerTexture(map.get(p_174848_), p_174848_, pSkinAvailableCallback);
                  }

               });
            });
         });
      };
      Util.backgroundExecutor().execute(runnable);
   }

   public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile pProfile) {
      Property property = Iterables.getFirst(pProfile.getProperties().get("textures"), (Property)null);
      return (Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>)(property == null ? ImmutableMap.of() : this.insecureSkinCache.getUnchecked(property.getValue()));
   }

   public ResourceLocation getInsecureSkinLocation(GameProfile pProfile) {
      MinecraftProfileTexture minecraftprofiletexture = this.getInsecureSkinInformation(pProfile).get(Type.SKIN);
      return minecraftprofiletexture != null ? this.registerTexture(minecraftprofiletexture, Type.SKIN) : DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(pProfile));
   }

   @OnlyIn(Dist.CLIENT)
   public interface SkinTextureCallback {
      void onSkinTextureAvailable(MinecraftProfileTexture.Type pTextureType, ResourceLocation pLocation, MinecraftProfileTexture pProfileTexture);
   }
}