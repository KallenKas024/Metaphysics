package net.minecraft.network.protocol.game;

/**
 * PacketListener for the server side of the PLAY protocol.
 */
public interface ServerGamePacketListener extends ServerPacketListener {
   void handleAnimate(ServerboundSwingPacket pPacket);

   /**
    * Process chat messages (broadcast back to clients) and commands (executes)
    */
   void handleChat(ServerboundChatPacket pPacket);

   void handleChatCommand(ServerboundChatCommandPacket pPacket);

   void handleChatAck(ServerboundChatAckPacket pPacket);

   /**
    * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or acquiring
    * 'open inventory' achievement
    */
   void handleClientCommand(ServerboundClientCommandPacket pPacket);

   /**
    * Updates serverside copy of client settings: language, render distance, chat visibility, chat colours, difficulty,
    * and whether to show the cape
    */
   void handleClientInformation(ServerboundClientInformationPacket pPacket);

   /**
    * Enchants the item identified by the packet given some convoluted conditions (matching window, which
    * should/shouldn't be in use?)
    */
   void handleContainerButtonClick(ServerboundContainerButtonClickPacket pPacket);

   /**
    * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they
    * didn't match the indicated result and prevents further manipulation by the player until he confirms that it has
    * the same open container/inventory
    */
   void handleContainerClick(ServerboundContainerClickPacket pPacket);

   void handlePlaceRecipe(ServerboundPlaceRecipePacket pPacket);

   /**
    * Processes the client closing windows (container)
    */
   void handleContainerClose(ServerboundContainerClosePacket pPacket);

   /**
    * Synchronizes serverside and clientside book contents and signing
    */
   void handleCustomPayload(ServerboundCustomPayloadPacket pPacket);

   /**
    * Processes left and right clicks on entities
    */
   void handleInteract(ServerboundInteractPacket pPacket);

   /**
    * Updates a players' ping statistics
    */
   void handleKeepAlive(ServerboundKeepAlivePacket pPacket);

   /**
    * Processes clients perspective on player positioning and/or orientation
    */
   void handleMovePlayer(ServerboundMovePlayerPacket pPacket);

   void handlePong(ServerboundPongPacket pPacket);

   /**
    * Processes a player starting/stopping flying
    */
   void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket pPacket);

   /**
    * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items
    */
   void handlePlayerAction(ServerboundPlayerActionPacket pPacket);

   /**
    * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump
    * height of the horse the player is riding
    */
   void handlePlayerCommand(ServerboundPlayerCommandPacket pPacket);

   /**
    * Processes player movement input. Includes walking, strafing, jumping, and sneaking. Excludes riding and toggling
    * flying/sprinting.
    */
   void handlePlayerInput(ServerboundPlayerInputPacket pPacket);

   /**
    * Updates which quickbar slot is selected
    */
   void handleSetCarriedItem(ServerboundSetCarriedItemPacket pPacket);

   /**
    * Update the server with an ItemStack in a slot.
    */
   void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket pPacket);

   void handleSignUpdate(ServerboundSignUpdatePacket pPacket);

   void handleUseItemOn(ServerboundUseItemOnPacket pPacket);

   /**
    * Called when a client is using an item while not pointing at a block, but simply using an item
    */
   void handleUseItem(ServerboundUseItemPacket pPacket);

   void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket pPacket);

   void handleResourcePackResponse(ServerboundResourcePackPacket pPacket);

   void handlePaddleBoat(ServerboundPaddleBoatPacket pPacket);

   void handleMoveVehicle(ServerboundMoveVehiclePacket pPacket);

   void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket pPacket);

   void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket pPacket);

   void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket pPacket);

   void handleSeenAdvancements(ServerboundSeenAdvancementsPacket pPacket);

   /**
    * This method is only called for manual tab-completion (the {@link
    * net.minecraft.commands.synchronization.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
    */
   void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket pPacket);

   void handleSetCommandBlock(ServerboundSetCommandBlockPacket pPacket);

   void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket pPacket);

   void handlePickItem(ServerboundPickItemPacket pPacket);

   void handleRenameItem(ServerboundRenameItemPacket pPacket);

   void handleSetBeaconPacket(ServerboundSetBeaconPacket pPacket);

   void handleSetStructureBlock(ServerboundSetStructureBlockPacket pPacket);

   void handleSelectTrade(ServerboundSelectTradePacket pPacket);

   void handleEditBook(ServerboundEditBookPacket pPacket);

   void handleEntityTagQuery(ServerboundEntityTagQuery pPacket);

   void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery pPacket);

   void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket pPacket);

   void handleJigsawGenerate(ServerboundJigsawGeneratePacket pPacket);

   void handleChangeDifficulty(ServerboundChangeDifficultyPacket pPacket);

   void handleLockDifficulty(ServerboundLockDifficultyPacket pPacket);

   void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket pPacket);
}