package com.mojang.blaze3d.platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public enum IconSet {
   RELEASE("icons"),
   SNAPSHOT("icons", "snapshot");

   private final String[] path;

   private IconSet(String... pPath) {
      this.path = pPath;
   }

   public List<IoSupplier<InputStream>> getStandardIcons(PackResources pResources) throws IOException {
      return List.of(this.getFile(pResources, "icon_16x16.png"), this.getFile(pResources, "icon_32x32.png"), this.getFile(pResources, "icon_48x48.png"), this.getFile(pResources, "icon_128x128.png"), this.getFile(pResources, "icon_256x256.png"));
   }

   public IoSupplier<InputStream> getMacIcon(PackResources pResources) throws IOException {
      return this.getFile(pResources, "minecraft.icns");
   }

   private IoSupplier<InputStream> getFile(PackResources pResources, String pFilename) throws IOException {
      String[] astring = ArrayUtils.add(this.path, pFilename);
      IoSupplier<InputStream> iosupplier = pResources.getRootResource(astring);
      if (iosupplier == null) {
         throw new FileNotFoundException(String.join("/", astring));
      } else {
         return iosupplier;
      }
   }
}