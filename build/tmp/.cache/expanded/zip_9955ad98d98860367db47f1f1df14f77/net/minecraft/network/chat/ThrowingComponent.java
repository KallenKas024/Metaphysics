package net.minecraft.network.chat;

public class ThrowingComponent extends Exception {
   private final Component component;

   public ThrowingComponent(Component pComponent) {
      super(pComponent.getString());
      this.component = pComponent;
   }

   public ThrowingComponent(Component pComponent, Throwable pCause) {
      super(pComponent.getString(), pCause);
      this.component = pComponent;
   }

   public Component getComponent() {
      return this.component;
   }
}