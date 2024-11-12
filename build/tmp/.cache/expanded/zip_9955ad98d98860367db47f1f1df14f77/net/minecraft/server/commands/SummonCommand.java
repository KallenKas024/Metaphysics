package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
   private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
   private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
      pDispatcher.register(Commands.literal("summon").requires((p_138819_) -> {
         return p_138819_.hasPermission(2);
      }).then(Commands.argument("entity", ResourceArgument.resource(pContext, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((p_248175_) -> {
         return spawnEntity(p_248175_.getSource(), ResourceArgument.getSummonableEntityType(p_248175_, "entity"), p_248175_.getSource().getPosition(), new CompoundTag(), true);
      }).then(Commands.argument("pos", Vec3Argument.vec3()).executes((p_248173_) -> {
         return spawnEntity(p_248173_.getSource(), ResourceArgument.getSummonableEntityType(p_248173_, "entity"), Vec3Argument.getVec3(p_248173_, "pos"), new CompoundTag(), true);
      }).then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((p_248174_) -> {
         return spawnEntity(p_248174_.getSource(), ResourceArgument.getSummonableEntityType(p_248174_, "entity"), Vec3Argument.getVec3(p_248174_, "pos"), CompoundTagArgument.getCompoundTag(p_248174_, "nbt"), false);
      })))));
   }

   public static Entity createEntity(CommandSourceStack pSource, Holder.Reference<EntityType<?>> pType, Vec3 pPos, CompoundTag pTag, boolean pRandomizeProperties) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(pPos);
      if (!Level.isInSpawnableBounds(blockpos)) {
         throw INVALID_POSITION.create();
      } else {
         CompoundTag compoundtag = pTag.copy();
         compoundtag.putString("id", pType.key().location().toString());
         ServerLevel serverlevel = pSource.getLevel();
         Entity entity = EntityType.loadEntityRecursive(compoundtag, serverlevel, (p_138828_) -> {
            p_138828_.moveTo(pPos.x, pPos.y, pPos.z, p_138828_.getYRot(), p_138828_.getXRot());
            return p_138828_;
         });
         if (entity == null) {
            throw ERROR_FAILED.create();
         } else {
            if (pRandomizeProperties && entity instanceof Mob) {
               ((Mob)entity).finalizeSpawn(pSource.getLevel(), pSource.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, (SpawnGroupData)null, (CompoundTag)null);
            }

            if (!serverlevel.tryAddFreshEntityWithPassengers(entity)) {
               throw ERROR_DUPLICATE_UUID.create();
            } else {
               return entity;
            }
         }
      }
   }

   private static int spawnEntity(CommandSourceStack pSource, Holder.Reference<EntityType<?>> pType, Vec3 pPos, CompoundTag pTag, boolean pRandomizeProperties) throws CommandSyntaxException {
      Entity entity = createEntity(pSource, pType, pPos, pTag, pRandomizeProperties);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.summon.success", entity.getDisplayName());
      }, true);
      return 1;
   }
}