package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PartDefinition {
   private final List<CubeDefinition> cubes;
   private final PartPose partPose;
   private final Map<String, PartDefinition> children = Maps.newHashMap();

   PartDefinition(List<CubeDefinition> pCubes, PartPose pPartPose) {
      this.cubes = pCubes;
      this.partPose = pPartPose;
   }

   public PartDefinition addOrReplaceChild(String pName, CubeListBuilder pCubes, PartPose pPartPose) {
      PartDefinition partdefinition = new PartDefinition(pCubes.getCubes(), pPartPose);
      PartDefinition partdefinition1 = this.children.put(pName, partdefinition);
      if (partdefinition1 != null) {
         partdefinition.children.putAll(partdefinition1.children);
      }

      return partdefinition;
   }

   public ModelPart bake(int pTexWidth, int pTexHeight) {
      Object2ObjectArrayMap<String, ModelPart> object2objectarraymap = this.children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (p_171593_) -> {
         return p_171593_.getValue().bake(pTexWidth, pTexHeight);
      }, (p_171595_, p_171596_) -> {
         return p_171595_;
      }, Object2ObjectArrayMap::new));
      List<ModelPart.Cube> list = this.cubes.stream().map((p_171589_) -> {
         return p_171589_.bake(pTexWidth, pTexHeight);
      }).collect(ImmutableList.toImmutableList());
      ModelPart modelpart = new ModelPart(list, object2objectarraymap);
      modelpart.setInitialPose(this.partPose);
      modelpart.loadPose(this.partPose);
      return modelpart;
   }

   public PartDefinition getChild(String pName) {
      return this.children.get(pName);
   }
}