package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ServerLevelAccessor;

public class CappedProcessor extends StructureProcessor {
   public static final Codec<CappedProcessor> CODEC = RecordCodecBuilder.create((p_277598_) -> {
      return p_277598_.group(StructureProcessorType.SINGLE_CODEC.fieldOf("delegate").forGetter((p_277456_) -> {
         return p_277456_.delegate;
      }), IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter((p_277680_) -> {
         return p_277680_.limit;
      })).apply(p_277598_, CappedProcessor::new);
   });
   private final StructureProcessor delegate;
   private final IntProvider limit;

   public CappedProcessor(StructureProcessor p_277972_, IntProvider p_277402_) {
      this.delegate = p_277972_;
      this.limit = p_277402_;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.CAPPED;
   }

   public final List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor pServerLevel, BlockPos pOffset, BlockPos pPos, List<StructureTemplate.StructureBlockInfo> pOriginalBlockInfos, List<StructureTemplate.StructureBlockInfo> pProcessedBlockInfos, StructurePlaceSettings pSettings) {
      if (this.limit.getMaxValue() != 0 && !pProcessedBlockInfos.isEmpty()) {
         if (pOriginalBlockInfos.size() != pProcessedBlockInfos.size()) {
            Util.logAndPauseIfInIde("Original block info list not in sync with processed list, skipping processing. Original size: " + pOriginalBlockInfos.size() + ", Processed size: " + pProcessedBlockInfos.size());
            return pProcessedBlockInfos;
         } else {
            RandomSource randomsource = RandomSource.create(pServerLevel.getLevel().getSeed()).forkPositional().at(pOffset);
            int i = Math.min(this.limit.sample(randomsource), pProcessedBlockInfos.size());
            if (i < 1) {
               return pProcessedBlockInfos;
            } else {
               IntArrayList intarraylist = Util.toShuffledList(IntStream.range(0, pProcessedBlockInfos.size()), randomsource);
               IntIterator intiterator = intarraylist.intIterator();
               int j = 0;

               while(intiterator.hasNext() && j < i) {
                  int k = intiterator.nextInt();
                  StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo = pOriginalBlockInfos.get(k);
                  StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 = pProcessedBlockInfos.get(k);
                  StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo2 = this.delegate.processBlock(pServerLevel, pOffset, pPos, structuretemplate$structureblockinfo, structuretemplate$structureblockinfo1, pSettings);
                  if (structuretemplate$structureblockinfo2 != null && !structuretemplate$structureblockinfo1.equals(structuretemplate$structureblockinfo2)) {
                     ++j;
                     pProcessedBlockInfos.set(k, structuretemplate$structureblockinfo2);
                  }
               }

               return pProcessedBlockInfos;
            }
         }
      } else {
         return pProcessedBlockInfos;
      }
   }
}