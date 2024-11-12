package net.minecraft.data.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetadataGenerator implements DataProvider {
   private final PackOutput output;
   private final Map<String, Supplier<JsonElement>> elements = new HashMap<>();

   public PackMetadataGenerator(PackOutput pOutput) {
      this.output = pOutput;
   }

   public <T> PackMetadataGenerator add(MetadataSectionType<T> pType, T pValue) {
      this.elements.put(pType.getMetadataSectionName(), () -> {
         return pType.toJson(pValue);
      });
      return this;
   }

   public CompletableFuture<?> run(CachedOutput pOutput) {
      JsonObject jsonobject = new JsonObject();
      this.elements.forEach((p_249290_, p_251317_) -> {
         jsonobject.add(p_249290_, p_251317_.get());
      });
      return DataProvider.saveStable(pOutput, jsonobject, this.output.getOutputFolder().resolve("pack.mcmeta"));
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public final String getName() {
      return "Pack Metadata";
   }

   public static PackMetadataGenerator forFeaturePack(PackOutput pOutput, Component pDescription) {
      return (new PackMetadataGenerator(pOutput)).add(PackMetadataSection.TYPE, new PackMetadataSection(pDescription, DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA)));
   }

   public static PackMetadataGenerator forFeaturePack(PackOutput pOutput, Component pDescription, FeatureFlagSet pFlags) {
      return forFeaturePack(pOutput, pDescription).add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(pFlags));
   }
}