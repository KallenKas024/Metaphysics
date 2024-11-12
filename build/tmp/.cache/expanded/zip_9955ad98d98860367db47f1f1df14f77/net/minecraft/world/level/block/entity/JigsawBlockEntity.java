package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class JigsawBlockEntity extends BlockEntity {
   public static final String TARGET = "target";
   public static final String POOL = "pool";
   public static final String JOINT = "joint";
   public static final String NAME = "name";
   public static final String FINAL_STATE = "final_state";
   private ResourceLocation name = new ResourceLocation("empty");
   private ResourceLocation target = new ResourceLocation("empty");
   private ResourceKey<StructureTemplatePool> pool = ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation("empty"));
   private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
   private String finalState = "minecraft:air";

   public JigsawBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.JIGSAW, pPos, pBlockState);
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public ResourceLocation getTarget() {
      return this.target;
   }

   public ResourceKey<StructureTemplatePool> getPool() {
      return this.pool;
   }

   public String getFinalState() {
      return this.finalState;
   }

   public JigsawBlockEntity.JointType getJoint() {
      return this.joint;
   }

   public void setName(ResourceLocation pName) {
      this.name = pName;
   }

   public void setTarget(ResourceLocation pTarget) {
      this.target = pTarget;
   }

   public void setPool(ResourceKey<StructureTemplatePool> pPool) {
      this.pool = pPool;
   }

   public void setFinalState(String pFinalState) {
      this.finalState = pFinalState;
   }

   public void setJoint(JigsawBlockEntity.JointType pJoint) {
      this.joint = pJoint;
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putString("name", this.name.toString());
      pTag.putString("target", this.target.toString());
      pTag.putString("pool", this.pool.location().toString());
      pTag.putString("final_state", this.finalState);
      pTag.putString("joint", this.joint.getSerializedName());
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.name = new ResourceLocation(pTag.getString("name"));
      this.target = new ResourceLocation(pTag.getString("target"));
      this.pool = ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(pTag.getString("pool")));
      this.finalState = pTag.getString("final_state");
      this.joint = JigsawBlockEntity.JointType.byName(pTag.getString("joint")).orElseGet(() -> {
         return JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE;
      });
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public void generate(ServerLevel pLevel, int pMaxDepth, boolean pKeepJigsaws) {
      BlockPos blockpos = this.getBlockPos().relative(this.getBlockState().getValue(JigsawBlock.ORIENTATION).front());
      Registry<StructureTemplatePool> registry = pLevel.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
      Holder<StructureTemplatePool> holder = registry.getHolderOrThrow(this.pool);
      JigsawPlacement.generateJigsaw(pLevel, holder, this.target, pMaxDepth, blockpos, pKeepJigsaws);
   }

   public static enum JointType implements StringRepresentable {
      ROLLABLE("rollable"),
      ALIGNED("aligned");

      private final String name;

      private JointType(String pName) {
         this.name = pName;
      }

      public String getSerializedName() {
         return this.name;
      }

      public static Optional<JigsawBlockEntity.JointType> byName(String pName) {
         return Arrays.stream(values()).filter((p_59461_) -> {
            return p_59461_.getSerializedName().equals(pName);
         }).findFirst();
      }

      public Component getTranslatedName() {
         return Component.translatable("jigsaw_block.joint." + this.name);
      }
   }
}