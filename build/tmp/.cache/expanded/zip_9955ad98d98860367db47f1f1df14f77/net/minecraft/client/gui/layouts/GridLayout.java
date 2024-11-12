package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GridLayout extends AbstractLayout {
   private final List<LayoutElement> children = new ArrayList<>();
   private final List<GridLayout.CellInhabitant> cellInhabitants = new ArrayList<>();
   private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();
   private int rowSpacing = 0;
   private int columnSpacing = 0;

   public GridLayout() {
      this(0, 0);
   }

   public GridLayout(int pX, int pY) {
      super(pX, pY, 0, 0);
   }

   public void arrangeElements() {
      super.arrangeElements();
      int i = 0;
      int j = 0;

      for(GridLayout.CellInhabitant gridlayout$cellinhabitant : this.cellInhabitants) {
         i = Math.max(gridlayout$cellinhabitant.getLastOccupiedRow(), i);
         j = Math.max(gridlayout$cellinhabitant.getLastOccupiedColumn(), j);
      }

      int[] aint = new int[j + 1];
      int[] aint1 = new int[i + 1];

      for(GridLayout.CellInhabitant gridlayout$cellinhabitant1 : this.cellInhabitants) {
         int k = gridlayout$cellinhabitant1.getHeight() - (gridlayout$cellinhabitant1.occupiedRows - 1) * this.rowSpacing;
         Divisor divisor = new Divisor(k, gridlayout$cellinhabitant1.occupiedRows);

         for(int l = gridlayout$cellinhabitant1.row; l <= gridlayout$cellinhabitant1.getLastOccupiedRow(); ++l) {
            aint1[l] = Math.max(aint1[l], divisor.nextInt());
         }

         int l1 = gridlayout$cellinhabitant1.getWidth() - (gridlayout$cellinhabitant1.occupiedColumns - 1) * this.columnSpacing;
         Divisor divisor1 = new Divisor(l1, gridlayout$cellinhabitant1.occupiedColumns);

         for(int i1 = gridlayout$cellinhabitant1.column; i1 <= gridlayout$cellinhabitant1.getLastOccupiedColumn(); ++i1) {
            aint[i1] = Math.max(aint[i1], divisor1.nextInt());
         }
      }

      int[] aint2 = new int[j + 1];
      int[] aint3 = new int[i + 1];
      aint2[0] = 0;

      for(int j1 = 1; j1 <= j; ++j1) {
         aint2[j1] = aint2[j1 - 1] + aint[j1 - 1] + this.columnSpacing;
      }

      aint3[0] = 0;

      for(int k1 = 1; k1 <= i; ++k1) {
         aint3[k1] = aint3[k1 - 1] + aint1[k1 - 1] + this.rowSpacing;
      }

      for(GridLayout.CellInhabitant gridlayout$cellinhabitant2 : this.cellInhabitants) {
         int i2 = 0;

         for(int j2 = gridlayout$cellinhabitant2.column; j2 <= gridlayout$cellinhabitant2.getLastOccupiedColumn(); ++j2) {
            i2 += aint[j2];
         }

         i2 += this.columnSpacing * (gridlayout$cellinhabitant2.occupiedColumns - 1);
         gridlayout$cellinhabitant2.setX(this.getX() + aint2[gridlayout$cellinhabitant2.column], i2);
         int k2 = 0;

         for(int l2 = gridlayout$cellinhabitant2.row; l2 <= gridlayout$cellinhabitant2.getLastOccupiedRow(); ++l2) {
            k2 += aint1[l2];
         }

         k2 += this.rowSpacing * (gridlayout$cellinhabitant2.occupiedRows - 1);
         gridlayout$cellinhabitant2.setY(this.getY() + aint3[gridlayout$cellinhabitant2.row], k2);
      }

      this.width = aint2[j] + aint[j];
      this.height = aint3[i] + aint1[i];
   }

   public <T extends LayoutElement> T addChild(T pChild, int pRow, int pColumn) {
      return this.addChild(pChild, pRow, pColumn, this.newCellSettings());
   }

   public <T extends LayoutElement> T addChild(T pChild, int pRow, int pColumn, LayoutSettings pLayoutSettings) {
      return this.addChild(pChild, pRow, pColumn, 1, 1, pLayoutSettings);
   }

   public <T extends LayoutElement> T addChild(T pChild, int pRow, int pColumn, int pOccupiedRows, int pOccupiedColumns) {
      return this.addChild(pChild, pRow, pColumn, pOccupiedRows, pOccupiedColumns, this.newCellSettings());
   }

   public <T extends LayoutElement> T addChild(T pChild, int pRow, int pColumn, int pOccupiedRows, int pOccupiedColumns, LayoutSettings pLayourSettings) {
      if (pOccupiedRows < 1) {
         throw new IllegalArgumentException("Occupied rows must be at least 1");
      } else if (pOccupiedColumns < 1) {
         throw new IllegalArgumentException("Occupied columns must be at least 1");
      } else {
         this.cellInhabitants.add(new GridLayout.CellInhabitant(pChild, pRow, pColumn, pOccupiedRows, pOccupiedColumns, pLayourSettings));
         this.children.add(pChild);
         return pChild;
      }
   }

   public GridLayout columnSpacing(int pColumnSpacing) {
      this.columnSpacing = pColumnSpacing;
      return this;
   }

   public GridLayout rowSpacing(int pRowSpacing) {
      this.rowSpacing = pRowSpacing;
      return this;
   }

   public GridLayout spacing(int pSpacing) {
      return this.columnSpacing(pSpacing).rowSpacing(pSpacing);
   }

   public void visitChildren(Consumer<LayoutElement> pConsumer) {
      this.children.forEach(pConsumer);
   }

   public LayoutSettings newCellSettings() {
      return this.defaultCellSettings.copy();
   }

   public LayoutSettings defaultCellSetting() {
      return this.defaultCellSettings;
   }

   public GridLayout.RowHelper createRowHelper(int pColumns) {
      return new GridLayout.RowHelper(pColumns);
   }

   @OnlyIn(Dist.CLIENT)
   static class CellInhabitant extends AbstractLayout.AbstractChildWrapper {
      final int row;
      final int column;
      final int occupiedRows;
      final int occupiedColumns;

      CellInhabitant(LayoutElement pChild, int pRow, int pColumn, int pOccupiedRows, int pOccupiedColumns, LayoutSettings pLayoutSettings) {
         super(pChild, pLayoutSettings.getExposed());
         this.row = pRow;
         this.column = pColumn;
         this.occupiedRows = pOccupiedRows;
         this.occupiedColumns = pOccupiedColumns;
      }

      public int getLastOccupiedRow() {
         return this.row + this.occupiedRows - 1;
      }

      public int getLastOccupiedColumn() {
         return this.column + this.occupiedColumns - 1;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public final class RowHelper {
      private final int columns;
      private int index;

      RowHelper(int pColumns) {
         this.columns = pColumns;
      }

      public <T extends LayoutElement> T addChild(T pChild) {
         return this.addChild(pChild, 1);
      }

      public <T extends LayoutElement> T addChild(T pChild, int pOccupiedColumns) {
         return this.addChild(pChild, pOccupiedColumns, this.defaultCellSetting());
      }

      public <T extends LayoutElement> T addChild(T pChild, LayoutSettings pLayoutSettings) {
         return this.addChild(pChild, 1, pLayoutSettings);
      }

      public <T extends LayoutElement> T addChild(T pChild, int pOccupiedColumns, LayoutSettings pLayoutSettings) {
         int i = this.index / this.columns;
         int j = this.index % this.columns;
         if (j + pOccupiedColumns > this.columns) {
            ++i;
            j = 0;
            this.index = Mth.roundToward(this.index, this.columns);
         }

         this.index += pOccupiedColumns;
         return GridLayout.this.addChild(pChild, i, j, 1, pOccupiedColumns, pLayoutSettings);
      }

      public GridLayout getGrid() {
         return GridLayout.this;
      }

      public LayoutSettings newCellSettings() {
         return GridLayout.this.newCellSettings();
      }

      public LayoutSettings defaultCellSetting() {
         return GridLayout.this.defaultCellSetting();
      }
   }
}