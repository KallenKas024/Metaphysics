package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class BossBarCommands {
   private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType((p_136636_) -> {
      return Component.translatable("commands.bossbar.create.failed", p_136636_);
   });
   private static final DynamicCommandExceptionType ERROR_DOESNT_EXIST = new DynamicCommandExceptionType((p_136623_) -> {
      return Component.translatable("commands.bossbar.unknown", p_136623_);
   });
   private static final SimpleCommandExceptionType ERROR_NO_PLAYER_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.players.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_NAME_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.name.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_COLOR_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.color.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_STYLE_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.style.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_VALUE_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.value.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_MAX_CHANGE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.max.unchanged"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_HIDDEN = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.visibility.unchanged.hidden"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_VISIBLE = new SimpleCommandExceptionType(Component.translatable("commands.bossbar.set.visibility.unchanged.visible"));
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS_BAR = (p_136587_, p_136588_) -> {
      return SharedSuggestionProvider.suggestResource(p_136587_.getSource().getServer().getCustomBossEvents().getIds(), p_136588_);
   };

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("bossbar").requires((p_136627_) -> {
         return p_136627_.hasPermission(2);
      }).then(Commands.literal("add").then(Commands.argument("id", ResourceLocationArgument.id()).then(Commands.argument("name", ComponentArgument.textComponent()).executes((p_136693_) -> {
         return createBar(p_136693_.getSource(), ResourceLocationArgument.getId(p_136693_, "id"), ComponentArgument.getComponent(p_136693_, "name"));
      })))).then(Commands.literal("remove").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).executes((p_136691_) -> {
         return removeBar(p_136691_.getSource(), getBossBar(p_136691_));
      }))).then(Commands.literal("list").executes((p_136689_) -> {
         return listBars(p_136689_.getSource());
      })).then(Commands.literal("set").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("name").then(Commands.argument("name", ComponentArgument.textComponent()).executes((p_136687_) -> {
         return setName(p_136687_.getSource(), getBossBar(p_136687_), ComponentArgument.getComponent(p_136687_, "name"));
      }))).then(Commands.literal("color").then(Commands.literal("pink").executes((p_136685_) -> {
         return setColor(p_136685_.getSource(), getBossBar(p_136685_), BossEvent.BossBarColor.PINK);
      })).then(Commands.literal("blue").executes((p_136683_) -> {
         return setColor(p_136683_.getSource(), getBossBar(p_136683_), BossEvent.BossBarColor.BLUE);
      })).then(Commands.literal("red").executes((p_136681_) -> {
         return setColor(p_136681_.getSource(), getBossBar(p_136681_), BossEvent.BossBarColor.RED);
      })).then(Commands.literal("green").executes((p_136679_) -> {
         return setColor(p_136679_.getSource(), getBossBar(p_136679_), BossEvent.BossBarColor.GREEN);
      })).then(Commands.literal("yellow").executes((p_136677_) -> {
         return setColor(p_136677_.getSource(), getBossBar(p_136677_), BossEvent.BossBarColor.YELLOW);
      })).then(Commands.literal("purple").executes((p_136675_) -> {
         return setColor(p_136675_.getSource(), getBossBar(p_136675_), BossEvent.BossBarColor.PURPLE);
      })).then(Commands.literal("white").executes((p_136673_) -> {
         return setColor(p_136673_.getSource(), getBossBar(p_136673_), BossEvent.BossBarColor.WHITE);
      }))).then(Commands.literal("style").then(Commands.literal("progress").executes((p_136671_) -> {
         return setStyle(p_136671_.getSource(), getBossBar(p_136671_), BossEvent.BossBarOverlay.PROGRESS);
      })).then(Commands.literal("notched_6").executes((p_136669_) -> {
         return setStyle(p_136669_.getSource(), getBossBar(p_136669_), BossEvent.BossBarOverlay.NOTCHED_6);
      })).then(Commands.literal("notched_10").executes((p_136667_) -> {
         return setStyle(p_136667_.getSource(), getBossBar(p_136667_), BossEvent.BossBarOverlay.NOTCHED_10);
      })).then(Commands.literal("notched_12").executes((p_136665_) -> {
         return setStyle(p_136665_.getSource(), getBossBar(p_136665_), BossEvent.BossBarOverlay.NOTCHED_12);
      })).then(Commands.literal("notched_20").executes((p_136663_) -> {
         return setStyle(p_136663_.getSource(), getBossBar(p_136663_), BossEvent.BossBarOverlay.NOTCHED_20);
      }))).then(Commands.literal("value").then(Commands.argument("value", IntegerArgumentType.integer(0)).executes((p_136661_) -> {
         return setValue(p_136661_.getSource(), getBossBar(p_136661_), IntegerArgumentType.getInteger(p_136661_, "value"));
      }))).then(Commands.literal("max").then(Commands.argument("max", IntegerArgumentType.integer(1)).executes((p_136659_) -> {
         return setMax(p_136659_.getSource(), getBossBar(p_136659_), IntegerArgumentType.getInteger(p_136659_, "max"));
      }))).then(Commands.literal("visible").then(Commands.argument("visible", BoolArgumentType.bool()).executes((p_136657_) -> {
         return setVisible(p_136657_.getSource(), getBossBar(p_136657_), BoolArgumentType.getBool(p_136657_, "visible"));
      }))).then(Commands.literal("players").executes((p_136655_) -> {
         return setPlayers(p_136655_.getSource(), getBossBar(p_136655_), Collections.emptyList());
      }).then(Commands.argument("targets", EntityArgument.players()).executes((p_136653_) -> {
         return setPlayers(p_136653_.getSource(), getBossBar(p_136653_), EntityArgument.getOptionalPlayers(p_136653_, "targets"));
      }))))).then(Commands.literal("get").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("value").executes((p_136648_) -> {
         return getValue(p_136648_.getSource(), getBossBar(p_136648_));
      })).then(Commands.literal("max").executes((p_136643_) -> {
         return getMax(p_136643_.getSource(), getBossBar(p_136643_));
      })).then(Commands.literal("visible").executes((p_136638_) -> {
         return getVisible(p_136638_.getSource(), getBossBar(p_136638_));
      })).then(Commands.literal("players").executes((p_136625_) -> {
         return getPlayers(p_136625_.getSource(), getBossBar(p_136625_));
      })))));
   }

   private static int getValue(CommandSourceStack pSource, CustomBossEvent pBossbar) {
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.bossbar.get.value", pBossbar.getDisplayName(), pBossbar.getValue());
      }, true);
      return pBossbar.getValue();
   }

   private static int getMax(CommandSourceStack pSource, CustomBossEvent pBossbar) {
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.bossbar.get.max", pBossbar.getDisplayName(), pBossbar.getMax());
      }, true);
      return pBossbar.getMax();
   }

   private static int getVisible(CommandSourceStack pSource, CustomBossEvent pBossbar) {
      if (pBossbar.isVisible()) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.get.visible.visible", pBossbar.getDisplayName());
         }, true);
         return 1;
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.get.visible.hidden", pBossbar.getDisplayName());
         }, true);
         return 0;
      }
   }

   private static int getPlayers(CommandSourceStack pSource, CustomBossEvent pBossbar) {
      if (pBossbar.getPlayers().isEmpty()) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.get.players.none", pBossbar.getDisplayName());
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.get.players.some", pBossbar.getDisplayName(), pBossbar.getPlayers().size(), ComponentUtils.formatList(pBossbar.getPlayers(), Player::getDisplayName));
         }, true);
      }

      return pBossbar.getPlayers().size();
   }

   private static int setVisible(CommandSourceStack pSource, CustomBossEvent pBossbar, boolean pVisible) throws CommandSyntaxException {
      if (pBossbar.isVisible() == pVisible) {
         if (pVisible) {
            throw ERROR_ALREADY_VISIBLE.create();
         } else {
            throw ERROR_ALREADY_HIDDEN.create();
         }
      } else {
         pBossbar.setVisible(pVisible);
         if (pVisible) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.bossbar.set.visible.success.visible", pBossbar.getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.bossbar.set.visible.success.hidden", pBossbar.getDisplayName());
            }, true);
         }

         return 0;
      }
   }

   private static int setValue(CommandSourceStack pSource, CustomBossEvent pBossbar, int pValue) throws CommandSyntaxException {
      if (pBossbar.getValue() == pValue) {
         throw ERROR_NO_VALUE_CHANGE.create();
      } else {
         pBossbar.setValue(pValue);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.set.value.success", pBossbar.getDisplayName(), pValue);
         }, true);
         return pValue;
      }
   }

   private static int setMax(CommandSourceStack pSource, CustomBossEvent pBossbar, int pMax) throws CommandSyntaxException {
      if (pBossbar.getMax() == pMax) {
         throw ERROR_NO_MAX_CHANGE.create();
      } else {
         pBossbar.setMax(pMax);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.set.max.success", pBossbar.getDisplayName(), pMax);
         }, true);
         return pMax;
      }
   }

   private static int setColor(CommandSourceStack pSource, CustomBossEvent pBossbar, BossEvent.BossBarColor pColor) throws CommandSyntaxException {
      if (pBossbar.getColor().equals(pColor)) {
         throw ERROR_NO_COLOR_CHANGE.create();
      } else {
         pBossbar.setColor(pColor);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.set.color.success", pBossbar.getDisplayName());
         }, true);
         return 0;
      }
   }

   private static int setStyle(CommandSourceStack pSource, CustomBossEvent pBossbar, BossEvent.BossBarOverlay pStyle) throws CommandSyntaxException {
      if (pBossbar.getOverlay().equals(pStyle)) {
         throw ERROR_NO_STYLE_CHANGE.create();
      } else {
         pBossbar.setOverlay(pStyle);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.set.style.success", pBossbar.getDisplayName());
         }, true);
         return 0;
      }
   }

   private static int setName(CommandSourceStack pSource, CustomBossEvent pBossbar, Component pName) throws CommandSyntaxException {
      Component component = ComponentUtils.updateForEntity(pSource, pName, (Entity)null, 0);
      if (pBossbar.getName().equals(component)) {
         throw ERROR_NO_NAME_CHANGE.create();
      } else {
         pBossbar.setName(component);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.set.name.success", pBossbar.getDisplayName());
         }, true);
         return 0;
      }
   }

   private static int setPlayers(CommandSourceStack pSource, CustomBossEvent pBossbar, Collection<ServerPlayer> pPlayers) throws CommandSyntaxException {
      boolean flag = pBossbar.setPlayers(pPlayers);
      if (!flag) {
         throw ERROR_NO_PLAYER_CHANGE.create();
      } else {
         if (pBossbar.getPlayers().isEmpty()) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.bossbar.set.players.success.none", pBossbar.getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.bossbar.set.players.success.some", pBossbar.getDisplayName(), pPlayers.size(), ComponentUtils.formatList(pPlayers, Player::getDisplayName));
            }, true);
         }

         return pBossbar.getPlayers().size();
      }
   }

   private static int listBars(CommandSourceStack pSource) {
      Collection<CustomBossEvent> collection = pSource.getServer().getCustomBossEvents().getEvents();
      if (collection.isEmpty()) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.list.bars.none");
         }, false);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.list.bars.some", collection.size(), ComponentUtils.formatList(collection, CustomBossEvent::getDisplayName));
         }, false);
      }

      return collection.size();
   }

   private static int createBar(CommandSourceStack pSource, ResourceLocation pId, Component pDisplayName) throws CommandSyntaxException {
      CustomBossEvents custombossevents = pSource.getServer().getCustomBossEvents();
      if (custombossevents.get(pId) != null) {
         throw ERROR_ALREADY_EXISTS.create(pId.toString());
      } else {
         CustomBossEvent custombossevent = custombossevents.create(pId, ComponentUtils.updateForEntity(pSource, pDisplayName, (Entity)null, 0));
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.bossbar.create.success", custombossevent.getDisplayName());
         }, true);
         return custombossevents.getEvents().size();
      }
   }

   private static int removeBar(CommandSourceStack pSource, CustomBossEvent pBossbar) {
      CustomBossEvents custombossevents = pSource.getServer().getCustomBossEvents();
      pBossbar.removeAllPlayers();
      custombossevents.remove(pBossbar);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.bossbar.remove.success", pBossbar.getDisplayName());
      }, true);
      return custombossevents.getEvents().size();
   }

   public static CustomBossEvent getBossBar(CommandContext<CommandSourceStack> pSource) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocationArgument.getId(pSource, "id");
      CustomBossEvent custombossevent = pSource.getSource().getServer().getCustomBossEvents().get(resourcelocation);
      if (custombossevent == null) {
         throw ERROR_DOESNT_EXIST.create(resourcelocation.toString());
      } else {
         return custombossevent;
      }
   }
}