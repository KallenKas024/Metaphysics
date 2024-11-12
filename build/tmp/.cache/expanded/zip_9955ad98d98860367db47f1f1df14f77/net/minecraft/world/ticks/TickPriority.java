package net.minecraft.world.ticks;

public enum TickPriority {
   EXTREMELY_HIGH(-3),
   VERY_HIGH(-2),
   HIGH(-1),
   NORMAL(0),
   LOW(1),
   VERY_LOW(2),
   EXTREMELY_LOW(3);

   private final int value;

   private TickPriority(int pValue) {
      this.value = pValue;
   }

   public static TickPriority byValue(int pPriority) {
      for(TickPriority tickpriority : values()) {
         if (tickpriority.value == pPriority) {
            return tickpriority;
         }
      }

      return pPriority < EXTREMELY_HIGH.value ? EXTREMELY_HIGH : EXTREMELY_LOW;
   }

   public int getValue() {
      return this.value;
   }
}