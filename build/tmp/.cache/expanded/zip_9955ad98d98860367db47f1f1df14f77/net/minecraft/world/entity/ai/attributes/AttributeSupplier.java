package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;

public class AttributeSupplier {
   private final Map<Attribute, AttributeInstance> instances;

   public AttributeSupplier(Map<Attribute, AttributeInstance> pInstances) {
      this.instances = ImmutableMap.copyOf(pInstances);
   }

   private AttributeInstance getAttributeInstance(Attribute pAttribute) {
      AttributeInstance attributeinstance = this.instances.get(pAttribute);
      if (attributeinstance == null) {
         throw new IllegalArgumentException("Can't find attribute " + BuiltInRegistries.ATTRIBUTE.getKey(pAttribute));
      } else {
         return attributeinstance;
      }
   }

   public double getValue(Attribute pAttribute) {
      return this.getAttributeInstance(pAttribute).getValue();
   }

   public double getBaseValue(Attribute pAttribute) {
      return this.getAttributeInstance(pAttribute).getBaseValue();
   }

   public double getModifierValue(Attribute pAttribute, UUID pId) {
      AttributeModifier attributemodifier = this.getAttributeInstance(pAttribute).getModifier(pId);
      if (attributemodifier == null) {
         throw new IllegalArgumentException("Can't find modifier " + pId + " on attribute " + BuiltInRegistries.ATTRIBUTE.getKey(pAttribute));
      } else {
         return attributemodifier.getAmount();
      }
   }

   @Nullable
   public AttributeInstance createInstance(Consumer<AttributeInstance> pOnChangedCallback, Attribute pAttribute) {
      AttributeInstance attributeinstance = this.instances.get(pAttribute);
      if (attributeinstance == null) {
         return null;
      } else {
         AttributeInstance attributeinstance1 = new AttributeInstance(pAttribute, pOnChangedCallback);
         attributeinstance1.replaceFrom(attributeinstance);
         return attributeinstance1;
      }
   }

   public static AttributeSupplier.Builder builder() {
      return new AttributeSupplier.Builder();
   }

   public boolean hasAttribute(Attribute pAttribute) {
      return this.instances.containsKey(pAttribute);
   }

   public boolean hasModifier(Attribute pAttribute, UUID pId) {
      AttributeInstance attributeinstance = this.instances.get(pAttribute);
      return attributeinstance != null && attributeinstance.getModifier(pId) != null;
   }

   public static class Builder {
      private final Map<Attribute, AttributeInstance> builder = Maps.newHashMap();
      private boolean instanceFrozen;
      private final java.util.List<AttributeSupplier.Builder> others = new java.util.ArrayList<>();

      public Builder() { }

      public Builder(AttributeSupplier attributeMap) {
         this.builder.putAll(attributeMap.instances);
      }

      public void combine(Builder other) {
         this.builder.putAll(other.builder);
         others.add(other);
      }

      public boolean hasAttribute(Attribute attribute) {
         return this.builder.containsKey(attribute);
      }

      private AttributeInstance create(Attribute pAttribute) {
         AttributeInstance attributeinstance = new AttributeInstance(pAttribute, (p_258260_) -> {
            if (this.instanceFrozen) {
               throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + BuiltInRegistries.ATTRIBUTE.getKey(pAttribute));
            }
         });
         this.builder.put(pAttribute, attributeinstance);
         return attributeinstance;
      }

      public AttributeSupplier.Builder add(Attribute pAttribute) {
         this.create(pAttribute);
         return this;
      }

      public AttributeSupplier.Builder add(Attribute pAttribute, double pValue) {
         AttributeInstance attributeinstance = this.create(pAttribute);
         attributeinstance.setBaseValue(pValue);
         return this;
      }

      public AttributeSupplier build() {
         this.instanceFrozen = true;
         others.forEach(pBranchBase -> pBranchBase.instanceFrozen = true);
         return new AttributeSupplier(this.builder);
      }
   }
}
