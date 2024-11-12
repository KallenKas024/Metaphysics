package net.minecraft.world;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public abstract class BossEvent {
   private final UUID id;
   protected Component name;
   protected float progress;
   protected BossEvent.BossBarColor color;
   protected BossEvent.BossBarOverlay overlay;
   protected boolean darkenScreen;
   protected boolean playBossMusic;
   protected boolean createWorldFog;

   public BossEvent(UUID pId, Component pName, BossEvent.BossBarColor pColor, BossEvent.BossBarOverlay pOverlay) {
      this.id = pId;
      this.name = pName;
      this.color = pColor;
      this.overlay = pOverlay;
      this.progress = 1.0F;
   }

   public UUID getId() {
      return this.id;
   }

   public Component getName() {
      return this.name;
   }

   public void setName(Component pName) {
      this.name = pName;
   }

   public float getProgress() {
      return this.progress;
   }

   public void setProgress(float pProgress) {
      this.progress = pProgress;
   }

   public BossEvent.BossBarColor getColor() {
      return this.color;
   }

   public void setColor(BossEvent.BossBarColor pColor) {
      this.color = pColor;
   }

   public BossEvent.BossBarOverlay getOverlay() {
      return this.overlay;
   }

   public void setOverlay(BossEvent.BossBarOverlay pOverlay) {
      this.overlay = pOverlay;
   }

   public boolean shouldDarkenScreen() {
      return this.darkenScreen;
   }

   public BossEvent setDarkenScreen(boolean pDarkenSky) {
      this.darkenScreen = pDarkenSky;
      return this;
   }

   public boolean shouldPlayBossMusic() {
      return this.playBossMusic;
   }

   public BossEvent setPlayBossMusic(boolean pPlayEndBossMusic) {
      this.playBossMusic = pPlayEndBossMusic;
      return this;
   }

   public BossEvent setCreateWorldFog(boolean pCreateFog) {
      this.createWorldFog = pCreateFog;
      return this;
   }

   public boolean shouldCreateWorldFog() {
      return this.createWorldFog;
   }

   public static enum BossBarColor {
      PINK("pink", ChatFormatting.RED),
      BLUE("blue", ChatFormatting.BLUE),
      RED("red", ChatFormatting.DARK_RED),
      GREEN("green", ChatFormatting.GREEN),
      YELLOW("yellow", ChatFormatting.YELLOW),
      PURPLE("purple", ChatFormatting.DARK_BLUE),
      WHITE("white", ChatFormatting.WHITE);

      private final String name;
      private final ChatFormatting formatting;

      private BossBarColor(String pName, ChatFormatting pFormatting) {
         this.name = pName;
         this.formatting = pFormatting;
      }

      public ChatFormatting getFormatting() {
         return this.formatting;
      }

      public String getName() {
         return this.name;
      }

      public static BossEvent.BossBarColor byName(String pName) {
         for(BossEvent.BossBarColor bossevent$bossbarcolor : values()) {
            if (bossevent$bossbarcolor.name.equals(pName)) {
               return bossevent$bossbarcolor;
            }
         }

         return WHITE;
      }
   }

   public static enum BossBarOverlay {
      PROGRESS("progress"),
      NOTCHED_6("notched_6"),
      NOTCHED_10("notched_10"),
      NOTCHED_12("notched_12"),
      NOTCHED_20("notched_20");

      private final String name;

      private BossBarOverlay(String pName) {
         this.name = pName;
      }

      public String getName() {
         return this.name;
      }

      public static BossEvent.BossBarOverlay byName(String pName) {
         for(BossEvent.BossBarOverlay bossevent$bossbaroverlay : values()) {
            if (bossevent$bossbaroverlay.name.equals(pName)) {
               return bossevent$bossbaroverlay;
            }
         }

         return PROGRESS;
      }
   }
}