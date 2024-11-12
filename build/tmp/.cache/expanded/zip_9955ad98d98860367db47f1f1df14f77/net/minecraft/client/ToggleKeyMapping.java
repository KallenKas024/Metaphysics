package net.minecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.BooleanSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToggleKeyMapping extends KeyMapping {
   private final BooleanSupplier needsToggle;

   public ToggleKeyMapping(String pName, int pKeyCode, String pCategory, BooleanSupplier pNeedsToggle) {
      super(pName, InputConstants.Type.KEYSYM, pKeyCode, pCategory);
      this.needsToggle = pNeedsToggle;
   }

   public void setDown(boolean pValue) {
      if (this.needsToggle.getAsBoolean()) {
         if (pValue && isConflictContextAndModifierActive()) {
            super.setDown(!this.isDown());
         }
      } else {
         super.setDown(pValue);
      }

   }
   @Override public boolean isDown() { return this.isDown && (isConflictContextAndModifierActive() || needsToggle.getAsBoolean()); }

   protected void reset() {
      super.setDown(false);
   }
}
