package net.minecraft.world.level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicLike;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class GameRules {
   public static final int DEFAULT_RANDOM_TICK_SPEED = 3;
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing((p_46218_) -> {
      return p_46218_.id;
   }));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOFIRETICK = register("doFireTick", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING = register("mobGriefing", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_KEEPINVENTORY = register("keepInventory", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBSPAWNING = register("doMobSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBLOOT = register("doMobLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOBLOCKDROPS = register("doTileDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOENTITYDROPS = register("doEntityDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT = register("commandBlockOutput", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_NATURAL_REGENERATION = register("naturalRegeneration", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DAYLIGHT = register("doDaylightCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS = register("logAdminCommands", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES = register("showDeathMessages", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_RANDOMTICKING = register("randomTickSpeed", GameRules.Category.UPDATES, GameRules.IntegerValue.create(3));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK = register("sendCommandFeedback", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO = register("reducedDebugInfo", GameRules.Category.MISC, GameRules.BooleanValue.create(false, (p_46212_, p_46213_) -> {
      byte b0 = (byte)(p_46213_.get() ? 22 : 23);

      for(ServerPlayer serverplayer : p_46212_.getPlayerList().getPlayers()) {
         serverplayer.connection.send(new ClientboundEntityEventPacket(serverplayer, b0));
      }

   }));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS = register("spectatorsGenerateChunks", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_RADIUS = register("spawnRadius", GameRules.Category.PLAYER, GameRules.IntegerValue.create(10));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register("disableElytraMovementCheck", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING = register("maxEntityCramming", GameRules.Category.MOBS, GameRules.IntegerValue.create(24));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_WEATHER_CYCLE = register("doWeatherCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_LIMITED_CRAFTING = register("doLimitedCrafting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = register("maxCommandChainLength", GameRules.Category.MISC, GameRules.IntegerValue.create(65536));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_COMMAND_MODIFICATION_BLOCK_LIMIT = register("commandModificationBlockLimit", GameRules.Category.MISC, GameRules.IntegerValue.create(32768));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = register("announceAdvancements", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_RAIDS = register("disableRaids", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOINSOMNIA = register("doInsomnia", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = register("doImmediateRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (p_46200_, p_46201_) -> {
      for(ServerPlayer serverplayer : p_46200_.getPlayerList().getPlayers()) {
         serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, p_46201_.get() ? 1.0F : 0.0F));
      }

   }));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DROWNING_DAMAGE = register("drowningDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FALL_DAMAGE = register("fallDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FIRE_DAMAGE = register("fireDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FREEZE_DAMAGE = register("freezeDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_PATROL_SPAWNING = register("doPatrolSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_TRADER_SPAWNING = register("doTraderSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_WARDEN_SPAWNING = register("doWardenSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FORGIVE_DEAD_PLAYERS = register("forgiveDeadPlayers", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_UNIVERSAL_ANGER = register("universalAnger", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_PLAYERS_SLEEPING_PERCENTAGE = register("playersSleepingPercentage", GameRules.Category.PLAYER, GameRules.IntegerValue.create(100));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_BLOCK_EXPLOSION_DROP_DECAY = register("blockExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_MOB_EXPLOSION_DROP_DECAY = register("mobExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_TNT_EXPLOSION_DROP_DECAY = register("tntExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_SNOW_ACCUMULATION_HEIGHT = register("snowAccumulationHeight", GameRules.Category.UPDATES, GameRules.IntegerValue.create(1));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_WATER_SOURCE_CONVERSION = register("waterSourceConversion", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_LAVA_SOURCE_CONVERSION = register("lavaSourceConversion", GameRules.Category.UPDATES, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_GLOBAL_SOUND_EVENTS = register("globalSoundEvents", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_VINES_SPREAD = register("doVinesSpread", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   private final Map<GameRules.Key<?>, GameRules.Value<?>> rules;

   public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String pName, GameRules.Category pCategory, GameRules.Type<T> pType) {
      GameRules.Key<T> key = new GameRules.Key<>(pName, pCategory);
      GameRules.Type<?> type = GAME_RULE_TYPES.put(key, pType);
      if (type != null) {
         throw new IllegalStateException("Duplicate game rule registration for " + pName);
      } else {
         return key;
      }
   }

   public GameRules(DynamicLike<?> pTag) {
      this();
      this.loadFromTag(pTag);
   }

   public GameRules() {
      this.rules = GAME_RULE_TYPES.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (p_46210_) -> {
         return p_46210_.getValue().createRule();
      }));
   }

   private GameRules(Map<GameRules.Key<?>, GameRules.Value<?>> pRules) {
      this.rules = pRules;
   }

   public <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> pKey) {
      return (T)(this.rules.get(pKey));
   }

   /**
    * Return the defined game rules as NBT.
    */
   public CompoundTag createTag() {
      CompoundTag compoundtag = new CompoundTag();
      this.rules.forEach((p_46197_, p_46198_) -> {
         compoundtag.putString(p_46197_.id, p_46198_.serialize());
      });
      return compoundtag;
   }

   private void loadFromTag(DynamicLike<?> pDynamic) {
      this.rules.forEach((p_46187_, p_46188_) -> {
         pDynamic.get(p_46187_.id).asString().result().ifPresent(p_46188_::deserialize);
      });
   }

   public GameRules copy() {
      return new GameRules(this.rules.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (p_46194_) -> {
         return p_46194_.getValue().copy();
      })));
   }

   public static void visitGameRuleTypes(GameRules.GameRuleTypeVisitor pVisitor) {
      GAME_RULE_TYPES.forEach((p_46205_, p_46206_) -> {
         callVisitorCap(pVisitor, p_46205_, p_46206_);
      });
   }

   private static <T extends GameRules.Value<T>> void callVisitorCap(GameRules.GameRuleTypeVisitor pVisitor, GameRules.Key<?> pKey, GameRules.Type<?> pType) {
      pVisitor.visit((GameRules.Key<T>)pKey, (GameRules.Type<T>)pType);
      ((GameRules.Type<T>)pType).callVisitor(pVisitor, (GameRules.Key<T>)pKey);
   }

   public void assignFrom(GameRules pRules, @Nullable MinecraftServer pServer) {
      pRules.rules.keySet().forEach((p_46182_) -> {
         this.assignCap(p_46182_, pRules, pServer);
      });
   }

   private <T extends GameRules.Value<T>> void assignCap(GameRules.Key<T> pKey, GameRules pRules, @Nullable MinecraftServer pServer) {
      T t = pRules.getRule(pKey);
      this.<T>getRule(pKey).setFrom(t, pServer);
   }

   public boolean getBoolean(GameRules.Key<GameRules.BooleanValue> pKey) {
      return this.getRule(pKey).get();
   }

   public int getInt(GameRules.Key<GameRules.IntegerValue> pKey) {
      return this.getRule(pKey).get();
   }

   public static class BooleanValue extends GameRules.Value<GameRules.BooleanValue> {
      private boolean value;

      public static GameRules.Type<GameRules.BooleanValue> create(boolean pDefaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> pChangeListener) {
         return new GameRules.Type<>(BoolArgumentType::bool, (p_46242_) -> {
            return new GameRules.BooleanValue(p_46242_, pDefaultValue);
         }, pChangeListener, GameRules.GameRuleTypeVisitor::visitBoolean);
      }

      public static GameRules.Type<GameRules.BooleanValue> create(boolean pDefaultValue) {
         return create(pDefaultValue, (p_46236_, p_46237_) -> {
         });
      }

      public BooleanValue(GameRules.Type<GameRules.BooleanValue> pType, boolean pValue) {
         super(pType);
         this.value = pValue;
      }

      protected void updateFromArgument(CommandContext<CommandSourceStack> pContext, String pParamName) {
         this.value = BoolArgumentType.getBool(pContext, pParamName);
      }

      public boolean get() {
         return this.value;
      }

      public void set(boolean pValue, @Nullable MinecraftServer pServer) {
         this.value = pValue;
         this.onChanged(pServer);
      }

      public String serialize() {
         return Boolean.toString(this.value);
      }

      protected void deserialize(String pValue) {
         this.value = Boolean.parseBoolean(pValue);
      }

      public int getCommandResult() {
         return this.value ? 1 : 0;
      }

      protected GameRules.BooleanValue getSelf() {
         return this;
      }

      protected GameRules.BooleanValue copy() {
         return new GameRules.BooleanValue(this.type, this.value);
      }

      public void setFrom(GameRules.BooleanValue pValue, @Nullable MinecraftServer pServer) {
         this.value = pValue.value;
         this.onChanged(pServer);
      }
   }

   public static enum Category {
      PLAYER("gamerule.category.player"),
      MOBS("gamerule.category.mobs"),
      SPAWNING("gamerule.category.spawning"),
      DROPS("gamerule.category.drops"),
      UPDATES("gamerule.category.updates"),
      CHAT("gamerule.category.chat"),
      MISC("gamerule.category.misc");

      private final String descriptionId;

      private Category(String pDescriptionId) {
         this.descriptionId = pDescriptionId;
      }

      public String getDescriptionId() {
         return this.descriptionId;
      }
   }

   public interface GameRuleTypeVisitor {
      default <T extends GameRules.Value<T>> void visit(GameRules.Key<T> pKey, GameRules.Type<T> pType) {
      }

      default void visitBoolean(GameRules.Key<GameRules.BooleanValue> pKey, GameRules.Type<GameRules.BooleanValue> pType) {
      }

      default void visitInteger(GameRules.Key<GameRules.IntegerValue> pKey, GameRules.Type<GameRules.IntegerValue> pType) {
      }
   }

   public static class IntegerValue extends GameRules.Value<GameRules.IntegerValue> {
      private int value;

      public static GameRules.Type<GameRules.IntegerValue> create(int pDefaultValue, BiConsumer<MinecraftServer, GameRules.IntegerValue> pChangeListener) {
         return new GameRules.Type<>(IntegerArgumentType::integer, (p_46293_) -> {
            return new GameRules.IntegerValue(p_46293_, pDefaultValue);
         }, pChangeListener, GameRules.GameRuleTypeVisitor::visitInteger);
      }

      public static GameRules.Type<GameRules.IntegerValue> create(int pDefaultValue) {
         return create(pDefaultValue, (p_46309_, p_46310_) -> {
         });
      }

      public IntegerValue(GameRules.Type<GameRules.IntegerValue> pType, int pValue) {
         super(pType);
         this.value = pValue;
      }

      protected void updateFromArgument(CommandContext<CommandSourceStack> pContext, String pParamName) {
         this.value = IntegerArgumentType.getInteger(pContext, pParamName);
      }

      public int get() {
         return this.value;
      }

      public void set(int pValue, @Nullable MinecraftServer pServer) {
         this.value = pValue;
         this.onChanged(pServer);
      }

      public String serialize() {
         return Integer.toString(this.value);
      }

      protected void deserialize(String pValue) {
         this.value = safeParse(pValue);
      }

      public boolean tryDeserialize(String pName) {
         try {
            this.value = Integer.parseInt(pName);
            return true;
         } catch (NumberFormatException numberformatexception) {
            return false;
         }
      }

      private static int safeParse(String pStrValue) {
         if (!pStrValue.isEmpty()) {
            try {
               return Integer.parseInt(pStrValue);
            } catch (NumberFormatException numberformatexception) {
               GameRules.LOGGER.warn("Failed to parse integer {}", (Object)pStrValue);
            }
         }

         return 0;
      }

      public int getCommandResult() {
         return this.value;
      }

      protected GameRules.IntegerValue getSelf() {
         return this;
      }

      protected GameRules.IntegerValue copy() {
         return new GameRules.IntegerValue(this.type, this.value);
      }

      public void setFrom(GameRules.IntegerValue pValue, @Nullable MinecraftServer pServer) {
         this.value = pValue.value;
         this.onChanged(pServer);
      }
   }

   public static final class Key<T extends GameRules.Value<T>> {
      final String id;
      private final GameRules.Category category;

      public Key(String pId, GameRules.Category pCategory) {
         this.id = pId;
         this.category = pCategory;
      }

      public String toString() {
         return this.id;
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else {
            return pOther instanceof GameRules.Key && ((GameRules.Key)pOther).id.equals(this.id);
         }
      }

      public int hashCode() {
         return this.id.hashCode();
      }

      public String getId() {
         return this.id;
      }

      public String getDescriptionId() {
         return "gamerule." + this.id;
      }

      public GameRules.Category getCategory() {
         return this.category;
      }
   }

   public static class Type<T extends GameRules.Value<T>> {
      private final Supplier<ArgumentType<?>> argument;
      private final Function<GameRules.Type<T>, T> constructor;
      final BiConsumer<MinecraftServer, T> callback;
      private final GameRules.VisitorCaller<T> visitorCaller;

      Type(Supplier<ArgumentType<?>> pArgument, Function<GameRules.Type<T>, T> pConstructor, BiConsumer<MinecraftServer, T> pCallback, GameRules.VisitorCaller<T> pVisitorCaller) {
         this.argument = pArgument;
         this.constructor = pConstructor;
         this.callback = pCallback;
         this.visitorCaller = pVisitorCaller;
      }

      public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String pName) {
         return Commands.argument(pName, this.argument.get());
      }

      public T createRule() {
         return this.constructor.apply(this);
      }

      public void callVisitor(GameRules.GameRuleTypeVisitor pVisitor, GameRules.Key<T> pKey) {
         this.visitorCaller.call(pVisitor, pKey, this);
      }
   }

   public abstract static class Value<T extends GameRules.Value<T>> {
      protected final GameRules.Type<T> type;

      public Value(GameRules.Type<T> pType) {
         this.type = pType;
      }

      protected abstract void updateFromArgument(CommandContext<CommandSourceStack> pContext, String pParamName);

      public void setFromArgument(CommandContext<CommandSourceStack> pContext, String pParamName) {
         this.updateFromArgument(pContext, pParamName);
         this.onChanged(pContext.getSource().getServer());
      }

      protected void onChanged(@Nullable MinecraftServer pServer) {
         if (pServer != null) {
            this.type.callback.accept(pServer, this.getSelf());
         }

      }

      protected abstract void deserialize(String pValue);

      public abstract String serialize();

      public String toString() {
         return this.serialize();
      }

      public abstract int getCommandResult();

      protected abstract T getSelf();

      protected abstract T copy();

      public abstract void setFrom(T pValue, @Nullable MinecraftServer pServer);
   }

   interface VisitorCaller<T extends GameRules.Value<T>> {
      void call(GameRules.GameRuleTypeVisitor pVisitor, GameRules.Key<T> pKey, GameRules.Type<T> pType);
   }
}