package net.minecraft.client.gui.screens;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class Screen extends AbstractContainerEventHandler implements Renderable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");
   private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
   public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
   protected final Component title;
   private final List<GuiEventListener> children = Lists.newArrayList();
   private final List<NarratableEntry> narratables = Lists.newArrayList();
   @Nullable
   protected Minecraft minecraft;
   private boolean initialized;
   public int width;
   public int height;
   public final List<Renderable> renderables = Lists.newArrayList();
   protected Font font;
   @Nullable
   private URI clickedLink;
   private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
   private static final long NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME;
   private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
   private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
   private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
   private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
   private long narrationSuppressTime = Long.MIN_VALUE;
   private long nextNarrationTime = Long.MAX_VALUE;
   @Nullable
   private NarratableEntry lastNarratable;
   @Nullable
   private Screen.DeferredTooltipRendering deferredTooltipRendering;
   protected final Executor screenExecutor = (p_289626_) -> {
      this.minecraft.execute(() -> {
         if (this.minecraft.screen == this) {
            p_289626_.run();
         }

      });
   };

   protected Screen(Component pTitle) {
      this.title = pTitle;
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getNarrationMessage() {
      return this.getTitle();
   }

   public final void renderWithTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.deferredTooltipRendering != null) {
         pGuiGraphics.renderTooltip(this.font, this.deferredTooltipRendering.tooltip(), this.deferredTooltipRendering.positioner(), pMouseX, pMouseY);
         this.deferredTooltipRendering = null;
      }

   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      for(Renderable renderable : this.renderables) {
         renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }

   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256 && this.shouldCloseOnEsc()) {
         this.onClose();
         return true;
      } else if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else {
         Object object;
         switch (pKeyCode) {
            case 258:
               object = this.createTabEvent();
               break;
            case 259:
            case 260:
            case 261:
            default:
               object = null;
               break;
            case 262:
               object = this.createArrowEvent(ScreenDirection.RIGHT);
               break;
            case 263:
               object = this.createArrowEvent(ScreenDirection.LEFT);
               break;
            case 264:
               object = this.createArrowEvent(ScreenDirection.DOWN);
               break;
            case 265:
               object = this.createArrowEvent(ScreenDirection.UP);
         }

         FocusNavigationEvent focusnavigationevent = (FocusNavigationEvent)object;
         if (focusnavigationevent != null) {
            ComponentPath componentpath = super.nextFocusPath(focusnavigationevent);
            if (componentpath == null && focusnavigationevent instanceof FocusNavigationEvent.TabNavigation) {
               this.clearFocus();
               componentpath = super.nextFocusPath(focusnavigationevent);
            }

            if (componentpath != null) {
               this.changeFocus(componentpath);
            }
         }

         return false;
      }
   }

   private FocusNavigationEvent.TabNavigation createTabEvent() {
      boolean flag = !hasShiftDown();
      return new FocusNavigationEvent.TabNavigation(flag);
   }

   private FocusNavigationEvent.ArrowNavigation createArrowEvent(ScreenDirection pDirection) {
      return new FocusNavigationEvent.ArrowNavigation(pDirection);
   }

   protected void setInitialFocus(GuiEventListener pListener) {
      ComponentPath componentpath = ComponentPath.path(this, pListener.nextFocusPath(new FocusNavigationEvent.InitialFocus()));
      if (componentpath != null) {
         this.changeFocus(componentpath);
      }

   }

   private void clearFocus() {
      ComponentPath componentpath = this.getCurrentFocusPath();
      if (componentpath != null) {
         componentpath.applyFocus(false);
      }

   }

   @VisibleForTesting
   protected void changeFocus(ComponentPath pPath) {
      this.clearFocus();
      pPath.applyFocus(true);
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public void onClose() {
      this.minecraft.popGuiLayer();
   }

   protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T pWidget) {
      this.renderables.add(pWidget);
      return this.addWidget(pWidget);
   }

   protected <T extends Renderable> T addRenderableOnly(T pRenderable) {
      this.renderables.add(pRenderable);
      return pRenderable;
   }

   protected <T extends GuiEventListener & NarratableEntry> T addWidget(T pListener) {
      this.children.add(pListener);
      this.narratables.add(pListener);
      return pListener;
   }

   protected void removeWidget(GuiEventListener pListener) {
      if (pListener instanceof Renderable) {
         this.renderables.remove((Renderable)pListener);
      }

      if (pListener instanceof NarratableEntry) {
         this.narratables.remove((NarratableEntry)pListener);
      }

      this.children.remove(pListener);
   }

   protected void clearWidgets() {
      this.renderables.clear();
      this.children.clear();
      this.narratables.clear();
   }

   public static List<Component> getTooltipFromItem(Minecraft pMinecraft, ItemStack pItem) {
      return pItem.getTooltipLines(pMinecraft.player, pMinecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
   }

   protected void insertText(String pText, boolean pOverwrite) {
   }

   public boolean handleComponentClicked(@Nullable Style pStyle) {
      if (pStyle == null) {
         return false;
      } else {
         ClickEvent clickevent = pStyle.getClickEvent();
         if (hasShiftDown()) {
            if (pStyle.getInsertion() != null) {
               this.insertText(pStyle.getInsertion(), false);
            }
         } else if (clickevent != null) {
            if (clickevent.getAction() == ClickEvent.Action.OPEN_URL) {
               if (!this.minecraft.options.chatLinks().get()) {
                  return false;
               }

               try {
                  URI uri = new URI(clickevent.getValue());
                  String s = uri.getScheme();
                  if (s == null) {
                     throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                  }

                  if (!ALLOWED_PROTOCOLS.contains(s.toLowerCase(Locale.ROOT))) {
                     throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
                  }

                  if (this.minecraft.options.chatLinksPrompt().get()) {
                     this.clickedLink = uri;
                     this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickevent.getValue(), false));
                  } else {
                     this.openLink(uri);
                  }
               } catch (URISyntaxException urisyntaxexception) {
                  LOGGER.error("Can't open url for {}", clickevent, urisyntaxexception);
               }
            } else if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE) {
               URI uri1 = (new File(clickevent.getValue())).toURI();
               this.openLink(uri1);
            } else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
               this.insertText(SharedConstants.filterText(clickevent.getValue()), true);
            } else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
               String s1 = SharedConstants.filterText(clickevent.getValue());
               if (s1.startsWith("/")) {
                  if (!this.minecraft.player.connection.sendUnsignedCommand(s1.substring(1))) {
                     LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", (Object)s1);
                  }
               } else {
                  LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", (Object)s1);
               }
            } else if (clickevent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
               this.minecraft.keyboardHandler.setClipboard(clickevent.getValue());
            } else {
               LOGGER.error("Don't know how to handle {}", (Object)clickevent);
            }

            return true;
         }

         return false;
      }
   }

   public final void init(Minecraft pMinecraft, int pWidth, int pHeight) {
      this.minecraft = pMinecraft;
      this.font = pMinecraft.font;
      this.width = pWidth;
      this.height = pHeight;
      if (!this.initialized) {
         if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.Init.Pre(this, this.children, this::addEventWidget, this::removeWidget))) {
         this.init();
         }
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.Init.Post(this, this.children, this::addEventWidget, this::removeWidget));
      } else {
         this.repositionElements();
      }

      this.initialized = true;
      this.triggerImmediateNarration(false);
      this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
   }

   protected void rebuildWidgets() {
      this.clearWidgets();
      this.clearFocus();
      if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.Init.Pre(this, this.children, this::addEventWidget, this::removeWidget))) {
      this.init();
      }
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.Init.Post(this, this.children, this::addEventWidget, this::removeWidget));
   }

   /**
    * {@return a List containing all GUI element children of this GUI element}
    */
   public List<? extends GuiEventListener> children() {
      return this.children;
   }

   protected void init() {
   }

   public void tick() {
   }

   public void removed() {
   }

   public void added() {
   }

   public void renderBackground(GuiGraphics pGuiGraphics) {
      if (this.minecraft.level != null) {
         pGuiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundRendered(this, pGuiGraphics));
      } else {
         this.renderDirtBackground(pGuiGraphics);
      }

   }

   public void renderDirtBackground(GuiGraphics pGuiGraphics) {
      pGuiGraphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
      int i = 32;
      pGuiGraphics.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundRendered(this, pGuiGraphics));
   }

   public boolean isPauseScreen() {
      return true;
   }

   private void confirmLink(boolean p_96623_) {
      if (p_96623_) {
         this.openLink(this.clickedLink);
      }

      this.clickedLink = null;
      this.minecraft.setScreen(this);
   }

   private void openLink(URI pUri) {
      Util.getPlatform().openUri(pUri);
   }

   public static boolean hasControlDown() {
      if (Minecraft.ON_OSX) {
         return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
      } else {
         return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
      }
   }

   public static boolean hasShiftDown() {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
   }

   public static boolean hasAltDown() {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
   }

   public static boolean isCut(int pKeyCode) {
      return pKeyCode == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isPaste(int pKeyCode) {
      return pKeyCode == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isCopy(int pKeyCode) {
      return pKeyCode == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isSelectAll(int pKeyCode) {
      return pKeyCode == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   protected void repositionElements() {
      this.rebuildWidgets();
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      this.width = pWidth;
      this.height = pHeight;
      this.repositionElements();
   }

   public static void wrapScreenError(Runnable pAction, String pErrorDesc, String pScreenName) {
      try {
         pAction.run();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, pErrorDesc);
         CrashReportCategory crashreportcategory = crashreport.addCategory("Affected screen");
         crashreportcategory.setDetail("Screen name", () -> {
            return pScreenName;
         });
         throw new ReportedException(crashreport);
      }
   }

   protected boolean isValidCharacterForName(String pText, char pCharTyped, int pCursorPos) {
      int i = pText.indexOf(58);
      int j = pText.indexOf(47);
      if (pCharTyped == ':') {
         return (j == -1 || pCursorPos <= j) && i == -1;
      } else if (pCharTyped == '/') {
         return pCursorPos > i;
      } else {
         return pCharTyped == '_' || pCharTyped == '-' || pCharTyped >= 'a' && pCharTyped <= 'z' || pCharTyped >= '0' && pCharTyped <= '9' || pCharTyped == '.';
      }
   }

   /**
    * Checks if the given mouse coordinates are over the GUI element.
    * <p>
    * @return {@code true} if the mouse is over the GUI element, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    */
   public boolean isMouseOver(double pMouseX, double pMouseY) {
      return true;
   }

   public void onFilesDrop(List<Path> pPacks) {
   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   private void scheduleNarration(long pDelay, boolean pStopSuppression) {
      this.nextNarrationTime = Util.getMillis() + pDelay;
      if (pStopSuppression) {
         this.narrationSuppressTime = Long.MIN_VALUE;
      }

   }

   private void suppressNarration(long pTime) {
      this.narrationSuppressTime = Util.getMillis() + pTime;
   }

   public void afterMouseMove() {
      this.scheduleNarration(750L, false);
   }

   public void afterMouseAction() {
      this.scheduleNarration(200L, true);
   }

   public void afterKeyboardAction() {
      this.scheduleNarration(200L, true);
   }

   private boolean shouldRunNarration() {
      return this.minecraft.getNarrator().isActive();
   }

   public void handleDelayedNarration() {
      if (this.shouldRunNarration()) {
         long i = Util.getMillis();
         if (i > this.nextNarrationTime && i > this.narrationSuppressTime) {
            this.runNarration(true);
            this.nextNarrationTime = Long.MAX_VALUE;
         }
      }

   }

   public void triggerImmediateNarration(boolean pOnlyNarrateNew) {
      if (this.shouldRunNarration()) {
         this.runNarration(pOnlyNarrateNew);
      }

   }

   private void runNarration(boolean pOnlyNarrateNew) {
      this.narrationState.update(this::updateNarrationState);
      String s = this.narrationState.collectNarrationText(!pOnlyNarrateNew);
      if (!s.isEmpty()) {
         this.minecraft.getNarrator().sayNow(s);
      }

   }

   protected boolean shouldNarrateNavigation() {
      return true;
   }

   protected void updateNarrationState(NarrationElementOutput p_169396_) {
      p_169396_.add(NarratedElementType.TITLE, this.getNarrationMessage());
      if (this.shouldNarrateNavigation()) {
         p_169396_.add(NarratedElementType.USAGE, USAGE_NARRATION);
      }

      this.updateNarratedWidget(p_169396_);
   }

   protected void updateNarratedWidget(NarrationElementOutput pNarrationElementOutput) {
      List<NarratableEntry> list = this.narratables.stream().filter(NarratableEntry::isActive).collect(Collectors.toList());
      Collections.sort(list, Comparator.comparingInt(TabOrderedElement::getTabOrderGroup));
      Screen.NarratableSearchResult screen$narratablesearchresult = findNarratableWidget(list, this.lastNarratable);
      if (screen$narratablesearchresult != null) {
         if (screen$narratablesearchresult.priority.isTerminal()) {
            this.lastNarratable = screen$narratablesearchresult.entry;
         }

         if (list.size() > 1) {
            pNarrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.screen", screen$narratablesearchresult.index + 1, list.size()));
            if (screen$narratablesearchresult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
               pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
            }
         }

         screen$narratablesearchresult.entry.updateNarration(pNarrationElementOutput.nest());
      }

   }

   @Nullable
   public static Screen.NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> pEntries, @Nullable NarratableEntry pTarget) {
      Screen.NarratableSearchResult screen$narratablesearchresult = null;
      Screen.NarratableSearchResult screen$narratablesearchresult1 = null;
      int i = 0;

      for(int j = pEntries.size(); i < j; ++i) {
         NarratableEntry narratableentry = pEntries.get(i);
         NarratableEntry.NarrationPriority narratableentry$narrationpriority = narratableentry.narrationPriority();
         if (narratableentry$narrationpriority.isTerminal()) {
            if (narratableentry != pTarget) {
               return new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
            }

            screen$narratablesearchresult1 = new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
         } else if (narratableentry$narrationpriority.compareTo(screen$narratablesearchresult != null ? screen$narratablesearchresult.priority : NarratableEntry.NarrationPriority.NONE) > 0) {
            screen$narratablesearchresult = new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
         }
      }

      return screen$narratablesearchresult != null ? screen$narratablesearchresult : screen$narratablesearchresult1;
   }

   public void narrationEnabled() {
      this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
   }

   public void setTooltipForNextRenderPass(List<FormattedCharSequence> pTooltip) {
      this.setTooltipForNextRenderPass(pTooltip, DefaultTooltipPositioner.INSTANCE, true);
   }

   public void setTooltipForNextRenderPass(List<FormattedCharSequence> pTooltip, ClientTooltipPositioner pPositioner, boolean pOverride) {
      if (this.deferredTooltipRendering == null || pOverride) {
         this.deferredTooltipRendering = new Screen.DeferredTooltipRendering(pTooltip, pPositioner);
      }

   }

   protected void setTooltipForNextRenderPass(Component pTooltip) {
      this.setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, pTooltip));
   }

   public void setTooltipForNextRenderPass(Tooltip pTooltip, ClientTooltipPositioner pPositioner, boolean pOverride) {
      this.setTooltipForNextRenderPass(pTooltip.toCharSequence(this.minecraft), pPositioner, pOverride);
   }

   protected static void hideWidgets(AbstractWidget... pWidgets) {
      for(AbstractWidget abstractwidget : pWidgets) {
         abstractwidget.visible = false;
      }

   }

   /**
    * {@return the {@link ScreenRectangle} occupied by the GUI element}
    */
   public ScreenRectangle getRectangle() {
      return new ScreenRectangle(0, 0, this.width, this.height);
   }

   @Nullable
   public Music getBackgroundMusic() {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   static record DeferredTooltipRendering(List<FormattedCharSequence> tooltip, ClientTooltipPositioner positioner) {
   }

   @OnlyIn(Dist.CLIENT)
   public static class NarratableSearchResult {
      public final NarratableEntry entry;
      public final int index;
      public final NarratableEntry.NarrationPriority priority;

      public NarratableSearchResult(NarratableEntry pEntry, int pIndex, NarratableEntry.NarrationPriority pPriority) {
         this.entry = pEntry;
         this.index = pIndex;
         this.priority = pPriority;
      }
   }

   private void addEventWidget(GuiEventListener b) {
      if (b instanceof Renderable r)
         this.renderables.add(r);
      if (b instanceof NarratableEntry ne)
         this.narratables.add(ne);
      children.add(b);
   }
}
