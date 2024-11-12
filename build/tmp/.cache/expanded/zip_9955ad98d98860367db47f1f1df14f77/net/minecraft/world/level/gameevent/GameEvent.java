package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Describes an in game event or action that can be detected by listeners such as the Sculk Sensor block.
 */
public class GameEvent {
   public static final GameEvent BLOCK_ACTIVATE = register("block_activate");
   /**
    * This event is broadcast when a block is attached to another. For example when the tripwire is attached to a
    * tripwire hook.
    */
   public static final GameEvent BLOCK_ATTACH = register("block_attach");
   /** This event is broadcast when a block is changed. For example when a flower is removed from a flower pot. */
   public static final GameEvent BLOCK_CHANGE = register("block_change");
   /** This event is broadcast when a block such as a door, trap door, or gate is closed. */
   public static final GameEvent BLOCK_CLOSE = register("block_close");
   public static final GameEvent BLOCK_DEACTIVATE = register("block_deactivate");
   /** This event is broadcast when a block is destroyed or picked up by an enderman. */
   public static final GameEvent BLOCK_DESTROY = register("block_destroy");
   /**
    * This event is broadcast when a block is detached from another block. For example when the tripwire is removed from
    * the hook.
    */
   public static final GameEvent BLOCK_DETACH = register("block_detach");
   /** This event is broadcast when a block such as a door, trap door, or gate has been opened. */
   public static final GameEvent BLOCK_OPEN = register("block_open");
   /** This event is broadcast when a block is placed in the world. */
   public static final GameEvent BLOCK_PLACE = register("block_place");
   /**
    * This event is broadcast when a block with a storage inventory such as a chest or barrel is closed. Some entities
    * like a minecart with chest may also cause this event to be broadcast.
    */
   public static final GameEvent CONTAINER_CLOSE = register("container_close");
   /**
    * This event is broadcast when a block with a storage inventory such as a chest or barrel is opened. Some entities
    * like a minecart with chest may also cause this event to be broadcast.
    */
   public static final GameEvent CONTAINER_OPEN = register("container_open");
   public static final GameEvent DRINK = register("drink");
   /**
    * This event is broadcast when an entity consumes food. This includes animals eating grass and other sources of
    * food.
    */
   public static final GameEvent EAT = register("eat");
   public static final GameEvent ELYTRA_GLIDE = register("elytra_glide");
   public static final GameEvent ENTITY_DAMAGE = register("entity_damage");
   public static final GameEvent ENTITY_DIE = register("entity_die");
   public static final GameEvent ENTITY_DISMOUNT = register("entity_dismount");
   public static final GameEvent ENTITY_INTERACT = register("entity_interact");
   public static final GameEvent ENTITY_MOUNT = register("entity_mount");
   /**
    * This event is broadcast when an entity is artificially placed in the world using an item. For example when a spawn
    * egg is used.
    */
   public static final GameEvent ENTITY_PLACE = register("entity_place");
   public static final GameEvent ENTITY_ROAR = register("entity_roar");
   public static final GameEvent ENTITY_SHAKE = register("entity_shake");
   /** This event is broadcast when an item is equipped to an entity or armor stand. */
   public static final GameEvent EQUIP = register("equip");
   /** This event is broadcast when an entity such as a creeper, tnt, or a firework explodes. */
   public static final GameEvent EXPLODE = register("explode");
   /** This event is broadcast when a flying entity such as the ender dragon flaps its wings. */
   public static final GameEvent FLAP = register("flap");
   /**
    * This event is broadcast when a fluid is picked up. This includes using a bucket, harvesting honey, filling a
    * bottle, and removing fluid from a cauldron.
    */
   public static final GameEvent FLUID_PICKUP = register("fluid_pickup");
   /**
    * This event is broadcast when fluid is placed. This includes adding fluid to a cauldron and placing a bucket of
    * fluid.
    */
   public static final GameEvent FLUID_PLACE = register("fluid_place");
   /** This event is broadcast when an entity falls far enough to take fall damage. */
   public static final GameEvent HIT_GROUND = register("hit_ground");
   public static final GameEvent INSTRUMENT_PLAY = register("instrument_play");
   public static final GameEvent ITEM_INTERACT_FINISH = register("item_interact_finish");
   public static final GameEvent ITEM_INTERACT_START = register("item_interact_start");
   public static final GameEvent JUKEBOX_PLAY = register("jukebox_play", 10);
   public static final GameEvent JUKEBOX_STOP_PLAY = register("jukebox_stop_play", 10);
   /** This event is broadcast when lightning strikes a block. */
   public static final GameEvent LIGHTNING_STRIKE = register("lightning_strike");
   public static final GameEvent NOTE_BLOCK_PLAY = register("note_block_play");
   /** This event is broadcast when an entity such as a creeper or TNT begins exploding. */
   public static final GameEvent PRIME_FUSE = register("prime_fuse");
   /** This event is broadcast when a projectile hits something. */
   public static final GameEvent PROJECTILE_LAND = register("projectile_land");
   /** This event is broadcast when a projectile is fired. */
   public static final GameEvent PROJECTILE_SHOOT = register("projectile_shoot");
   public static final GameEvent SCULK_SENSOR_TENDRILS_CLICKING = register("sculk_sensor_tendrils_clicking");
   /**
    * This event is broadcast when a shear is used. This includes disarming tripwires, harvesting honeycombs, carving
    * pumpkins, etc.
    */
   public static final GameEvent SHEAR = register("shear");
   public static final GameEvent SHRIEK = register("shriek", 32);
   /**
    * This event is broadcast wen an entity splashes in the water. This includes boats paddling or hitting bubble
    * columns.
    */
   public static final GameEvent SPLASH = register("splash");
   /** This event is broadcast when an entity moves on the ground. This includes entities such as minecarts. */
   public static final GameEvent STEP = register("step");
   /** This event is broadcast as an entity swims around in water. */
   public static final GameEvent SWIM = register("swim");
   public static final GameEvent TELEPORT = register("teleport");
   public static final GameEvent RESONATE_1 = register("resonate_1");
   public static final GameEvent RESONATE_2 = register("resonate_2");
   public static final GameEvent RESONATE_3 = register("resonate_3");
   public static final GameEvent RESONATE_4 = register("resonate_4");
   public static final GameEvent RESONATE_5 = register("resonate_5");
   public static final GameEvent RESONATE_6 = register("resonate_6");
   public static final GameEvent RESONATE_7 = register("resonate_7");
   public static final GameEvent RESONATE_8 = register("resonate_8");
   public static final GameEvent RESONATE_9 = register("resonate_9");
   public static final GameEvent RESONATE_10 = register("resonate_10");
   public static final GameEvent RESONATE_11 = register("resonate_11");
   public static final GameEvent RESONATE_12 = register("resonate_12");
   public static final GameEvent RESONATE_13 = register("resonate_13");
   public static final GameEvent RESONATE_14 = register("resonate_14");
   public static final GameEvent RESONATE_15 = register("resonate_15");
   /**
    * The default notification radius for events to be broadcasted. @see
    * net.minecraft.world.level.gameevent.GameEvent#register
    */
   public static final int DEFAULT_NOTIFICATION_RADIUS = 16;
   /**
    * The name of the event. This is primarily used for debugging game events. @see
    * net.minecraft.client.renderer.debug.GameEventListenerRenderer#render
    */
   private final String name;
   /**
    * The radius around an event source to broadcast this event. Any listeners within this radius will be notified when
    * the event happens.
    */
   private final int notificationRadius;
   private final Holder.Reference<GameEvent> builtInRegistryHolder = BuiltInRegistries.GAME_EVENT.createIntrusiveHolder(this);

   public GameEvent(String pName, int pNotificationRadius) {
      this.name = pName;
      this.notificationRadius = pNotificationRadius;
   }

   /**
    * Gets the name of the event. This is primarily used for debugging game events.
    * @see net.minecraft.client.renderer.debug.GameEventListenerRenderer#render
    */
   public String getName() {
      return this.name;
   }

   /**
    * Gets the radius around an event source to broadcast the event. Any valid listeners within this radius will be
    * notified when the event happens.
    */
   public int getNotificationRadius() {
      return this.notificationRadius;
   }

   /**
    * Creates a new game event with the default notification radius and then registers it with the game registry.
    * @see net.minecraft.core.Registry#GAME_EVENT
    * @return The newly registered game event.
    * @param pName The name of the event. This will be used to generate the namespaced identifier for the event.
    */
   private static GameEvent register(String pName) {
      return register(pName, 16);
   }

   /**
    * Creates a new game event and then registers it with the game registry.
    * @see net.minecraft.core.Registry#GAME_EVENT
    * @return The newly registered game event.
    * @param pName The name of the event. This will be used to generate the namespaced identifier for the event.
    * @param pNotificationRadius The radius around an event source to broadcast the event. Any valid listeners within
    * this radius will be notified when the event happens.
    */
   private static GameEvent register(String pName, int pNotificationRadius) {
      return Registry.register(BuiltInRegistries.GAME_EVENT, pName, new GameEvent(pName, pNotificationRadius));
   }

   public String toString() {
      return "Game Event{ " + this.name + " , " + this.notificationRadius + "}";
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<GameEvent> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   public boolean is(TagKey<GameEvent> pEventTag) {
      return this.builtInRegistryHolder.is(pEventTag);
   }

   public static record Context(@Nullable Entity sourceEntity, @Nullable BlockState affectedState) {
      public static GameEvent.Context of(@Nullable Entity pSourceEntity) {
         return new GameEvent.Context(pSourceEntity, (BlockState)null);
      }

      public static GameEvent.Context of(@Nullable BlockState pAffectedState) {
         return new GameEvent.Context((Entity)null, pAffectedState);
      }

      public static GameEvent.Context of(@Nullable Entity pSourceEntity, @Nullable BlockState pAffectedState) {
         return new GameEvent.Context(pSourceEntity, pAffectedState);
      }
   }

   public static final class ListenerInfo implements Comparable<GameEvent.ListenerInfo> {
      private final GameEvent gameEvent;
      private final Vec3 source;
      private final GameEvent.Context context;
      private final GameEventListener recipient;
      private final double distanceToRecipient;

      public ListenerInfo(GameEvent pGameEvent, Vec3 pSource, GameEvent.Context pContext, GameEventListener pRecipient, Vec3 pRecipientPosition) {
         this.gameEvent = pGameEvent;
         this.source = pSource;
         this.context = pContext;
         this.recipient = pRecipient;
         this.distanceToRecipient = pSource.distanceToSqr(pRecipientPosition);
      }

      public int compareTo(GameEvent.ListenerInfo pOther) {
         return Double.compare(this.distanceToRecipient, pOther.distanceToRecipient);
      }

      public GameEvent gameEvent() {
         return this.gameEvent;
      }

      public Vec3 source() {
         return this.source;
      }

      public GameEvent.Context context() {
         return this.context;
      }

      public GameEventListener recipient() {
         return this.recipient;
      }
   }
}