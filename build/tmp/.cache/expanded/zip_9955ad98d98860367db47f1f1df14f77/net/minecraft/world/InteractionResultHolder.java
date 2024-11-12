package net.minecraft.world;

public class InteractionResultHolder<T> {
   private final InteractionResult result;
   private final T object;

   public InteractionResultHolder(InteractionResult pResult, T pObject) {
      this.result = pResult;
      this.object = pObject;
   }

   public InteractionResult getResult() {
      return this.result;
   }

   public T getObject() {
      return this.object;
   }

   public static <T> InteractionResultHolder<T> success(T pType) {
      return new InteractionResultHolder<>(InteractionResult.SUCCESS, pType);
   }

   public static <T> InteractionResultHolder<T> consume(T pType) {
      return new InteractionResultHolder<>(InteractionResult.CONSUME, pType);
   }

   public static <T> InteractionResultHolder<T> pass(T pType) {
      return new InteractionResultHolder<>(InteractionResult.PASS, pType);
   }

   public static <T> InteractionResultHolder<T> fail(T pType) {
      return new InteractionResultHolder<>(InteractionResult.FAIL, pType);
   }

   public static <T> InteractionResultHolder<T> sidedSuccess(T pObject, boolean pIsClientSide) {
      return pIsClientSide ? success(pObject) : consume(pObject);
   }
}