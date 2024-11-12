package net.minecraft.server.bossevents;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class CustomBossEvent extends ServerBossEvent {
   private final ResourceLocation id;
   private final Set<UUID> players = Sets.newHashSet();
   private int value;
   private int max = 100;

   public CustomBossEvent(ResourceLocation pId, Component pName) {
      super(pName, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
      this.id = pId;
      this.setProgress(0.0F);
   }

   public ResourceLocation getTextId() {
      return this.id;
   }

   /**
    * Makes the boss visible to the given player.
    */
   public void addPlayer(ServerPlayer pPlayer) {
      super.addPlayer(pPlayer);
      this.players.add(pPlayer.getUUID());
   }

   public void addOfflinePlayer(UUID pPlayer) {
      this.players.add(pPlayer);
   }

   /**
    * Makes the boss non-visible to the given player.
    */
   public void removePlayer(ServerPlayer pPlayer) {
      super.removePlayer(pPlayer);
      this.players.remove(pPlayer.getUUID());
   }

   public void removeAllPlayers() {
      super.removeAllPlayers();
      this.players.clear();
   }

   public int getValue() {
      return this.value;
   }

   public int getMax() {
      return this.max;
   }

   public void setValue(int pValue) {
      this.value = pValue;
      this.setProgress(Mth.clamp((float)pValue / (float)this.max, 0.0F, 1.0F));
   }

   public void setMax(int pMax) {
      this.max = pMax;
      this.setProgress(Mth.clamp((float)this.value / (float)pMax, 0.0F, 1.0F));
   }

   public final Component getDisplayName() {
      return ComponentUtils.wrapInSquareBrackets(this.getName()).withStyle((p_136276_) -> {
         return p_136276_.withColor(this.getColor().getFormatting()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.getTextId().toString()))).withInsertion(this.getTextId().toString());
      });
   }

   public boolean setPlayers(Collection<ServerPlayer> pServerPlayerList) {
      Set<UUID> set = Sets.newHashSet();
      Set<ServerPlayer> set1 = Sets.newHashSet();

      for(UUID uuid : this.players) {
         boolean flag = false;

         for(ServerPlayer serverplayer : pServerPlayerList) {
            if (serverplayer.getUUID().equals(uuid)) {
               flag = true;
               break;
            }
         }

         if (!flag) {
            set.add(uuid);
         }
      }

      for(ServerPlayer serverplayer1 : pServerPlayerList) {
         boolean flag1 = false;

         for(UUID uuid2 : this.players) {
            if (serverplayer1.getUUID().equals(uuid2)) {
               flag1 = true;
               break;
            }
         }

         if (!flag1) {
            set1.add(serverplayer1);
         }
      }

      for(UUID uuid1 : set) {
         for(ServerPlayer serverplayer3 : this.getPlayers()) {
            if (serverplayer3.getUUID().equals(uuid1)) {
               this.removePlayer(serverplayer3);
               break;
            }
         }

         this.players.remove(uuid1);
      }

      for(ServerPlayer serverplayer2 : set1) {
         this.addPlayer(serverplayer2);
      }

      return !set.isEmpty() || !set1.isEmpty();
   }

   public CompoundTag save() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("Name", Component.Serializer.toJson(this.name));
      compoundtag.putBoolean("Visible", this.isVisible());
      compoundtag.putInt("Value", this.value);
      compoundtag.putInt("Max", this.max);
      compoundtag.putString("Color", this.getColor().getName());
      compoundtag.putString("Overlay", this.getOverlay().getName());
      compoundtag.putBoolean("DarkenScreen", this.shouldDarkenScreen());
      compoundtag.putBoolean("PlayBossMusic", this.shouldPlayBossMusic());
      compoundtag.putBoolean("CreateWorldFog", this.shouldCreateWorldFog());
      ListTag listtag = new ListTag();

      for(UUID uuid : this.players) {
         listtag.add(NbtUtils.createUUID(uuid));
      }

      compoundtag.put("Players", listtag);
      return compoundtag;
   }

   public static CustomBossEvent load(CompoundTag pNbt, ResourceLocation pId) {
      CustomBossEvent custombossevent = new CustomBossEvent(pId, Component.Serializer.fromJson(pNbt.getString("Name")));
      custombossevent.setVisible(pNbt.getBoolean("Visible"));
      custombossevent.setValue(pNbt.getInt("Value"));
      custombossevent.setMax(pNbt.getInt("Max"));
      custombossevent.setColor(BossEvent.BossBarColor.byName(pNbt.getString("Color")));
      custombossevent.setOverlay(BossEvent.BossBarOverlay.byName(pNbt.getString("Overlay")));
      custombossevent.setDarkenScreen(pNbt.getBoolean("DarkenScreen"));
      custombossevent.setPlayBossMusic(pNbt.getBoolean("PlayBossMusic"));
      custombossevent.setCreateWorldFog(pNbt.getBoolean("CreateWorldFog"));
      ListTag listtag = pNbt.getList("Players", 11);

      for(int i = 0; i < listtag.size(); ++i) {
         custombossevent.addOfflinePlayer(NbtUtils.loadUUID(listtag.get(i)));
      }

      return custombossevent;
   }

   public void onPlayerConnect(ServerPlayer pPlayer) {
      if (this.players.contains(pPlayer.getUUID())) {
         this.addPlayer(pPlayer);
      }

   }

   public void onPlayerDisconnect(ServerPlayer pPlayer) {
      super.removePlayer(pPlayer);
   }
}