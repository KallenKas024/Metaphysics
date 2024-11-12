package net.minecraft.world.level.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BannerPatterns {
   public static final ResourceKey<BannerPattern> BASE = create("base");
   public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_LEFT = create("square_bottom_left");
   public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_RIGHT = create("square_bottom_right");
   public static final ResourceKey<BannerPattern> SQUARE_TOP_LEFT = create("square_top_left");
   public static final ResourceKey<BannerPattern> SQUARE_TOP_RIGHT = create("square_top_right");
   public static final ResourceKey<BannerPattern> STRIPE_BOTTOM = create("stripe_bottom");
   public static final ResourceKey<BannerPattern> STRIPE_TOP = create("stripe_top");
   public static final ResourceKey<BannerPattern> STRIPE_LEFT = create("stripe_left");
   public static final ResourceKey<BannerPattern> STRIPE_RIGHT = create("stripe_right");
   public static final ResourceKey<BannerPattern> STRIPE_CENTER = create("stripe_center");
   public static final ResourceKey<BannerPattern> STRIPE_MIDDLE = create("stripe_middle");
   public static final ResourceKey<BannerPattern> STRIPE_DOWNRIGHT = create("stripe_downright");
   public static final ResourceKey<BannerPattern> STRIPE_DOWNLEFT = create("stripe_downleft");
   public static final ResourceKey<BannerPattern> STRIPE_SMALL = create("small_stripes");
   public static final ResourceKey<BannerPattern> CROSS = create("cross");
   public static final ResourceKey<BannerPattern> STRAIGHT_CROSS = create("straight_cross");
   public static final ResourceKey<BannerPattern> TRIANGLE_BOTTOM = create("triangle_bottom");
   public static final ResourceKey<BannerPattern> TRIANGLE_TOP = create("triangle_top");
   public static final ResourceKey<BannerPattern> TRIANGLES_BOTTOM = create("triangles_bottom");
   public static final ResourceKey<BannerPattern> TRIANGLES_TOP = create("triangles_top");
   public static final ResourceKey<BannerPattern> DIAGONAL_LEFT = create("diagonal_left");
   public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT = create("diagonal_up_right");
   public static final ResourceKey<BannerPattern> DIAGONAL_LEFT_MIRROR = create("diagonal_up_left");
   public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT_MIRROR = create("diagonal_right");
   public static final ResourceKey<BannerPattern> CIRCLE_MIDDLE = create("circle");
   public static final ResourceKey<BannerPattern> RHOMBUS_MIDDLE = create("rhombus");
   public static final ResourceKey<BannerPattern> HALF_VERTICAL = create("half_vertical");
   public static final ResourceKey<BannerPattern> HALF_HORIZONTAL = create("half_horizontal");
   public static final ResourceKey<BannerPattern> HALF_VERTICAL_MIRROR = create("half_vertical_right");
   public static final ResourceKey<BannerPattern> HALF_HORIZONTAL_MIRROR = create("half_horizontal_bottom");
   public static final ResourceKey<BannerPattern> BORDER = create("border");
   public static final ResourceKey<BannerPattern> CURLY_BORDER = create("curly_border");
   public static final ResourceKey<BannerPattern> GRADIENT = create("gradient");
   public static final ResourceKey<BannerPattern> GRADIENT_UP = create("gradient_up");
   public static final ResourceKey<BannerPattern> BRICKS = create("bricks");
   public static final ResourceKey<BannerPattern> GLOBE = create("globe");
   public static final ResourceKey<BannerPattern> CREEPER = create("creeper");
   public static final ResourceKey<BannerPattern> SKULL = create("skull");
   public static final ResourceKey<BannerPattern> FLOWER = create("flower");
   public static final ResourceKey<BannerPattern> MOJANG = create("mojang");
   public static final ResourceKey<BannerPattern> PIGLIN = create("piglin");

   private static ResourceKey<BannerPattern> create(String pName) {
      return ResourceKey.create(Registries.BANNER_PATTERN, new ResourceLocation(pName));
   }

   public static BannerPattern bootstrap(Registry<BannerPattern> pRegistry) {
      Registry.register(pRegistry, BASE, new BannerPattern("b"));
      Registry.register(pRegistry, SQUARE_BOTTOM_LEFT, new BannerPattern("bl"));
      Registry.register(pRegistry, SQUARE_BOTTOM_RIGHT, new BannerPattern("br"));
      Registry.register(pRegistry, SQUARE_TOP_LEFT, new BannerPattern("tl"));
      Registry.register(pRegistry, SQUARE_TOP_RIGHT, new BannerPattern("tr"));
      Registry.register(pRegistry, STRIPE_BOTTOM, new BannerPattern("bs"));
      Registry.register(pRegistry, STRIPE_TOP, new BannerPattern("ts"));
      Registry.register(pRegistry, STRIPE_LEFT, new BannerPattern("ls"));
      Registry.register(pRegistry, STRIPE_RIGHT, new BannerPattern("rs"));
      Registry.register(pRegistry, STRIPE_CENTER, new BannerPattern("cs"));
      Registry.register(pRegistry, STRIPE_MIDDLE, new BannerPattern("ms"));
      Registry.register(pRegistry, STRIPE_DOWNRIGHT, new BannerPattern("drs"));
      Registry.register(pRegistry, STRIPE_DOWNLEFT, new BannerPattern("dls"));
      Registry.register(pRegistry, STRIPE_SMALL, new BannerPattern("ss"));
      Registry.register(pRegistry, CROSS, new BannerPattern("cr"));
      Registry.register(pRegistry, STRAIGHT_CROSS, new BannerPattern("sc"));
      Registry.register(pRegistry, TRIANGLE_BOTTOM, new BannerPattern("bt"));
      Registry.register(pRegistry, TRIANGLE_TOP, new BannerPattern("tt"));
      Registry.register(pRegistry, TRIANGLES_BOTTOM, new BannerPattern("bts"));
      Registry.register(pRegistry, TRIANGLES_TOP, new BannerPattern("tts"));
      Registry.register(pRegistry, DIAGONAL_LEFT, new BannerPattern("ld"));
      Registry.register(pRegistry, DIAGONAL_RIGHT, new BannerPattern("rd"));
      Registry.register(pRegistry, DIAGONAL_LEFT_MIRROR, new BannerPattern("lud"));
      Registry.register(pRegistry, DIAGONAL_RIGHT_MIRROR, new BannerPattern("rud"));
      Registry.register(pRegistry, CIRCLE_MIDDLE, new BannerPattern("mc"));
      Registry.register(pRegistry, RHOMBUS_MIDDLE, new BannerPattern("mr"));
      Registry.register(pRegistry, HALF_VERTICAL, new BannerPattern("vh"));
      Registry.register(pRegistry, HALF_HORIZONTAL, new BannerPattern("hh"));
      Registry.register(pRegistry, HALF_VERTICAL_MIRROR, new BannerPattern("vhr"));
      Registry.register(pRegistry, HALF_HORIZONTAL_MIRROR, new BannerPattern("hhb"));
      Registry.register(pRegistry, BORDER, new BannerPattern("bo"));
      Registry.register(pRegistry, CURLY_BORDER, new BannerPattern("cbo"));
      Registry.register(pRegistry, GRADIENT, new BannerPattern("gra"));
      Registry.register(pRegistry, GRADIENT_UP, new BannerPattern("gru"));
      Registry.register(pRegistry, BRICKS, new BannerPattern("bri"));
      Registry.register(pRegistry, GLOBE, new BannerPattern("glb"));
      Registry.register(pRegistry, CREEPER, new BannerPattern("cre"));
      Registry.register(pRegistry, SKULL, new BannerPattern("sku"));
      Registry.register(pRegistry, FLOWER, new BannerPattern("flo"));
      Registry.register(pRegistry, MOJANG, new BannerPattern("moj"));
      return Registry.register(pRegistry, PIGLIN, new BannerPattern("pig"));
   }
}