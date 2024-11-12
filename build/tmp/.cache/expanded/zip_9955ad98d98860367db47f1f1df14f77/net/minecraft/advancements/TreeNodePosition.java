package net.minecraft.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;

public class TreeNodePosition {
   private final Advancement advancement;
   @Nullable
   private final TreeNodePosition parent;
   @Nullable
   private final TreeNodePosition previousSibling;
   private final int childIndex;
   private final List<TreeNodePosition> children = Lists.newArrayList();
   private TreeNodePosition ancestor;
   @Nullable
   private TreeNodePosition thread;
   private int x;
   private float y;
   private float mod;
   private float change;
   private float shift;

   public TreeNodePosition(Advancement pAdvancement, @Nullable TreeNodePosition pParent, @Nullable TreeNodePosition pPreviousSibling, int pChildIndex, int pX) {
      if (pAdvancement.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position an invisible advancement!");
      } else {
         this.advancement = pAdvancement;
         this.parent = pParent;
         this.previousSibling = pPreviousSibling;
         this.childIndex = pChildIndex;
         this.ancestor = this;
         this.x = pX;
         this.y = -1.0F;
         TreeNodePosition treenodeposition = null;

         for(Advancement advancement : pAdvancement.getChildren()) {
            treenodeposition = this.addChild(advancement, treenodeposition);
         }

      }
   }

   @Nullable
   private TreeNodePosition addChild(Advancement pAdvancement, @Nullable TreeNodePosition pPrevious) {
      if (pAdvancement.getDisplay() != null) {
         pPrevious = new TreeNodePosition(pAdvancement, this, pPrevious, this.children.size() + 1, this.x + 1);
         this.children.add(pPrevious);
      } else {
         for(Advancement advancement : pAdvancement.getChildren()) {
            pPrevious = this.addChild(advancement, pPrevious);
         }
      }

      return pPrevious;
   }

   private void firstWalk() {
      if (this.children.isEmpty()) {
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
         } else {
            this.y = 0.0F;
         }

      } else {
         TreeNodePosition treenodeposition = null;

         for(TreeNodePosition treenodeposition1 : this.children) {
            treenodeposition1.firstWalk();
            treenodeposition = treenodeposition1.apportion(treenodeposition == null ? treenodeposition1 : treenodeposition);
         }

         this.executeShifts();
         float f = ((this.children.get(0)).y + (this.children.get(this.children.size() - 1)).y) / 2.0F;
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
            this.mod = this.y - f;
         } else {
            this.y = f;
         }

      }
   }

   private float secondWalk(float pOffsetY, int pColumnX, float pSubtreeTopY) {
      this.y += pOffsetY;
      this.x = pColumnX;
      if (this.y < pSubtreeTopY) {
         pSubtreeTopY = this.y;
      }

      for(TreeNodePosition treenodeposition : this.children) {
         pSubtreeTopY = treenodeposition.secondWalk(pOffsetY + this.mod, pColumnX + 1, pSubtreeTopY);
      }

      return pSubtreeTopY;
   }

   private void thirdWalk(float pY) {
      this.y += pY;

      for(TreeNodePosition treenodeposition : this.children) {
         treenodeposition.thirdWalk(pY);
      }

   }

   private void executeShifts() {
      float f = 0.0F;
      float f1 = 0.0F;

      for(int i = this.children.size() - 1; i >= 0; --i) {
         TreeNodePosition treenodeposition = this.children.get(i);
         treenodeposition.y += f;
         treenodeposition.mod += f;
         f1 += treenodeposition.change;
         f += treenodeposition.shift + f1;
      }

   }

   @Nullable
   private TreeNodePosition previousOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(0) : null;
      }
   }

   @Nullable
   private TreeNodePosition nextOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(this.children.size() - 1) : null;
      }
   }

   private TreeNodePosition apportion(TreeNodePosition pNode) {
      if (this.previousSibling == null) {
         return pNode;
      } else {
         TreeNodePosition treenodeposition = this;
         TreeNodePosition treenodeposition1 = this;
         TreeNodePosition treenodeposition2 = this.previousSibling;
         TreeNodePosition treenodeposition3 = this.parent.children.get(0);
         float f = this.mod;
         float f1 = this.mod;
         float f2 = treenodeposition2.mod;

         float f3;
         for(f3 = treenodeposition3.mod; treenodeposition2.nextOrThread() != null && treenodeposition.previousOrThread() != null; f1 += treenodeposition1.mod) {
            treenodeposition2 = treenodeposition2.nextOrThread();
            treenodeposition = treenodeposition.previousOrThread();
            treenodeposition3 = treenodeposition3.previousOrThread();
            treenodeposition1 = treenodeposition1.nextOrThread();
            treenodeposition1.ancestor = this;
            float f4 = treenodeposition2.y + f2 - (treenodeposition.y + f) + 1.0F;
            if (f4 > 0.0F) {
               treenodeposition2.getAncestor(this, pNode).moveSubtree(this, f4);
               f += f4;
               f1 += f4;
            }

            f2 += treenodeposition2.mod;
            f += treenodeposition.mod;
            f3 += treenodeposition3.mod;
         }

         if (treenodeposition2.nextOrThread() != null && treenodeposition1.nextOrThread() == null) {
            treenodeposition1.thread = treenodeposition2.nextOrThread();
            treenodeposition1.mod += f2 - f1;
         } else {
            if (treenodeposition.previousOrThread() != null && treenodeposition3.previousOrThread() == null) {
               treenodeposition3.thread = treenodeposition.previousOrThread();
               treenodeposition3.mod += f - f3;
            }

            pNode = this;
         }

         return pNode;
      }
   }

   private void moveSubtree(TreeNodePosition pNode, float pShift) {
      float f = (float)(pNode.childIndex - this.childIndex);
      if (f != 0.0F) {
         pNode.change -= pShift / f;
         this.change += pShift / f;
      }

      pNode.shift += pShift;
      pNode.y += pShift;
      pNode.mod += pShift;
   }

   private TreeNodePosition getAncestor(TreeNodePosition pSelf, TreeNodePosition pOther) {
      return this.ancestor != null && pSelf.parent.children.contains(this.ancestor) ? this.ancestor : pOther;
   }

   private void finalizePosition() {
      if (this.advancement.getDisplay() != null) {
         this.advancement.getDisplay().setLocation((float)this.x, this.y);
      }

      if (!this.children.isEmpty()) {
         for(TreeNodePosition treenodeposition : this.children) {
            treenodeposition.finalizePosition();
         }
      }

   }

   public static void run(Advancement pRoot) {
      if (pRoot.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position children of an invisible root!");
      } else {
         TreeNodePosition treenodeposition = new TreeNodePosition(pRoot, (TreeNodePosition)null, (TreeNodePosition)null, 1, 0);
         treenodeposition.firstWalk();
         float f = treenodeposition.secondWalk(0.0F, 0, treenodeposition.y);
         if (f < 0.0F) {
            treenodeposition.thirdWalk(-f);
         }

         treenodeposition.finalizePosition();
      }
   }
}