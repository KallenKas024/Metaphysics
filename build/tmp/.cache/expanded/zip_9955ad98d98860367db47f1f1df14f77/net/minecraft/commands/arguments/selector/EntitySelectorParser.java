package net.minecraft.commands.arguments.selector;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelectorParser {
   public static final char SYNTAX_SELECTOR_START = '@';
   private static final char SYNTAX_OPTIONS_START = '[';
   private static final char SYNTAX_OPTIONS_END = ']';
   public static final char SYNTAX_OPTIONS_KEY_VALUE_SEPARATOR = '=';
   private static final char SYNTAX_OPTIONS_SEPARATOR = ',';
   public static final char SYNTAX_NOT = '!';
   public static final char SYNTAX_TAG = '#';
   private static final char SELECTOR_NEAREST_PLAYER = 'p';
   private static final char SELECTOR_ALL_PLAYERS = 'a';
   private static final char SELECTOR_RANDOM_PLAYERS = 'r';
   private static final char SELECTOR_CURRENT_ENTITY = 's';
   private static final char SELECTOR_ALL_ENTITIES = 'e';
   public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = new SimpleCommandExceptionType(Component.translatable("argument.entity.invalid"));
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_SELECTOR_TYPE = new DynamicCommandExceptionType((p_121301_) -> {
      return Component.translatable("argument.entity.selector.unknown", p_121301_);
   });
   public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.entity.selector.not_allowed"));
   public static final SimpleCommandExceptionType ERROR_MISSING_SELECTOR_TYPE = new SimpleCommandExceptionType(Component.translatable("argument.entity.selector.missing"));
   public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_OPTIONS = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.unterminated"));
   public static final DynamicCommandExceptionType ERROR_EXPECTED_OPTION_VALUE = new DynamicCommandExceptionType((p_121267_) -> {
      return Component.translatable("argument.entity.options.valueless", p_121267_);
   });
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_NEAREST = (p_121313_, p_121314_) -> {
      p_121314_.sort((p_175140_, p_175141_) -> {
         return Doubles.compare(p_175140_.distanceToSqr(p_121313_), p_175141_.distanceToSqr(p_121313_));
      });
   };
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_FURTHEST = (p_121298_, p_121299_) -> {
      p_121299_.sort((p_175131_, p_175132_) -> {
         return Doubles.compare(p_175132_.distanceToSqr(p_121298_), p_175131_.distanceToSqr(p_121298_));
      });
   };
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_RANDOM = (p_121264_, p_121265_) -> {
      Collections.shuffle(p_121265_);
   };
   public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (p_121363_, p_121364_) -> {
      return p_121363_.buildFuture();
   };
   private final StringReader reader;
   private final boolean allowSelectors;
   private int maxResults;
   private boolean includesEntities;
   private boolean worldLimited;
   private MinMaxBounds.Doubles distance = MinMaxBounds.Doubles.ANY;
   private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
   @Nullable
   private Double x;
   @Nullable
   private Double y;
   @Nullable
   private Double z;
   @Nullable
   private Double deltaX;
   @Nullable
   private Double deltaY;
   @Nullable
   private Double deltaZ;
   private WrappedMinMaxBounds rotX = WrappedMinMaxBounds.ANY;
   private WrappedMinMaxBounds rotY = WrappedMinMaxBounds.ANY;
   private Predicate<Entity> predicate = (p_121321_) -> {
      return true;
   };
   private BiConsumer<Vec3, List<? extends Entity>> order = EntitySelector.ORDER_ARBITRARY;
   private boolean currentEntity;
   @Nullable
   private String playerName;
   private int startPosition;
   @Nullable
   private UUID entityUUID;
   private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;
   private boolean hasNameEquals;
   private boolean hasNameNotEquals;
   private boolean isLimited;
   private boolean isSorted;
   private boolean hasGamemodeEquals;
   private boolean hasGamemodeNotEquals;
   private boolean hasTeamEquals;
   private boolean hasTeamNotEquals;
   @Nullable
   private EntityType<?> type;
   private boolean typeInverse;
   private boolean hasScores;
   private boolean hasAdvancements;
   private boolean usesSelectors;

   public EntitySelectorParser(StringReader pReader) {
      this(pReader, true);
   }

   public EntitySelectorParser(StringReader pReader, boolean pAllowSelectors) {
      this.reader = pReader;
      this.allowSelectors = pAllowSelectors;
   }

   public EntitySelector getSelector() {
      AABB aabb;
      if (this.deltaX == null && this.deltaY == null && this.deltaZ == null) {
         if (this.distance.getMax() != null) {
            double d0 = this.distance.getMax();
            aabb = new AABB(-d0, -d0, -d0, d0 + 1.0D, d0 + 1.0D, d0 + 1.0D);
         } else {
            aabb = null;
         }
      } else {
         aabb = this.createAabb(this.deltaX == null ? 0.0D : this.deltaX, this.deltaY == null ? 0.0D : this.deltaY, this.deltaZ == null ? 0.0D : this.deltaZ);
      }

      Function<Vec3, Vec3> function;
      if (this.x == null && this.y == null && this.z == null) {
         function = (p_121292_) -> {
            return p_121292_;
         };
      } else {
         function = (p_121258_) -> {
            return new Vec3(this.x == null ? p_121258_.x : this.x, this.y == null ? p_121258_.y : this.y, this.z == null ? p_121258_.z : this.z);
         };
      }

      return new EntitySelector(this.maxResults, this.includesEntities, this.worldLimited, this.predicate, this.distance, function, aabb, this.order, this.currentEntity, this.playerName, this.entityUUID, this.type, this.usesSelectors);
   }

   private AABB createAabb(double pSizeX, double pSizeY, double pSizeZ) {
      boolean flag = pSizeX < 0.0D;
      boolean flag1 = pSizeY < 0.0D;
      boolean flag2 = pSizeZ < 0.0D;
      double d0 = flag ? pSizeX : 0.0D;
      double d1 = flag1 ? pSizeY : 0.0D;
      double d2 = flag2 ? pSizeZ : 0.0D;
      double d3 = (flag ? 0.0D : pSizeX) + 1.0D;
      double d4 = (flag1 ? 0.0D : pSizeY) + 1.0D;
      double d5 = (flag2 ? 0.0D : pSizeZ) + 1.0D;
      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   public void finalizePredicates() {
      if (this.rotX != WrappedMinMaxBounds.ANY) {
         this.predicate = this.predicate.and(this.createRotationPredicate(this.rotX, Entity::getXRot));
      }

      if (this.rotY != WrappedMinMaxBounds.ANY) {
         this.predicate = this.predicate.and(this.createRotationPredicate(this.rotY, Entity::getYRot));
      }

      if (!this.level.isAny()) {
         this.predicate = this.predicate.and((p_287322_) -> {
            return !(p_287322_ instanceof ServerPlayer) ? false : this.level.matches(((ServerPlayer)p_287322_).experienceLevel);
         });
      }

   }

   private Predicate<Entity> createRotationPredicate(WrappedMinMaxBounds pAngleBounds, ToDoubleFunction<Entity> pAngleFunction) {
      double d0 = (double)Mth.wrapDegrees(pAngleBounds.getMin() == null ? 0.0F : pAngleBounds.getMin());
      double d1 = (double)Mth.wrapDegrees(pAngleBounds.getMax() == null ? 359.0F : pAngleBounds.getMax());
      return (p_175137_) -> {
         double d2 = Mth.wrapDegrees(pAngleFunction.applyAsDouble(p_175137_));
         if (d0 > d1) {
            return d2 >= d0 || d2 <= d1;
         } else {
            return d2 >= d0 && d2 <= d1;
         }
      };
   }

   protected void parseSelector() throws CommandSyntaxException {
      this.usesSelectors = true;
      this.suggestions = this::suggestSelector;
      if (!this.reader.canRead()) {
         throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(this.reader);
      } else {
         int i = this.reader.getCursor();
         char c0 = this.reader.read();
         if (c0 == 'p') {
            this.maxResults = 1;
            this.includesEntities = false;
            this.order = ORDER_NEAREST;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 'a') {
            this.maxResults = Integer.MAX_VALUE;
            this.includesEntities = false;
            this.order = EntitySelector.ORDER_ARBITRARY;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 'r') {
            this.maxResults = 1;
            this.includesEntities = false;
            this.order = ORDER_RANDOM;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 's') {
            this.maxResults = 1;
            this.includesEntities = true;
            this.currentEntity = true;
         } else {
            if (c0 != 'e') {
               this.reader.setCursor(i);
               throw ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(this.reader, "@" + String.valueOf(c0));
            }

            this.maxResults = Integer.MAX_VALUE;
            this.includesEntities = true;
            this.order = EntitySelector.ORDER_ARBITRARY;
            this.predicate = Entity::isAlive;
         }

         this.suggestions = this::suggestOpenOptions;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.reader.skip();
            this.suggestions = this::suggestOptionsKeyOrClose;
            this.parseOptions();
         }

      }
   }

   protected void parseNameOrUUID() throws CommandSyntaxException {
      if (this.reader.canRead()) {
         this.suggestions = this::suggestName;
      }

      int i = this.reader.getCursor();
      String s = this.reader.readString();

      try {
         this.entityUUID = UUID.fromString(s);
         this.includesEntities = true;
      } catch (IllegalArgumentException illegalargumentexception) {
         if (s.isEmpty() || s.length() > 16) {
            this.reader.setCursor(i);
            throw ERROR_INVALID_NAME_OR_UUID.createWithContext(this.reader);
         }

         this.includesEntities = false;
         this.playerName = s;
      }

      this.maxResults = 1;
   }

   public void parseOptions() throws CommandSyntaxException {
      this.suggestions = this::suggestOptionsKey;
      this.reader.skipWhitespace();

      while(true) {
         if (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String s = this.reader.readString();
            EntitySelectorOptions.Modifier entityselectoroptions$modifier = EntitySelectorOptions.get(this, s, i);
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
               this.reader.setCursor(i);
               throw ERROR_EXPECTED_OPTION_VALUE.createWithContext(this.reader, s);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = SUGGEST_NOTHING;
            entityselectoroptions$modifier.handle(this);
            this.reader.skipWhitespace();
            this.suggestions = this::suggestOptionsNextOrClose;
            if (!this.reader.canRead()) {
               continue;
            }

            if (this.reader.peek() == ',') {
               this.reader.skip();
               this.suggestions = this::suggestOptionsKey;
               continue;
            }

            if (this.reader.peek() != ']') {
               throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
            }
         }

         if (this.reader.canRead()) {
            this.reader.skip();
            this.suggestions = SUGGEST_NOTHING;
            return;
         }

         throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
      }
   }

   public boolean shouldInvertValue() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '!') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public boolean isTag() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public StringReader getReader() {
      return this.reader;
   }

   public void addPredicate(Predicate<Entity> pPredicate) {
      this.predicate = this.predicate.and(pPredicate);
   }

   public void setWorldLimited() {
      this.worldLimited = true;
   }

   public MinMaxBounds.Doubles getDistance() {
      return this.distance;
   }

   public void setDistance(MinMaxBounds.Doubles pDistance) {
      this.distance = pDistance;
   }

   public MinMaxBounds.Ints getLevel() {
      return this.level;
   }

   public void setLevel(MinMaxBounds.Ints pLevel) {
      this.level = pLevel;
   }

   public WrappedMinMaxBounds getRotX() {
      return this.rotX;
   }

   public void setRotX(WrappedMinMaxBounds pRotX) {
      this.rotX = pRotX;
   }

   public WrappedMinMaxBounds getRotY() {
      return this.rotY;
   }

   public void setRotY(WrappedMinMaxBounds pRotY) {
      this.rotY = pRotY;
   }

   @Nullable
   public Double getX() {
      return this.x;
   }

   @Nullable
   public Double getY() {
      return this.y;
   }

   @Nullable
   public Double getZ() {
      return this.z;
   }

   public void setX(double pX) {
      this.x = pX;
   }

   public void setY(double pY) {
      this.y = pY;
   }

   public void setZ(double pZ) {
      this.z = pZ;
   }

   public void setDeltaX(double pDeltaX) {
      this.deltaX = pDeltaX;
   }

   public void setDeltaY(double pDeltaY) {
      this.deltaY = pDeltaY;
   }

   public void setDeltaZ(double pDeltaZ) {
      this.deltaZ = pDeltaZ;
   }

   @Nullable
   public Double getDeltaX() {
      return this.deltaX;
   }

   @Nullable
   public Double getDeltaY() {
      return this.deltaY;
   }

   @Nullable
   public Double getDeltaZ() {
      return this.deltaZ;
   }

   public void setMaxResults(int pMaxResults) {
      this.maxResults = pMaxResults;
   }

   public void setIncludesEntities(boolean pIncludesEntities) {
      this.includesEntities = pIncludesEntities;
   }

   public BiConsumer<Vec3, List<? extends Entity>> getOrder() {
      return this.order;
   }

   public void setOrder(BiConsumer<Vec3, List<? extends Entity>> pOrder) {
      this.order = pOrder;
   }

   public EntitySelector parse() throws CommandSyntaxException {
      this.startPosition = this.reader.getCursor();
      this.suggestions = this::suggestNameOrSelector;
      if (this.reader.canRead() && this.reader.peek() == '@') {
         if (!this.allowSelectors) {
            throw ERROR_SELECTORS_NOT_ALLOWED.createWithContext(this.reader);
         }

         this.reader.skip();
         EntitySelector forgeSelector = net.minecraftforge.common.command.EntitySelectorManager.parseSelector(this);
         if (forgeSelector != null)
            return forgeSelector;
         this.parseSelector();
      } else {
         this.parseNameOrUUID();
      }

      this.finalizePredicates();
      return this.getSelector();
   }

   private static void fillSelectorSuggestions(SuggestionsBuilder pBuilder) {
      pBuilder.suggest("@p", Component.translatable("argument.entity.selector.nearestPlayer"));
      pBuilder.suggest("@a", Component.translatable("argument.entity.selector.allPlayers"));
      pBuilder.suggest("@r", Component.translatable("argument.entity.selector.randomPlayer"));
      pBuilder.suggest("@s", Component.translatable("argument.entity.selector.self"));
      pBuilder.suggest("@e", Component.translatable("argument.entity.selector.allEntities"));
      net.minecraftforge.common.command.EntitySelectorManager.fillSelectorSuggestions(pBuilder);
   }

   private CompletableFuture<Suggestions> suggestNameOrSelector(SuggestionsBuilder p_121287_, Consumer<SuggestionsBuilder> p_121288_) {
      p_121288_.accept(p_121287_);
      if (this.allowSelectors) {
         fillSelectorSuggestions(p_121287_);
      }

      return p_121287_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestName(SuggestionsBuilder p_121310_, Consumer<SuggestionsBuilder> p_121311_) {
      SuggestionsBuilder suggestionsbuilder = p_121310_.createOffset(this.startPosition);
      p_121311_.accept(suggestionsbuilder);
      return p_121310_.add(suggestionsbuilder).buildFuture();
   }

   private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder p_121323_, Consumer<SuggestionsBuilder> p_121324_) {
      SuggestionsBuilder suggestionsbuilder = p_121323_.createOffset(p_121323_.getStart() - 1);
      fillSelectorSuggestions(suggestionsbuilder);
      p_121323_.add(suggestionsbuilder);
      return p_121323_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder p_121334_, Consumer<SuggestionsBuilder> p_121335_) {
      p_121334_.suggest(String.valueOf('['));
      return p_121334_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder p_121342_, Consumer<SuggestionsBuilder> p_121343_) {
      p_121342_.suggest(String.valueOf(']'));
      EntitySelectorOptions.suggestNames(this, p_121342_);
      return p_121342_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKey(SuggestionsBuilder p_121348_, Consumer<SuggestionsBuilder> p_121349_) {
      EntitySelectorOptions.suggestNames(this, p_121348_);
      return p_121348_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsNextOrClose(SuggestionsBuilder p_121354_, Consumer<SuggestionsBuilder> p_121355_) {
      p_121354_.suggest(String.valueOf(','));
      p_121354_.suggest(String.valueOf(']'));
      return p_121354_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder pBuilder, Consumer<SuggestionsBuilder> pConsumer) {
      pBuilder.suggest(String.valueOf('='));
      return pBuilder.buildFuture();
   }

   public boolean isCurrentEntity() {
      return this.currentEntity;
   }

   public void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> pSuggestionHandler) {
      this.suggestions = pSuggestionHandler;
   }

   public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder pBuilder, Consumer<SuggestionsBuilder> pConsumer) {
      return this.suggestions.apply(pBuilder.createOffset(this.reader.getCursor()), pConsumer);
   }

   public boolean hasNameEquals() {
      return this.hasNameEquals;
   }

   public void setHasNameEquals(boolean pHasNameEquals) {
      this.hasNameEquals = pHasNameEquals;
   }

   public boolean hasNameNotEquals() {
      return this.hasNameNotEquals;
   }

   public void setHasNameNotEquals(boolean pHasNameNotEquals) {
      this.hasNameNotEquals = pHasNameNotEquals;
   }

   public boolean isLimited() {
      return this.isLimited;
   }

   public void setLimited(boolean pIsLimited) {
      this.isLimited = pIsLimited;
   }

   public boolean isSorted() {
      return this.isSorted;
   }

   public void setSorted(boolean pIsSorted) {
      this.isSorted = pIsSorted;
   }

   public boolean hasGamemodeEquals() {
      return this.hasGamemodeEquals;
   }

   public void setHasGamemodeEquals(boolean pHasGamemodeEquals) {
      this.hasGamemodeEquals = pHasGamemodeEquals;
   }

   public boolean hasGamemodeNotEquals() {
      return this.hasGamemodeNotEquals;
   }

   public void setHasGamemodeNotEquals(boolean pHasGamemodeNotEquals) {
      this.hasGamemodeNotEquals = pHasGamemodeNotEquals;
   }

   public boolean hasTeamEquals() {
      return this.hasTeamEquals;
   }

   public void setHasTeamEquals(boolean pHasTeamEquals) {
      this.hasTeamEquals = pHasTeamEquals;
   }

   public boolean hasTeamNotEquals() {
      return this.hasTeamNotEquals;
   }

   public void setHasTeamNotEquals(boolean pHasTeamNotEquals) {
      this.hasTeamNotEquals = pHasTeamNotEquals;
   }

   public void limitToType(EntityType<?> pType) {
      this.type = pType;
   }

   public void setTypeLimitedInversely() {
      this.typeInverse = true;
   }

   public boolean isTypeLimited() {
      return this.type != null;
   }

   public boolean isTypeLimitedInversely() {
      return this.typeInverse;
   }

   public boolean hasScores() {
      return this.hasScores;
   }

   public void setHasScores(boolean pHasScores) {
      this.hasScores = pHasScores;
   }

   public boolean hasAdvancements() {
      return this.hasAdvancements;
   }

   public void setHasAdvancements(boolean pHasAdvancements) {
      this.hasAdvancements = pHasAdvancements;
   }
}
