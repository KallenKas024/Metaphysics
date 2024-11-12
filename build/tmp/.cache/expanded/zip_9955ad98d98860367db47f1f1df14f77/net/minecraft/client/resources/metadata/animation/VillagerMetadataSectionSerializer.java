package net.minecraft.client.resources.metadata.animation;

import com.google.gson.JsonObject;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerMetadataSectionSerializer implements MetadataSectionSerializer<VillagerMetaDataSection> {
   public VillagerMetaDataSection fromJson(JsonObject pJson) {
      return new VillagerMetaDataSection(VillagerMetaDataSection.Hat.getByName(GsonHelper.getAsString(pJson, "hat", "none")));
   }

   /**
    * The name of this section type as it appears in JSON.
    */
   public String getMetadataSectionName() {
      return "villager";
   }
}