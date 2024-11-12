package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

public enum PackCompatibility {
   TOO_OLD("old"),
   TOO_NEW("new"),
   COMPATIBLE("compatible");

   private final Component description;
   private final Component confirmation;

   private PackCompatibility(String pType) {
      this.description = Component.translatable("pack.incompatible." + pType).withStyle(ChatFormatting.GRAY);
      this.confirmation = Component.translatable("pack.incompatible.confirm." + pType);
   }

   public boolean isCompatible() {
      return this == COMPATIBLE;
   }

   public static PackCompatibility forFormat(int pVersion, PackType pType) {
      int i = SharedConstants.getCurrentVersion().getPackVersion(pType);
      if (pVersion < i) {
         return TOO_OLD;
      } else {
         return pVersion > i ? TOO_NEW : COMPATIBLE;
      }
   }

   public Component getDescription() {
      return this.description;
   }

   public Component getConfirmation() {
      return this.confirmation;
   }
}