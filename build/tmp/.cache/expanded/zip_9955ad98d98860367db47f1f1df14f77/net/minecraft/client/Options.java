package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Options {
   static final Logger LOGGER = LogUtils.getLogger();
   static final Gson GSON = new Gson();
   private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>() {
   };
   public static final int RENDER_DISTANCE_TINY = 2;
   public static final int RENDER_DISTANCE_SHORT = 4;
   public static final int RENDER_DISTANCE_NORMAL = 8;
   public static final int RENDER_DISTANCE_FAR = 12;
   public static final int RENDER_DISTANCE_REALLY_FAR = 16;
   public static final int RENDER_DISTANCE_EXTREME = 32;
   private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
   private static final float DEFAULT_VOLUME = 1.0F;
   public static final String DEFAULT_SOUND_DEVICE = "";
   private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = Component.translatable("options.darkMojangStudiosBackgroundColor.tooltip");
   private final OptionInstance<Boolean> darkMojangStudiosBackground = OptionInstance.createBoolean("options.darkMojangStudiosBackgroundColor", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND), false);
   private static final Component ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES = Component.translatable("options.hideLightningFlashes.tooltip");
   private final OptionInstance<Boolean> hideLightningFlash = OptionInstance.createBoolean("options.hideLightningFlashes", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES), false);
   private final OptionInstance<Double> sensitivity = new OptionInstance<>("options.sensitivity", OptionInstance.noTooltip(), (p_232096_, p_232097_) -> {
      if (p_232097_ == 0.0D) {
         return genericValueLabel(p_232096_, Component.translatable("options.sensitivity.min"));
      } else {
         return p_232097_ == 1.0D ? genericValueLabel(p_232096_, Component.translatable("options.sensitivity.max")) : percentValueLabel(p_232096_, 2.0D * p_232097_);
      }
   }, OptionInstance.UnitDouble.INSTANCE, 0.5D, (p_232115_) -> {
   });
   private final OptionInstance<Integer> renderDistance;
   private final OptionInstance<Integer> simulationDistance;
   private int serverRenderDistance = 0;
   private final OptionInstance<Double> entityDistanceScaling = new OptionInstance<>("options.entityDistanceScaling", OptionInstance.noTooltip(), Options::percentValueLabel, (new OptionInstance.IntRange(2, 20)).xmap((p_232020_) -> {
      return (double)p_232020_ / 4.0D;
   }, (p_232112_) -> {
      return (int)(p_232112_ * 4.0D);
   }), Codec.doubleRange(0.5D, 5.0D), 1.0D, (p_265235_) -> {
   });
   public static final int UNLIMITED_FRAMERATE_CUTOFF = 260;
   private final OptionInstance<Integer> framerateLimit = new OptionInstance<>("options.framerateLimit", OptionInstance.noTooltip(), (p_232048_, p_232049_) -> {
      return p_232049_ == 260 ? genericValueLabel(p_232048_, Component.translatable("options.framerateLimit.max")) : genericValueLabel(p_232048_, Component.translatable("options.framerate", p_232049_));
   }, (new OptionInstance.IntRange(1, 26)).xmap((p_232003_) -> {
      return p_232003_ * 10;
   }, (p_232094_) -> {
      return p_232094_ / 10;
   }), Codec.intRange(10, 260), 120, (p_232086_) -> {
      Minecraft.getInstance().getWindow().setFramerateLimit(p_232086_);
   });
   private final OptionInstance<CloudStatus> cloudStatus = new OptionInstance<>("options.renderClouds", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(CloudStatus.values()), Codec.either(Codec.BOOL, Codec.STRING).xmap((p_231939_) -> {
      return p_231939_.map((p_232082_) -> {
         return p_232082_ ? CloudStatus.FANCY : CloudStatus.OFF;
      }, (p_232043_) -> {
         CloudStatus cloudstatus;
         switch (p_232043_) {
            case "true":
               cloudstatus = CloudStatus.FANCY;
               break;
            case "fast":
               cloudstatus = CloudStatus.FAST;
               break;
            default:
               cloudstatus = CloudStatus.OFF;
         }

         return cloudstatus;
      });
   }, (p_231941_) -> {
      String s;
      switch (p_231941_) {
         case FANCY:
            s = "true";
            break;
         case FAST:
            s = "fast";
            break;
         case OFF:
            s = "false";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return Either.right(s);
   })), CloudStatus.FANCY, (p_231854_) -> {
      if (Minecraft.useShaderTransparency()) {
         RenderTarget rendertarget = Minecraft.getInstance().levelRenderer.getCloudsTarget();
         if (rendertarget != null) {
            rendertarget.clear(Minecraft.ON_OSX);
         }
      }

   });
   private static final Component GRAPHICS_TOOLTIP_FAST = Component.translatable("options.graphics.fast.tooltip");
   private static final Component GRAPHICS_TOOLTIP_FABULOUS = Component.translatable("options.graphics.fabulous.tooltip", Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC));
   private static final Component GRAPHICS_TOOLTIP_FANCY = Component.translatable("options.graphics.fancy.tooltip");
   private final OptionInstance<GraphicsStatus> graphicsMode = new OptionInstance<>("options.graphics", (p_258117_) -> {
      Tooltip tooltip;
      switch (p_258117_) {
         case FANCY:
            tooltip = Tooltip.create(GRAPHICS_TOOLTIP_FANCY);
            break;
         case FAST:
            tooltip = Tooltip.create(GRAPHICS_TOOLTIP_FAST);
            break;
         case FABULOUS:
            tooltip = Tooltip.create(GRAPHICS_TOOLTIP_FABULOUS);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return tooltip;
   }, (p_231904_, p_231905_) -> {
      MutableComponent mutablecomponent = Component.translatable(p_231905_.getKey());
      return p_231905_ == GraphicsStatus.FABULOUS ? mutablecomponent.withStyle(ChatFormatting.ITALIC) : mutablecomponent;
   }, new OptionInstance.AltEnum<>(Arrays.asList(GraphicsStatus.values()), Stream.of(GraphicsStatus.values()).filter((p_231943_) -> {
      return p_231943_ != GraphicsStatus.FABULOUS;
   }).collect(Collectors.toList()), () -> {
      return Minecraft.getInstance().isRunning() && Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous();
   }, (p_231862_, p_231863_) -> {
      Minecraft minecraft = Minecraft.getInstance();
      GpuWarnlistManager gpuwarnlistmanager = minecraft.getGpuWarnlistManager();
      if (p_231863_ == GraphicsStatus.FABULOUS && gpuwarnlistmanager.willShowWarning()) {
         gpuwarnlistmanager.showWarning();
      } else {
         p_231862_.set(p_231863_);
         minecraft.levelRenderer.allChanged();
      }
   }, Codec.INT.xmap(GraphicsStatus::byId, GraphicsStatus::getId)), GraphicsStatus.FANCY, (p_268192_) -> {
   });
   private final OptionInstance<Boolean> ambientOcclusion = OptionInstance.createBoolean("options.ao", true, (p_263512_) -> {
      Minecraft.getInstance().levelRenderer.allChanged();
   });
   private static final Component PRIORITIZE_CHUNK_TOOLTIP_NONE = Component.translatable("options.prioritizeChunkUpdates.none.tooltip");
   private static final Component PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED = Component.translatable("options.prioritizeChunkUpdates.byPlayer.tooltip");
   private static final Component PRIORITIZE_CHUNK_TOOLTIP_NEARBY = Component.translatable("options.prioritizeChunkUpdates.nearby.tooltip");
   private final OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates = new OptionInstance<>("options.prioritizeChunkUpdates", (p_258118_) -> {
      Tooltip tooltip;
      switch (p_258118_) {
         case NONE:
            tooltip = Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_NONE);
            break;
         case PLAYER_AFFECTED:
            tooltip = Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED);
            break;
         case NEARBY:
            tooltip = Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_NEARBY);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return tooltip;
   }, OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(PrioritizeChunkUpdates.values()), Codec.INT.xmap(PrioritizeChunkUpdates::byId, PrioritizeChunkUpdates::getId)), PrioritizeChunkUpdates.NONE, (p_268073_) -> {
   });
   public List<String> resourcePacks = Lists.newArrayList();
   public List<String> incompatibleResourcePacks = Lists.newArrayList();
   private final OptionInstance<ChatVisiblity> chatVisibility = new OptionInstance<>("options.chat.visibility", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(ChatVisiblity.values()), Codec.INT.xmap(ChatVisiblity::byId, ChatVisiblity::getId)), ChatVisiblity.FULL, (p_268018_) -> {
   });
   private final OptionInstance<Double> chatOpacity = new OptionInstance<>("options.chat.opacity", OptionInstance.noTooltip(), (p_232088_, p_232089_) -> {
      return percentValueLabel(p_232088_, p_232089_ * 0.9D + 0.1D);
   }, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_232106_) -> {
      Minecraft.getInstance().gui.getChat().rescaleChat();
   });
   private final OptionInstance<Double> chatLineSpacing = new OptionInstance<>("options.chat.line_spacing", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 0.0D, (p_232103_) -> {
   });
   private final OptionInstance<Double> textBackgroundOpacity = new OptionInstance<>("options.accessibility.text_background_opacity", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 0.5D, (p_232100_) -> {
      Minecraft.getInstance().gui.getChat().rescaleChat();
   });
   private final OptionInstance<Double> panoramaSpeed = new OptionInstance<>("options.accessibility.panorama_speed", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_232109_) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_CONTRAST_MODE = Component.translatable("options.accessibility.high_contrast.tooltip");
   private final OptionInstance<Boolean> highContrast = OptionInstance.createBoolean("options.accessibility.high_contrast", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_CONTRAST_MODE), false, (p_275860_) -> {
      PackRepository packrepository = Minecraft.getInstance().getResourcePackRepository();
      boolean flag2 = packrepository.getSelectedIds().contains("high_contrast");
      if (!flag2 && p_275860_) {
         if (packrepository.addPack("high_contrast")) {
            this.updateResourcePacks(packrepository);
         }
      } else if (flag2 && !p_275860_ && packrepository.removePack("high_contrast")) {
         this.updateResourcePacks(packrepository);
      }

   });
   @Nullable
   public String fullscreenVideoModeString;
   public boolean hideServerAddress;
   public boolean advancedItemTooltips;
   public boolean pauseOnLostFocus = true;
   private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
   private final OptionInstance<HumanoidArm> mainHand = new OptionInstance<>("options.mainHand", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(HumanoidArm.values()), Codec.STRING.xmap((p_232028_) -> {
      return "left".equals(p_232028_) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
   }, (p_231937_) -> {
      return p_231937_ == HumanoidArm.LEFT ? "left" : "right";
   })), HumanoidArm.RIGHT, (p_231842_) -> {
      this.broadcastOptions();
   });
   public int overrideWidth;
   public int overrideHeight;
   private final OptionInstance<Double> chatScale = new OptionInstance<>("options.chat.scale", OptionInstance.noTooltip(), (p_232078_, p_232079_) -> {
      return (Component)(p_232079_ == 0.0D ? CommonComponents.optionStatus(p_232078_, false) : percentValueLabel(p_232078_, p_232079_));
   }, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_232092_) -> {
      Minecraft.getInstance().gui.getChat().rescaleChat();
   });
   private final OptionInstance<Double> chatWidth = new OptionInstance<>("options.chat.width", OptionInstance.noTooltip(), (p_232068_, p_232069_) -> {
      return pixelValueLabel(p_232068_, ChatComponent.getWidth(p_232069_));
   }, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_232084_) -> {
      Minecraft.getInstance().gui.getChat().rescaleChat();
   });
   private final OptionInstance<Double> chatHeightUnfocused = new OptionInstance<>("options.chat.height.unfocused", OptionInstance.noTooltip(), (p_232058_, p_232059_) -> {
      return pixelValueLabel(p_232058_, ChatComponent.getHeight(p_232059_));
   }, OptionInstance.UnitDouble.INSTANCE, ChatComponent.defaultUnfocusedPct(), (p_232074_) -> {
      Minecraft.getInstance().gui.getChat().rescaleChat();
   });
   private final OptionInstance<Double> chatHeightFocused = new OptionInstance<>("options.chat.height.focused", OptionInstance.noTooltip(), (p_232045_, p_232046_) -> {
      return pixelValueLabel(p_232045_, ChatComponent.getHeight(p_232046_));
   }, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_232064_) -> {
      Minecraft.getInstance().gui.getChat().rescaleChat();
   });
   private final OptionInstance<Double> chatDelay = new OptionInstance<>("options.chat.delay_instant", OptionInstance.noTooltip(), (p_232030_, p_232031_) -> {
      return p_232031_ <= 0.0D ? Component.translatable("options.chat.delay_none") : Component.translatable("options.chat.delay", String.format(Locale.ROOT, "%.1f", p_232031_));
   }, (new OptionInstance.IntRange(0, 60)).xmap((p_231986_) -> {
      return (double)p_231986_ / 10.0D;
   }, (p_232054_) -> {
      return (int)(p_232054_ * 10.0D);
   }), Codec.doubleRange(0.0D, 6.0D), 0.0D, (p_232039_) -> {
      Minecraft.getInstance().getChatListener().setMessageDelay(p_232039_);
   });
   private static final Component ACCESSIBILITY_TOOLTIP_NOTIFICATION_DISPLAY_TIME = Component.translatable("options.notifications.display_time.tooltip");
   private final OptionInstance<Double> notificationDisplayTime = new OptionInstance<>("options.notifications.display_time", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_NOTIFICATION_DISPLAY_TIME), (p_264664_, p_270572_) -> {
      return genericValueLabel(p_264664_, Component.translatable("options.multiplier", p_270572_));
   }, (new OptionInstance.IntRange(5, 100)).xmap((p_264666_) -> {
      return (double)p_264666_ / 10.0D;
   }, (p_264667_) -> {
      return (int)(p_264667_ * 10.0D);
   }), Codec.doubleRange(0.5D, 10.0D), 1.0D, (p_268049_) -> {
   });
   private final OptionInstance<Integer> mipmapLevels = new OptionInstance<>("options.mipmapLevels", OptionInstance.noTooltip(), (p_232033_, p_232034_) -> {
      return (Component)(p_232034_ == 0 ? CommonComponents.optionStatus(p_232033_, false) : genericValueLabel(p_232033_, p_232034_));
   }, new OptionInstance.IntRange(0, 4), 4, (p_268254_) -> {
   });
   public boolean useNativeTransport = true;
   private final OptionInstance<AttackIndicatorStatus> attackIndicator = new OptionInstance<>("options.attackIndicator", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(AttackIndicatorStatus.values()), Codec.INT.xmap(AttackIndicatorStatus::byId, AttackIndicatorStatus::getId)), AttackIndicatorStatus.CROSSHAIR, (p_268185_) -> {
   });
   public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
   public boolean joinedFirstServer = false;
   public boolean hideBundleTutorial = false;
   private final OptionInstance<Integer> biomeBlendRadius = new OptionInstance<>("options.biomeBlendRadius", OptionInstance.noTooltip(), (p_232016_, p_232017_) -> {
      int i = p_232017_ * 2 + 1;
      return genericValueLabel(p_232016_, Component.translatable("options.biomeBlendRadius." + i));
   }, new OptionInstance.IntRange(0, 7), 2, (p_232026_) -> {
      Minecraft.getInstance().levelRenderer.allChanged();
   });
   private final OptionInstance<Double> mouseWheelSensitivity = new OptionInstance<>("options.mouseWheelSensitivity", OptionInstance.noTooltip(), (p_232013_, p_232014_) -> {
      return genericValueLabel(p_232013_, Component.literal(String.format(Locale.ROOT, "%.2f", p_232014_)));
   }, (new OptionInstance.IntRange(-200, 100)).xmap(Options::logMouse, Options::unlogMouse), Codec.doubleRange(logMouse(-200), logMouse(100)), logMouse(0), (p_268246_) -> {
   });
   private final OptionInstance<Boolean> rawMouseInput = OptionInstance.createBoolean("options.rawMouseInput", true, (p_232062_) -> {
      Window window = Minecraft.getInstance().getWindow();
      if (window != null) {
         window.updateRawMouseInput(p_232062_);
      }

   });
   public int glDebugVerbosity = 1;
   private final OptionInstance<Boolean> autoJump = OptionInstance.createBoolean("options.autoJump", false);
   private final OptionInstance<Boolean> operatorItemsTab = OptionInstance.createBoolean("options.operatorItemsTab", false);
   private final OptionInstance<Boolean> autoSuggestions = OptionInstance.createBoolean("options.autoSuggestCommands", true);
   private final OptionInstance<Boolean> chatColors = OptionInstance.createBoolean("options.chat.color", true);
   private final OptionInstance<Boolean> chatLinks = OptionInstance.createBoolean("options.chat.links", true);
   private final OptionInstance<Boolean> chatLinksPrompt = OptionInstance.createBoolean("options.chat.links.prompt", true);
   private final OptionInstance<Boolean> enableVsync = OptionInstance.createBoolean("options.vsync", true, (p_232052_) -> {
      if (Minecraft.getInstance().getWindow() != null) {
         Minecraft.getInstance().getWindow().updateVsync(p_232052_);
      }

   });
   private final OptionInstance<Boolean> entityShadows = OptionInstance.createBoolean("options.entityShadows", true);
   private final OptionInstance<Boolean> forceUnicodeFont = OptionInstance.createBoolean("options.forceUnicodeFont", false, (p_232037_) -> {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getWindow() != null) {
         minecraft.selectMainFont(p_232037_);
         minecraft.resizeDisplay();
      }

   });
   private final OptionInstance<Boolean> invertYMouse = OptionInstance.createBoolean("options.invertMouse", false);
   private final OptionInstance<Boolean> discreteMouseScroll = OptionInstance.createBoolean("options.discrete_mouse_scroll", false);
   private final OptionInstance<Boolean> realmsNotifications = OptionInstance.createBoolean("options.realmsNotifications", true);
   private static final Component ALLOW_SERVER_LISTING_TOOLTIP = Component.translatable("options.allowServerListing.tooltip");
   private final OptionInstance<Boolean> allowServerListing = OptionInstance.createBoolean("options.allowServerListing", OptionInstance.cachedConstantTooltip(ALLOW_SERVER_LISTING_TOOLTIP), true, (p_232022_) -> {
      this.broadcastOptions();
   });
   private final OptionInstance<Boolean> reducedDebugInfo = OptionInstance.createBoolean("options.reducedDebugInfo", false);
   private final Map<SoundSource, OptionInstance<Double>> soundSourceVolumes = Util.make(new EnumMap<>(SoundSource.class), (p_247766_) -> {
      for(SoundSource soundsource : SoundSource.values()) {
         p_247766_.put(soundsource, this.createSoundSliderOptionInstance("soundCategory." + soundsource.getName(), soundsource));
      }

   });
   private final OptionInstance<Boolean> showSubtitles = OptionInstance.createBoolean("options.showSubtitles", false);
   private static final Component DIRECTIONAL_AUDIO_TOOLTIP_ON = Component.translatable("options.directionalAudio.on.tooltip");
   private static final Component DIRECTIONAL_AUDIO_TOOLTIP_OFF = Component.translatable("options.directionalAudio.off.tooltip");
   private final OptionInstance<Boolean> directionalAudio = OptionInstance.createBoolean("options.directionalAudio", (p_231858_) -> {
      return p_231858_ ? Tooltip.create(DIRECTIONAL_AUDIO_TOOLTIP_ON) : Tooltip.create(DIRECTIONAL_AUDIO_TOOLTIP_OFF);
   }, false, (p_275545_) -> {
      SoundManager soundmanager = Minecraft.getInstance().getSoundManager();
      soundmanager.reload();
      soundmanager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   });
   private final OptionInstance<Boolean> backgroundForChatOnly = new OptionInstance<>("options.accessibility.text_background", OptionInstance.noTooltip(), (p_231976_, p_231977_) -> {
      return p_231977_ ? Component.translatable("options.accessibility.text_background.chat") : Component.translatable("options.accessibility.text_background.everywhere");
   }, OptionInstance.BOOLEAN_VALUES, true, (p_231875_) -> {
   });
   private final OptionInstance<Boolean> touchscreen = OptionInstance.createBoolean("options.touchscreen", false);
   private final OptionInstance<Boolean> fullscreen = OptionInstance.createBoolean("options.fullscreen", false, (p_231970_) -> {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getWindow() != null && minecraft.getWindow().isFullscreen() != p_231970_) {
         minecraft.getWindow().toggleFullScreen();
         this.fullscreen().set(minecraft.getWindow().isFullscreen());
      }

   });
   private final OptionInstance<Boolean> bobView = OptionInstance.createBoolean("options.viewBobbing", true);
   private static final Component MOVEMENT_TOGGLE = Component.translatable("options.key.toggle");
   private static final Component MOVEMENT_HOLD = Component.translatable("options.key.hold");
   private final OptionInstance<Boolean> toggleCrouch = new OptionInstance<>("key.sneak", OptionInstance.noTooltip(), (p_231956_, p_231957_) -> {
      return p_231957_ ? MOVEMENT_TOGGLE : MOVEMENT_HOLD;
   }, OptionInstance.BOOLEAN_VALUES, false, (p_261689_) -> {
   });
   private final OptionInstance<Boolean> toggleSprint = new OptionInstance<>("key.sprint", OptionInstance.noTooltip(), (p_231910_, p_231911_) -> {
      return p_231911_ ? MOVEMENT_TOGGLE : MOVEMENT_HOLD;
   }, OptionInstance.BOOLEAN_VALUES, false, (p_265526_) -> {
   });
   public boolean skipMultiplayerWarning;
   public boolean skipRealms32bitWarning;
   private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = Component.translatable("options.hideMatchedNames.tooltip");
   private final OptionInstance<Boolean> hideMatchedNames = OptionInstance.createBoolean("options.hideMatchedNames", OptionInstance.cachedConstantTooltip(CHAT_TOOLTIP_HIDE_MATCHED_NAMES), true);
   private final OptionInstance<Boolean> showAutosaveIndicator = OptionInstance.createBoolean("options.autosaveIndicator", true);
   private static final Component CHAT_TOOLTIP_ONLY_SHOW_SECURE = Component.translatable("options.onlyShowSecureChat.tooltip");
   private final OptionInstance<Boolean> onlyShowSecureChat = OptionInstance.createBoolean("options.onlyShowSecureChat", OptionInstance.cachedConstantTooltip(CHAT_TOOLTIP_ONLY_SHOW_SECURE), false);
   public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
   public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
   public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
   public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
   public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
   public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", this.toggleCrouch::get);
   public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", this.toggleSprint::get);
   public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
   public final KeyMapping keySwapOffhand = new KeyMapping("key.swapOffhand", 70, "key.categories.inventory");
   public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
   public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
   public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
   public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
   public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
   public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
   public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
   public final KeyMapping keySocialInteractions = new KeyMapping("key.socialInteractions", 80, "key.categories.multiplayer");
   public final KeyMapping keyScreenshot = new KeyMapping("key.screenshot", 291, "key.categories.misc");
   public final KeyMapping keyTogglePerspective = new KeyMapping("key.togglePerspective", 294, "key.categories.misc");
   public final KeyMapping keySmoothCamera = new KeyMapping("key.smoothCamera", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
   public final KeyMapping keyFullscreen = new KeyMapping("key.fullscreen", 300, "key.categories.misc");
   public final KeyMapping keySpectatorOutlines = new KeyMapping("key.spectatorOutlines", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
   public final KeyMapping keyAdvancements = new KeyMapping("key.advancements", 76, "key.categories.misc");
   public final KeyMapping[] keyHotbarSlots = new KeyMapping[]{new KeyMapping("key.hotbar.1", 49, "key.categories.inventory"), new KeyMapping("key.hotbar.2", 50, "key.categories.inventory"), new KeyMapping("key.hotbar.3", 51, "key.categories.inventory"), new KeyMapping("key.hotbar.4", 52, "key.categories.inventory"), new KeyMapping("key.hotbar.5", 53, "key.categories.inventory"), new KeyMapping("key.hotbar.6", 54, "key.categories.inventory"), new KeyMapping("key.hotbar.7", 55, "key.categories.inventory"), new KeyMapping("key.hotbar.8", 56, "key.categories.inventory"), new KeyMapping("key.hotbar.9", 57, "key.categories.inventory")};
   public final KeyMapping keySaveHotbarActivator = new KeyMapping("key.saveToolbarActivator", 67, "key.categories.creative");
   public final KeyMapping keyLoadHotbarActivator = new KeyMapping("key.loadToolbarActivator", 88, "key.categories.creative");
   public KeyMapping[] keyMappings = ArrayUtils.addAll((KeyMapping[])(new KeyMapping[]{this.keyAttack, this.keyUse, this.keyUp, this.keyLeft, this.keyDown, this.keyRight, this.keyJump, this.keyShift, this.keySprint, this.keyDrop, this.keyInventory, this.keyChat, this.keyPlayerList, this.keyPickItem, this.keyCommand, this.keySocialInteractions, this.keyScreenshot, this.keyTogglePerspective, this.keySmoothCamera, this.keyFullscreen, this.keySpectatorOutlines, this.keySwapOffhand, this.keySaveHotbarActivator, this.keyLoadHotbarActivator, this.keyAdvancements}), (KeyMapping[])this.keyHotbarSlots);
   protected Minecraft minecraft;
   private final File optionsFile;
   public boolean hideGui;
   private CameraType cameraType = CameraType.FIRST_PERSON;
   public boolean renderDebug;
   public boolean renderDebugCharts;
   public boolean renderFpsChart;
   public String lastMpIp = "";
   public boolean smoothCamera;
   private final OptionInstance<Integer> fov = new OptionInstance<>("options.fov", OptionInstance.noTooltip(), (p_231999_, p_232000_) -> {
      Component component;
      switch (p_232000_) {
         case 70:
            component = genericValueLabel(p_231999_, Component.translatable("options.fov.min"));
            break;
         case 110:
            component = genericValueLabel(p_231999_, Component.translatable("options.fov.max"));
            break;
         default:
            component = genericValueLabel(p_231999_, p_232000_);
      }

      return component;
   }, new OptionInstance.IntRange(30, 110), Codec.DOUBLE.xmap((p_232007_) -> {
      return (int)(p_232007_ * 40.0D + 70.0D);
   }, (p_232009_) -> {
      return ((double)p_232009_.intValue() - 70.0D) / 40.0D;
   }), 70, (p_231951_) -> {
      Minecraft.getInstance().levelRenderer.needsUpdate();
   });
   private static final Component TELEMETRY_TOOLTIP = Component.translatable("options.telemetry.button.tooltip", Component.translatable("options.telemetry.state.minimal"), Component.translatable("options.telemetry.state.all"));
   private final OptionInstance<Boolean> telemetryOptInExtra = OptionInstance.createBoolean("options.telemetry.button", OptionInstance.cachedConstantTooltip(TELEMETRY_TOOLTIP), (p_261356_, p_261357_) -> {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.allowsTelemetry()) {
         return Component.translatable("options.telemetry.state.none");
      } else {
         return p_261357_ && minecraft.extraTelemetryAvailable() ? Component.translatable("options.telemetry.state.all") : Component.translatable("options.telemetry.state.minimal");
      }
   }, false, (p_268147_) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = Component.translatable("options.screenEffectScale.tooltip");
   private final OptionInstance<Double> screenEffectScale = new OptionInstance<>("options.screenEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT), (p_231996_, p_231997_) -> {
      return p_231997_ == 0.0D ? genericValueLabel(p_231996_, CommonComponents.OPTION_OFF) : percentValueLabel(p_231996_, p_231997_);
   }, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_231949_) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = Component.translatable("options.fovEffectScale.tooltip");
   private final OptionInstance<Double> fovEffectScale = new OptionInstance<>("options.fovEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_FOV_EFFECT), (p_231979_, p_231980_) -> {
      return p_231980_ == 0.0D ? genericValueLabel(p_231979_, CommonComponents.OPTION_OFF) : percentValueLabel(p_231979_, p_231980_);
   }, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), Codec.doubleRange(0.0D, 1.0D), 1.0D, (p_231877_) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT = Component.translatable("options.darknessEffectScale.tooltip");
   private final OptionInstance<Double> darknessEffectScale = new OptionInstance<>("options.darknessEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT), (p_231959_, p_231960_) -> {
      return p_231960_ == 0.0D ? genericValueLabel(p_231959_, CommonComponents.OPTION_OFF) : percentValueLabel(p_231959_, p_231960_);
   }, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), 1.0D, (p_265799_) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_GLINT_SPEED = Component.translatable("options.glintSpeed.tooltip");
   private final OptionInstance<Double> glintSpeed = new OptionInstance<>("options.glintSpeed", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_GLINT_SPEED), (p_267835_, p_267836_) -> {
      return p_267836_ == 0.0D ? genericValueLabel(p_267835_, CommonComponents.OPTION_OFF) : percentValueLabel(p_267835_, p_267836_);
   }, OptionInstance.UnitDouble.INSTANCE, 0.5D, (p_267960_) -> {
   });
   private static final Component ACCESSIBILITY_TOOLTIP_GLINT_STRENGTH = Component.translatable("options.glintStrength.tooltip");
   private final OptionInstance<Double> glintStrength = new OptionInstance<>("options.glintStrength", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_GLINT_STRENGTH), (p_267838_, p_267839_) -> {
      return p_267839_ == 0.0D ? genericValueLabel(p_267838_, CommonComponents.OPTION_OFF) : percentValueLabel(p_267838_, p_267839_);
   }, OptionInstance.UnitDouble.INSTANCE, 0.75D, RenderSystem::setShaderGlintAlpha);
   private static final Component ACCESSIBILITY_TOOLTIP_DAMAGE_TILT_STRENGTH = Component.translatable("options.damageTiltStrength.tooltip");
   private final OptionInstance<Double> damageTiltStrength = new OptionInstance<>("options.damageTiltStrength", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DAMAGE_TILT_STRENGTH), (p_269609_, p_269610_) -> {
      return p_269610_ == 0.0D ? genericValueLabel(p_269609_, CommonComponents.OPTION_OFF) : percentValueLabel(p_269609_, p_269610_);
   }, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_268127_) -> {
   });
   private final OptionInstance<Double> gamma = new OptionInstance<>("options.gamma", OptionInstance.noTooltip(), (p_231913_, p_231914_) -> {
      int i = (int)(p_231914_ * 100.0D);
      if (i == 0) {
         return genericValueLabel(p_231913_, Component.translatable("options.gamma.min"));
      } else if (i == 50) {
         return genericValueLabel(p_231913_, Component.translatable("options.gamma.default"));
      } else {
         return i == 100 ? genericValueLabel(p_231913_, Component.translatable("options.gamma.max")) : genericValueLabel(p_231913_, i);
      }
   }, OptionInstance.UnitDouble.INSTANCE, 0.5D, (p_270408_) -> {
   });
   public static final int AUTO_GUI_SCALE = 0;
   private static final int MAX_GUI_SCALE_INCLUSIVE = 2147483646;
   private final OptionInstance<Integer> guiScale = new OptionInstance<>("options.guiScale", OptionInstance.noTooltip(), (p_231982_, p_231983_) -> {
      return p_231983_ == 0 ? Component.translatable("options.guiScale.auto") : Component.literal(Integer.toString(p_231983_));
   }, new OptionInstance.ClampingLazyMaxIntRange(0, () -> {
      Minecraft minecraft = Minecraft.getInstance();
      return !minecraft.isRunning() ? 2147483646 : minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode());
   }, 2147483646), 0, (p_270071_) -> {
   });
   private final OptionInstance<ParticleStatus> particles = new OptionInstance<>("options.particles", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<>(Arrays.asList(ParticleStatus.values()), Codec.INT.xmap(ParticleStatus::byId, ParticleStatus::getId)), ParticleStatus.ALL, (p_269611_) -> {
   });
   private final OptionInstance<NarratorStatus> narrator = new OptionInstance<>("options.narrator", OptionInstance.noTooltip(), (p_231907_, p_231908_) -> {
      return (Component)(this.minecraft.getNarrator().isActive() ? p_231908_.getName() : Component.translatable("options.narrator.notavailable"));
   }, new OptionInstance.Enum<>(Arrays.asList(NarratorStatus.values()), Codec.INT.xmap(NarratorStatus::byId, NarratorStatus::getId)), NarratorStatus.OFF, (p_231860_) -> {
      this.minecraft.getNarrator().updateNarratorStatus(p_231860_);
   });
   public String languageCode = "en_us";
   private final OptionInstance<String> soundDevice = new OptionInstance<>("options.audioDevice", OptionInstance.noTooltip(), (p_231919_, p_231920_) -> {
      if ("".equals(p_231920_)) {
         return Component.translatable("options.audioDevice.default");
      } else {
         return p_231920_.startsWith("OpenAL Soft on ") ? Component.literal(p_231920_.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH)) : Component.literal(p_231920_);
      }
   }, new OptionInstance.LazyEnum<>(() -> {
      return Stream.concat(Stream.of(""), Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().stream()).toList();
   }, (p_232011_) -> {
      // FORGE: fix incorrect string comparison - PR #8767
      return Minecraft.getInstance().isRunning() && (p_232011_ == null || !p_232011_.isEmpty()) && !Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().contains(p_232011_) ? Optional.empty() : Optional.of(p_232011_);
   }, Codec.STRING), "", (p_275584_) -> {
      SoundManager soundmanager = Minecraft.getInstance().getSoundManager();
      soundmanager.reload();
      soundmanager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   });
   public boolean onboardAccessibility = true;
   public boolean syncWrites;

   public OptionInstance<Boolean> darkMojangStudiosBackground() {
      return this.darkMojangStudiosBackground;
   }

   public OptionInstance<Boolean> hideLightningFlash() {
      return this.hideLightningFlash;
   }

   public OptionInstance<Double> sensitivity() {
      return this.sensitivity;
   }

   public OptionInstance<Integer> renderDistance() {
      return this.renderDistance;
   }

   public OptionInstance<Integer> simulationDistance() {
      return this.simulationDistance;
   }

   public OptionInstance<Double> entityDistanceScaling() {
      return this.entityDistanceScaling;
   }

   public OptionInstance<Integer> framerateLimit() {
      return this.framerateLimit;
   }

   public OptionInstance<CloudStatus> cloudStatus() {
      return this.cloudStatus;
   }

   public OptionInstance<GraphicsStatus> graphicsMode() {
      return this.graphicsMode;
   }

   public OptionInstance<Boolean> ambientOcclusion() {
      return this.ambientOcclusion;
   }

   public OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates() {
      return this.prioritizeChunkUpdates;
   }

   public void updateResourcePacks(PackRepository pPackRepository) {
      List<String> list = ImmutableList.copyOf(this.resourcePacks);
      this.resourcePacks.clear();
      this.incompatibleResourcePacks.clear();

      for(Pack pack : pPackRepository.getSelectedPacks()) {
         if (!pack.isFixedPosition()) {
            this.resourcePacks.add(pack.getId());
            if (!pack.getCompatibility().isCompatible()) {
               this.incompatibleResourcePacks.add(pack.getId());
            }
         }
      }

      this.save();
      List<String> list1 = ImmutableList.copyOf(this.resourcePacks);
      if (!list1.equals(list)) {
         this.minecraft.reloadResourcePacks();
      }

   }

   public OptionInstance<ChatVisiblity> chatVisibility() {
      return this.chatVisibility;
   }

   public OptionInstance<Double> chatOpacity() {
      return this.chatOpacity;
   }

   public OptionInstance<Double> chatLineSpacing() {
      return this.chatLineSpacing;
   }

   public OptionInstance<Double> textBackgroundOpacity() {
      return this.textBackgroundOpacity;
   }

   public OptionInstance<Double> panoramaSpeed() {
      return this.panoramaSpeed;
   }

   public OptionInstance<Boolean> highContrast() {
      return this.highContrast;
   }

   public OptionInstance<HumanoidArm> mainHand() {
      return this.mainHand;
   }

   public OptionInstance<Double> chatScale() {
      return this.chatScale;
   }

   public OptionInstance<Double> chatWidth() {
      return this.chatWidth;
   }

   public OptionInstance<Double> chatHeightUnfocused() {
      return this.chatHeightUnfocused;
   }

   public OptionInstance<Double> chatHeightFocused() {
      return this.chatHeightFocused;
   }

   public OptionInstance<Double> chatDelay() {
      return this.chatDelay;
   }

   public OptionInstance<Double> notificationDisplayTime() {
      return this.notificationDisplayTime;
   }

   public OptionInstance<Integer> mipmapLevels() {
      return this.mipmapLevels;
   }

   public OptionInstance<AttackIndicatorStatus> attackIndicator() {
      return this.attackIndicator;
   }

   public OptionInstance<Integer> biomeBlendRadius() {
      return this.biomeBlendRadius;
   }

   private static double logMouse(int p_231966_) {
      return Math.pow(10.0D, (double)p_231966_ / 100.0D);
   }

   private static int unlogMouse(double p_231840_) {
      return Mth.floor(Math.log10(p_231840_) * 100.0D);
   }

   public OptionInstance<Double> mouseWheelSensitivity() {
      return this.mouseWheelSensitivity;
   }

   public OptionInstance<Boolean> rawMouseInput() {
      return this.rawMouseInput;
   }

   public OptionInstance<Boolean> autoJump() {
      return this.autoJump;
   }

   public OptionInstance<Boolean> operatorItemsTab() {
      return this.operatorItemsTab;
   }

   public OptionInstance<Boolean> autoSuggestions() {
      return this.autoSuggestions;
   }

   public OptionInstance<Boolean> chatColors() {
      return this.chatColors;
   }

   public OptionInstance<Boolean> chatLinks() {
      return this.chatLinks;
   }

   public OptionInstance<Boolean> chatLinksPrompt() {
      return this.chatLinksPrompt;
   }

   public OptionInstance<Boolean> enableVsync() {
      return this.enableVsync;
   }

   public OptionInstance<Boolean> entityShadows() {
      return this.entityShadows;
   }

   public OptionInstance<Boolean> forceUnicodeFont() {
      return this.forceUnicodeFont;
   }

   public OptionInstance<Boolean> invertYMouse() {
      return this.invertYMouse;
   }

   public OptionInstance<Boolean> discreteMouseScroll() {
      return this.discreteMouseScroll;
   }

   public OptionInstance<Boolean> realmsNotifications() {
      return this.realmsNotifications;
   }

   public OptionInstance<Boolean> allowServerListing() {
      return this.allowServerListing;
   }

   public OptionInstance<Boolean> reducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   public final float getSoundSourceVolume(SoundSource pCategory) {
      return this.getSoundSourceOptionInstance(pCategory).get().floatValue();
   }

   public final OptionInstance<Double> getSoundSourceOptionInstance(SoundSource pSoundSource) {
      return Objects.requireNonNull(this.soundSourceVolumes.get(pSoundSource));
   }

   private OptionInstance<Double> createSoundSliderOptionInstance(String pText, SoundSource pSoundSource) {
      return new OptionInstance<>(pText, OptionInstance.noTooltip(), (p_262709_, p_262710_) -> {
         return p_262710_ == 0.0D ? genericValueLabel(p_262709_, CommonComponents.OPTION_OFF) : percentValueLabel(p_262709_, p_262710_);
      }, OptionInstance.UnitDouble.INSTANCE, 1.0D, (p_247768_) -> {
         Minecraft.getInstance().getSoundManager().updateSourceVolume(pSoundSource, p_247768_.floatValue());
      });
   }

   public OptionInstance<Boolean> showSubtitles() {
      return this.showSubtitles;
   }

   public OptionInstance<Boolean> directionalAudio() {
      return this.directionalAudio;
   }

   public OptionInstance<Boolean> backgroundForChatOnly() {
      return this.backgroundForChatOnly;
   }

   public OptionInstance<Boolean> touchscreen() {
      return this.touchscreen;
   }

   public OptionInstance<Boolean> fullscreen() {
      return this.fullscreen;
   }

   public OptionInstance<Boolean> bobView() {
      return this.bobView;
   }

   public OptionInstance<Boolean> toggleCrouch() {
      return this.toggleCrouch;
   }

   public OptionInstance<Boolean> toggleSprint() {
      return this.toggleSprint;
   }

   public OptionInstance<Boolean> hideMatchedNames() {
      return this.hideMatchedNames;
   }

   public OptionInstance<Boolean> showAutosaveIndicator() {
      return this.showAutosaveIndicator;
   }

   public OptionInstance<Boolean> onlyShowSecureChat() {
      return this.onlyShowSecureChat;
   }

   public OptionInstance<Integer> fov() {
      return this.fov;
   }

   public OptionInstance<Boolean> telemetryOptInExtra() {
      return this.telemetryOptInExtra;
   }

   public OptionInstance<Double> screenEffectScale() {
      return this.screenEffectScale;
   }

   public OptionInstance<Double> fovEffectScale() {
      return this.fovEffectScale;
   }

   public OptionInstance<Double> darknessEffectScale() {
      return this.darknessEffectScale;
   }

   public OptionInstance<Double> glintSpeed() {
      return this.glintSpeed;
   }

   public OptionInstance<Double> glintStrength() {
      return this.glintStrength;
   }

   public OptionInstance<Double> damageTiltStrength() {
      return this.damageTiltStrength;
   }

   public OptionInstance<Double> gamma() {
      return this.gamma;
   }

   public OptionInstance<Integer> guiScale() {
      return this.guiScale;
   }

   public OptionInstance<ParticleStatus> particles() {
      return this.particles;
   }

   public OptionInstance<NarratorStatus> narrator() {
      return this.narrator;
   }

   public OptionInstance<String> soundDevice() {
      return this.soundDevice;
   }

   public Options(Minecraft pMinecraft, File pGameDirectory) {
      setForgeKeybindProperties();
      this.minecraft = pMinecraft;
      this.optionsFile = new File(pGameDirectory, "options.txt");
      boolean flag = pMinecraft.is64Bit();
      boolean flag1 = flag && Runtime.getRuntime().maxMemory() >= 1000000000L;
      this.renderDistance = new OptionInstance<>("options.renderDistance", OptionInstance.noTooltip(), (p_231962_, p_268036_) -> {
         return genericValueLabel(p_231962_, Component.translatable("options.chunks", p_268036_));
      }, new OptionInstance.IntRange(2, flag1 ? 32 : 16), flag ? 12 : 8, (p_231992_) -> {
         Minecraft.getInstance().levelRenderer.needsUpdate();
      });
      this.simulationDistance = new OptionInstance<>("options.simulationDistance", OptionInstance.noTooltip(), (p_231916_, p_270801_) -> {
         return genericValueLabel(p_231916_, Component.translatable("options.chunks", p_270801_));
      }, new OptionInstance.IntRange(5, flag1 ? 32 : 16), flag ? 12 : 8, (p_268325_) -> {
      });
      this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
      this.load();
   }

   public float getBackgroundOpacity(float pOpacity) {
      return this.backgroundForChatOnly.get() ? pOpacity : this.textBackgroundOpacity().get().floatValue();
   }

   public int getBackgroundColor(float pOpacity) {
      return (int)(this.getBackgroundOpacity(pOpacity) * 255.0F) << 24 & -16777216;
   }

   public int getBackgroundColor(int pChatColor) {
      return this.backgroundForChatOnly.get() ? pChatColor : (int)(this.textBackgroundOpacity.get() * 255.0D) << 24 & -16777216;
   }

   public void setKey(KeyMapping pKeyBinding, InputConstants.Key pInput) {
      pKeyBinding.setKey(pInput);
      this.save();
   }

   private void processOptions(Options.FieldAccess pAccessor) {
      pAccessor.process("autoJump", this.autoJump);
      pAccessor.process("operatorItemsTab", this.operatorItemsTab);
      pAccessor.process("autoSuggestions", this.autoSuggestions);
      pAccessor.process("chatColors", this.chatColors);
      pAccessor.process("chatLinks", this.chatLinks);
      pAccessor.process("chatLinksPrompt", this.chatLinksPrompt);
      pAccessor.process("enableVsync", this.enableVsync);
      pAccessor.process("entityShadows", this.entityShadows);
      pAccessor.process("forceUnicodeFont", this.forceUnicodeFont);
      pAccessor.process("discrete_mouse_scroll", this.discreteMouseScroll);
      pAccessor.process("invertYMouse", this.invertYMouse);
      pAccessor.process("realmsNotifications", this.realmsNotifications);
      pAccessor.process("reducedDebugInfo", this.reducedDebugInfo);
      pAccessor.process("showSubtitles", this.showSubtitles);
      pAccessor.process("directionalAudio", this.directionalAudio);
      pAccessor.process("touchscreen", this.touchscreen);
      pAccessor.process("fullscreen", this.fullscreen);
      pAccessor.process("bobView", this.bobView);
      pAccessor.process("toggleCrouch", this.toggleCrouch);
      pAccessor.process("toggleSprint", this.toggleSprint);
      pAccessor.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
      pAccessor.process("hideLightningFlashes", this.hideLightningFlash);
      pAccessor.process("mouseSensitivity", this.sensitivity);
      pAccessor.process("fov", this.fov);
      pAccessor.process("screenEffectScale", this.screenEffectScale);
      pAccessor.process("fovEffectScale", this.fovEffectScale);
      pAccessor.process("darknessEffectScale", this.darknessEffectScale);
      pAccessor.process("glintSpeed", this.glintSpeed);
      pAccessor.process("glintStrength", this.glintStrength);
      pAccessor.process("damageTiltStrength", this.damageTiltStrength);
      pAccessor.process("highContrast", this.highContrast);
      pAccessor.process("gamma", this.gamma);
      pAccessor.process("renderDistance", this.renderDistance);
      pAccessor.process("simulationDistance", this.simulationDistance);
      pAccessor.process("entityDistanceScaling", this.entityDistanceScaling);
      pAccessor.process("guiScale", this.guiScale);
      pAccessor.process("particles", this.particles);
      pAccessor.process("maxFps", this.framerateLimit);
      pAccessor.process("graphicsMode", this.graphicsMode);
      pAccessor.process("ao", this.ambientOcclusion);
      pAccessor.process("prioritizeChunkUpdates", this.prioritizeChunkUpdates);
      pAccessor.process("biomeBlendRadius", this.biomeBlendRadius);
      pAccessor.process("renderClouds", this.cloudStatus);
      this.resourcePacks = pAccessor.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
      this.incompatibleResourcePacks = pAccessor.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
      this.lastMpIp = pAccessor.process("lastServer", this.lastMpIp);
      this.languageCode = pAccessor.process("lang", this.languageCode);
      pAccessor.process("soundDevice", this.soundDevice);
      pAccessor.process("chatVisibility", this.chatVisibility);
      pAccessor.process("chatOpacity", this.chatOpacity);
      pAccessor.process("chatLineSpacing", this.chatLineSpacing);
      pAccessor.process("textBackgroundOpacity", this.textBackgroundOpacity);
      pAccessor.process("backgroundForChatOnly", this.backgroundForChatOnly);
      this.hideServerAddress = pAccessor.process("hideServerAddress", this.hideServerAddress);
      this.advancedItemTooltips = pAccessor.process("advancedItemTooltips", this.advancedItemTooltips);
      this.pauseOnLostFocus = pAccessor.process("pauseOnLostFocus", this.pauseOnLostFocus);
      this.overrideWidth = pAccessor.process("overrideWidth", this.overrideWidth);
      this.overrideHeight = pAccessor.process("overrideHeight", this.overrideHeight);
      pAccessor.process("chatHeightFocused", this.chatHeightFocused);
      pAccessor.process("chatDelay", this.chatDelay);
      pAccessor.process("chatHeightUnfocused", this.chatHeightUnfocused);
      pAccessor.process("chatScale", this.chatScale);
      pAccessor.process("chatWidth", this.chatWidth);
      pAccessor.process("notificationDisplayTime", this.notificationDisplayTime);
      pAccessor.process("mipmapLevels", this.mipmapLevels);
      this.useNativeTransport = pAccessor.process("useNativeTransport", this.useNativeTransport);
      pAccessor.process("mainHand", this.mainHand);
      pAccessor.process("attackIndicator", this.attackIndicator);
      pAccessor.process("narrator", this.narrator);
      this.tutorialStep = pAccessor.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
      pAccessor.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
      pAccessor.process("rawMouseInput", this.rawMouseInput);
      this.glDebugVerbosity = pAccessor.process("glDebugVerbosity", this.glDebugVerbosity);
      this.skipMultiplayerWarning = pAccessor.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
      this.skipRealms32bitWarning = pAccessor.process("skipRealms32bitWarning", this.skipRealms32bitWarning);
      pAccessor.process("hideMatchedNames", this.hideMatchedNames);
      this.joinedFirstServer = pAccessor.process("joinedFirstServer", this.joinedFirstServer);
      this.hideBundleTutorial = pAccessor.process("hideBundleTutorial", this.hideBundleTutorial);
      this.syncWrites = pAccessor.process("syncChunkWrites", this.syncWrites);
      pAccessor.process("showAutosaveIndicator", this.showAutosaveIndicator);
      pAccessor.process("allowServerListing", this.allowServerListing);
      pAccessor.process("onlyShowSecureChat", this.onlyShowSecureChat);
      pAccessor.process("panoramaScrollSpeed", this.panoramaSpeed);
      pAccessor.process("telemetryOptInExtra", this.telemetryOptInExtra);
      this.onboardAccessibility = pAccessor.process("onboardAccessibility", this.onboardAccessibility);

      net.minecraftforge.forge.snapshots.ForgeSnapshotsMod.processOptions(pAccessor);
      processOptionsForge(pAccessor);
   }
   // FORGE: split off to allow reloading options after mod loading is done
   private void processOptionsForge(Options.FieldAccess pAccessor)
   {
      for(KeyMapping keymapping : this.keyMappings) {
         String s = keymapping.saveString() + (keymapping.getKeyModifier() != net.minecraftforge.client.settings.KeyModifier.NONE ? ":" + keymapping.getKeyModifier() : "");
         String s1 = pAccessor.process("key_" + keymapping.getName(), s);
         if (!s.equals(s1)) {
            if (s1.indexOf(':') != -1) {
               String[] pts = s1.split(":");
               keymapping.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.valueFromString(pts[1]), InputConstants.getKey(pts[0]));
            } else
               keymapping.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.NONE, InputConstants.getKey(s1));
         }
      }

      for(SoundSource soundsource : SoundSource.values()) {
         pAccessor.process("soundCategory_" + soundsource.getName(), this.soundSourceVolumes.get(soundsource));
      }

      for(PlayerModelPart playermodelpart : PlayerModelPart.values()) {
         boolean flag = this.modelParts.contains(playermodelpart);
         boolean flag1 = pAccessor.process("modelPart_" + playermodelpart.getId(), flag);
         if (flag1 != flag) {
            this.setModelPart(playermodelpart, flag1);
         }
      }

   }

   /**
    * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
    */
   public void load() {
      this.load(false);
   }
   public void load(boolean limited) {
      try {
         if (!this.optionsFile.exists()) {
            return;
         }

         CompoundTag compoundtag = new CompoundTag();

         try (BufferedReader bufferedreader = Files.newReader(this.optionsFile, Charsets.UTF_8)) {
            bufferedreader.lines().forEach((p_231896_) -> {
               try {
                  Iterator<String> iterator = OPTION_SPLITTER.split(p_231896_).iterator();
                  compoundtag.putString(iterator.next(), iterator.next());
               } catch (Exception exception1) {
                  LOGGER.warn("Skipping bad option: {}", (Object)p_231896_);
               }

            });
         }

         final CompoundTag compoundtag1 = this.dataFix(compoundtag);
         if (!compoundtag1.contains("graphicsMode") && compoundtag1.contains("fancyGraphics")) {
            if (isTrue(compoundtag1.getString("fancyGraphics"))) {
               this.graphicsMode.set(GraphicsStatus.FANCY);
            } else {
               this.graphicsMode.set(GraphicsStatus.FAST);
            }
         }

         java.util.function.Consumer<FieldAccess> processor = limited ? this::processOptionsForge : this::processOptions;
         processor.accept(new Options.FieldAccess() {
            @Nullable
            private String getValueOrNull(String p_168459_) {
               return compoundtag1.contains(p_168459_) ? compoundtag1.getString(p_168459_) : null;
            }

            public <T> void process(String p_232125_, OptionInstance<T> p_232126_) {
               String s = this.getValueOrNull(p_232125_);
               if (s != null) {
                  JsonReader jsonreader = new JsonReader(new StringReader(s.isEmpty() ? "\"\"" : s));
                  JsonElement jsonelement = JsonParser.parseReader(jsonreader);
                  DataResult<T> dataresult = p_232126_.codec().parse(JsonOps.INSTANCE, jsonelement);
                  dataresult.error().ifPresent((p_232130_) -> {
                     Options.LOGGER.error("Error parsing option value " + s + " for option " + p_232126_ + ": " + p_232130_.message());
                  });
                  dataresult.result().ifPresent(p_232126_::set);
               }

            }

            public int process(String p_168467_, int p_168468_) {
               String s = this.getValueOrNull(p_168467_);
               if (s != null) {
                  try {
                     return Integer.parseInt(s);
                  } catch (NumberFormatException numberformatexception) {
                     Options.LOGGER.warn("Invalid integer value for option {} = {}", p_168467_, s, numberformatexception);
                  }
               }

               return p_168468_;
            }

            public boolean process(String p_168483_, boolean p_168484_) {
               String s = this.getValueOrNull(p_168483_);
               return s != null ? Options.isTrue(s) : p_168484_;
            }

            public String process(String p_168480_, String p_168481_) {
               return MoreObjects.firstNonNull(this.getValueOrNull(p_168480_), p_168481_);
            }

            public float process(String p_168464_, float p_168465_) {
               String s = this.getValueOrNull(p_168464_);
               if (s != null) {
                  if (Options.isTrue(s)) {
                     return 1.0F;
                  }

                  if (Options.isFalse(s)) {
                     return 0.0F;
                  }

                  try {
                     return Float.parseFloat(s);
                  } catch (NumberFormatException numberformatexception) {
                     Options.LOGGER.warn("Invalid floating point value for option {} = {}", p_168464_, s, numberformatexception);
                  }
               }

               return p_168465_;
            }

            public <T> T process(String p_168470_, T p_168471_, Function<String, T> p_168472_, Function<T, String> p_168473_) {
               String s = this.getValueOrNull(p_168470_);
               return (T)(s == null ? p_168471_ : p_168472_.apply(s));
            }
         });
         if (compoundtag1.contains("fullscreenResolution")) {
            this.fullscreenVideoModeString = compoundtag1.getString("fullscreenResolution");
         }

         if (this.minecraft.getWindow() != null) {
            this.minecraft.getWindow().setFramerateLimit(this.framerateLimit.get());
         }

         KeyMapping.resetMapping();
      } catch (Exception exception) {
         LOGGER.error("Failed to load options", (Throwable)exception);
      }

   }

   static boolean isTrue(String pValue) {
      return "true".equals(pValue);
   }

   static boolean isFalse(String pValue) {
      return "false".equals(pValue);
   }

   private CompoundTag dataFix(CompoundTag pNbt) {
      int i = 0;

      try {
         i = Integer.parseInt(pNbt.getString("version"));
      } catch (RuntimeException runtimeexception) {
      }

      return DataFixTypes.OPTIONS.updateToCurrentVersion(this.minecraft.getFixerUpper(), pNbt, i);
   }

   /**
    * Saves the options to the options file.
    */
   public void save() {
      try (final PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8))) {
         printwriter.println("version:" + SharedConstants.getCurrentVersion().getDataVersion().getVersion());
         this.processOptions(new Options.FieldAccess() {
            public void writePrefix(String p_168491_) {
               printwriter.print(p_168491_);
               printwriter.print(':');
            }

            public <T> void process(String p_232135_, OptionInstance<T> p_232136_) {
               DataResult<JsonElement> dataresult = p_232136_.codec().encodeStart(JsonOps.INSTANCE, p_232136_.get());
               dataresult.error().ifPresent((p_232133_) -> {
                  Options.LOGGER.error("Error saving option " + p_232136_ + ": " + p_232133_);
               });
               dataresult.result().ifPresent((p_232140_) -> {
                  this.writePrefix(p_232135_);
                  printwriter.println(Options.GSON.toJson(p_232140_));
               });
            }

            public int process(String p_168499_, int p_168500_) {
               this.writePrefix(p_168499_);
               printwriter.println(p_168500_);
               return p_168500_;
            }

            public boolean process(String p_168515_, boolean p_168516_) {
               this.writePrefix(p_168515_);
               printwriter.println(p_168516_);
               return p_168516_;
            }

            public String process(String p_168512_, String p_168513_) {
               this.writePrefix(p_168512_);
               printwriter.println(p_168513_);
               return p_168513_;
            }

            public float process(String p_168496_, float p_168497_) {
               this.writePrefix(p_168496_);
               printwriter.println(p_168497_);
               return p_168497_;
            }

            public <T> T process(String p_168502_, T p_168503_, Function<String, T> p_168504_, Function<T, String> p_168505_) {
               this.writePrefix(p_168502_);
               printwriter.println(p_168505_.apply(p_168503_));
               return p_168503_;
            }
         });
         if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent()) {
            printwriter.println("fullscreenResolution:" + this.minecraft.getWindow().getPreferredFullscreenVideoMode().get().write());
         }
      } catch (Exception exception) {
         LOGGER.error("Failed to save options", (Throwable)exception);
      }

      this.broadcastOptions();
   }

   /**
    * Send a client info packet with settings information to the server
    */
   public void broadcastOptions() {
      if (net.minecraftforge.client.loading.ClientModLoader.isLoading()) return; //Don't save settings before mods add keybindigns and the like to prevent them from being deleted.
      if (this.minecraft.player != null) {
         int i = 0;

         for(PlayerModelPart playermodelpart : this.modelParts) {
            i |= playermodelpart.getMask();
         }

         this.minecraft.player.connection.send(new ServerboundClientInformationPacket(this.languageCode, this.renderDistance.get(), this.chatVisibility.get(), this.chatColors.get(), i, this.mainHand.get(), this.minecraft.isTextFilteringEnabled(), this.allowServerListing.get()));
      }

   }

   private void setModelPart(PlayerModelPart pModelPart, boolean pEnable) {
      if (pEnable) {
         this.modelParts.add(pModelPart);
      } else {
         this.modelParts.remove(pModelPart);
      }

   }

   public boolean isModelPartEnabled(PlayerModelPart pPlayerModelPart) {
      return this.modelParts.contains(pPlayerModelPart);
   }

   public void toggleModelPart(PlayerModelPart pPlayerModelPart, boolean pEnable) {
      this.setModelPart(pPlayerModelPart, pEnable);
      this.broadcastOptions();
   }

   public CloudStatus getCloudsType() {
      return this.getEffectiveRenderDistance() >= 4 ? this.cloudStatus.get() : CloudStatus.OFF;
   }

   /**
    * Returns {@code true} if the client connect to a server using the native transport system.
    */
   public boolean useNativeTransport() {
      return this.useNativeTransport;
   }

   public void loadSelectedResourcePacks(PackRepository pResourcePackList) {
      Set<String> set = Sets.newLinkedHashSet();
      Iterator<String> iterator = this.resourcePacks.iterator();

      while(iterator.hasNext()) {
         String s = iterator.next();
         Pack pack = pResourcePackList.getPack(s);
         if (pack == null && !s.startsWith("file/")) {
            pack = pResourcePackList.getPack("file/" + s);
         }

         if (pack == null) {
            LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", (Object)s);
            iterator.remove();
         } else if (!pack.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(s)) {
            LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", (Object)s);
            iterator.remove();
         } else if (pack.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(s)) {
            LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", (Object)s);
            this.incompatibleResourcePacks.remove(s);
         } else {
            set.add(pack.getId());
         }
      }

      pResourcePackList.setSelected(set);
   }

   private void setForgeKeybindProperties() {
      net.minecraftforge.client.settings.KeyConflictContext inGame = net.minecraftforge.client.settings.KeyConflictContext.IN_GAME;
      keyUp.setKeyConflictContext(inGame);
      keyLeft.setKeyConflictContext(inGame);
      keyDown.setKeyConflictContext(inGame);
      keyRight.setKeyConflictContext(inGame);
      keyJump.setKeyConflictContext(inGame);
      keyShift.setKeyConflictContext(inGame);
      keySprint.setKeyConflictContext(inGame);
      keyAttack.setKeyConflictContext(inGame);
      keyChat.setKeyConflictContext(inGame);
      keyPlayerList.setKeyConflictContext(inGame);
      keyCommand.setKeyConflictContext(inGame);
      keyTogglePerspective.setKeyConflictContext(inGame);
      keySmoothCamera.setKeyConflictContext(inGame);
   }

   public CameraType getCameraType() {
      return this.cameraType;
   }

   public void setCameraType(CameraType pPointOfView) {
      this.cameraType = pPointOfView;
   }

   private static List<String> readPackList(String p_168443_) {
      List<String> list = GsonHelper.fromNullableJson(GSON, p_168443_, RESOURCE_PACK_TYPE);
      return (List<String>)(list != null ? list : Lists.newArrayList());
   }

   public File getFile() {
      return this.optionsFile;
   }

   public String dumpOptionsForReport() {
      Stream<Pair<String, Object>> stream = Stream.<Pair<String, Object>>builder().add(Pair.of("ao", this.ambientOcclusion.get())).add(Pair.of("biomeBlendRadius", this.biomeBlendRadius.get())).add(Pair.of("enableVsync", this.enableVsync.get())).add(Pair.of("entityDistanceScaling", this.entityDistanceScaling.get())).add(Pair.of("entityShadows", this.entityShadows.get())).add(Pair.of("forceUnicodeFont", this.forceUnicodeFont.get())).add(Pair.of("fov", this.fov.get())).add(Pair.of("fovEffectScale", this.fovEffectScale.get())).add(Pair.of("darknessEffectScale", this.darknessEffectScale.get())).add(Pair.of("glintSpeed", this.glintSpeed.get())).add(Pair.of("glintStrength", this.glintStrength.get())).add(Pair.of("prioritizeChunkUpdates", this.prioritizeChunkUpdates.get())).add(Pair.of("fullscreen", this.fullscreen.get())).add(Pair.of("fullscreenResolution", String.valueOf((Object)this.fullscreenVideoModeString))).add(Pair.of("gamma", this.gamma.get())).add(Pair.of("glDebugVerbosity", this.glDebugVerbosity)).add(Pair.of("graphicsMode", this.graphicsMode.get())).add(Pair.of("guiScale", this.guiScale.get())).add(Pair.of("maxFps", this.framerateLimit.get())).add(Pair.of("mipmapLevels", this.mipmapLevels.get())).add(Pair.of("narrator", this.narrator.get())).add(Pair.of("overrideHeight", this.overrideHeight)).add(Pair.of("overrideWidth", this.overrideWidth)).add(Pair.of("particles", this.particles.get())).add(Pair.of("reducedDebugInfo", this.reducedDebugInfo.get())).add(Pair.of("renderClouds", this.cloudStatus.get())).add(Pair.of("renderDistance", this.renderDistance.get())).add(Pair.of("simulationDistance", this.simulationDistance.get())).add(Pair.of("resourcePacks", this.resourcePacks)).add(Pair.of("screenEffectScale", this.screenEffectScale.get())).add(Pair.of("syncChunkWrites", this.syncWrites)).add(Pair.of("useNativeTransport", this.useNativeTransport)).add(Pair.of("soundDevice", this.soundDevice.get())).build();
      return stream.map((p_231848_) -> {
         return (String)p_231848_.getFirst() + ": " + p_231848_.getSecond();
      }).collect(Collectors.joining(System.lineSeparator()));
   }

   public void setServerRenderDistance(int pServerRenderDistance) {
      this.serverRenderDistance = pServerRenderDistance;
   }

   public int getEffectiveRenderDistance() {
      return this.serverRenderDistance > 0 ? Math.min(this.renderDistance.get(), this.serverRenderDistance) : this.renderDistance.get();
   }

   private static Component pixelValueLabel(Component pText, int pValue) {
      return Component.translatable("options.pixel_value", pText, pValue);
   }

   private static Component percentValueLabel(Component p_231898_, double p_231899_) {
      return Component.translatable("options.percent_value", p_231898_, (int)(p_231899_ * 100.0D));
   }

   public static Component genericValueLabel(Component pText, Component pValue) {
      return Component.translatable("options.generic_value", pText, pValue);
   }

   public static Component genericValueLabel(Component pText, int pValue) {
      return genericValueLabel(pText, Component.literal(Integer.toString(pValue)));
   }

   @OnlyIn(Dist.CLIENT)
   public interface FieldAccess {
      <T> void process(String pName, OptionInstance<T> pOptionInstance);

      int process(String pName, int pValue);

      boolean process(String pName, boolean pValue);

      String process(String pName, String pValue);

      float process(String pName, float pValue);

      <T> T process(String pName, T pValue, Function<String, T> pStringValuefier, Function<T, String> pValueStringifier);
   }
}
