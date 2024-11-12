package net.minecraft.server.dedicated;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class DedicatedServerSettings {
   private final Path source;
   private DedicatedServerProperties properties;

   public DedicatedServerSettings(Path pSource) {
      this.source = pSource;
      this.properties = DedicatedServerProperties.fromFile(pSource);
   }

   public DedicatedServerProperties getProperties() {
      return this.properties;
   }

   public void forceSave() {
      this.properties.store(this.source);
   }

   public DedicatedServerSettings update(UnaryOperator<DedicatedServerProperties> pPropertiesOperator) {
      (this.properties = pPropertiesOperator.apply(this.properties)).store(this.source);
      return this;
   }
}