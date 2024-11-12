package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;

/**
 * PacketListener for the client side of the PLAY protocol.
 */
public interface ClientGamePacketListener extends PacketListener {
   /**
    * Spawns an instance of the objecttype indicated by the packet and sets its position and momentum
    */
   void handleAddEntity(ClientboundAddEntityPacket pPacket);

   /**
    * Spawns an experience orb and sets its value (amount of XP)
    */
   void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket pPacket);

   /**
    * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
    */
   void handleAddObjective(ClientboundSetObjectivePacket pPacket);

   /**
    * Handles the creation of a nearby player entity, sets the position and held item
    */
   void handleAddPlayer(ClientboundAddPlayerPacket pPacket);

   /**
    * Renders a specified animation: Waking up a player, a living entity swinging its currently held item, being hurt or
    * receiving a critical hit by normal or magical means
    */
   void handleAnimate(ClientboundAnimatePacket pPacket);

   void handleHurtAnimation(ClientboundHurtAnimationPacket pPacket);

   /**
    * Updates the players statistics or achievements
    */
   void handleAwardStats(ClientboundAwardStatsPacket pPacket);

   void handleAddOrRemoveRecipes(ClientboundRecipePacket pPacket);

   /**
    * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
    */
   void handleBlockDestruction(ClientboundBlockDestructionPacket pPacket);

   /**
    * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
    */
   void handleOpenSignEditor(ClientboundOpenSignEditorPacket pPacket);

   /**
    * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
    * beacons, skulls, flowerpot
    */
   void handleBlockEntityData(ClientboundBlockEntityDataPacket pPacket);

   /**
    * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote
    * for setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players
    * accessing a (Ender)Chest
    */
   void handleBlockEvent(ClientboundBlockEventPacket pPacket);

   /**
    * Updates the block and metadata and generates a blockupdate (and notify the clients)
    */
   void handleBlockUpdate(ClientboundBlockUpdatePacket pPacket);

   void handleSystemChat(ClientboundSystemChatPacket pPacket);

   void handlePlayerChat(ClientboundPlayerChatPacket pPacket);

   void handleDisguisedChat(ClientboundDisguisedChatPacket pPacket);

   void handleDeleteChat(ClientboundDeleteChatPacket pPacket);

   /**
    * Received from the servers PlayerManager if between 1 and 64 blocks in a chunk are changed. If only one block
    * requires an update, the server sends S23PacketBlockChange and if 64 or more blocks are changed, the server sends
    * S21PacketChunkData
    */
   void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket pPacket);

   /**
    * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
    * MapItemRenderer for it
    */
   void handleMapItemData(ClientboundMapItemDataPacket pPacket);

   /**
    * Resets the ItemStack held in hand and closes the window that is opened
    */
   void handleContainerClose(ClientboundContainerClosePacket pPacket);

   /**
    * Handles the placement of a specified ItemStack in a specified container/inventory slot
    */
   void handleContainerContent(ClientboundContainerSetContentPacket pPacket);

   void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket pPacket);

   /**
    * Sets the progressbar of the opened window to the specified value
    */
   void handleContainerSetData(ClientboundContainerSetDataPacket pPacket);

   /**
    * Handles picking up an ItemStack or dropping one in your inventory or an open (non-creative) container
    */
   void handleContainerSetSlot(ClientboundContainerSetSlotPacket pPacket);

   /**
    * Handles packets that have room for a channel specification. Vanilla implemented channels are "MC|TrList" to
    * acquire a MerchantRecipeList trades for a villager merchant, "MC|Brand" which sets the server brand? on the player
    * instance and finally "MC|RPack" which the server uses to communicate the identifier of the default server
    * resourcepack for the client to load.
    */
   void handleCustomPayload(ClientboundCustomPayloadPacket pPacket);

   /**
    * Closes the network channel
    */
   void handleDisconnect(ClientboundDisconnectPacket pPacket);

   /**
    * Invokes the entities' handleUpdateHealth method which is implemented in LivingBase (hurt/death),
    * MinecartMobSpawner (spawn delay), FireworkRocket & MinecartTNT (explosion), IronGolem (throwing,...), Witch (spawn
    * particles), Zombie (villager transformation), Animal (breeding mode particles), Horse (breeding/smoke particles),
    * Sheep (...), Tameable (...), Villager (particles for breeding mode, angry and happy), Wolf (...)
    */
   void handleEntityEvent(ClientboundEntityEventPacket pPacket);

   void handleEntityLinkPacket(ClientboundSetEntityLinkPacket pPacket);

   void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket pPacket);

   /**
    * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
    */
   void handleExplosion(ClientboundExplodePacket pPacket);

   void handleGameEvent(ClientboundGameEventPacket pPacket);

   void handleKeepAlive(ClientboundKeepAlivePacket pPacket);

   void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket pPacket);

   void handleChunksBiomes(ClientboundChunksBiomesPacket pPacket);

   void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket pPacket);

   void handleLevelEvent(ClientboundLevelEventPacket pPacket);

   /**
    * Registers some server properties (gametype,hardcore-mode,terraintype,difficulty,player limit), creates a new
    * WorldClient and sets the player initial dimension
    */
   void handleLogin(ClientboundLoginPacket pPacket);

   /**
    * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
    * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
    * rotation or both).
    */
   void handleMoveEntity(ClientboundMoveEntityPacket pPacket);

   void handleMovePlayer(ClientboundPlayerPositionPacket pPacket);

   /**
    * Spawns a specified number of particles at the specified location with a randomized displacement according to
    * specified bounds
    */
   void handleParticleEvent(ClientboundLevelParticlesPacket pPacket);

   void handlePing(ClientboundPingPacket pPacket);

   void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket pPacket);

   void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket pPacket);

   void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket pPacket);

   void handleRemoveEntities(ClientboundRemoveEntitiesPacket pPacket);

   void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket pPacket);

   void handleRespawn(ClientboundRespawnPacket pPacket);

   /**
    * Updates the direction in which the specified entity is looking, normally this head rotation is independent of the
    * rotation of the entity itself
    */
   void handleRotateMob(ClientboundRotateHeadPacket pPacket);

   /**
    * Updates which hotbar slot of the player is currently selected
    */
   void handleSetCarriedItem(ClientboundSetCarriedItemPacket pPacket);

   /**
    * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below name)
    */
   void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket pPacket);

   /**
    * Invoked when the server registers new proximate objects in your watchlist or when objects in your watchlist have
    * changed -> Registers any changes locally
    */
   void handleSetEntityData(ClientboundSetEntityDataPacket pPacket);

   /**
    * Sets the velocity of the specified entity to the specified value
    */
   void handleSetEntityMotion(ClientboundSetEntityMotionPacket pPacket);

   void handleSetEquipment(ClientboundSetEquipmentPacket pPacket);

   void handleSetExperience(ClientboundSetExperiencePacket pPacket);

   void handleSetHealth(ClientboundSetHealthPacket pPacket);

   /**
    * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
    * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
    */
   void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket pPacket);

   /**
    * Either updates the score with a specified value or removes the score for an objective
    */
   void handleSetScore(ClientboundSetScorePacket pPacket);

   void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket pPacket);

   void handleSetTime(ClientboundSetTimePacket pPacket);

   void handleSoundEvent(ClientboundSoundPacket pPacket);

   void handleSoundEntityEvent(ClientboundSoundEntityPacket pPacket);

   void handleTakeItemEntity(ClientboundTakeItemEntityPacket pPacket);

   /**
    * Updates an entity's position and rotation as specified by the packet
    */
   void handleTeleportEntity(ClientboundTeleportEntityPacket pPacket);

   /**
    * Updates en entity's attributes and their respective modifiers, which are used for speed bonuses (player sprinting,
    * animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie maxHealth and
    * knockback resistance as well as reinforcement spawning chance.
    */
   void handleUpdateAttributes(ClientboundUpdateAttributesPacket pPacket);

   void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket pPacket);

   void handleUpdateTags(ClientboundUpdateTagsPacket pPacket);

   void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket pPacket);

   void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket pPacket);

   void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket pPacket);

   void handleChangeDifficulty(ClientboundChangeDifficultyPacket pPacket);

   void handleSetCamera(ClientboundSetCameraPacket pPacket);

   void handleInitializeBorder(ClientboundInitializeBorderPacket pPacket);

   void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket pPacket);

   void handleSetBorderSize(ClientboundSetBorderSizePacket pPacket);

   void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket pPacket);

   void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket pPacket);

   void handleSetBorderCenter(ClientboundSetBorderCenterPacket pPacket);

   void handleTabListCustomisation(ClientboundTabListPacket pPacket);

   void handleResourcePack(ClientboundResourcePackPacket pPacket);

   void handleBossUpdate(ClientboundBossEventPacket pPacket);

   void handleItemCooldown(ClientboundCooldownPacket pPacket);

   void handleMoveVehicle(ClientboundMoveVehiclePacket pPacket);

   void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket pPacket);

   void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket pPacket);

   void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket);

   void handleCommands(ClientboundCommandsPacket pPacket);

   void handleStopSoundEvent(ClientboundStopSoundPacket pPacket);

   /**
    * This method is only called for manual tab-completion (the {@link
    * net.minecraft.commands.synchronization.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
    */
   void handleCommandSuggestions(ClientboundCommandSuggestionsPacket pPacket);

   void handleUpdateRecipes(ClientboundUpdateRecipesPacket pPacket);

   void handleLookAt(ClientboundPlayerLookAtPacket pPacket);

   void handleTagQueryPacket(ClientboundTagQueryPacket pPacket);

   void handleLightUpdatePacket(ClientboundLightUpdatePacket pPacket);

   void handleOpenBook(ClientboundOpenBookPacket pPacket);

   void handleOpenScreen(ClientboundOpenScreenPacket pPacket);

   void handleMerchantOffers(ClientboundMerchantOffersPacket pPacket);

   void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket pPacket);

   void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket pPacket);

   void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket pPacket);

   void handleBlockChangedAck(ClientboundBlockChangedAckPacket pPacket);

   void setActionBarText(ClientboundSetActionBarTextPacket pPacket);

   void setSubtitleText(ClientboundSetSubtitleTextPacket pPacket);

   void setTitleText(ClientboundSetTitleTextPacket pPacket);

   void setTitlesAnimation(ClientboundSetTitlesAnimationPacket pPacket);

   void handleTitlesClear(ClientboundClearTitlesPacket pPacket);

   void handleServerData(ClientboundServerDataPacket pPacket);

   void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket pPacket);

   void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket pPacket);

   void handleBundlePacket(ClientboundBundlePacket pPacket);

   void handleDamageEvent(ClientboundDamageEventPacket pPacket);
}