package net.minecraft.world.entity.ai.attributes;

/**
 * Defines an entity attribute. These are properties of entities that can be dynamically modified.
 * @see net.minecraft.core.Registry#ATTRIBUTE
 */
public class Attribute {
   public static final int MAX_NAME_LENGTH = 64;
   /** The default value of the attribute. */
   private final double defaultValue;
   /** Whether the value of this attribute should be kept in sync on the client. */
   private boolean syncable;
   /** A description Id for the attribute. This is most commonly used as the localization key. */
   private final String descriptionId;

   protected Attribute(String pDescriptionId, double pDefaultValue) {
      this.defaultValue = pDefaultValue;
      this.descriptionId = pDescriptionId;
   }

   /**
    * Gets the default value for the attribute.
    * @return The default value for the attribute.
    */
   public double getDefaultValue() {
      return this.defaultValue;
   }

   /**
    * Checks if the attribute value should be kept in sync on the client.
    * @return Whether the attribute value should be kept in sync on the client.
    */
   public boolean isClientSyncable() {
      return this.syncable;
   }

   /**
    * Sets whether the attribute value should be synced to the client.
    * @return The same attribute instance being modified.
    * @param pWatch Whether the attribute value should be kept in sync.
    */
   public Attribute setSyncable(boolean pWatch) {
      this.syncable = pWatch;
      return this;
   }

   /**
    * Sanitizes the value of the attribute to fit within the expected parameter range of the attribute.
    * @return The sanitized attribute value.
    * @param pValue The value of the attribute to sanitize.
    */
   public double sanitizeValue(double pValue) {
      return pValue;
   }

   /**
    * Gets the description Id of the attribute. This is most commonly used as a localization key.
    * @return The description Id of the attribute.
    */
   public String getDescriptionId() {
      return this.descriptionId;
   }
}