package com.mojang.realmsclient.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ErrorCallback {
   void error(Component pError);

   default void error(String pError) {
      this.error(Component.literal(pError));
   }
}