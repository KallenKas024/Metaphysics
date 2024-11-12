package net.minecraft.util;

import net.minecraft.network.chat.Component;

public interface ProgressListener {
   void progressStartNoAbort(Component pComponent);

   void progressStart(Component pHeader);

   void progressStage(Component pStage);

   /**
    * Updates the progress bar on the loading screen to the specified amount.
    */
   void progressStagePercentage(int pProgress);

   void stop();
}