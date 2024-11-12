package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFunctionArgument;

public class ArgumentTypeInfos {
   private static final Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS = Maps.newHashMap();

   /**
    * Forge: Use this in conjunction with a
    * {@link net.minecraftforge.registries.DeferredRegister#register(String, java.util.function.Supplier) DeferredRegister#register(String, Supplier)}
    * call to both populate the {@code BY_CLASS} map and register the argument type info so it can be used in commands.
    *
    * @param infoClass the class type of the argument type info
    * @param argumentTypeInfo the argument type info instance
    * @return the provided argument type info instance for chaining
    */
   public static synchronized <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> I registerByClass(Class<A> infoClass, I argumentTypeInfo) {
      BY_CLASS.put(infoClass, argumentTypeInfo);
      return argumentTypeInfo;
   }

   private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> pRegistry, String pId, Class<? extends A> pArgumentClass, ArgumentTypeInfo<A, T> pInfo) {
      BY_CLASS.put(pArgumentClass, pInfo);
      return Registry.register(pRegistry, pId, pInfo);
   }

   public static ArgumentTypeInfo<?, ?> bootstrap(Registry<ArgumentTypeInfo<?, ?>> pRegistry) {
      register(pRegistry, "brigadier:bool", BoolArgumentType.class, SingletonArgumentInfo.contextFree(BoolArgumentType::bool));
      register(pRegistry, "brigadier:float", FloatArgumentType.class, new FloatArgumentInfo());
      register(pRegistry, "brigadier:double", DoubleArgumentType.class, new DoubleArgumentInfo());
      register(pRegistry, "brigadier:integer", IntegerArgumentType.class, new IntegerArgumentInfo());
      register(pRegistry, "brigadier:long", LongArgumentType.class, new LongArgumentInfo());
      register(pRegistry, "brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
      register(pRegistry, "entity", EntityArgument.class, new EntityArgument.Info());
      register(pRegistry, "game_profile", GameProfileArgument.class, SingletonArgumentInfo.contextFree(GameProfileArgument::gameProfile));
      register(pRegistry, "block_pos", BlockPosArgument.class, SingletonArgumentInfo.contextFree(BlockPosArgument::blockPos));
      register(pRegistry, "column_pos", ColumnPosArgument.class, SingletonArgumentInfo.contextFree(ColumnPosArgument::columnPos));
      register(pRegistry, "vec3", Vec3Argument.class, SingletonArgumentInfo.contextFree(Vec3Argument::vec3));
      register(pRegistry, "vec2", Vec2Argument.class, SingletonArgumentInfo.contextFree(Vec2Argument::vec2));
      register(pRegistry, "block_state", BlockStateArgument.class, SingletonArgumentInfo.contextAware(BlockStateArgument::block));
      register(pRegistry, "block_predicate", BlockPredicateArgument.class, SingletonArgumentInfo.contextAware(BlockPredicateArgument::blockPredicate));
      register(pRegistry, "item_stack", ItemArgument.class, SingletonArgumentInfo.contextAware(ItemArgument::item));
      register(pRegistry, "item_predicate", ItemPredicateArgument.class, SingletonArgumentInfo.contextAware(ItemPredicateArgument::itemPredicate));
      register(pRegistry, "color", ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::color));
      register(pRegistry, "component", ComponentArgument.class, SingletonArgumentInfo.contextFree(ComponentArgument::textComponent));
      register(pRegistry, "message", MessageArgument.class, SingletonArgumentInfo.contextFree(MessageArgument::message));
      register(pRegistry, "nbt_compound_tag", CompoundTagArgument.class, SingletonArgumentInfo.contextFree(CompoundTagArgument::compoundTag));
      register(pRegistry, "nbt_tag", NbtTagArgument.class, SingletonArgumentInfo.contextFree(NbtTagArgument::nbtTag));
      register(pRegistry, "nbt_path", NbtPathArgument.class, SingletonArgumentInfo.contextFree(NbtPathArgument::nbtPath));
      register(pRegistry, "objective", ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
      register(pRegistry, "objective_criteria", ObjectiveCriteriaArgument.class, SingletonArgumentInfo.contextFree(ObjectiveCriteriaArgument::criteria));
      register(pRegistry, "operation", OperationArgument.class, SingletonArgumentInfo.contextFree(OperationArgument::operation));
      register(pRegistry, "particle", ParticleArgument.class, SingletonArgumentInfo.contextAware(ParticleArgument::particle));
      register(pRegistry, "angle", AngleArgument.class, SingletonArgumentInfo.contextFree(AngleArgument::angle));
      register(pRegistry, "rotation", RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation));
      register(pRegistry, "scoreboard_slot", ScoreboardSlotArgument.class, SingletonArgumentInfo.contextFree(ScoreboardSlotArgument::displaySlot));
      register(pRegistry, "score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Info());
      register(pRegistry, "swizzle", SwizzleArgument.class, SingletonArgumentInfo.contextFree(SwizzleArgument::swizzle));
      register(pRegistry, "team", TeamArgument.class, SingletonArgumentInfo.contextFree(TeamArgument::team));
      register(pRegistry, "item_slot", SlotArgument.class, SingletonArgumentInfo.contextFree(SlotArgument::slot));
      register(pRegistry, "resource_location", ResourceLocationArgument.class, SingletonArgumentInfo.contextFree(ResourceLocationArgument::id));
      register(pRegistry, "function", FunctionArgument.class, SingletonArgumentInfo.contextFree(FunctionArgument::functions));
      register(pRegistry, "entity_anchor", EntityAnchorArgument.class, SingletonArgumentInfo.contextFree(EntityAnchorArgument::anchor));
      register(pRegistry, "int_range", RangeArgument.Ints.class, SingletonArgumentInfo.contextFree(RangeArgument::intRange));
      register(pRegistry, "float_range", RangeArgument.Floats.class, SingletonArgumentInfo.contextFree(RangeArgument::floatRange));
      register(pRegistry, "dimension", DimensionArgument.class, SingletonArgumentInfo.contextFree(DimensionArgument::dimension));
      register(pRegistry, "gamemode", GameModeArgument.class, SingletonArgumentInfo.contextFree(GameModeArgument::gameMode));
      register(pRegistry, "time", TimeArgument.class, new TimeArgument.Info());
      register(pRegistry, "resource_or_tag", fixClassType(ResourceOrTagArgument.class), new ResourceOrTagArgument.Info<Object>());
      register(pRegistry, "resource_or_tag_key", fixClassType(ResourceOrTagKeyArgument.class), new ResourceOrTagKeyArgument.Info<Object>());
      register(pRegistry, "resource", fixClassType(ResourceArgument.class), new ResourceArgument.Info<Object>());
      register(pRegistry, "resource_key", fixClassType(ResourceKeyArgument.class), new ResourceKeyArgument.Info<Object>());
      register(pRegistry, "template_mirror", TemplateMirrorArgument.class, SingletonArgumentInfo.contextFree(TemplateMirrorArgument::templateMirror));
      register(pRegistry, "template_rotation", TemplateRotationArgument.class, SingletonArgumentInfo.contextFree(TemplateRotationArgument::templateRotation));
      register(pRegistry, "heightmap", HeightmapTypeArgument.class, SingletonArgumentInfo.contextFree(HeightmapTypeArgument::heightmap));
      // Forge: Register before gametest arguments to provide forge server <-> vanilla client interop and matching int ids
      var uuidInfo = register(pRegistry, "uuid", UuidArgument.class, SingletonArgumentInfo.contextFree(UuidArgument::uuid));
      if (true) { // Forge: Always register gametest arguments to prevent issues when connecting from gametest-enabled client/server to non-gametest-enabled client/server
         register(pRegistry, "test_argument", TestFunctionArgument.class, SingletonArgumentInfo.contextFree(TestFunctionArgument::testFunctionArgument));
         register(pRegistry, "test_class", TestClassNameArgument.class, SingletonArgumentInfo.contextFree(TestClassNameArgument::testClassName));
      }

      return uuidInfo;
   }

   private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> pType) {
      return (Class<T>)pType;
   }

   public static boolean isClassRecognized(Class<?> pClazz) {
      return BY_CLASS.containsKey(pClazz);
   }

   public static <A extends ArgumentType<?>> ArgumentTypeInfo<A, ?> byClass(A pArgument) {
      ArgumentTypeInfo<?, ?> argumenttypeinfo = BY_CLASS.get(pArgument.getClass());
      if (argumenttypeinfo == null) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "Unrecognized argument type %s (%s)", pArgument, pArgument.getClass()));
      } else {
         return (ArgumentTypeInfo<A, ?>)argumenttypeinfo;
      }
   }

   public static <A extends ArgumentType<?>> ArgumentTypeInfo.Template<A> unpack(A pArgument) {
      return byClass(pArgument).unpack(pArgument);
   }
}
