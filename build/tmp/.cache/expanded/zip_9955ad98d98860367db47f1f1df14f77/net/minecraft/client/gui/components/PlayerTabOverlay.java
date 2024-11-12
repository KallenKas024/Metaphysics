package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerTabOverlay {
   private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.<PlayerInfo>comparingInt((p_253306_) -> {
      return p_253306_.getGameMode() == GameType.SPECTATOR ? 1 : 0;
   }).thenComparing((p_269613_) -> {
      return Optionull.mapOrDefault(p_269613_.getTeam(), PlayerTeam::getName, "");
   }).thenComparing((p_253305_) -> {
      return p_253305_.getProfile().getName();
   }, String::compareToIgnoreCase);
   private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
   public static final int MAX_ROWS_PER_COL = 20;
   public static final int HEART_EMPTY_CONTAINER = 16;
   public static final int HEART_EMPTY_CONTAINER_BLINKING = 25;
   public static final int HEART_FULL = 52;
   public static final int HEART_HALF_FULL = 61;
   public static final int HEART_GOLDEN_FULL = 160;
   public static final int HEART_GOLDEN_HALF_FULL = 169;
   public static final int HEART_GHOST_FULL = 70;
   public static final int HEART_GHOST_HALF_FULL = 79;
   private final Minecraft minecraft;
   private final Gui gui;
   @Nullable
   private Component footer;
   @Nullable
   private Component header;
   /** Weither or not the playerlist is currently being rendered */
   private boolean visible;
   private final Map<UUID, PlayerTabOverlay.HealthState> healthStates = new Object2ObjectOpenHashMap<>();

   public PlayerTabOverlay(Minecraft pMinecraft, Gui pGui) {
      this.minecraft = pMinecraft;
      this.gui = pGui;
   }

   public Component getNameForDisplay(PlayerInfo p_94550_) {
      return p_94550_.getTabListDisplayName() != null ? this.decorateName(p_94550_, p_94550_.getTabListDisplayName().copy()) : this.decorateName(p_94550_, PlayerTeam.formatNameForTeam(p_94550_.getTeam(), Component.literal(p_94550_.getProfile().getName())));
   }

   private Component decorateName(PlayerInfo pPlayerInfo, MutableComponent pName) {
      return pPlayerInfo.getGameMode() == GameType.SPECTATOR ? pName.withStyle(ChatFormatting.ITALIC) : pName;
   }

   /**
    * Called by GuiIngame to update the information stored in the playerlist, does not actually render the list,
    * however.
    */
   public void setVisible(boolean pVisible) {
      if (this.visible != pVisible) {
         this.healthStates.clear();
         this.visible = pVisible;
         if (pVisible) {
            Component component = ComponentUtils.formatList(this.getPlayerInfos(), Component.literal(", "), this::getNameForDisplay);
            this.minecraft.getNarrator().sayNow(Component.translatable("multiplayer.player.list.narration", component));
         }
      }

   }

   private List<PlayerInfo> getPlayerInfos() {
      return this.minecraft.player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
   }

   public void render(GuiGraphics pGuiGraphics, int pWidth, Scoreboard pScoreboard, @Nullable Objective pObjective) {
      List<PlayerInfo> list = this.getPlayerInfos();
      int i = 0;
      int j = 0;

      for(PlayerInfo playerinfo : list) {
         int k = this.minecraft.font.width(this.getNameForDisplay(playerinfo));
         i = Math.max(i, k);
         if (pObjective != null && pObjective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
            k = this.minecraft.font.width(" " + pScoreboard.getOrCreatePlayerScore(playerinfo.getProfile().getName(), pObjective).getScore());
            j = Math.max(j, k);
         }
      }

      if (!this.healthStates.isEmpty()) {
         Set<UUID> set = list.stream().map((p_250472_) -> {
            return p_250472_.getProfile().getId();
         }).collect(Collectors.toSet());
         this.healthStates.keySet().removeIf((p_248583_) -> {
            return !set.contains(p_248583_);
         });
      }

      int i3 = list.size();
      int j3 = i3;

      int k3;
      for(k3 = 1; j3 > 20; j3 = (i3 + k3 - 1) / k3) {
         ++k3;
      }

      boolean flag = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
      int l;
      if (pObjective != null) {
         if (pObjective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            l = 90;
         } else {
            l = j;
         }
      } else {
         l = 0;
      }

      int i1 = Math.min(k3 * ((flag ? 9 : 0) + i + l + 13), pWidth - 50) / k3;
      int j1 = pWidth / 2 - (i1 * k3 + (k3 - 1) * 5) / 2;
      int k1 = 10;
      int l1 = i1 * k3 + (k3 - 1) * 5;
      List<FormattedCharSequence> list1 = null;
      if (this.header != null) {
         list1 = this.minecraft.font.split(this.header, pWidth - 50);

         for(FormattedCharSequence formattedcharsequence : list1) {
            l1 = Math.max(l1, this.minecraft.font.width(formattedcharsequence));
         }
      }

      List<FormattedCharSequence> list2 = null;
      if (this.footer != null) {
         list2 = this.minecraft.font.split(this.footer, pWidth - 50);

         for(FormattedCharSequence formattedcharsequence1 : list2) {
            l1 = Math.max(l1, this.minecraft.font.width(formattedcharsequence1));
         }
      }

      if (list1 != null) {
         pGuiGraphics.fill(pWidth / 2 - l1 / 2 - 1, k1 - 1, pWidth / 2 + l1 / 2 + 1, k1 + list1.size() * 9, Integer.MIN_VALUE);

         for(FormattedCharSequence formattedcharsequence2 : list1) {
            int i2 = this.minecraft.font.width(formattedcharsequence2);
            pGuiGraphics.drawString(this.minecraft.font, formattedcharsequence2, pWidth / 2 - i2 / 2, k1, -1);
            k1 += 9;
         }

         ++k1;
      }

      pGuiGraphics.fill(pWidth / 2 - l1 / 2 - 1, k1 - 1, pWidth / 2 + l1 / 2 + 1, k1 + j3 * 9, Integer.MIN_VALUE);
      int l3 = this.minecraft.options.getBackgroundColor(553648127);

      for(int i4 = 0; i4 < i3; ++i4) {
         int j4 = i4 / j3;
         int j2 = i4 % j3;
         int k2 = j1 + j4 * i1 + j4 * 5;
         int l2 = k1 + j2 * 9;
         pGuiGraphics.fill(k2, l2, k2 + i1, l2 + 8, l3);
         RenderSystem.enableBlend();
         if (i4 < list.size()) {
            PlayerInfo playerinfo1 = list.get(i4);
            GameProfile gameprofile = playerinfo1.getProfile();
            if (flag) {
               Player player = this.minecraft.level.getPlayerByUUID(gameprofile.getId());
               boolean flag1 = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
               boolean flag2 = player != null && player.isModelPartShown(PlayerModelPart.HAT);
               PlayerFaceRenderer.draw(pGuiGraphics, playerinfo1.getSkinLocation(), k2, l2, 8, flag2, flag1);
               k2 += 9;
            }

            pGuiGraphics.drawString(this.minecraft.font, this.getNameForDisplay(playerinfo1), k2, l2, playerinfo1.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
            if (pObjective != null && playerinfo1.getGameMode() != GameType.SPECTATOR) {
               int l4 = k2 + i + 1;
               int i5 = l4 + l;
               if (i5 - l4 > 5) {
                  this.renderTablistScore(pObjective, l2, gameprofile.getName(), l4, i5, gameprofile.getId(), pGuiGraphics);
               }
            }

            this.renderPingIcon(pGuiGraphics, i1, k2 - (flag ? 9 : 0), l2, playerinfo1);
         }
      }

      if (list2 != null) {
         k1 += j3 * 9 + 1;
         pGuiGraphics.fill(pWidth / 2 - l1 / 2 - 1, k1 - 1, pWidth / 2 + l1 / 2 + 1, k1 + list2.size() * 9, Integer.MIN_VALUE);

         for(FormattedCharSequence formattedcharsequence3 : list2) {
            int k4 = this.minecraft.font.width(formattedcharsequence3);
            pGuiGraphics.drawString(this.minecraft.font, formattedcharsequence3, pWidth / 2 - k4 / 2, k1, -1);
            k1 += 9;
         }
      }

   }

   protected void renderPingIcon(GuiGraphics pGuiGraphics, int p_281809_, int p_282801_, int pY, PlayerInfo pPlayerInfo) {
      int i = 0;
      int j;
      if (pPlayerInfo.getLatency() < 0) {
         j = 5;
      } else if (pPlayerInfo.getLatency() < 150) {
         j = 0;
      } else if (pPlayerInfo.getLatency() < 300) {
         j = 1;
      } else if (pPlayerInfo.getLatency() < 600) {
         j = 2;
      } else if (pPlayerInfo.getLatency() < 1000) {
         j = 3;
      } else {
         j = 4;
      }

      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
      pGuiGraphics.blit(GUI_ICONS_LOCATION, p_282801_ + p_281809_ - 11, pY, 0, 176 + j * 8, 10, 8);
      pGuiGraphics.pose().popPose();
   }

   private void renderTablistScore(Objective pObjective, int pY, String pUsername, int p_283533_, int p_281254_, UUID pPlayerUuid, GuiGraphics pGuiGraphics) {
      int i = pObjective.getScoreboard().getOrCreatePlayerScore(pUsername, pObjective).getScore();
      if (pObjective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
         this.renderTablistHearts(pY, p_283533_, p_281254_, pPlayerUuid, pGuiGraphics, i);
      } else {
         String s = "" + ChatFormatting.YELLOW + i;
         pGuiGraphics.drawString(this.minecraft.font, s, p_281254_ - this.minecraft.font.width(s), pY, 16777215);
      }
   }

   private void renderTablistHearts(int pY, int p_283173_, int p_282149_, UUID pPlayerUuid, GuiGraphics pGuiGraphics, int pHealth) {
      PlayerTabOverlay.HealthState playertaboverlay$healthstate = this.healthStates.computeIfAbsent(pPlayerUuid, (p_249546_) -> {
         return new PlayerTabOverlay.HealthState(pHealth);
      });
      playertaboverlay$healthstate.update(pHealth, (long)this.gui.getGuiTicks());
      int i = Mth.positiveCeilDiv(Math.max(pHealth, playertaboverlay$healthstate.displayedValue()), 2);
      int j = Math.max(pHealth, Math.max(playertaboverlay$healthstate.displayedValue(), 20)) / 2;
      boolean flag = playertaboverlay$healthstate.isBlinking((long)this.gui.getGuiTicks());
      if (i > 0) {
         int k = Mth.floor(Math.min((float)(p_282149_ - p_283173_ - 4) / (float)j, 9.0F));
         if (k <= 3) {
            float f = Mth.clamp((float)pHealth / 20.0F, 0.0F, 1.0F);
            int i1 = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
            String s = "" + (float)pHealth / 2.0F;
            if (p_282149_ - this.minecraft.font.width(s + "hp") >= p_283173_) {
               s = s + "hp";
            }

            pGuiGraphics.drawString(this.minecraft.font, s, (p_282149_ + p_283173_ - this.minecraft.font.width(s)) / 2, pY, i1);
         } else {
            for(int l = i; l < j; ++l) {
               pGuiGraphics.blit(GUI_ICONS_LOCATION, p_283173_ + l * k, pY, flag ? 25 : 16, 0, 9, 9);
            }

            for(int j1 = 0; j1 < i; ++j1) {
               pGuiGraphics.blit(GUI_ICONS_LOCATION, p_283173_ + j1 * k, pY, flag ? 25 : 16, 0, 9, 9);
               if (flag) {
                  if (j1 * 2 + 1 < playertaboverlay$healthstate.displayedValue()) {
                     pGuiGraphics.blit(GUI_ICONS_LOCATION, p_283173_ + j1 * k, pY, 70, 0, 9, 9);
                  }

                  if (j1 * 2 + 1 == playertaboverlay$healthstate.displayedValue()) {
                     pGuiGraphics.blit(GUI_ICONS_LOCATION, p_283173_ + j1 * k, pY, 79, 0, 9, 9);
                  }
               }

               if (j1 * 2 + 1 < pHealth) {
                  pGuiGraphics.blit(GUI_ICONS_LOCATION, p_283173_ + j1 * k, pY, j1 >= 10 ? 160 : 52, 0, 9, 9);
               }

               if (j1 * 2 + 1 == pHealth) {
                  pGuiGraphics.blit(GUI_ICONS_LOCATION, p_283173_ + j1 * k, pY, j1 >= 10 ? 169 : 61, 0, 9, 9);
               }
            }

         }
      }
   }

   public void setFooter(@Nullable Component pFooter) {
      this.footer = pFooter;
   }

   public void setHeader(@Nullable Component pHeader) {
      this.header = pHeader;
   }

   public void reset() {
      this.header = null;
      this.footer = null;
   }

   @OnlyIn(Dist.CLIENT)
   static class HealthState {
      private static final long DISPLAY_UPDATE_DELAY = 20L;
      private static final long DECREASE_BLINK_DURATION = 20L;
      private static final long INCREASE_BLINK_DURATION = 10L;
      private int lastValue;
      private int displayedValue;
      private long lastUpdateTick;
      private long blinkUntilTick;

      public HealthState(int pDisplayedValue) {
         this.displayedValue = pDisplayedValue;
         this.lastValue = pDisplayedValue;
      }

      public void update(int pValue, long pGuiTicks) {
         if (pValue != this.lastValue) {
            long i = pValue < this.lastValue ? 20L : 10L;
            this.blinkUntilTick = pGuiTicks + i;
            this.lastValue = pValue;
            this.lastUpdateTick = pGuiTicks;
         }

         if (pGuiTicks - this.lastUpdateTick > 20L) {
            this.displayedValue = pValue;
         }

      }

      public int displayedValue() {
         return this.displayedValue;
      }

      public boolean isBlinking(long pGuiTicks) {
         return this.blinkUntilTick > pGuiTicks && (this.blinkUntilTick - pGuiTicks) % 6L >= 3L;
      }
   }
}