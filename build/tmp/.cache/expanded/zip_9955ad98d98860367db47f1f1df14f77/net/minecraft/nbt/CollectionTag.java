package net.minecraft.nbt;

import java.util.AbstractList;

public abstract class CollectionTag<T extends Tag> extends AbstractList<T> implements Tag {
   public abstract T set(int p_128318_, T p_128319_);

   public abstract void add(int p_128315_, T p_128316_);

   public abstract T remove(int p_128313_);

   public abstract boolean setTag(int pIndex, Tag pTag);

   public abstract boolean addTag(int pIndex, Tag pTag);

   public abstract byte getElementType();
}