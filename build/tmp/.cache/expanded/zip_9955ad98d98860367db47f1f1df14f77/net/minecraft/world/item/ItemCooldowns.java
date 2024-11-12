package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Mth;

public class ItemCooldowns {
   private final Map<Item, ItemCooldowns.CooldownInstance> cooldowns = Maps.newHashMap();
   private int tickCount;

   public boolean isOnCooldown(Item pItem) {
      return this.getCooldownPercent(pItem, 0.0F) > 0.0F;
   }

   public float getCooldownPercent(Item pItem, float pPartialTicks) {
      ItemCooldowns.CooldownInstance itemcooldowns$cooldowninstance = this.cooldowns.get(pItem);
      if (itemcooldowns$cooldowninstance != null) {
         float f = (float)(itemcooldowns$cooldowninstance.endTime - itemcooldowns$cooldowninstance.startTime);
         float f1 = (float)itemcooldowns$cooldowninstance.endTime - ((float)this.tickCount + pPartialTicks);
         return Mth.clamp(f1 / f, 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }

   public void tick() {
      ++this.tickCount;
      if (!this.cooldowns.isEmpty()) {
         Iterator<Map.Entry<Item, ItemCooldowns.CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<Item, ItemCooldowns.CooldownInstance> entry = iterator.next();
            if ((entry.getValue()).endTime <= this.tickCount) {
               iterator.remove();
               this.onCooldownEnded(entry.getKey());
            }
         }
      }

   }

   public void addCooldown(Item pItem, int pTicks) {
      this.cooldowns.put(pItem, new ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + pTicks));
      this.onCooldownStarted(pItem, pTicks);
   }

   public void removeCooldown(Item pItem) {
      this.cooldowns.remove(pItem);
      this.onCooldownEnded(pItem);
   }

   protected void onCooldownStarted(Item pItem, int pTicks) {
   }

   protected void onCooldownEnded(Item pItem) {
   }

   static class CooldownInstance {
      final int startTime;
      final int endTime;

      CooldownInstance(int pStartTime, int pEndTime) {
         this.startTime = pStartTime;
         this.endTime = pEndTime;
      }
   }
}