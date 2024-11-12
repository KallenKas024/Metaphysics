package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class LighthingBoltPredicate implements EntitySubPredicate {
   private static final String BLOCKS_SET_ON_FIRE_KEY = "blocks_set_on_fire";
   private static final String ENTITY_STRUCK_KEY = "entity_struck";
   private final MinMaxBounds.Ints blocksSetOnFire;
   private final EntityPredicate entityStruck;

   private LighthingBoltPredicate(MinMaxBounds.Ints pBlocksSetOnFire, EntityPredicate pEntityStruck) {
      this.blocksSetOnFire = pBlocksSetOnFire;
      this.entityStruck = pEntityStruck;
   }

   public static LighthingBoltPredicate blockSetOnFire(MinMaxBounds.Ints pBlocksSetOnFire) {
      return new LighthingBoltPredicate(pBlocksSetOnFire, EntityPredicate.ANY);
   }

   public static LighthingBoltPredicate fromJson(JsonObject pJson) {
      return new LighthingBoltPredicate(MinMaxBounds.Ints.fromJson(pJson.get("blocks_set_on_fire")), EntityPredicate.fromJson(pJson.get("entity_struck")));
   }

   public JsonObject serializeCustomData() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("blocks_set_on_fire", this.blocksSetOnFire.serializeToJson());
      jsonobject.add("entity_struck", this.entityStruck.serializeToJson());
      return jsonobject;
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.LIGHTNING;
   }

   public boolean matches(Entity pEntity, ServerLevel pLevel, @Nullable Vec3 pPosition) {
      if (!(pEntity instanceof LightningBolt lightningbolt)) {
         return false;
      } else {
         return this.blocksSetOnFire.matches(lightningbolt.getBlocksSetOnFire()) && (this.entityStruck == EntityPredicate.ANY || lightningbolt.getHitEntities().anyMatch((p_153245_) -> {
            return this.entityStruck.matches(pLevel, pPosition, p_153245_);
         }));
      }
   }
}