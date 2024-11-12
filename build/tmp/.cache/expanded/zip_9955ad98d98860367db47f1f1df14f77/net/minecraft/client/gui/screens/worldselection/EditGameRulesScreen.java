package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditGameRulesScreen extends Screen {
   private final Consumer<Optional<GameRules>> exitCallback;
   private EditGameRulesScreen.RuleList rules;
   private final Set<EditGameRulesScreen.RuleEntry> invalidEntries = Sets.newHashSet();
   private Button doneButton;
   @Nullable
   private List<FormattedCharSequence> tooltip;
   private final GameRules gameRules;

   public EditGameRulesScreen(GameRules pGameRules, Consumer<Optional<GameRules>> pExitCallback) {
      super(Component.translatable("editGamerule.title"));
      this.gameRules = pGameRules;
      this.exitCallback = pExitCallback;
   }

   protected void init() {
      this.rules = new EditGameRulesScreen.RuleList(this.gameRules);
      this.addWidget(this.rules);
      GridLayout.RowHelper gridlayout$rowhelper = (new GridLayout()).columnSpacing(10).createRowHelper(2);
      this.doneButton = gridlayout$rowhelper.addChild(Button.builder(CommonComponents.GUI_DONE, (p_101059_) -> {
         this.exitCallback.accept(Optional.of(this.gameRules));
      }).build());
      gridlayout$rowhelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, (p_101073_) -> {
         this.exitCallback.accept(Optional.empty());
      }).build());
      gridlayout$rowhelper.getGrid().visitWidgets((p_267855_) -> {
         AbstractWidget abstractwidget = this.addRenderableWidget(p_267855_);
      });
      gridlayout$rowhelper.getGrid().setPosition(this.width / 2 - 155, this.height - 28);
      gridlayout$rowhelper.getGrid().arrangeElements();
   }

   public void onClose() {
      this.exitCallback.accept(Optional.empty());
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.tooltip = null;
      this.rules.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   private void updateDoneButton() {
      this.doneButton.active = this.invalidEntries.isEmpty();
   }

   void markInvalid(EditGameRulesScreen.RuleEntry pRuleEntry) {
      this.invalidEntries.add(pRuleEntry);
      this.updateDoneButton();
   }

   void clearInvalid(EditGameRulesScreen.RuleEntry pRuleEntry) {
      this.invalidEntries.remove(pRuleEntry);
      this.updateDoneButton();
   }

   @OnlyIn(Dist.CLIENT)
   public class BooleanRuleEntry extends EditGameRulesScreen.GameRuleEntry {
      private final CycleButton<Boolean> checkbox;

      public BooleanRuleEntry(Component pLabel, List<FormattedCharSequence> pTooltip, String p_101103_, GameRules.BooleanValue pValue) {
         super(pTooltip, pLabel);
         this.checkbox = CycleButton.onOffBuilder(pValue.get()).displayOnlyValue().withCustomNarration((p_170219_) -> {
            return p_170219_.createDefaultNarrationMessage().append("\n").append(p_101103_);
         }).create(10, 5, 44, 20, pLabel, (p_170215_, p_170216_) -> {
            pValue.set(p_170216_, (MinecraftServer)null);
         });
         this.children.add(this.checkbox);
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.renderLabel(pGuiGraphics, pTop, pLeft);
         this.checkbox.setX(pLeft + pWidth - 45);
         this.checkbox.setY(pTop);
         this.checkbox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public class CategoryRuleEntry extends EditGameRulesScreen.RuleEntry {
      final Component label;

      public CategoryRuleEntry(Component pLabel) {
         super((List<FormattedCharSequence>)null);
         this.label = pLabel;
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         pGuiGraphics.drawCenteredString(EditGameRulesScreen.this.minecraft.font, this.label, pLeft + pWidth / 2, pTop + 5, 16777215);
      }

      /**
       * {@return a List containing all GUI element children of this GUI element}
       */
      public List<? extends GuiEventListener> children() {
         return ImmutableList.of();
      }

      public List<? extends NarratableEntry> narratables() {
         return ImmutableList.of(new NarratableEntry() {
            /**
             * {@return the narration priority}
             */
            public NarratableEntry.NarrationPriority narrationPriority() {
               return NarratableEntry.NarrationPriority.HOVERED;
            }

            /**
             * Updates the narration output with the current narration information.
             * @param pNarrationElementOutput the output to update with narration information.
             */
            public void updateNarration(NarrationElementOutput p_170225_) {
               p_170225_.add(NarratedElementType.TITLE, CategoryRuleEntry.this.label);
            }
         });
      }
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   interface EntryFactory<T extends GameRules.Value<T>> {
      EditGameRulesScreen.RuleEntry create(Component pLabel, List<FormattedCharSequence> pTooltip, String p_101157_, T p_101158_);
   }

   @OnlyIn(Dist.CLIENT)
   public abstract class GameRuleEntry extends EditGameRulesScreen.RuleEntry {
      private final List<FormattedCharSequence> label;
      protected final List<AbstractWidget> children = Lists.newArrayList();

      public GameRuleEntry(@Nullable List<FormattedCharSequence> pTooltip, Component pLabel) {
         super(pTooltip);
         this.label = EditGameRulesScreen.this.minecraft.font.split(pLabel, 175);
      }

      /**
       * {@return a List containing all GUI element children of this GUI element}
       */
      public List<? extends GuiEventListener> children() {
         return this.children;
      }

      public List<? extends NarratableEntry> narratables() {
         return this.children;
      }

      protected void renderLabel(GuiGraphics pGuiGraphics, int pX, int pY) {
         if (this.label.size() == 1) {
            pGuiGraphics.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(0), pY, pX + 5, 16777215, false);
         } else if (this.label.size() >= 2) {
            pGuiGraphics.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(0), pY, pX, 16777215, false);
            pGuiGraphics.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(1), pY, pX + 10, 16777215, false);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public class IntegerRuleEntry extends EditGameRulesScreen.GameRuleEntry {
      private final EditBox input;

      public IntegerRuleEntry(Component pLabel, List<FormattedCharSequence> pTooltip, String p_101177_, GameRules.IntegerValue pValue) {
         super(pTooltip, pLabel);
         this.input = new EditBox(EditGameRulesScreen.this.minecraft.font, 10, 5, 42, 20, pLabel.copy().append("\n").append(p_101177_).append("\n"));
         this.input.setValue(Integer.toString(pValue.get()));
         this.input.setResponder((p_101181_) -> {
            if (pValue.tryDeserialize(p_101181_)) {
               this.input.setTextColor(14737632);
               EditGameRulesScreen.this.clearInvalid(this);
            } else {
               this.input.setTextColor(16711680);
               EditGameRulesScreen.this.markInvalid(this);
            }

         });
         this.children.add(this.input);
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.renderLabel(pGuiGraphics, pTop, pLeft);
         this.input.setX(pLeft + pWidth - 44);
         this.input.setY(pTop);
         this.input.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public abstract static class RuleEntry extends ContainerObjectSelectionList.Entry<EditGameRulesScreen.RuleEntry> {
      @Nullable
      final List<FormattedCharSequence> tooltip;

      public RuleEntry(@Nullable List<FormattedCharSequence> pTooltip) {
         this.tooltip = pTooltip;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public class RuleList extends ContainerObjectSelectionList<EditGameRulesScreen.RuleEntry> {
      public RuleList(final GameRules pGameRules) {
         super(EditGameRulesScreen.this.minecraft, EditGameRulesScreen.this.width, EditGameRulesScreen.this.height, 43, EditGameRulesScreen.this.height - 32, 24);
         final Map<GameRules.Category, Map<GameRules.Key<?>, EditGameRulesScreen.RuleEntry>> map = Maps.newHashMap();
         GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            public void visitBoolean(GameRules.Key<GameRules.BooleanValue> p_101238_, GameRules.Type<GameRules.BooleanValue> p_101239_) {
               this.addEntry(p_101238_, (p_101228_, p_101229_, p_101230_, p_101231_) -> {
                  return EditGameRulesScreen.this.new BooleanRuleEntry(p_101228_, p_101229_, p_101230_, p_101231_);
               });
            }

            public void visitInteger(GameRules.Key<GameRules.IntegerValue> p_101241_, GameRules.Type<GameRules.IntegerValue> p_101242_) {
               this.addEntry(p_101241_, (p_101233_, p_101234_, p_101235_, p_101236_) -> {
                  return EditGameRulesScreen.this.new IntegerRuleEntry(p_101233_, p_101234_, p_101235_, p_101236_);
               });
            }

            private <T extends GameRules.Value<T>> void addEntry(GameRules.Key<T> p_101225_, EditGameRulesScreen.EntryFactory<T> p_101226_) {
               Component component = Component.translatable(p_101225_.getDescriptionId());
               Component component1 = Component.literal(p_101225_.getId()).withStyle(ChatFormatting.YELLOW);
               T t = pGameRules.getRule(p_101225_);
               String s = t.serialize();
               Component component2 = Component.translatable("editGamerule.default", Component.literal(s)).withStyle(ChatFormatting.GRAY);
               String s1 = p_101225_.getDescriptionId() + ".description";
               List<FormattedCharSequence> list;
               String s2;
               if (I18n.exists(s1)) {
                  ImmutableList.Builder<FormattedCharSequence> builder = ImmutableList.<FormattedCharSequence>builder().add(component1.getVisualOrderText());
                  Component component3 = Component.translatable(s1);
                  EditGameRulesScreen.this.font.split(component3, 150).forEach(builder::add);
                  list = builder.add(component2.getVisualOrderText()).build();
                  s2 = component3.getString() + "\n" + component2.getString();
               } else {
                  list = ImmutableList.of(component1.getVisualOrderText(), component2.getVisualOrderText());
                  s2 = component2.getString();
               }

               map.computeIfAbsent(p_101225_.getCategory(), (p_101223_) -> {
                  return Maps.newHashMap();
               }).put(p_101225_, p_101226_.create(component, list, s2, t));
            }
         });
         map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((p_101210_) -> {
            this.addEntry(EditGameRulesScreen.this.new CategoryRuleEntry(Component.translatable(p_101210_.getKey().getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            p_101210_.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(GameRules.Key::getId))).forEach((p_170229_) -> {
               this.addEntry(p_170229_.getValue());
            });
         });
      }

      /**
       * Renders the graphical user interface (GUI) element.
       * @param pGuiGraphics the GuiGraphics object used for rendering.
       * @param pMouseX the x-coordinate of the mouse cursor.
       * @param pMouseY the y-coordinate of the mouse cursor.
       * @param pPartialTick the partial tick time.
       */
      public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
         super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         EditGameRulesScreen.RuleEntry editgamerulesscreen$ruleentry = this.getHovered();
         if (editgamerulesscreen$ruleentry != null && editgamerulesscreen$ruleentry.tooltip != null) {
            EditGameRulesScreen.this.setTooltipForNextRenderPass(editgamerulesscreen$ruleentry.tooltip);
         }

      }
   }
}