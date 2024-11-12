package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
   LEVEL(References.LEVEL),
   PLAYER(References.PLAYER),
   CHUNK(References.CHUNK),
   HOTBAR(References.HOTBAR),
   OPTIONS(References.OPTIONS),
   STRUCTURE(References.STRUCTURE),
   STATS(References.STATS),
   SAVED_DATA(References.SAVED_DATA),
   ADVANCEMENTS(References.ADVANCEMENTS),
   POI_CHUNK(References.POI_CHUNK),
   WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
   ENTITY_CHUNK(References.ENTITY_CHUNK);

   public static final Set<DSL.TypeReference> TYPES_FOR_LEVEL_LIST;
   private final DSL.TypeReference type;

   private DataFixTypes(DSL.TypeReference pType) {
      this.type = pType;
   }

   private static int currentVersion() {
      return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
   }

   public <T> Dynamic<T> update(DataFixer pFixer, Dynamic<T> pInput, int pVersion, int pNewVersion) {
      return pFixer.update(this.type, pInput, pVersion, pNewVersion);
   }

   public <T> Dynamic<T> updateToCurrentVersion(DataFixer pFixer, Dynamic<T> pInput, int pVersion) {
      return this.update(pFixer, pInput, pVersion, currentVersion());
   }

   public CompoundTag update(DataFixer pFixer, CompoundTag pTag, int pVersion, int pNewVersion) {
      return (CompoundTag)this.update(pFixer, new Dynamic<>(NbtOps.INSTANCE, pTag), pVersion, pNewVersion).getValue();
   }

   public CompoundTag updateToCurrentVersion(DataFixer pFixer, CompoundTag pTag, int pVersion) {
      return this.update(pFixer, pTag, pVersion, currentVersion());
   }

   static {
      TYPES_FOR_LEVEL_LIST = Set.of(LEVEL.type);
   }
}