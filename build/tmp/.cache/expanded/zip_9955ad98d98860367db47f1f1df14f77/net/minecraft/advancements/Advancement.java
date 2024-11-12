package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
   @Nullable
   private final Advancement parent;
   @Nullable
   private final DisplayInfo display;
   private final AdvancementRewards rewards;
   private final ResourceLocation id;
   private final Map<String, Criterion> criteria;
   private final String[][] requirements;
   private final Set<Advancement> children = Sets.newLinkedHashSet();
   private final Component chatComponent;
   private final boolean sendsTelemetryEvent;

   public Advancement(ResourceLocation pId, @Nullable Advancement pParent, @Nullable DisplayInfo pDisplay, AdvancementRewards pRewards, Map<String, Criterion> pCriteria, String[][] pRequirements, boolean pSendsTelemetryEvent) {
      this.id = pId;
      this.display = pDisplay;
      this.criteria = ImmutableMap.copyOf(pCriteria);
      this.parent = pParent;
      this.rewards = pRewards;
      this.requirements = pRequirements;
      this.sendsTelemetryEvent = pSendsTelemetryEvent;
      if (pParent != null) {
         pParent.addChild(this);
      }

      if (pDisplay == null) {
         this.chatComponent = Component.literal(pId.toString());
      } else {
         Component component = pDisplay.getTitle();
         ChatFormatting chatformatting = pDisplay.getFrame().getChatColor();
         Component component1 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatformatting)).append("\n").append(pDisplay.getDescription());
         Component component2 = component.copy().withStyle((p_138316_) -> {
            return p_138316_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1));
         });
         this.chatComponent = ComponentUtils.wrapInSquareBrackets(component2).withStyle(chatformatting);
      }

   }

   /**
    * Deconstructs this advancement into a {@link net.minecraft.advancements.Advancement#Builder}.
    */
   public Advancement.Builder deconstruct() {
      return new Advancement.Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements, this.sendsTelemetryEvent);
   }

   /**
    * @return The parent advancement to display in the advancements screen or {@code null} to signify this is a root
    * advancement.
    */
   @Nullable
   public Advancement getParent() {
      return this.parent;
   }

   public Advancement getRoot() {
      return getRoot(this);
   }

   public static Advancement getRoot(Advancement pAdvancement) {
      Advancement advancement = pAdvancement;

      while(true) {
         Advancement advancement1 = advancement.getParent();
         if (advancement1 == null) {
            return advancement;
         }

         advancement = advancement1;
      }
   }

   /**
    * @return Display information for this advancement, or {@code null} if this advancement is invisible.
    */
   @Nullable
   public DisplayInfo getDisplay() {
      return this.display;
   }

   public boolean sendsTelemetryEvent() {
      return this.sendsTelemetryEvent;
   }

   public AdvancementRewards getRewards() {
      return this.rewards;
   }

   public String toString() {
      return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + ", sendsTelemetryEvent=" + this.sendsTelemetryEvent + "}";
   }

   /**
    * @return An iterable through this advancement's children.
    */
   public Iterable<Advancement> getChildren() {
      return this.children;
   }

   /**
    * @return A map of criteria required to complete this advancement. Keys represent the criteria names and values are
    * the {@link net.minecraft.advancements.Criterion} instances.
    */
   public Map<String, Criterion> getCriteria() {
      return this.criteria;
   }

   /**
    * @return How many requirements this advancement has.
    */
   public int getMaxCriteraRequired() {
      return this.requirements.length;
   }

   /**
    * Add the provided {@code child} as a child of this advancement.
    */
   public void addChild(Advancement pChild) {
      this.children.add(pChild);
   }

   /**
    * @return The ID of this advancement.
    */
   public ResourceLocation getId() {
      return this.id;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof Advancement)) {
         return false;
      } else {
         Advancement advancement = (Advancement)pOther;
         return this.id.equals(advancement.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public String[][] getRequirements() {
      return this.requirements;
   }

   /**
    * @return The {@link net.minecraft.network.chat.Component} that is shown in the chat message sent after this
    * advancement is completed.
    */
   public Component getChatComponent() {
      return this.chatComponent;
   }

   public static class Builder implements net.minecraftforge.common.extensions.IForgeAdvancementBuilder {
      @Nullable
      private ResourceLocation parentId;
      @Nullable
      private Advancement parent;
      @Nullable
      private DisplayInfo display;
      private AdvancementRewards rewards = AdvancementRewards.EMPTY;
      private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
      @Nullable
      private String[][] requirements;
      private RequirementsStrategy requirementsStrategy = RequirementsStrategy.AND;
      private final boolean sendsTelemetryEvent;

      Builder(@Nullable ResourceLocation pParentId, @Nullable DisplayInfo pDisplay, AdvancementRewards pRewards, Map<String, Criterion> pCriteria, String[][] pRequirements, boolean pSendsTelemetryEvent) {
         this.parentId = pParentId;
         this.display = pDisplay;
         this.rewards = pRewards;
         this.criteria = pCriteria;
         this.requirements = pRequirements;
         this.sendsTelemetryEvent = pSendsTelemetryEvent;
      }

      private Builder(boolean pSendsTelemetryEvent) {
         this.sendsTelemetryEvent = pSendsTelemetryEvent;
      }

      public static Advancement.Builder advancement() {
         return new Advancement.Builder(true);
      }

      public static Advancement.Builder recipeAdvancement() {
         return new Advancement.Builder(false);
      }

      public Advancement.Builder parent(Advancement pParent) {
         this.parent = pParent;
         return this;
      }

      public Advancement.Builder parent(ResourceLocation pParentId) {
         this.parentId = pParentId;
         return this;
      }

      public Advancement.Builder display(ItemStack pStack, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
         return this.display(new DisplayInfo(pStack, pTitle, pDescription, pBackground, pFrame, pShowToast, pAnnounceToChat, pHidden));
      }

      public Advancement.Builder display(ItemLike pItem, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
         return this.display(new DisplayInfo(new ItemStack(pItem.asItem()), pTitle, pDescription, pBackground, pFrame, pShowToast, pAnnounceToChat, pHidden));
      }

      public Advancement.Builder display(DisplayInfo pDisplay) {
         this.display = pDisplay;
         return this;
      }

      public Advancement.Builder rewards(AdvancementRewards.Builder pRewardsBuilder) {
         return this.rewards(pRewardsBuilder.build());
      }

      public Advancement.Builder rewards(AdvancementRewards pRewards) {
         this.rewards = pRewards;
         return this;
      }

      public Advancement.Builder addCriterion(String pKey, CriterionTriggerInstance pCriterion) {
         return this.addCriterion(pKey, new Criterion(pCriterion));
      }

      public Advancement.Builder addCriterion(String pKey, Criterion pCriterion) {
         if (this.criteria.containsKey(pKey)) {
            throw new IllegalArgumentException("Duplicate criterion " + pKey);
         } else {
            this.criteria.put(pKey, pCriterion);
            return this;
         }
      }

      public Advancement.Builder requirements(RequirementsStrategy pStrategy) {
         this.requirementsStrategy = pStrategy;
         return this;
      }

      public Advancement.Builder requirements(String[][] pRequirements) {
         this.requirements = pRequirements;
         return this;
      }

      /**
       * Tries to resolve the parent of this advancement, if possible. Returns {@code true} on success.
       */
      public boolean canBuild(Function<ResourceLocation, Advancement> pParentLookup) {
         if (this.parentId == null) {
            return true;
         } else {
            if (this.parent == null) {
               this.parent = pParentLookup.apply(this.parentId);
            }

            return this.parent != null;
         }
      }

      public Advancement build(ResourceLocation pId) {
         if (!this.canBuild((p_138407_) -> {
            return null;
         })) {
            throw new IllegalStateException("Tried to build incomplete advancement!");
         } else {
            if (this.requirements == null) {
               this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }

            return new Advancement(pId, this.parent, this.display, this.rewards, this.criteria, this.requirements, this.sendsTelemetryEvent);
         }
      }

      public Advancement save(Consumer<Advancement> pConsumer, String pId) {
         Advancement advancement = this.build(new ResourceLocation(pId));
         pConsumer.accept(advancement);
         return advancement;
      }

      public JsonObject serializeToJson() {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
         }

         JsonObject jsonobject = new JsonObject();
         if (this.parent != null) {
            jsonobject.addProperty("parent", this.parent.getId().toString());
         } else if (this.parentId != null) {
            jsonobject.addProperty("parent", this.parentId.toString());
         }

         if (this.display != null) {
            jsonobject.add("display", this.display.serializeToJson());
         }

         jsonobject.add("rewards", this.rewards.serializeToJson());
         JsonObject jsonobject1 = new JsonObject();

         for(Map.Entry<String, Criterion> entry : this.criteria.entrySet()) {
            jsonobject1.add(entry.getKey(), entry.getValue().serializeToJson());
         }

         jsonobject.add("criteria", jsonobject1);
         JsonArray jsonarray1 = new JsonArray();

         for(String[] astring : this.requirements) {
            JsonArray jsonarray = new JsonArray();

            for(String s : astring) {
               jsonarray.add(s);
            }

            jsonarray1.add(jsonarray);
         }

         jsonobject.add("requirements", jsonarray1);
         jsonobject.addProperty("sends_telemetry_event", this.sendsTelemetryEvent);
         return jsonobject;
      }

      public void serializeToNetwork(FriendlyByteBuf pBuffer) {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
         }

         pBuffer.writeNullable(this.parentId, FriendlyByteBuf::writeResourceLocation);
         pBuffer.writeNullable(this.display, (p_214831_, p_214832_) -> {
            p_214832_.serializeToNetwork(p_214831_);
         });
         Criterion.serializeToNetwork(this.criteria, pBuffer);
         pBuffer.writeVarInt(this.requirements.length);

         for(String[] astring : this.requirements) {
            pBuffer.writeVarInt(astring.length);

            for(String s : astring) {
               pBuffer.writeUtf(s);
            }
         }

         pBuffer.writeBoolean(this.sendsTelemetryEvent);
      }

      public String toString() {
         return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + ", sends_telemetry_event=" + this.sendsTelemetryEvent + "}";
      }

      /** @deprecated Forge: use {@linkplain #fromJson(JsonObject, DeserializationContext, net.minecraftforge.common.crafting.conditions.ICondition.IContext) overload with context}. */
      @Deprecated
      public static Advancement.Builder fromJson(JsonObject pJson, DeserializationContext pContext) {
         return fromJson(pJson, pContext, net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
      }

      public static Advancement.Builder fromJson(JsonObject pJson, DeserializationContext pContext, net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
         if ((pJson = net.minecraftforge.common.crafting.ConditionalAdvancement.processConditional(pJson, context)) == null) return null;
         ResourceLocation resourcelocation = pJson.has("parent") ? new ResourceLocation(GsonHelper.getAsString(pJson, "parent")) : null;
         DisplayInfo displayinfo = pJson.has("display") ? DisplayInfo.fromJson(GsonHelper.getAsJsonObject(pJson, "display")) : null;
         AdvancementRewards advancementrewards = pJson.has("rewards") ? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(pJson, "rewards")) : AdvancementRewards.EMPTY;
         Map<String, Criterion> map = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(pJson, "criteria"), pContext);
         if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, "requirements", new JsonArray());
            String[][] astring = new String[jsonarray.size()][];

            for(int i = 0; i < jsonarray.size(); ++i) {
               JsonArray jsonarray1 = GsonHelper.convertToJsonArray(jsonarray.get(i), "requirements[" + i + "]");
               astring[i] = new String[jsonarray1.size()];

               for(int j = 0; j < jsonarray1.size(); ++j) {
                  astring[i][j] = GsonHelper.convertToString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
               }
            }

            if (astring.length == 0) {
               astring = new String[map.size()][];
               int k = 0;

               for(String s2 : map.keySet()) {
                  astring[k++] = new String[]{s2};
               }
            }

            for(String[] astring1 : astring) {
               if (astring1.length == 0 && map.isEmpty()) {
                  throw new JsonSyntaxException("Requirement entry cannot be empty");
               }

               for(String s : astring1) {
                  if (!map.containsKey(s)) {
                     throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
                  }
               }
            }

            for(String s1 : map.keySet()) {
               boolean flag1 = false;

               for(String[] astring2 : astring) {
                  if (ArrayUtils.contains(astring2, s1)) {
                     flag1 = true;
                     break;
                  }
               }

               if (!flag1) {
                  throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
               }
            }

            boolean flag = GsonHelper.getAsBoolean(pJson, "sends_telemetry_event", false);
            return new Advancement.Builder(resourcelocation, displayinfo, advancementrewards, map, astring, flag);
         }
      }

      public static Advancement.Builder fromNetwork(FriendlyByteBuf pBuffer) {
         ResourceLocation resourcelocation = pBuffer.readNullable(FriendlyByteBuf::readResourceLocation);
         DisplayInfo displayinfo = pBuffer.readNullable(DisplayInfo::fromNetwork);
         Map<String, Criterion> map = Criterion.criteriaFromNetwork(pBuffer);
         String[][] astring = new String[pBuffer.readVarInt()][];

         for(int i = 0; i < astring.length; ++i) {
            astring[i] = new String[pBuffer.readVarInt()];

            for(int j = 0; j < astring[i].length; ++j) {
               astring[i][j] = pBuffer.readUtf();
            }
         }

         boolean flag = pBuffer.readBoolean();
         return new Advancement.Builder(resourcelocation, displayinfo, AdvancementRewards.EMPTY, map, astring, flag);
      }

      public Map<String, Criterion> getCriteria() {
         return this.criteria;
      }
   }
}
