package net.minecraft.world.entity;

public interface VariantHolder<T> {
   void setVariant(T pVariant);

   T getVariant();
}