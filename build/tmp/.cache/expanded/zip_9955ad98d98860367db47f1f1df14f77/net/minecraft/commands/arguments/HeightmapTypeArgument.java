package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapTypeArgument extends StringRepresentableArgument<Heightmap.Types> {
   private static final Codec<Heightmap.Types> LOWER_CASE_CODEC = StringRepresentable.fromEnumWithMapping(HeightmapTypeArgument::keptTypes, (p_275334_) -> {
      return p_275334_.toLowerCase(Locale.ROOT);
   });

   private static Heightmap.Types[] keptTypes() {
      return Arrays.stream(Heightmap.Types.values()).filter(Heightmap.Types::keepAfterWorldgen).toArray((p_275295_) -> {
         return new Heightmap.Types[p_275295_];
      });
   }

   private HeightmapTypeArgument() {
      super(LOWER_CASE_CODEC, HeightmapTypeArgument::keptTypes);
   }

   public static HeightmapTypeArgument heightmap() {
      return new HeightmapTypeArgument();
   }

   public static Heightmap.Types getHeightmap(CommandContext<CommandSourceStack> pContext, String pArgument) {
      return pContext.getArgument(pArgument, Heightmap.Types.class);
   }

   protected String convertId(String pId) {
      return pId.toLowerCase(Locale.ROOT);
   }
}