package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClickEvent {
   private final ClickEvent.Action action;
   private final String value;

   public ClickEvent(ClickEvent.Action pAction, String pValue) {
      this.action = pAction;
      this.value = pValue;
   }

   /**
    * Gets the action to perform when this event is raised.
    */
   public ClickEvent.Action getAction() {
      return this.action;
   }

   /**
    * Gets the value to perform the action on when this event is raised.  For example, if the action is "open URL", this
    * would be the URL to open.
    */
   public String getValue() {
      return this.value;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         ClickEvent clickevent = (ClickEvent)pOther;
         if (this.action != clickevent.action) {
            return false;
         } else {
            if (this.value != null) {
               if (!this.value.equals(clickevent.value)) {
                  return false;
               }
            } else if (clickevent.value != null) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public String toString() {
      return "ClickEvent{action=" + this.action + ", value='" + this.value + "'}";
   }

   public int hashCode() {
      int i = this.action.hashCode();
      return 31 * i + (this.value != null ? this.value.hashCode() : 0);
   }

   public static enum Action {
      OPEN_URL("open_url", true),
      OPEN_FILE("open_file", false),
      RUN_COMMAND("run_command", true),
      SUGGEST_COMMAND("suggest_command", true),
      CHANGE_PAGE("change_page", true),
      COPY_TO_CLIPBOARD("copy_to_clipboard", true);

      private static final Map<String, ClickEvent.Action> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(ClickEvent.Action::getName, (p_130648_) -> {
         return p_130648_;
      }));
      private final boolean allowFromServer;
      /** The canonical name used to refer to this action. */
      private final String name;

      private Action(String pName, boolean pAllowFromServer) {
         this.name = pName;
         this.allowFromServer = pAllowFromServer;
      }

      /**
       * Indicates whether this event can be run from chat text.
       */
      public boolean isAllowedFromServer() {
         return this.allowFromServer;
      }

      /**
       * Gets the canonical name for this action (e.g., "run_command")
       */
      public String getName() {
         return this.name;
      }

      /**
       * Gets a value by its canonical name.
       */
      public static ClickEvent.Action getByName(String pCanonicalName) {
         return LOOKUP.get(pCanonicalName);
      }
   }
}