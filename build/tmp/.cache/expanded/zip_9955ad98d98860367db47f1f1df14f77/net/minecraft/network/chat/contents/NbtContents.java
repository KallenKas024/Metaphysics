package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class NbtContents implements ComponentContents {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final boolean interpreting;
   private final Optional<Component> separator;
   private final String nbtPathPattern;
   private final DataSource dataSource;
   @Nullable
   protected final NbtPathArgument.NbtPath compiledNbtPath;

   public NbtContents(String pNbtPathPattern, boolean pInterpreting, Optional<Component> pSeparator, DataSource pDataSource) {
      this(pNbtPathPattern, compileNbtPath(pNbtPathPattern), pInterpreting, pSeparator, pDataSource);
   }

   private NbtContents(String pNbtPathPattern, @Nullable NbtPathArgument.NbtPath pCompiledNbtPath, boolean pInterpreting, Optional<Component> pSeparator, DataSource pDataSource) {
      this.nbtPathPattern = pNbtPathPattern;
      this.compiledNbtPath = pCompiledNbtPath;
      this.interpreting = pInterpreting;
      this.separator = pSeparator;
      this.dataSource = pDataSource;
   }

   @Nullable
   private static NbtPathArgument.NbtPath compileNbtPath(String pNbtPathPattern) {
      try {
         return (new NbtPathArgument()).parse(new StringReader(pNbtPathPattern));
      } catch (CommandSyntaxException commandsyntaxexception) {
         return null;
      }
   }

   public String getNbtPath() {
      return this.nbtPathPattern;
   }

   public boolean isInterpreting() {
      return this.interpreting;
   }

   public Optional<Component> getSeparator() {
      return this.separator;
   }

   public DataSource getDataSource() {
      return this.dataSource;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof NbtContents) {
            NbtContents nbtcontents = (NbtContents)pOther;
            if (this.dataSource.equals(nbtcontents.dataSource) && this.separator.equals(nbtcontents.separator) && this.interpreting == nbtcontents.interpreting && this.nbtPathPattern.equals(nbtcontents.nbtPathPattern)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = this.interpreting ? 1 : 0;
      i = 31 * i + this.separator.hashCode();
      i = 31 * i + this.nbtPathPattern.hashCode();
      return 31 * i + this.dataSource.hashCode();
   }

   public String toString() {
      return "nbt{" + this.dataSource + ", interpreting=" + this.interpreting + ", separator=" + this.separator + "}";
   }

   public MutableComponent resolve(@Nullable CommandSourceStack pNbtPathPattern, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      if (pNbtPathPattern != null && this.compiledNbtPath != null) {
         Stream<String> stream = this.dataSource.getData(pNbtPathPattern).flatMap((p_237417_) -> {
            try {
               return this.compiledNbtPath.get(p_237417_).stream();
            } catch (CommandSyntaxException commandsyntaxexception) {
               return Stream.empty();
            }
         }).map(Tag::getAsString);
         if (this.interpreting) {
            Component component = DataFixUtils.orElse(ComponentUtils.updateForEntity(pNbtPathPattern, this.separator, pEntity, pRecursionDepth), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR);
            return stream.flatMap((p_237408_) -> {
               try {
                  MutableComponent mutablecomponent = Component.Serializer.fromJson(p_237408_);
                  return Stream.of(ComponentUtils.updateForEntity(pNbtPathPattern, mutablecomponent, pEntity, pRecursionDepth));
               } catch (Exception exception) {
                  LOGGER.warn("Failed to parse component: {}", p_237408_, exception);
                  return Stream.of();
               }
            }).reduce((p_237420_, p_237421_) -> {
               return p_237420_.append(component).append(p_237421_);
            }).orElseGet(Component::empty);
         } else {
            return ComponentUtils.updateForEntity(pNbtPathPattern, this.separator, pEntity, pRecursionDepth).map((p_237415_) -> {
               return stream.map(Component::literal).reduce((p_237424_, p_237425_) -> {
                  return p_237424_.append(p_237415_).append(p_237425_);
               }).orElseGet(Component::empty);
            }).orElseGet(() -> {
               return Component.literal(stream.collect(Collectors.joining(", ")));
            });
         }
      } else {
         return Component.empty();
      }
   }
}