package net.minecraft.server.packs.metadata.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public class PackMetadataSection {
   public static final MetadataSectionType<PackMetadataSection> TYPE = new PackMetadataSectionSerializer();
   private final Component description;
   private final int packFormat;
   private final java.util.Map<net.minecraft.server.packs.PackType, Integer> packTypeVersions;

   public PackMetadataSection(Component pDescription, int pPackFormat) {
      this.description = pDescription;
      this.packFormat = pPackFormat;
      this.packTypeVersions = java.util.Map.of();
   }
   public PackMetadataSection(Component pDescription, int pPackFormat, java.util.Map<net.minecraft.server.packs.PackType, Integer> packTypeVersions) {
      this.description = pDescription;
      this.packFormat = pPackFormat;
      this.packTypeVersions = packTypeVersions;
   }

   public Component getDescription() {
      return this.description;
   }

   /** @deprecated Forge: Use {@link #getPackFormat(net.minecraft.server.packs.PackType)} instead.*/
   @Deprecated
   public int getPackFormat() {
      return this.packFormat;
   }
   public int getPackFormat(net.minecraft.server.packs.PackType packType) {
      return packTypeVersions.getOrDefault(packType, this.packFormat);
   }
}
