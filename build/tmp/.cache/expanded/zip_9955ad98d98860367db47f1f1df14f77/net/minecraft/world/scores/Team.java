package net.minecraft.world.scores;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class Team {
   /**
    * Same as ==
    */
   public boolean isAlliedTo(@Nullable Team pOther) {
      if (pOther == null) {
         return false;
      } else {
         return this == pOther;
      }
   }

   /**
    * Retrieve the name by which this team is registered in the scoreboard
    */
   public abstract String getName();

   public abstract MutableComponent getFormattedName(Component pFormattedName);

   /**
    * Checks whether members of this team can see other members that are invisible.
    */
   public abstract boolean canSeeFriendlyInvisibles();

   /**
    * Checks whether friendly fire (PVP between members of the team) is allowed.
    */
   public abstract boolean isAllowFriendlyFire();

   /**
    * Gets the visibility flags for player name tags.
    */
   public abstract Team.Visibility getNameTagVisibility();

   /**
    * Gets the color for this team. The team color is used mainly for team kill objectives and team-specific setDisplay
    * usage. It does _not_ affect all situations (for instance, the prefix is used for the glowing effect).
    */
   public abstract ChatFormatting getColor();

   /**
    * Gets a collection of all members of this team.
    */
   public abstract Collection<String> getPlayers();

   /**
    * Gets the visibility flags for player death messages.
    */
   public abstract Team.Visibility getDeathMessageVisibility();

   /**
    * Gets the rule to be used for handling collisions with members of this team.
    */
   public abstract Team.CollisionRule getCollisionRule();

   public static enum CollisionRule {
      ALWAYS("always", 0),
      NEVER("never", 1),
      PUSH_OTHER_TEAMS("pushOtherTeams", 2),
      PUSH_OWN_TEAM("pushOwnTeam", 3);

      private static final Map<String, Team.CollisionRule> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((p_83559_) -> {
         return p_83559_.name;
      }, (p_83554_) -> {
         return p_83554_;
      }));
      public final String name;
      public final int id;

      @Nullable
      public static Team.CollisionRule byName(String pName) {
         return BY_NAME.get(pName);
      }

      private CollisionRule(String pName, int pId) {
         this.name = pName;
         this.id = pId;
      }

      public Component getDisplayName() {
         return Component.translatable("team.collision." + this.name);
      }
   }

   public static enum Visibility {
      ALWAYS("always", 0),
      NEVER("never", 1),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

      private static final Map<String, Team.Visibility> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((p_83583_) -> {
         return p_83583_.name;
      }, (p_83578_) -> {
         return p_83578_;
      }));
      public final String name;
      public final int id;

      public static String[] getAllNames() {
         return BY_NAME.keySet().toArray(new String[0]);
      }

      @Nullable
      public static Team.Visibility byName(String pName) {
         return BY_NAME.get(pName);
      }

      private Visibility(String pName, int pId) {
         this.name = pName;
         this.id = pId;
      }

      public Component getDisplayName() {
         return Component.translatable("team.visibility." + this.name);
      }
   }
}