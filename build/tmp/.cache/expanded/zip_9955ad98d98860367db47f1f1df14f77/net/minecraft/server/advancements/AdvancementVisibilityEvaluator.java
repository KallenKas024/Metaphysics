package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
   private static final int VISIBILITY_DEPTH = 2;

   private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement pAdvancement, boolean pAlwaysShow) {
      DisplayInfo displayinfo = pAdvancement.getDisplay();
      if (displayinfo == null) {
         return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
      } else if (pAlwaysShow) {
         return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
      } else {
         return displayinfo.isHidden() ? AdvancementVisibilityEvaluator.VisibilityRule.HIDE : AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
      }
   }

   private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.VisibilityRule> pVisibilityRules) {
      for(int i = 0; i <= 2; ++i) {
         AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator$visibilityrule = pVisibilityRules.peek(i);
         if (advancementvisibilityevaluator$visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.SHOW) {
            return true;
         }

         if (advancementvisibilityevaluator$visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.HIDE) {
            return false;
         }
      }

      return false;
   }

   private static boolean evaluateVisibility(Advancement pAdvancement, Stack<AdvancementVisibilityEvaluator.VisibilityRule> pVisibilityRules, Predicate<Advancement> pPredicate, AdvancementVisibilityEvaluator.Output pOutput) {
      boolean flag = pPredicate.test(pAdvancement);
      AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator$visibilityrule = evaluateVisibilityRule(pAdvancement, flag);
      boolean flag1 = flag;
      pVisibilityRules.push(advancementvisibilityevaluator$visibilityrule);

      for(Advancement advancement : pAdvancement.getChildren()) {
         flag1 |= evaluateVisibility(advancement, pVisibilityRules, pPredicate, pOutput);
      }

      boolean flag2 = flag1 || evaluateVisiblityForUnfinishedNode(pVisibilityRules);
      pVisibilityRules.pop();
      pOutput.accept(pAdvancement, flag2);
      return flag1;
   }

   public static void evaluateVisibility(Advancement pAdvancement, Predicate<Advancement> pPredicate, AdvancementVisibilityEvaluator.Output pOutput) {
      Advancement advancement = pAdvancement.getRoot();
      Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

      for(int i = 0; i <= 2; ++i) {
         stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
      }

      evaluateVisibility(advancement, stack, pPredicate, pOutput);
   }

   public static boolean isVisible(Advancement advancement, Predicate<Advancement> test) {
      Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

      for(int i = 0; i <= 2; ++i) {
         stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
      }
      return evaluateVisibility(advancement.getRoot(), stack, test, (pAdvancement, pVisible) -> {});
   }

   @FunctionalInterface
   public interface Output {
      void accept(Advancement pAdvancement, boolean pVisible);
   }

   static enum VisibilityRule {
      SHOW,
      HIDE,
      NO_CHANGE;
   }
}
