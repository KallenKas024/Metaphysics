package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractClientPlayer extends Player {
   private static final String SKIN_URL_TEMPLATE = "http://skins.minecraft.net/MinecraftSkins/%s.png";
   @Nullable
   private PlayerInfo playerInfo;
   protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
   public float elytraRotX;
   public float elytraRotY;
   public float elytraRotZ;
   public final ClientLevel clientLevel;

   public AbstractClientPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
      super(pClientLevel, pClientLevel.getSharedSpawnPos(), pClientLevel.getSharedSpawnAngle(), pGameProfile);
      this.clientLevel = pClientLevel;
   }

   /**
    * Returns {@code true} if the player is in spectator mode.
    */
   public boolean isSpectator() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.getGameMode() == GameType.SPECTATOR;
   }

   public boolean isCreative() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.getGameMode() == GameType.CREATIVE;
   }

   /**
    * Checks if this instance of AbstractClientPlayer has any associated player data.
    */
   public boolean isCapeLoaded() {
      return this.getPlayerInfo() != null;
   }

   @Nullable
   protected PlayerInfo getPlayerInfo() {
      if (this.playerInfo == null) {
         this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
      }

      return this.playerInfo;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      this.deltaMovementOnPreviousTick = this.getDeltaMovement();
      super.tick();
   }

   public Vec3 getDeltaMovementLerped(float pPatialTick) {
      return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), (double)pPatialTick);
   }

   /**
    * Returns {@code true} if the player has an associated skin.
    */
   public boolean isSkinLoaded() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.isSkinLoaded();
   }

   /**
    * Returns the ResourceLocation associated with the player's skin
    */
   public ResourceLocation getSkinTextureLocation() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : playerinfo.getSkinLocation();
   }

   @Nullable
   public ResourceLocation getCloakTextureLocation() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? null : playerinfo.getCapeLocation();
   }

   public boolean isElytraLoaded() {
      return this.getPlayerInfo() != null;
   }

   /**
    * Gets the special Elytra texture for the player.
    */
   @Nullable
   public ResourceLocation getElytraTextureLocation() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? null : playerinfo.getElytraLocation();
   }

   public static void registerSkinTexture(ResourceLocation pTextureLocation, String pName) {
      TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
      AbstractTexture abstracttexture = texturemanager.getTexture(pTextureLocation, MissingTextureAtlasSprite.getTexture());
      if (abstracttexture == MissingTextureAtlasSprite.getTexture()) {
         AbstractTexture httptexture = new HttpTexture((File)null, String.format(Locale.ROOT, "http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(pName)), DefaultPlayerSkin.getDefaultSkin(UUIDUtil.createOfflinePlayerUUID(pName)), true, (Runnable)null);
         texturemanager.register(pTextureLocation, httptexture);
      }

   }

   /**
    * Returns {@code true} if the username has an associated skin.
    */
   public static ResourceLocation getSkinLocation(String pUsername) {
      return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(pUsername)));
   }

   public String getModelName() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : playerinfo.getModelName();
   }

   public float getFieldOfViewModifier() {
      float f = 1.0F;
      if (this.getAbilities().flying) {
         f *= 1.1F;
      }

      f *= ((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / this.getAbilities().getWalkingSpeed() + 1.0F) / 2.0F;
      if (this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
         f = 1.0F;
      }

      ItemStack itemstack = this.getUseItem();
      if (this.isUsingItem()) {
         if (itemstack.is(Items.BOW)) {
            int i = this.getTicksUsingItem();
            float f1 = (float)i / 20.0F;
            if (f1 > 1.0F) {
               f1 = 1.0F;
            } else {
               f1 *= f1;
            }

            f *= 1.0F - f1 * 0.15F;
         } else if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping()) {
            return 0.1F;
         }
      }

      return net.minecraftforge.client.ForgeHooksClient.getFieldOfViewModifier(this, f);
   }
}
