package net.minecraft.client.renderer.block.model;

import com.mojang.math.Transformation;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class FaceBakery {
   public static final int VERTEX_INT_SIZE = 8;
   private static final float RESCALE_22_5 = 1.0F / (float)Math.cos((double)((float)Math.PI / 8F)) - 1.0F;
   private static final float RESCALE_45 = 1.0F / (float)Math.cos((double)((float)Math.PI / 4F)) - 1.0F;
   public static final int VERTEX_COUNT = 4;
   private static final int COLOR_INDEX = 3;
   public static final int UV_INDEX = 4;

   public BakedQuad bakeQuad(Vector3f pPosFrom, Vector3f pPosTo, BlockElementFace pFace, TextureAtlasSprite pSprite, Direction pFacing, ModelState pTransform, @Nullable BlockElementRotation pPartRotation, boolean pShade, ResourceLocation pModelLocation) {
      BlockFaceUV blockfaceuv = pFace.uv;
      if (pTransform.isUvLocked()) {
         blockfaceuv = recomputeUVs(pFace.uv, pFacing, pTransform.getRotation(), pModelLocation);
      }

      float[] afloat = new float[blockfaceuv.uvs.length];
      System.arraycopy(blockfaceuv.uvs, 0, afloat, 0, afloat.length);
      float f = pSprite.uvShrinkRatio();
      float f1 = (blockfaceuv.uvs[0] + blockfaceuv.uvs[0] + blockfaceuv.uvs[2] + blockfaceuv.uvs[2]) / 4.0F;
      float f2 = (blockfaceuv.uvs[1] + blockfaceuv.uvs[1] + blockfaceuv.uvs[3] + blockfaceuv.uvs[3]) / 4.0F;
      blockfaceuv.uvs[0] = Mth.lerp(f, blockfaceuv.uvs[0], f1);
      blockfaceuv.uvs[2] = Mth.lerp(f, blockfaceuv.uvs[2], f1);
      blockfaceuv.uvs[1] = Mth.lerp(f, blockfaceuv.uvs[1], f2);
      blockfaceuv.uvs[3] = Mth.lerp(f, blockfaceuv.uvs[3], f2);
      int[] aint = this.makeVertices(blockfaceuv, pSprite, pFacing, this.setupShape(pPosFrom, pPosTo), pTransform.getRotation(), pPartRotation, pShade);
      Direction direction = calculateFacing(aint);
      System.arraycopy(afloat, 0, blockfaceuv.uvs, 0, afloat.length);
      if (pPartRotation == null) {
         this.recalculateWinding(aint, direction);
      }

      net.minecraftforge.client.ForgeHooksClient.fillNormal(aint, direction, pFace.getFaceData().calculateNormals());
      var data = pFace.getFaceData();
      var quad = new BakedQuad(aint, pFace.tintIndex, direction, pSprite, pShade, data.ambientOcclusion());
      if (!net.minecraftforge.client.model.ForgeFaceData.DEFAULT.equals(data)) {
         net.minecraftforge.client.model.QuadTransformers.applyingLightmap(data.blockLight(), data.skyLight()).processInPlace(quad);
         net.minecraftforge.client.model.QuadTransformers.applyingColor(data.color()).processInPlace(quad);
      }
      return quad;
   }

   public static BlockFaceUV recomputeUVs(BlockFaceUV pUv, Direction pFacing, Transformation pModelRotation, ResourceLocation pModelLocation) {
      Matrix4f matrix4f = BlockMath.getUVLockTransform(pModelRotation, pFacing, () -> {
         return "Unable to resolve UVLock for model: " + pModelLocation;
      }).getMatrix();
      float f = pUv.getU(pUv.getReverseIndex(0));
      float f1 = pUv.getV(pUv.getReverseIndex(0));
      Vector4f vector4f = matrix4f.transform(new Vector4f(f / 16.0F, f1 / 16.0F, 0.0F, 1.0F));
      float f2 = 16.0F * vector4f.x();
      float f3 = 16.0F * vector4f.y();
      float f4 = pUv.getU(pUv.getReverseIndex(2));
      float f5 = pUv.getV(pUv.getReverseIndex(2));
      Vector4f vector4f1 = matrix4f.transform(new Vector4f(f4 / 16.0F, f5 / 16.0F, 0.0F, 1.0F));
      float f6 = 16.0F * vector4f1.x();
      float f7 = 16.0F * vector4f1.y();
      float f8;
      float f9;
      if (Math.signum(f4 - f) == Math.signum(f6 - f2)) {
         f8 = f2;
         f9 = f6;
      } else {
         f8 = f6;
         f9 = f2;
      }

      float f10;
      float f11;
      if (Math.signum(f5 - f1) == Math.signum(f7 - f3)) {
         f10 = f3;
         f11 = f7;
      } else {
         f10 = f7;
         f11 = f3;
      }

      float f12 = (float)Math.toRadians((double)pUv.rotation);
      Matrix3f matrix3f = new Matrix3f(matrix4f);
      Vector3f vector3f = matrix3f.transform(new Vector3f(Mth.cos(f12), Mth.sin(f12), 0.0F));
      int i = Math.floorMod(-((int)Math.round(Math.toDegrees(Math.atan2((double)vector3f.y(), (double)vector3f.x())) / 90.0D)) * 90, 360);
      return new BlockFaceUV(new float[]{f8, f10, f9, f11}, i);
   }

   private int[] makeVertices(BlockFaceUV pUvs, TextureAtlasSprite pSprite, Direction pOrientation, float[] pPosDiv16, Transformation pRotation, @Nullable BlockElementRotation pPartRotation, boolean pShade) {
      int[] aint = new int[32];

      for(int i = 0; i < 4; ++i) {
         this.bakeVertex(aint, i, pOrientation, pUvs, pPosDiv16, pSprite, pRotation, pPartRotation, pShade);
      }

      return aint;
   }

   private float[] setupShape(Vector3f pMin, Vector3f pMax) {
      float[] afloat = new float[Direction.values().length];
      afloat[FaceInfo.Constants.MIN_X] = pMin.x() / 16.0F;
      afloat[FaceInfo.Constants.MIN_Y] = pMin.y() / 16.0F;
      afloat[FaceInfo.Constants.MIN_Z] = pMin.z() / 16.0F;
      afloat[FaceInfo.Constants.MAX_X] = pMax.x() / 16.0F;
      afloat[FaceInfo.Constants.MAX_Y] = pMax.y() / 16.0F;
      afloat[FaceInfo.Constants.MAX_Z] = pMax.z() / 16.0F;
      return afloat;
   }

   private void bakeVertex(int[] pVertexData, int pVertexIndex, Direction pFacing, BlockFaceUV pBlockFaceUV, float[] pPosDiv16, TextureAtlasSprite pSprite, Transformation pRotation, @Nullable BlockElementRotation pPartRotation, boolean pShade) {
      FaceInfo.VertexInfo faceinfo$vertexinfo = FaceInfo.fromFacing(pFacing).getVertexInfo(pVertexIndex);
      Vector3f vector3f = new Vector3f(pPosDiv16[faceinfo$vertexinfo.xFace], pPosDiv16[faceinfo$vertexinfo.yFace], pPosDiv16[faceinfo$vertexinfo.zFace]);
      this.applyElementRotation(vector3f, pPartRotation);
      this.applyModelRotation(vector3f, pRotation);
      this.fillVertex(pVertexData, pVertexIndex, vector3f, pSprite, pBlockFaceUV);
   }

   private void fillVertex(int[] pVertexData, int pVertexIndex, Vector3f pVector, TextureAtlasSprite pSprite, BlockFaceUV pBlockFaceUV) {
      int i = pVertexIndex * 8;
      pVertexData[i] = Float.floatToRawIntBits(pVector.x());
      pVertexData[i + 1] = Float.floatToRawIntBits(pVector.y());
      pVertexData[i + 2] = Float.floatToRawIntBits(pVector.z());
      pVertexData[i + 3] = -1;
      pVertexData[i + 4] = Float.floatToRawIntBits(pSprite.getU((double)pBlockFaceUV.getU(pVertexIndex) * .999 + pBlockFaceUV.getU((pVertexIndex + 2) % 4) * .001));
      pVertexData[i + 4 + 1] = Float.floatToRawIntBits(pSprite.getV((double)pBlockFaceUV.getV(pVertexIndex) * .999 + pBlockFaceUV.getV((pVertexIndex + 2) % 4) * .001));
   }

   private void applyElementRotation(Vector3f pVec, @Nullable BlockElementRotation pPartRotation) {
      if (pPartRotation != null) {
         Vector3f vector3f;
         Vector3f vector3f1;
         switch (pPartRotation.axis()) {
            case X:
               vector3f = new Vector3f(1.0F, 0.0F, 0.0F);
               vector3f1 = new Vector3f(0.0F, 1.0F, 1.0F);
               break;
            case Y:
               vector3f = new Vector3f(0.0F, 1.0F, 0.0F);
               vector3f1 = new Vector3f(1.0F, 0.0F, 1.0F);
               break;
            case Z:
               vector3f = new Vector3f(0.0F, 0.0F, 1.0F);
               vector3f1 = new Vector3f(1.0F, 1.0F, 0.0F);
               break;
            default:
               throw new IllegalArgumentException("There are only 3 axes");
         }

         Quaternionf quaternionf = (new Quaternionf()).rotationAxis(pPartRotation.angle() * ((float)Math.PI / 180F), vector3f);
         if (pPartRotation.rescale()) {
            if (Math.abs(pPartRotation.angle()) == 22.5F) {
               vector3f1.mul(RESCALE_22_5);
            } else {
               vector3f1.mul(RESCALE_45);
            }

            vector3f1.add(1.0F, 1.0F, 1.0F);
         } else {
            vector3f1.set(1.0F, 1.0F, 1.0F);
         }

         this.rotateVertexBy(pVec, new Vector3f((Vector3fc)pPartRotation.origin()), (new Matrix4f()).rotation(quaternionf), vector3f1);
      }
   }

   public void applyModelRotation(Vector3f pPos, Transformation pTransform) {
      if (pTransform != Transformation.identity()) {
         this.rotateVertexBy(pPos, new Vector3f(0.5F, 0.5F, 0.5F), pTransform.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
      }
   }

   private void rotateVertexBy(Vector3f pPos, Vector3f pOrigin, Matrix4f pTransform, Vector3f pScale) {
      Vector4f vector4f = pTransform.transform(new Vector4f(pPos.x() - pOrigin.x(), pPos.y() - pOrigin.y(), pPos.z() - pOrigin.z(), 1.0F));
      vector4f.mul(new Vector4f(pScale, 1.0F));
      pPos.set(vector4f.x() + pOrigin.x(), vector4f.y() + pOrigin.y(), vector4f.z() + pOrigin.z());
   }

   public static Direction calculateFacing(int[] pFaceData) {
      Vector3f vector3f = new Vector3f(Float.intBitsToFloat(pFaceData[0]), Float.intBitsToFloat(pFaceData[1]), Float.intBitsToFloat(pFaceData[2]));
      Vector3f vector3f1 = new Vector3f(Float.intBitsToFloat(pFaceData[8]), Float.intBitsToFloat(pFaceData[9]), Float.intBitsToFloat(pFaceData[10]));
      Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(pFaceData[16]), Float.intBitsToFloat(pFaceData[17]), Float.intBitsToFloat(pFaceData[18]));
      Vector3f vector3f3 = (new Vector3f((Vector3fc)vector3f)).sub(vector3f1);
      Vector3f vector3f4 = (new Vector3f((Vector3fc)vector3f2)).sub(vector3f1);
      Vector3f vector3f5 = (new Vector3f((Vector3fc)vector3f4)).cross(vector3f3).normalize();
      if (!vector3f5.isFinite()) {
         return Direction.UP;
      } else {
         Direction direction = null;
         float f = 0.0F;

         for(Direction direction1 : Direction.values()) {
            Vec3i vec3i = direction1.getNormal();
            Vector3f vector3f6 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
            float f1 = vector3f5.dot(vector3f6);
            if (f1 >= 0.0F && f1 > f) {
               f = f1;
               direction = direction1;
            }
         }

         return direction == null ? Direction.UP : direction;
      }
   }

   private void recalculateWinding(int[] pVertices, Direction pDirection) {
      int[] aint = new int[pVertices.length];
      System.arraycopy(pVertices, 0, aint, 0, pVertices.length);
      float[] afloat = new float[Direction.values().length];
      afloat[FaceInfo.Constants.MIN_X] = 999.0F;
      afloat[FaceInfo.Constants.MIN_Y] = 999.0F;
      afloat[FaceInfo.Constants.MIN_Z] = 999.0F;
      afloat[FaceInfo.Constants.MAX_X] = -999.0F;
      afloat[FaceInfo.Constants.MAX_Y] = -999.0F;
      afloat[FaceInfo.Constants.MAX_Z] = -999.0F;

      for(int i = 0; i < 4; ++i) {
         int j = 8 * i;
         float f = Float.intBitsToFloat(aint[j]);
         float f1 = Float.intBitsToFloat(aint[j + 1]);
         float f2 = Float.intBitsToFloat(aint[j + 2]);
         if (f < afloat[FaceInfo.Constants.MIN_X]) {
            afloat[FaceInfo.Constants.MIN_X] = f;
         }

         if (f1 < afloat[FaceInfo.Constants.MIN_Y]) {
            afloat[FaceInfo.Constants.MIN_Y] = f1;
         }

         if (f2 < afloat[FaceInfo.Constants.MIN_Z]) {
            afloat[FaceInfo.Constants.MIN_Z] = f2;
         }

         if (f > afloat[FaceInfo.Constants.MAX_X]) {
            afloat[FaceInfo.Constants.MAX_X] = f;
         }

         if (f1 > afloat[FaceInfo.Constants.MAX_Y]) {
            afloat[FaceInfo.Constants.MAX_Y] = f1;
         }

         if (f2 > afloat[FaceInfo.Constants.MAX_Z]) {
            afloat[FaceInfo.Constants.MAX_Z] = f2;
         }
      }

      FaceInfo faceinfo = FaceInfo.fromFacing(pDirection);

      for(int i1 = 0; i1 < 4; ++i1) {
         int j1 = 8 * i1;
         FaceInfo.VertexInfo faceinfo$vertexinfo = faceinfo.getVertexInfo(i1);
         float f8 = afloat[faceinfo$vertexinfo.xFace];
         float f3 = afloat[faceinfo$vertexinfo.yFace];
         float f4 = afloat[faceinfo$vertexinfo.zFace];
         pVertices[j1] = Float.floatToRawIntBits(f8);
         pVertices[j1 + 1] = Float.floatToRawIntBits(f3);
         pVertices[j1 + 2] = Float.floatToRawIntBits(f4);

         for(int k = 0; k < 4; ++k) {
            int l = 8 * k;
            float f5 = Float.intBitsToFloat(aint[l]);
            float f6 = Float.intBitsToFloat(aint[l + 1]);
            float f7 = Float.intBitsToFloat(aint[l + 2]);
            if (Mth.equal(f8, f5) && Mth.equal(f3, f6) && Mth.equal(f4, f7)) {
               pVertices[j1 + 4] = aint[l + 4];
               pVertices[j1 + 4 + 1] = aint[l + 4 + 1];
            }
         }
      }

   }
}
