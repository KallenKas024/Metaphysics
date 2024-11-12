package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerDefinition {
   private final MeshDefinition mesh;
   private final MaterialDefinition material;

   private LayerDefinition(MeshDefinition pMesh, MaterialDefinition pMaterial) {
      this.mesh = pMesh;
      this.material = pMaterial;
   }

   public ModelPart bakeRoot() {
      return this.mesh.getRoot().bake(this.material.xTexSize, this.material.yTexSize);
   }

   public static LayerDefinition create(MeshDefinition pMesh, int pTexWidth, int pTexHeight) {
      return new LayerDefinition(pMesh, new MaterialDefinition(pTexWidth, pTexHeight));
   }
}