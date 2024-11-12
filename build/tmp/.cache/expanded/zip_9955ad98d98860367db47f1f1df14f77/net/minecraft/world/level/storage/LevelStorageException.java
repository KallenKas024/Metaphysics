package net.minecraft.world.level.storage;

import net.minecraft.network.chat.Component;

public class LevelStorageException extends RuntimeException {
   private final Component messageComponent;

   public LevelStorageException(Component pMessageComponent) {
      super(pMessageComponent.getString());
      this.messageComponent = pMessageComponent;
   }

   public Component getMessageComponent() {
      return this.messageComponent;
   }
}