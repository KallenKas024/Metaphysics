package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ScoreContents implements ComponentContents {
   private static final String SCORER_PLACEHOLDER = "*";
   private final String name;
   @Nullable
   private final EntitySelector selector;
   private final String objective;

   @Nullable
   private static EntitySelector parseSelector(String pSelector) {
      try {
         return (new EntitySelectorParser(new StringReader(pSelector))).parse();
      } catch (CommandSyntaxException commandsyntaxexception) {
         return null;
      }
   }

   public ScoreContents(String pName, String pObjective) {
      this.name = pName;
      this.selector = parseSelector(pName);
      this.objective = pObjective;
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public EntitySelector getSelector() {
      return this.selector;
   }

   public String getObjective() {
      return this.objective;
   }

   private String findTargetName(CommandSourceStack pSource) throws CommandSyntaxException {
      if (this.selector != null) {
         List<? extends Entity> list = this.selector.findEntities(pSource);
         if (!list.isEmpty()) {
            if (list.size() != 1) {
               throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            }

            return list.get(0).getScoreboardName();
         }
      }

      return this.name;
   }

   private String getScore(String pName, CommandSourceStack pSource) {
      MinecraftServer minecraftserver = pSource.getServer();
      if (minecraftserver != null) {
         Scoreboard scoreboard = minecraftserver.getScoreboard();
         Objective objective = scoreboard.getObjective(this.objective);
         if (scoreboard.hasPlayerScore(pName, objective)) {
            Score score = scoreboard.getOrCreatePlayerScore(pName, objective);
            return Integer.toString(score.getScore());
         }
      }

      return "";
   }

   public MutableComponent resolve(@Nullable CommandSourceStack pNbtPathPattern, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      if (pNbtPathPattern == null) {
         return Component.empty();
      } else {
         String s = this.findTargetName(pNbtPathPattern);
         String s1 = pEntity != null && s.equals("*") ? pEntity.getScoreboardName() : s;
         return Component.literal(this.getScore(s1, pNbtPathPattern));
      }
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof ScoreContents) {
            ScoreContents scorecontents = (ScoreContents)pOther;
            if (this.name.equals(scorecontents.name) && this.objective.equals(scorecontents.objective)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = this.name.hashCode();
      return 31 * i + this.objective.hashCode();
   }

   public String toString() {
      return "score{name='" + this.name + "', objective='" + this.objective + "'}";
   }
}