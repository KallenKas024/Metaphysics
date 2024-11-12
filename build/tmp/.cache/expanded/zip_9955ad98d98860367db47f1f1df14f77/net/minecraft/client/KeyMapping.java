package net.minecraft.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyMapping implements Comparable<KeyMapping>, net.minecraftforge.client.extensions.IForgeKeyMapping {
   private static final Map<String, KeyMapping> ALL = Maps.newHashMap();
   private static final net.minecraftforge.client.settings.KeyMappingLookup MAP = new net.minecraftforge.client.settings.KeyMappingLookup();
   private static final Set<String> CATEGORIES = Sets.newHashSet();
   public static final String CATEGORY_MOVEMENT = "key.categories.movement";
   public static final String CATEGORY_MISC = "key.categories.misc";
   public static final String CATEGORY_MULTIPLAYER = "key.categories.multiplayer";
   public static final String CATEGORY_GAMEPLAY = "key.categories.gameplay";
   public static final String CATEGORY_INVENTORY = "key.categories.inventory";
   public static final String CATEGORY_INTERFACE = "key.categories.ui";
   public static final String CATEGORY_CREATIVE = "key.categories.creative";
   private static final Map<String, Integer> CATEGORY_SORT_ORDER = Util.make(Maps.newHashMap(), (p_90845_) -> {
      p_90845_.put("key.categories.movement", 1);
      p_90845_.put("key.categories.gameplay", 2);
      p_90845_.put("key.categories.inventory", 3);
      p_90845_.put("key.categories.creative", 4);
      p_90845_.put("key.categories.multiplayer", 5);
      p_90845_.put("key.categories.ui", 6);
      p_90845_.put("key.categories.misc", 7);
   });
   private final String name;
   private final InputConstants.Key defaultKey;
   private final String category;
   private InputConstants.Key key;
   boolean isDown;
   private int clickCount;

   public static void click(InputConstants.Key pKey) {
      for (KeyMapping keymapping : MAP.getAll(pKey))
      if (keymapping != null) {
         ++keymapping.clickCount;
      }

   }

   public static void set(InputConstants.Key pKey, boolean pHeld) {
      for (KeyMapping keymapping : MAP.getAll(pKey))
      if (keymapping != null) {
         keymapping.setDown(pHeld);
      }

   }

   /**
    * Completely recalculates whether any keybinds are held, from scratch.
    */
   public static void setAll() {
      for(KeyMapping keymapping : ALL.values()) {
         if (keymapping.key.getType() == InputConstants.Type.KEYSYM && keymapping.key.getValue() != InputConstants.UNKNOWN.getValue()) {
            keymapping.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keymapping.key.getValue()));
         }
      }

   }

   public static void releaseAll() {
      for(KeyMapping keymapping : ALL.values()) {
         keymapping.release();
      }

   }

   public static void resetToggleKeys() {
      for(KeyMapping keymapping : ALL.values()) {
         if (keymapping instanceof ToggleKeyMapping togglekeymapping) {
            togglekeymapping.reset();
         }
      }

   }

   public static void resetMapping() {
      MAP.clear();

      for(KeyMapping keymapping : ALL.values()) {
         MAP.put(keymapping.key, keymapping);
      }

   }

   public KeyMapping(String pName, int pKeyCode, String pCategory) {
      this(pName, InputConstants.Type.KEYSYM, pKeyCode, pCategory);
   }

   public KeyMapping(String pName, InputConstants.Type pType, int pKeyCode, String pCategory) {
      this.name = pName;
      this.key = pType.getOrCreate(pKeyCode);
      this.defaultKey = this.key;
      this.category = pCategory;
      ALL.put(pName, this);
      MAP.put(this.key, this);
      CATEGORIES.add(pCategory);
   }

   /**
    * Returns {@code true} if the key is pressed (used for continuous querying). Should be used in tickers.
    */
   public boolean isDown() {
      return this.isDown && isConflictContextAndModifierActive();
   }

   public String getCategory() {
      return this.category;
   }

   /**
    * Returns {@code true} on the initial key press. For continuous querying use {@link isKeyDown()}. Should be used in
    * key events.
    */
   public boolean consumeClick() {
      if (this.clickCount == 0) {
         return false;
      } else {
         --this.clickCount;
         return true;
      }
   }

   private void release() {
      this.clickCount = 0;
      this.setDown(false);
   }

   public String getName() {
      return this.name;
   }

   public InputConstants.Key getDefaultKey() {
      return this.defaultKey;
   }

   /**
    * Binds a new KeyCode to this
    */
   public void setKey(InputConstants.Key pKey) {
      this.key = pKey;
   }

   public int compareTo(KeyMapping p_90841_) {
      if (this.category.equals(p_90841_.category)) return I18n.get(this.name).compareTo(I18n.get(p_90841_.name));
      Integer tCat = CATEGORY_SORT_ORDER.get(this.category);
      Integer oCat = CATEGORY_SORT_ORDER.get(p_90841_.category);
      if (tCat == null && oCat != null) return 1;
      if (tCat != null && oCat == null) return -1;
      if (tCat == null && oCat == null) return I18n.get(this.category).compareTo(I18n.get(p_90841_.category));
      return  tCat.compareTo(oCat);
   }

   /**
    * Returns a supplier which gets a keybind's current binding (eg, <code>key.forward</code> returns <samp>W</samp> by
    * default), or the keybind's name if no such keybind exists (eg, <code>key.invalid</code> returns
    * <samp>key.invalid</samp>)
    */
   public static Supplier<Component> createNameSupplier(String pKey) {
      KeyMapping keymapping = ALL.get(pKey);
      return keymapping == null ? () -> {
         return Component.translatable(pKey);
      } : keymapping::getTranslatedKeyMessage;
   }

   /**
    * Returns {@code true} if the supplied {@code KeyMapping} conflicts with this
    */
   public boolean same(KeyMapping pBinding) {
      if (getKeyConflictContext().conflicts(pBinding.getKeyConflictContext()) || pBinding.getKeyConflictContext().conflicts(getKeyConflictContext())) {
         net.minecraftforge.client.settings.KeyModifier keyModifier = getKeyModifier();
         net.minecraftforge.client.settings.KeyModifier otherKeyModifier = pBinding.getKeyModifier();
         if (keyModifier.matches(pBinding.getKey()) || otherKeyModifier.matches(getKey())) {
            return true;
         } else if (getKey().equals(pBinding.getKey())) {
            // IN_GAME key contexts have a conflict when at least one modifier is NONE.
            // For example: If you hold shift to crouch, you can still press E to open your inventory. This means that a Shift+E hotkey is in conflict with E.
            // GUI and other key contexts do not have this limitation.
            return keyModifier == otherKeyModifier ||
               (getKeyConflictContext().conflicts(net.minecraftforge.client.settings.KeyConflictContext.IN_GAME) &&
               (keyModifier == net.minecraftforge.client.settings.KeyModifier.NONE || otherKeyModifier == net.minecraftforge.client.settings.KeyModifier.NONE));
         }
      }
      return this.key.equals(pBinding.key);
   }

   public boolean isUnbound() {
      return this.key.equals(InputConstants.UNKNOWN);
   }

   public boolean matches(int pKeysym, int pScancode) {
      if (pKeysym == InputConstants.UNKNOWN.getValue()) {
         return this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == pScancode;
      } else {
         return this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == pKeysym;
      }
   }

   /**
    * Returns {@code true} if the {@code KeyMapping} is set to a mouse key and the key matches.
    */
   public boolean matchesMouse(int pKey) {
      return this.key.getType() == InputConstants.Type.MOUSE && this.key.getValue() == pKey;
   }

   public Component getTranslatedKeyMessage() {
      return getKeyModifier().getCombinedName(key, () -> {
      return this.key.getDisplayName();
      });
   }

   /**
    * Returns {@code true} if the {@code KeyMapping} is using the default key and key modifier
    */
   public boolean isDefault() {
      return this.key.equals(this.defaultKey) && getKeyModifier() == getDefaultKeyModifier();
   }

   public String saveString() {
      return this.key.getName();
   }

   public void setDown(boolean pValue) {
      this.isDown = pValue;
   }

   private net.minecraftforge.client.settings.KeyModifier keyModifierDefault = net.minecraftforge.client.settings.KeyModifier.NONE;
   private net.minecraftforge.client.settings.KeyModifier keyModifier = net.minecraftforge.client.settings.KeyModifier.NONE;
   private net.minecraftforge.client.settings.IKeyConflictContext keyConflictContext = net.minecraftforge.client.settings.KeyConflictContext.UNIVERSAL;

   /**
    * Convenience constructor for creating KeyBindings with keyConflictContext set.
    */
   public KeyMapping(String description, net.minecraftforge.client.settings.IKeyConflictContext keyConflictContext, final InputConstants.Type inputType, final int keyCode, String category) {
       this(description, keyConflictContext, inputType.getOrCreate(keyCode), category);
   }

   /**
    * Convenience constructor for creating KeyBindings with keyConflictContext set.
    */
   public KeyMapping(String description, net.minecraftforge.client.settings.IKeyConflictContext keyConflictContext, InputConstants.Key keyCode, String category) {
       this(description, keyConflictContext, net.minecraftforge.client.settings.KeyModifier.NONE, keyCode, category);
   }

   /**
    * Convenience constructor for creating KeyBindings with keyConflictContext and keyModifier set.
    */
   public KeyMapping(String description, net.minecraftforge.client.settings.IKeyConflictContext keyConflictContext, net.minecraftforge.client.settings.KeyModifier keyModifier, final InputConstants.Type inputType, final int keyCode, String category) {
       this(description, keyConflictContext, keyModifier, inputType.getOrCreate(keyCode), category);
   }

   /**
    * Convenience constructor for creating KeyBindings with keyConflictContext and keyModifier set.
    */
   public KeyMapping(String description, net.minecraftforge.client.settings.IKeyConflictContext keyConflictContext, net.minecraftforge.client.settings.KeyModifier keyModifier, InputConstants.Key keyCode, String category) {
      this.name = description;
      this.key = keyCode;
      this.defaultKey = keyCode;
      this.category = category;
      this.keyConflictContext = keyConflictContext;
      this.keyModifier = keyModifier;
      this.keyModifierDefault = keyModifier;
      if (this.keyModifier.matches(keyCode))
         this.keyModifier = net.minecraftforge.client.settings.KeyModifier.NONE;
      ALL.put(description, this);
      MAP.put(keyCode, this);
      CATEGORIES.add(category);
   }

   @Override
   public InputConstants.Key getKey() {
       return this.key;
   }

   @Override
   public void setKeyConflictContext(net.minecraftforge.client.settings.IKeyConflictContext keyConflictContext) {
       this.keyConflictContext = keyConflictContext;
   }

   @Override
   public net.minecraftforge.client.settings.IKeyConflictContext getKeyConflictContext() {
       return keyConflictContext;
   }

   @Override
   public net.minecraftforge.client.settings.KeyModifier getDefaultKeyModifier() {
       return keyModifierDefault;
   }

   @Override
   public net.minecraftforge.client.settings.KeyModifier getKeyModifier() {
       return keyModifier;
   }

   @Override
   public void setKeyModifierAndCode(@org.jetbrains.annotations.Nullable net.minecraftforge.client.settings.KeyModifier keyModifier, InputConstants.Key keyCode) {
       MAP.remove(this);

       if (keyModifier == null)
           keyModifier = net.minecraftforge.client.settings.KeyModifier.getModifier(this.key);
       if (keyModifier == null || keyCode == InputConstants.UNKNOWN || net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(keyCode))
           keyModifier = net.minecraftforge.client.settings.KeyModifier.NONE;

       this.key = keyCode;
       this.keyModifier = keyModifier;

       MAP.put(keyCode, this);
   }
}
