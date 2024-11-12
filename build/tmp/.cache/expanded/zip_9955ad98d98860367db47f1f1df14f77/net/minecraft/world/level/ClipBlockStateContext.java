package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ClipBlockStateContext {
   private final Vec3 from;
   private final Vec3 to;
   private final Predicate<BlockState> block;

   public ClipBlockStateContext(Vec3 pFrom, Vec3 pTo, Predicate<BlockState> pBlock) {
      this.from = pFrom;
      this.to = pTo;
      this.block = pBlock;
   }

   public Vec3 getTo() {
      return this.to;
   }

   public Vec3 getFrom() {
      return this.from;
   }

   public Predicate<BlockState> isTargetBlock() {
      return this.block;
   }
}