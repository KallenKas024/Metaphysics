package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final int MAX_RENDER_DIST = 160;
   private final Minecraft minecraft;
   private final Map<Integer, List<GoalSelectorDebugRenderer.DebugGoal>> goalSelectors = Maps.newHashMap();

   public void clear() {
      this.goalSelectors.clear();
   }

   public void addGoalSelector(int pMobId, List<GoalSelectorDebugRenderer.DebugGoal> pGoals) {
      this.goalSelectors.put(pMobId, pGoals);
   }

   public void removeGoalSelector(int pMobId) {
      this.goalSelectors.remove(pMobId);
   }

   public GoalSelectorDebugRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0D, camera.getPosition().z);
      this.goalSelectors.forEach((p_269742_, p_269743_) -> {
         for(int i = 0; i < p_269743_.size(); ++i) {
            GoalSelectorDebugRenderer.DebugGoal goalselectordebugrenderer$debuggoal = p_269743_.get(i);
            if (blockpos.closerThan(goalselectordebugrenderer$debuggoal.pos, 160.0D)) {
               double d0 = (double)goalselectordebugrenderer$debuggoal.pos.getX() + 0.5D;
               double d1 = (double)goalselectordebugrenderer$debuggoal.pos.getY() + 2.0D + (double)i * 0.25D;
               double d2 = (double)goalselectordebugrenderer$debuggoal.pos.getZ() + 0.5D;
               int j = goalselectordebugrenderer$debuggoal.isRunning ? -16711936 : -3355444;
               DebugRenderer.renderFloatingText(pPoseStack, pBuffer, goalselectordebugrenderer$debuggoal.name, d0, d1, d2, j);
            }
         }

      });
   }

   @OnlyIn(Dist.CLIENT)
   public static class DebugGoal {
      public final BlockPos pos;
      public final int priority;
      public final String name;
      public final boolean isRunning;

      public DebugGoal(BlockPos pPos, int pPriority, String pName, boolean pIsRunning) {
         this.pos = pPos;
         this.priority = pPriority;
         this.name = pName;
         this.isRunning = pIsRunning;
      }
   }
}