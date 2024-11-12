package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketItem extends Item {
   public static final byte[] CRAFTABLE_DURATIONS = new byte[]{1, 2, 3};
   public static final String TAG_FIREWORKS = "Fireworks";
   public static final String TAG_EXPLOSION = "Explosion";
   public static final String TAG_EXPLOSIONS = "Explosions";
   public static final String TAG_FLIGHT = "Flight";
   public static final String TAG_EXPLOSION_TYPE = "Type";
   public static final String TAG_EXPLOSION_TRAIL = "Trail";
   public static final String TAG_EXPLOSION_FLICKER = "Flicker";
   public static final String TAG_EXPLOSION_COLORS = "Colors";
   public static final String TAG_EXPLOSION_FADECOLORS = "FadeColors";
   public static final double ROCKET_PLACEMENT_OFFSET = 0.15D;

   public FireworkRocketItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      if (!level.isClientSide) {
         ItemStack itemstack = pContext.getItemInHand();
         Vec3 vec3 = pContext.getClickLocation();
         Direction direction = pContext.getClickedFace();
         FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(level, pContext.getPlayer(), vec3.x + (double)direction.getStepX() * 0.15D, vec3.y + (double)direction.getStepY() * 0.15D, vec3.z + (double)direction.getStepZ() * 0.15D, itemstack);
         level.addFreshEntity(fireworkrocketentity);
         itemstack.shrink(1);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      if (pPlayer.isFallFlying()) {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         if (!pLevel.isClientSide) {
            FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(pLevel, itemstack, pPlayer);
            pLevel.addFreshEntity(fireworkrocketentity);
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            pPlayer.awardStat(Stats.ITEM_USED.get(this));
         }

         return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pHand), pLevel.isClientSide());
      } else {
         return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
      }
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      CompoundTag compoundtag = pStack.getTagElement("Fireworks");
      if (compoundtag != null) {
         if (compoundtag.contains("Flight", 99)) {
            pTooltip.add(Component.translatable("item.minecraft.firework_rocket.flight").append(CommonComponents.SPACE).append(String.valueOf((int)compoundtag.getByte("Flight"))).withStyle(ChatFormatting.GRAY));
         }

         ListTag listtag = compoundtag.getList("Explosions", 10);
         if (!listtag.isEmpty()) {
            for(int i = 0; i < listtag.size(); ++i) {
               CompoundTag compoundtag1 = listtag.getCompound(i);
               List<Component> list = Lists.newArrayList();
               FireworkStarItem.appendHoverText(compoundtag1, list);
               if (!list.isEmpty()) {
                  for(int j = 1; j < list.size(); ++j) {
                     list.set(j, Component.literal("  ").append(list.get(j)).withStyle(ChatFormatting.GRAY));
                  }

                  pTooltip.addAll(list);
               }
            }
         }

      }
   }

   public static void setDuration(ItemStack pStack, byte pFlightDuration) {
      pStack.getOrCreateTagElement("Fireworks").putByte("Flight", pFlightDuration);
   }

   public ItemStack getDefaultInstance() {
      ItemStack itemstack = new ItemStack(this);
      setDuration(itemstack, (byte)1);
      return itemstack;
   }

   public static enum Shape implements net.minecraftforge.common.IExtensibleEnum {
      SMALL_BALL(0, "small_ball"),
      LARGE_BALL(1, "large_ball"),
      STAR(2, "star"),
      CREEPER(3, "creeper"),
      BURST(4, "burst");

      private static final IntFunction<FireworkRocketItem.Shape> BY_ID = ByIdMap.continuous(FireworkRocketItem.Shape::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      private final int id;
      private final String name;

      private Shape(int pId, String pName) {
         this.id = pId;
         this.name = pName;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public void save(net.minecraft.nbt.CompoundTag tag) {
         tag.putByte("Type", (byte)getId());
         tag.putString("forge:shape_type", name());
      }

      /** Use {getShape(ComoundTag)} */
      @Deprecated
      public static FireworkRocketItem.Shape byId(int pIndex) {
         return BY_ID.apply(pIndex);
      }

      public static FireworkRocketItem.Shape getShape(net.minecraft.nbt.CompoundTag tag) {
         var name = tag.contains("forge:shape_type", 8) ? tag.getString("forge:shape_type") : null;
         if (name == null) return byId(tag.getByte("Type"));
         for (var ret : values())
             if (ret.name().equals(name))
                 return ret;
         return SMALL_BALL;
      }

      public static Shape create(String registryName, int id, String shapeName) {
         throw new IllegalStateException("Enum not extended");
      }
   }
}
