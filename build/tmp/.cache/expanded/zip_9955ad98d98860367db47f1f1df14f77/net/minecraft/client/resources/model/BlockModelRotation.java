package net.minecraft.client.resources.model;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public enum BlockModelRotation implements ModelState {
   X0_Y0(0, 0),
   X0_Y90(0, 90),
   X0_Y180(0, 180),
   X0_Y270(0, 270),
   X90_Y0(90, 0),
   X90_Y90(90, 90),
   X90_Y180(90, 180),
   X90_Y270(90, 270),
   X180_Y0(180, 0),
   X180_Y90(180, 90),
   X180_Y180(180, 180),
   X180_Y270(180, 270),
   X270_Y0(270, 0),
   X270_Y90(270, 90),
   X270_Y180(270, 180),
   X270_Y270(270, 270);

   private static final int DEGREES = 360;
   private static final Map<Integer, BlockModelRotation> BY_INDEX = Arrays.stream(values()).collect(Collectors.toMap((p_119163_) -> {
      return p_119163_.index;
   }, (p_119157_) -> {
      return p_119157_;
   }));
   private final Transformation transformation;
   private final OctahedralGroup actualRotation;
   private final int index;

   private static int getIndex(int pX, int pY) {
      return pX * 360 + pY;
   }

   private BlockModelRotation(int pX, int pY) {
      this.index = getIndex(pX, pY);
      Quaternionf quaternionf = (new Quaternionf()).rotateYXZ((float)(-pY) * ((float)Math.PI / 180F), (float)(-pX) * ((float)Math.PI / 180F), 0.0F);
      OctahedralGroup octahedralgroup = OctahedralGroup.IDENTITY;

      for(int i = 0; i < pY; i += 90) {
         octahedralgroup = octahedralgroup.compose(OctahedralGroup.ROT_90_Y_NEG);
      }

      for(int j = 0; j < pX; j += 90) {
         octahedralgroup = octahedralgroup.compose(OctahedralGroup.ROT_90_X_NEG);
      }

      this.transformation = new Transformation((Vector3f)null, quaternionf, (Vector3f)null, (Quaternionf)null);
      this.actualRotation = octahedralgroup;
   }

   public Transformation getRotation() {
      return this.transformation;
   }

   public static BlockModelRotation by(int pX, int pY) {
      return BY_INDEX.get(getIndex(Mth.positiveModulo(pX, 360), Mth.positiveModulo(pY, 360)));
   }

   public OctahedralGroup actualRotation() {
      return this.actualRotation;
   }
}