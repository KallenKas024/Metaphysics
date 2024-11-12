package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.slf4j.Logger;

public class EnderDragonPhaseManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final EnderDragon dragon;
   private final DragonPhaseInstance[] phases = new DragonPhaseInstance[EnderDragonPhase.getCount()];
   @Nullable
   private DragonPhaseInstance currentPhase;

   public EnderDragonPhaseManager(EnderDragon pDragon) {
      this.dragon = pDragon;
      this.setPhase(EnderDragonPhase.HOVERING);
   }

   public void setPhase(EnderDragonPhase<?> pPhase) {
      if (this.currentPhase == null || pPhase != this.currentPhase.getPhase()) {
         if (this.currentPhase != null) {
            this.currentPhase.end();
         }

         this.currentPhase = this.getPhase(pPhase);
         if (!this.dragon.level().isClientSide) {
            this.dragon.getEntityData().set(EnderDragon.DATA_PHASE, pPhase.getId());
         }

         LOGGER.debug("Dragon is now in phase {} on the {}", pPhase, this.dragon.level().isClientSide ? "client" : "server");
         this.currentPhase.begin();
      }
   }

   public DragonPhaseInstance getCurrentPhase() {
      return this.currentPhase;
   }

   public <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> pPhase) {
      int i = pPhase.getId();
      if (this.phases[i] == null) {
         this.phases[i] = pPhase.createInstance(this.dragon);
      }

      return (T)this.phases[i];
   }
}