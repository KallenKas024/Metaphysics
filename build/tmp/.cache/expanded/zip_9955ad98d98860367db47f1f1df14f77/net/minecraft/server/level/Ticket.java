package net.minecraft.server.level;

import java.util.Objects;

public final class Ticket<T> implements Comparable<Ticket<?>> {
   private final TicketType<T> type;
   private final int ticketLevel;
   private final T key;
   private long createdTick;

   protected Ticket(TicketType<T> pType, int pTicketLevel, T pKey) {
      this(pType, pTicketLevel, pKey, false);
   }

   public Ticket(TicketType<T> pType, int pTicketLevel, T pKey, boolean forceTicks) {
      this.type = pType;
      this.ticketLevel = pTicketLevel;
      this.key = pKey;
      this.forceTicks = forceTicks;
   }

   public int compareTo(Ticket<?> p_9432_) {
      int i = Integer.compare(this.ticketLevel, p_9432_.ticketLevel);
      if (i != 0) {
         return i;
      } else {
         int j = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(p_9432_.type));
         return j != 0 ? j : this.type.getComparator().compare(this.key, (T)p_9432_.key);
      }
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof Ticket)) {
         return false;
      } else {
         Ticket<?> ticket = (Ticket)pOther;
         return this.ticketLevel == ticket.ticketLevel && Objects.equals(this.type, ticket.type) && Objects.equals(this.key, ticket.key) && this.forceTicks == ticket.forceTicks;
      }
   }

   public int hashCode() {
      return Objects.hash(this.type, this.ticketLevel, this.key, forceTicks);
   }

   public String toString() {
      return "Ticket[" + this.type + " " + this.ticketLevel + " (" + this.key + ")] at " + this.createdTick + " force ticks " + forceTicks;
   }

   public TicketType<T> getType() {
      return this.type;
   }

   public int getTicketLevel() {
      return this.ticketLevel;
   }

   protected void setCreatedTick(long pTimestamp) {
      this.createdTick = pTimestamp;
   }

   protected boolean timedOut(long pCurrentTime) {
      long i = this.type.timeout();
      return i != 0L && pCurrentTime - this.createdTick > i;
   }

   /* ======================================== FORGE START =====================================*/
   private final boolean forceTicks;

   public boolean isForceTicks() {
      return forceTicks;
   }
}
