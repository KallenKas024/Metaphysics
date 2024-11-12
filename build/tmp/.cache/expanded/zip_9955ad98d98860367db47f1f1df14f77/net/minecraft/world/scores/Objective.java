package net.minecraft.world.scores;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Objective {
   private final Scoreboard scoreboard;
   private final String name;
   private final ObjectiveCriteria criteria;
   private Component displayName;
   private Component formattedDisplayName;
   private ObjectiveCriteria.RenderType renderType;

   public Objective(Scoreboard pScoreboard, String pName, ObjectiveCriteria pCriteria, Component pDisplayName, ObjectiveCriteria.RenderType pRenderType) {
      this.scoreboard = pScoreboard;
      this.name = pName;
      this.criteria = pCriteria;
      this.displayName = pDisplayName;
      this.formattedDisplayName = this.createFormattedDisplayName();
      this.renderType = pRenderType;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public String getName() {
      return this.name;
   }

   public ObjectiveCriteria getCriteria() {
      return this.criteria;
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   private Component createFormattedDisplayName() {
      return ComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle((p_83319_) -> {
         return p_83319_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name)));
      }));
   }

   public Component getFormattedDisplayName() {
      return this.formattedDisplayName;
   }

   public void setDisplayName(Component pDisplayName) {
      this.displayName = pDisplayName;
      this.formattedDisplayName = this.createFormattedDisplayName();
      this.scoreboard.onObjectiveChanged(this);
   }

   public ObjectiveCriteria.RenderType getRenderType() {
      return this.renderType;
   }

   public void setRenderType(ObjectiveCriteria.RenderType pRenderType) {
      this.renderType = pRenderType;
      this.scoreboard.onObjectiveChanged(this);
   }
}