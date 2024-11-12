package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DataCommands {
   private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.data.merge.failed"));
   private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType((p_139491_) -> {
      return Component.translatable("commands.data.get.invalid", p_139491_);
   });
   private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType((p_139481_) -> {
      return Component.translatable("commands.data.get.unknown", p_139481_);
   });
   private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Component.translatable("commands.data.get.multiple"));
   private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType((p_139448_) -> {
      return Component.translatable("commands.data.modify.expected_object", p_139448_);
   });
   private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType((p_264853_) -> {
      return Component.translatable("commands.data.modify.expected_value", p_264853_);
   });
   private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType((p_288740_, p_288741_) -> {
      return Component.translatable("commands.data.modify.invalid_substring", p_288740_, p_288741_);
   });
   public static final List<Function<String, DataCommands.DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);
   public static final List<DataCommands.DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream().map((p_139450_) -> {
      return p_139450_.apply("target");
   }).collect(ImmutableList.toImmutableList());
   public static final List<DataCommands.DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream().map((p_139410_) -> {
      return p_139410_.apply("source");
   }).collect(ImmutableList.toImmutableList());

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("data").requires((p_139381_) -> {
         return p_139381_.hasPermission(2);
      });

      for(DataCommands.DataProvider datacommands$dataprovider : TARGET_PROVIDERS) {
         literalargumentbuilder.then(datacommands$dataprovider.wrap(Commands.literal("merge"), (p_139471_) -> {
            return p_139471_.then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((p_142857_) -> {
               return mergeData(p_142857_.getSource(), datacommands$dataprovider.access(p_142857_), CompoundTagArgument.getCompoundTag(p_142857_, "nbt"));
            }));
         })).then(datacommands$dataprovider.wrap(Commands.literal("get"), (p_139453_) -> {
            return p_139453_.executes((p_142849_) -> {
               return getData(p_142849_.getSource(), datacommands$dataprovider.access(p_142849_));
            }).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((p_142841_) -> {
               return getData(p_142841_.getSource(), datacommands$dataprovider.access(p_142841_), NbtPathArgument.getPath(p_142841_, "path"));
            }).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes((p_142833_) -> {
               return getNumeric(p_142833_.getSource(), datacommands$dataprovider.access(p_142833_), NbtPathArgument.getPath(p_142833_, "path"), DoubleArgumentType.getDouble(p_142833_, "scale"));
            })));
         })).then(datacommands$dataprovider.wrap(Commands.literal("remove"), (p_139413_) -> {
            return p_139413_.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((p_142820_) -> {
               return removeData(p_142820_.getSource(), datacommands$dataprovider.access(p_142820_), NbtPathArgument.getPath(p_142820_, "path"));
            }));
         })).then(decorateModification((p_139368_, p_139369_) -> {
            p_139368_.then(Commands.literal("insert").then(Commands.argument("index", IntegerArgumentType.integer()).then(p_139369_.create((p_142859_, p_142860_, p_142861_, p_142862_) -> {
               return p_142861_.insert(IntegerArgumentType.getInteger(p_142859_, "index"), p_142860_, p_142862_);
            })))).then(Commands.literal("prepend").then(p_139369_.create((p_142851_, p_142852_, p_142853_, p_142854_) -> {
               return p_142853_.insert(0, p_142852_, p_142854_);
            }))).then(Commands.literal("append").then(p_139369_.create((p_142843_, p_142844_, p_142845_, p_142846_) -> {
               return p_142845_.insert(-1, p_142844_, p_142846_);
            }))).then(Commands.literal("set").then(p_139369_.create((p_142835_, p_142836_, p_142837_, p_142838_) -> {
               return p_142837_.set(p_142836_, Iterables.getLast(p_142838_));
            }))).then(Commands.literal("merge").then(p_139369_.create((p_142822_, p_142823_, p_142824_, p_142825_) -> {
               CompoundTag compoundtag = new CompoundTag();

               for(Tag tag : p_142825_) {
                  if (NbtPathArgument.NbtPath.isTooDeep(tag, 0)) {
                     throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                  }

                  if (!(tag instanceof CompoundTag)) {
                     throw ERROR_EXPECTED_OBJECT.create(tag);
                  }

                  CompoundTag compoundtag1 = (CompoundTag)tag;
                  compoundtag.merge(compoundtag1);
               }

               Collection<Tag> collection = p_142824_.getOrCreate(p_142823_, CompoundTag::new);
               int i = 0;

               for(Tag tag1 : collection) {
                  if (!(tag1 instanceof CompoundTag)) {
                     throw ERROR_EXPECTED_OBJECT.create(tag1);
                  }

                  CompoundTag compoundtag2 = (CompoundTag)tag1;
                  CompoundTag $$12 = compoundtag2.copy();
                  compoundtag2.merge(compoundtag);
                  i += $$12.equals(compoundtag2) ? 0 : 1;
               }

               return i;
            })));
         }));
      }

      pDispatcher.register(literalargumentbuilder);
   }

   private static String getAsText(Tag pTag) throws CommandSyntaxException {
      if (pTag.getType().isValue()) {
         return pTag.getAsString();
      } else {
         throw ERROR_EXPECTED_VALUE.create(pTag);
      }
   }

   private static List<Tag> stringifyTagList(List<Tag> pTagList, DataCommands.StringProcessor pProcessor) throws CommandSyntaxException {
      List<Tag> list = new ArrayList<>(pTagList.size());

      for(Tag tag : pTagList) {
         String s = getAsText(tag);
         list.add(StringTag.valueOf(pProcessor.process(s)));
      }

      return list;
   }

   private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataCommands.DataManipulatorDecorator> pDecorator) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("modify");

      for(DataCommands.DataProvider datacommands$dataprovider : TARGET_PROVIDERS) {
         datacommands$dataprovider.wrap(literalargumentbuilder, (p_264816_) -> {
            ArgumentBuilder<CommandSourceStack, ?> argumentbuilder = Commands.argument("targetPath", NbtPathArgument.nbtPath());

            for(DataCommands.DataProvider datacommands$dataprovider1 : SOURCE_PROVIDERS) {
               pDecorator.accept(argumentbuilder, (p_142807_) -> {
                  return datacommands$dataprovider1.wrap(Commands.literal("from"), (p_142812_) -> {
                     return p_142812_.executes((p_264829_) -> {
                        return manipulateData(p_264829_, datacommands$dataprovider, p_142807_, getSingletonSource(p_264829_, datacommands$dataprovider1));
                     }).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes((p_264842_) -> {
                        return manipulateData(p_264842_, datacommands$dataprovider, p_142807_, resolveSourcePath(p_264842_, datacommands$dataprovider1));
                     }));
                  });
               });
               pDecorator.accept(argumentbuilder, (p_264836_) -> {
                  return datacommands$dataprovider1.wrap(Commands.literal("string"), (p_287357_) -> {
                     return p_287357_.executes((p_288732_) -> {
                        return manipulateData(p_288732_, datacommands$dataprovider, p_264836_, stringifyTagList(getSingletonSource(p_288732_, datacommands$dataprovider1), (p_264813_) -> {
                           return p_264813_;
                        }));
                     }).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes((p_288737_) -> {
                        return manipulateData(p_288737_, datacommands$dataprovider, p_264836_, stringifyTagList(resolveSourcePath(p_288737_, datacommands$dataprovider1), (p_264821_) -> {
                           return p_264821_;
                        }));
                     }).then(Commands.argument("start", IntegerArgumentType.integer()).executes((p_288753_) -> {
                        return manipulateData(p_288753_, datacommands$dataprovider, p_264836_, stringifyTagList(resolveSourcePath(p_288753_, datacommands$dataprovider1), (p_287353_) -> {
                           return substring(p_287353_, IntegerArgumentType.getInteger(p_288753_, "start"));
                        }));
                     }).then(Commands.argument("end", IntegerArgumentType.integer()).executes((p_288749_) -> {
                        return manipulateData(p_288749_, datacommands$dataprovider, p_264836_, stringifyTagList(resolveSourcePath(p_288749_, datacommands$dataprovider1), (p_287359_) -> {
                           return substring(p_287359_, IntegerArgumentType.getInteger(p_288749_, "start"), IntegerArgumentType.getInteger(p_288749_, "end"));
                        }));
                     }))));
                  });
               });
            }

            pDecorator.accept(argumentbuilder, (p_142799_) -> {
               return Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag()).executes((p_142803_) -> {
                  List<Tag> list = Collections.singletonList(NbtTagArgument.getNbtTag(p_142803_, "value"));
                  return manipulateData(p_142803_, datacommands$dataprovider, p_142799_, list);
               }));
            });
            return p_264816_.then(argumentbuilder);
         });
      }

      return literalargumentbuilder;
   }

   private static String validatedSubstring(String pSource, int pStart, int pEnd) throws CommandSyntaxException {
      if (pStart >= 0 && pEnd <= pSource.length() && pStart <= pEnd) {
         return pSource.substring(pStart, pEnd);
      } else {
         throw ERROR_INVALID_SUBSTRING.create(pStart, pEnd);
      }
   }

   private static String substring(String pSource, int pStart, int pEnd) throws CommandSyntaxException {
      int i = pSource.length();
      int j = getOffset(pStart, i);
      int k = getOffset(pEnd, i);
      return validatedSubstring(pSource, j, k);
   }

   private static String substring(String pSource, int pStart) throws CommandSyntaxException {
      int i = pSource.length();
      return validatedSubstring(pSource, getOffset(pStart, i), i);
   }

   private static int getOffset(int pIndex, int pLength) {
      return pIndex >= 0 ? pIndex : pLength + pIndex;
   }

   private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> pContext, DataCommands.DataProvider pDataProvider) throws CommandSyntaxException {
      DataAccessor dataaccessor = pDataProvider.access(pContext);
      return Collections.singletonList(dataaccessor.getData());
   }

   private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> pContext, DataCommands.DataProvider pDataProvider) throws CommandSyntaxException {
      DataAccessor dataaccessor = pDataProvider.access(pContext);
      NbtPathArgument.NbtPath nbtpathargument$nbtpath = NbtPathArgument.getPath(pContext, "sourcePath");
      return nbtpathargument$nbtpath.get(dataaccessor.getData());
   }

   private static int manipulateData(CommandContext<CommandSourceStack> pSource, DataCommands.DataProvider pDataProvider, DataCommands.DataManipulator pDataManipulator, List<Tag> pTags) throws CommandSyntaxException {
      DataAccessor dataaccessor = pDataProvider.access(pSource);
      NbtPathArgument.NbtPath nbtpathargument$nbtpath = NbtPathArgument.getPath(pSource, "targetPath");
      CompoundTag compoundtag = dataaccessor.getData();
      int i = pDataManipulator.modify(pSource, compoundtag, nbtpathargument$nbtpath, pTags);
      if (i == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         dataaccessor.setData(compoundtag);
         pSource.getSource().sendSuccess(() -> {
            return dataaccessor.getModifiedSuccess();
         }, true);
         return i;
      }
   }

   /**
    * Removes the tag at the end of the path.
    * 
    * @return 1
    */
   private static int removeData(CommandSourceStack pSource, DataAccessor pAccessor, NbtPathArgument.NbtPath pPath) throws CommandSyntaxException {
      CompoundTag compoundtag = pAccessor.getData();
      int i = pPath.remove(compoundtag);
      if (i == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         pAccessor.setData(compoundtag);
         pSource.sendSuccess(() -> {
            return pAccessor.getModifiedSuccess();
         }, true);
         return i;
      }
   }

   private static Tag getSingleTag(NbtPathArgument.NbtPath pPath, DataAccessor pAccessor) throws CommandSyntaxException {
      Collection<Tag> collection = pPath.get(pAccessor.getData());
      Iterator<Tag> iterator = collection.iterator();
      Tag tag = iterator.next();
      if (iterator.hasNext()) {
         throw ERROR_MULTIPLE_TAGS.create();
      } else {
         return tag;
      }
   }

   /**
    * Gets a value, which can be of any known NBT type.
    * 
    * @return The value associated with the element: length for strings, size for lists and compounds, and numeric value
    * for primitives.
    */
   private static int getData(CommandSourceStack pSource, DataAccessor pAccessor, NbtPathArgument.NbtPath pPath) throws CommandSyntaxException {
      Tag tag = getSingleTag(pPath, pAccessor);
      int i;
      if (tag instanceof NumericTag) {
         i = Mth.floor(((NumericTag)tag).getAsDouble());
      } else if (tag instanceof CollectionTag) {
         i = ((CollectionTag)tag).size();
      } else if (tag instanceof CompoundTag) {
         i = ((CompoundTag)tag).size();
      } else {
         if (!(tag instanceof StringTag)) {
            throw ERROR_GET_NON_EXISTENT.create(pPath.toString());
         }

         i = tag.getAsString().length();
      }

      pSource.sendSuccess(() -> {
         return pAccessor.getPrintSuccess(tag);
      }, false);
      return i;
   }

   /**
    * Gets a single numeric element, scaled by the given amount.
    * 
    * @return The element's value, scaled by scale.
    */
   private static int getNumeric(CommandSourceStack pSource, DataAccessor pAccessor, NbtPathArgument.NbtPath pPath, double pScale) throws CommandSyntaxException {
      Tag tag = getSingleTag(pPath, pAccessor);
      if (!(tag instanceof NumericTag)) {
         throw ERROR_GET_NOT_NUMBER.create(pPath.toString());
      } else {
         int i = Mth.floor(((NumericTag)tag).getAsDouble() * pScale);
         pSource.sendSuccess(() -> {
            return pAccessor.getPrintSuccess(pPath, pScale, i);
         }, false);
         return i;
      }
   }

   /**
    * Gets all NBT on the object, and applies syntax highlighting.
    * 
    * @return 1
    */
   private static int getData(CommandSourceStack pSource, DataAccessor pAccessor) throws CommandSyntaxException {
      CompoundTag compoundtag = pAccessor.getData();
      pSource.sendSuccess(() -> {
         return pAccessor.getPrintSuccess(compoundtag);
      }, false);
      return 1;
   }

   /**
    * Merges the given NBT into the targeted object's NBT.
    * 
    * @return 1
    */
   private static int mergeData(CommandSourceStack pSource, DataAccessor pAccessor, CompoundTag pNbt) throws CommandSyntaxException {
      CompoundTag compoundtag = pAccessor.getData();
      if (NbtPathArgument.NbtPath.isTooDeep(pNbt, 0)) {
         throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
      } else {
         CompoundTag compoundtag1 = compoundtag.copy().merge(pNbt);
         if (compoundtag.equals(compoundtag1)) {
            throw ERROR_MERGE_UNCHANGED.create();
         } else {
            pAccessor.setData(compoundtag1);
            pSource.sendSuccess(() -> {
               return pAccessor.getModifiedSuccess();
            }, true);
            return 1;
         }
      }
   }

   @FunctionalInterface
   interface DataManipulator {
      int modify(CommandContext<CommandSourceStack> pContext, CompoundTag pNbt, NbtPathArgument.NbtPath pNbtPath, List<Tag> pTags) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface DataManipulatorDecorator {
      ArgumentBuilder<CommandSourceStack, ?> create(DataCommands.DataManipulator pDataManipulator);
   }

   public interface DataProvider {
      /**
       * Creates an accessor based on the command context. This should only refer to arguments registered in {@link
       * createArgument}.
       */
      DataAccessor access(CommandContext<CommandSourceStack> pContext) throws CommandSyntaxException;

      /**
       * Creates an argument used for accessing data related to this type of thing, including a literal to distinguish
       * from other types.
       */
      ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> pBuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> pAction);
   }

   @FunctionalInterface
   interface StringProcessor {
      String process(String pInput) throws CommandSyntaxException;
   }
}