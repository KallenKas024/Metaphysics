package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CycleButton<T> extends AbstractButton {
   public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
   private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
   private final Component name;
   private int index;
   private T value;
   private final CycleButton.ValueListSupplier<T> values;
   private final Function<T, Component> valueStringifier;
   private final Function<CycleButton<T>, MutableComponent> narrationProvider;
   private final CycleButton.OnValueChange<T> onValueChange;
   private final boolean displayOnlyValue;
   private final OptionInstance.TooltipSupplier<T> tooltipSupplier;

   CycleButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Component pName, int pIndex, T pValue, CycleButton.ValueListSupplier<T> pValues, Function<T, Component> pValueStringifier, Function<CycleButton<T>, MutableComponent> pNarrationProvider, CycleButton.OnValueChange<T> pOnValueChange, OptionInstance.TooltipSupplier<T> pTooltipSupplier, boolean pDisplayOnlyValue) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.name = pName;
      this.index = pIndex;
      this.value = pValue;
      this.values = pValues;
      this.valueStringifier = pValueStringifier;
      this.narrationProvider = pNarrationProvider;
      this.onValueChange = pOnValueChange;
      this.displayOnlyValue = pDisplayOnlyValue;
      this.tooltipSupplier = pTooltipSupplier;
      this.updateTooltip();
   }

   private void updateTooltip() {
      this.setTooltip(this.tooltipSupplier.apply(this.value));
   }

   public void onPress() {
      if (Screen.hasShiftDown()) {
         this.cycleValue(-1);
      } else {
         this.cycleValue(1);
      }

   }

   private void cycleValue(int pDelta) {
      List<T> list = this.values.getSelectedList();
      this.index = Mth.positiveModulo(this.index + pDelta, list.size());
      T t = list.get(this.index);
      this.updateValue(t);
      this.onValueChange.onValueChange(this, t);
   }

   private T getCycledValue(int pDelta) {
      List<T> list = this.values.getSelectedList();
      return list.get(Mth.positiveModulo(this.index + pDelta, list.size()));
   }

   /**
    * Called when the mouse wheel is scrolled within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pDelta the scrolling delta.
    */
   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      if (pDelta > 0.0D) {
         this.cycleValue(-1);
      } else if (pDelta < 0.0D) {
         this.cycleValue(1);
      }

      return true;
   }

   public void setValue(T pValue) {
      List<T> list = this.values.getSelectedList();
      int i = list.indexOf(pValue);
      if (i != -1) {
         this.index = i;
      }

      this.updateValue(pValue);
   }

   private void updateValue(T pValue) {
      Component component = this.createLabelForValue(pValue);
      this.setMessage(component);
      this.value = pValue;
      this.updateTooltip();
   }

   private Component createLabelForValue(T pValue) {
      return (Component)(this.displayOnlyValue ? this.valueStringifier.apply(pValue) : this.createFullName(pValue));
   }

   private MutableComponent createFullName(T pValue) {
      return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(pValue));
   }

   public T getValue() {
      return this.value;
   }

   protected MutableComponent createNarrationMessage() {
      return this.narrationProvider.apply(this);
   }

   public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
      if (this.active) {
         T t = this.getCycledValue(1);
         Component component = this.createLabelForValue(t);
         if (this.isFocused()) {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", component));
         } else {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", component));
         }
      }

   }

   public MutableComponent createDefaultNarrationMessage() {
      return wrapDefaultNarrationMessage((Component)(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
   }

   public static <T> CycleButton.Builder<T> builder(Function<T, Component> pValueStringifier) {
      return new CycleButton.Builder<>(pValueStringifier);
   }

   public static CycleButton.Builder<Boolean> booleanBuilder(Component pComponentOn, Component pComponentOff) {
      return (new CycleButton.Builder<Boolean>((p_168902_) -> {
         return p_168902_ ? pComponentOn : pComponentOff;
      })).withValues(BOOLEAN_OPTIONS);
   }

   public static CycleButton.Builder<Boolean> onOffBuilder() {
      return (new CycleButton.Builder<Boolean>((p_168891_) -> {
         return p_168891_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
      })).withValues(BOOLEAN_OPTIONS);
   }

   public static CycleButton.Builder<Boolean> onOffBuilder(boolean pInitialValue) {
      return onOffBuilder().withInitialValue(pInitialValue);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Builder<T> {
      private int initialIndex;
      @Nullable
      private T initialValue;
      private final Function<T, Component> valueStringifier;
      private OptionInstance.TooltipSupplier<T> tooltipSupplier = (p_168964_) -> {
         return null;
      };
      private Function<CycleButton<T>, MutableComponent> narrationProvider = CycleButton::createDefaultNarrationMessage;
      private CycleButton.ValueListSupplier<T> values = CycleButton.ValueListSupplier.create(ImmutableList.of());
      private boolean displayOnlyValue;

      public Builder(Function<T, Component> pValueStringifier) {
         this.valueStringifier = pValueStringifier;
      }

      public CycleButton.Builder<T> withValues(Collection<T> pValues) {
         return this.withValues(CycleButton.ValueListSupplier.create(pValues));
      }

      @SafeVarargs
      public final CycleButton.Builder<T> withValues(T... pValues) {
         return this.withValues(ImmutableList.copyOf(pValues));
      }

      public CycleButton.Builder<T> withValues(List<T> pDefaultList, List<T> pSelectedList) {
         return this.withValues(CycleButton.ValueListSupplier.create(CycleButton.DEFAULT_ALT_LIST_SELECTOR, pDefaultList, pSelectedList));
      }

      public CycleButton.Builder<T> withValues(BooleanSupplier pAltListSelector, List<T> pDefaultList, List<T> pSelectedList) {
         return this.withValues(CycleButton.ValueListSupplier.create(pAltListSelector, pDefaultList, pSelectedList));
      }

      public CycleButton.Builder<T> withValues(CycleButton.ValueListSupplier<T> pValues) {
         this.values = pValues;
         return this;
      }

      public CycleButton.Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> pTooltipSupplier) {
         this.tooltipSupplier = pTooltipSupplier;
         return this;
      }

      public CycleButton.Builder<T> withInitialValue(T pInitialValue) {
         this.initialValue = pInitialValue;
         int i = this.values.getDefaultList().indexOf(pInitialValue);
         if (i != -1) {
            this.initialIndex = i;
         }

         return this;
      }

      public CycleButton.Builder<T> withCustomNarration(Function<CycleButton<T>, MutableComponent> pNarrationProvider) {
         this.narrationProvider = pNarrationProvider;
         return this;
      }

      public CycleButton.Builder<T> displayOnlyValue() {
         this.displayOnlyValue = true;
         return this;
      }

      public CycleButton<T> create(int pX, int pY, int pWidth, int pHeight, Component pName) {
         return this.create(pX, pY, pWidth, pHeight, pName, (p_168946_, p_168947_) -> {
         });
      }

      public CycleButton<T> create(int pX, int pY, int pWidth, int pHeight, Component pName, CycleButton.OnValueChange<T> pOnValueChange) {
         List<T> list = this.values.getDefaultList();
         if (list.isEmpty()) {
            throw new IllegalStateException("No values for cycle button");
         } else {
            T t = (T)(this.initialValue != null ? this.initialValue : list.get(this.initialIndex));
            Component component = this.valueStringifier.apply(t);
            Component component1 = (Component)(this.displayOnlyValue ? component : CommonComponents.optionNameValue(pName, component));
            return new CycleButton<>(pX, pY, pWidth, pHeight, component1, pName, this.initialIndex, t, this.values, this.valueStringifier, this.narrationProvider, pOnValueChange, this.tooltipSupplier, this.displayOnlyValue);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public interface OnValueChange<T> {
      void onValueChange(CycleButton<T> pCycleButton, T pValue);
   }

   @OnlyIn(Dist.CLIENT)
   public interface ValueListSupplier<T> {
      List<T> getSelectedList();

      List<T> getDefaultList();

      static <T> CycleButton.ValueListSupplier<T> create(Collection<T> pValues) {
         final List<T> list = ImmutableList.copyOf(pValues);
         return new CycleButton.ValueListSupplier<T>() {
            public List<T> getSelectedList() {
               return list;
            }

            public List<T> getDefaultList() {
               return list;
            }
         };
      }

      static <T> CycleButton.ValueListSupplier<T> create(final BooleanSupplier pAltListSelector, List<T> pDefaultList, List<T> pSelectedList) {
         final List<T> list = ImmutableList.copyOf(pDefaultList);
         final List<T> list1 = ImmutableList.copyOf(pSelectedList);
         return new CycleButton.ValueListSupplier<T>() {
            public List<T> getSelectedList() {
               return pAltListSelector.getAsBoolean() ? list1 : list;
            }

            public List<T> getDefaultList() {
               return list;
            }
         };
      }
   }
}