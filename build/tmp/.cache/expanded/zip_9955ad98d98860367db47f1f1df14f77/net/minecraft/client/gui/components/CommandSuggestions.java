package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommandSuggestions {
   private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
   private static final Style UNPARSED_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
   private static final Style LITERAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
   private static final List<Style> ARGUMENT_STYLES = Stream.of(ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());
   final Minecraft minecraft;
   private final Screen screen;
   final EditBox input;
   final Font font;
   private final boolean commandsOnly;
   private final boolean onlyShowIfCursorPastError;
   final int lineStartOffset;
   final int suggestionLineLimit;
   final boolean anchorToBottom;
   final int fillColor;
   private final List<FormattedCharSequence> commandUsage = Lists.newArrayList();
   private int commandUsagePosition;
   private int commandUsageWidth;
   @Nullable
   private ParseResults<SharedSuggestionProvider> currentParse;
   @Nullable
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Nullable
   private CommandSuggestions.SuggestionsList suggestions;
   private boolean allowSuggestions;
   boolean keepSuggestions;

   public CommandSuggestions(Minecraft pMinecraft, Screen pScreen, EditBox pInput, Font pFont, boolean pCommandsOnly, boolean pOnlyShowIfCursorPastError, int pLineStartOffset, int pSuggestionLineLimit, boolean pAnchorToBottom, int pFillColor) {
      this.minecraft = pMinecraft;
      this.screen = pScreen;
      this.input = pInput;
      this.font = pFont;
      this.commandsOnly = pCommandsOnly;
      this.onlyShowIfCursorPastError = pOnlyShowIfCursorPastError;
      this.lineStartOffset = pLineStartOffset;
      this.suggestionLineLimit = pSuggestionLineLimit;
      this.anchorToBottom = pAnchorToBottom;
      this.fillColor = pFillColor;
      pInput.setFormatter(this::formatChat);
   }

   public void setAllowSuggestions(boolean pAutoSuggest) {
      this.allowSuggestions = pAutoSuggest;
      if (!pAutoSuggest) {
         this.suggestions = null;
      }

   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.suggestions != null && this.suggestions.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (this.screen.getFocused() == this.input && pKeyCode == 258) {
         this.showSuggestions(true);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double pDelta) {
      return this.suggestions != null && this.suggestions.mouseScrolled(Mth.clamp(pDelta, -1.0D, 1.0D));
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pMouseButton) {
      return this.suggestions != null && this.suggestions.mouseClicked((int)pMouseX, (int)pMouseY, pMouseButton);
   }

   public void showSuggestions(boolean pNarrateFirstSuggestion) {
      if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
         Suggestions suggestions = this.pendingSuggestions.join();
         if (!suggestions.isEmpty()) {
            int i = 0;

            for(Suggestion suggestion : suggestions.getList()) {
               i = Math.max(i, this.font.width(suggestion.getText()));
            }

            int j = Mth.clamp(this.input.getScreenX(suggestions.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
            int k = this.anchorToBottom ? this.screen.height - 12 : 72;
            this.suggestions = new CommandSuggestions.SuggestionsList(j, k, i, this.sortSuggestions(suggestions), pNarrateFirstSuggestion);
         }
      }

   }

   public void hide() {
      this.suggestions = null;
   }

   private List<Suggestion> sortSuggestions(Suggestions pSuggestions) {
      String s = this.input.getValue().substring(0, this.input.getCursorPosition());
      int i = getLastWordIndex(s);
      String s1 = s.substring(i).toLowerCase(Locale.ROOT);
      List<Suggestion> list = Lists.newArrayList();
      List<Suggestion> list1 = Lists.newArrayList();

      for(Suggestion suggestion : pSuggestions.getList()) {
         if (!suggestion.getText().startsWith(s1) && !suggestion.getText().startsWith("minecraft:" + s1)) {
            list1.add(suggestion);
         } else {
            list.add(suggestion);
         }
      }

      list.addAll(list1);
      return list;
   }

   public void updateCommandInfo() {
      String s = this.input.getValue();
      if (this.currentParse != null && !this.currentParse.getReader().getString().equals(s)) {
         this.currentParse = null;
      }

      if (!this.keepSuggestions) {
         this.input.setSuggestion((String)null);
         this.suggestions = null;
      }

      this.commandUsage.clear();
      StringReader stringreader = new StringReader(s);
      boolean flag = stringreader.canRead() && stringreader.peek() == '/';
      if (flag) {
         stringreader.skip();
      }

      boolean flag1 = this.commandsOnly || flag;
      int i = this.input.getCursorPosition();
      if (flag1) {
         CommandDispatcher<SharedSuggestionProvider> commanddispatcher = this.minecraft.player.connection.getCommands();
         if (this.currentParse == null) {
            this.currentParse = commanddispatcher.parse(stringreader, this.minecraft.player.connection.getSuggestionsProvider());
         }

         int j = this.onlyShowIfCursorPastError ? stringreader.getCursor() : 1;
         if (i >= j && (this.suggestions == null || !this.keepSuggestions)) {
            this.pendingSuggestions = commanddispatcher.getCompletionSuggestions(this.currentParse, i);
            this.pendingSuggestions.thenRun(() -> {
               if (this.pendingSuggestions.isDone()) {
                  this.updateUsageInfo();
               }
            });
         }
      } else {
         String s1 = s.substring(0, i);
         int k = getLastWordIndex(s1);
         Collection<String> collection = this.minecraft.player.connection.getSuggestionsProvider().getCustomTabSugggestions();
         this.pendingSuggestions = SharedSuggestionProvider.suggest(collection, new SuggestionsBuilder(s1, k));
      }

   }

   private static int getLastWordIndex(String pText) {
      if (Strings.isNullOrEmpty(pText)) {
         return 0;
      } else {
         int i = 0;

         for(Matcher matcher = WHITESPACE_PATTERN.matcher(pText); matcher.find(); i = matcher.end()) {
         }

         return i;
      }
   }

   private static FormattedCharSequence getExceptionMessage(CommandSyntaxException pException) {
      Component component = ComponentUtils.fromMessage(pException.getRawMessage());
      String s = pException.getContext();
      return s == null ? component.getVisualOrderText() : Component.translatable("command.context.parse_error", component, pException.getCursor(), s).getVisualOrderText();
   }

   private void updateUsageInfo() {
      boolean flag = false;
      if (this.input.getCursorPosition() == this.input.getValue().length()) {
         if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
            int i = 0;

            for(Map.Entry<CommandNode<SharedSuggestionProvider>, CommandSyntaxException> entry : this.currentParse.getExceptions().entrySet()) {
               CommandSyntaxException commandsyntaxexception = entry.getValue();
               if (commandsyntaxexception.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                  ++i;
               } else {
                  this.commandUsage.add(getExceptionMessage(commandsyntaxexception));
               }
            }

            if (i > 0) {
               this.commandUsage.add(getExceptionMessage(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
            }
         } else if (this.currentParse.getReader().canRead()) {
            flag = true;
         }
      }

      this.commandUsagePosition = 0;
      this.commandUsageWidth = this.screen.width;
      if (this.commandUsage.isEmpty() && !this.fillNodeUsage(ChatFormatting.GRAY) && flag) {
         this.commandUsage.add(getExceptionMessage(Commands.getParseException(this.currentParse)));
      }

      this.suggestions = null;
      if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get()) {
         this.showSuggestions(false);
      }

   }

   private boolean fillNodeUsage(ChatFormatting pChatFormatting) {
      CommandContextBuilder<SharedSuggestionProvider> commandcontextbuilder = this.currentParse.getContext();
      SuggestionContext<SharedSuggestionProvider> suggestioncontext = commandcontextbuilder.findSuggestionContext(this.input.getCursorPosition());
      Map<CommandNode<SharedSuggestionProvider>, String> map = this.minecraft.player.connection.getCommands().getSmartUsage(suggestioncontext.parent, this.minecraft.player.connection.getSuggestionsProvider());
      List<FormattedCharSequence> list = Lists.newArrayList();
      int i = 0;
      Style style = Style.EMPTY.withColor(pChatFormatting);

      for(Map.Entry<CommandNode<SharedSuggestionProvider>, String> entry : map.entrySet()) {
         if (!(entry.getKey() instanceof LiteralCommandNode)) {
            list.add(FormattedCharSequence.forward(entry.getValue(), style));
            i = Math.max(i, this.font.width(entry.getValue()));
         }
      }

      if (!list.isEmpty()) {
         this.commandUsage.addAll(list);
         this.commandUsagePosition = Mth.clamp(this.input.getScreenX(suggestioncontext.startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
         this.commandUsageWidth = i;
         return true;
      } else {
         return false;
      }
   }

   private FormattedCharSequence formatChat(String p_93915_, int p_93916_) {
      return this.currentParse != null ? formatText(this.currentParse, p_93915_, p_93916_) : FormattedCharSequence.forward(p_93915_, Style.EMPTY);
   }

   @Nullable
   static String calculateSuggestionSuffix(String pInputText, String pSuggestionText) {
      return pSuggestionText.startsWith(pInputText) ? pSuggestionText.substring(pInputText.length()) : null;
   }

   private static FormattedCharSequence formatText(ParseResults<SharedSuggestionProvider> pProvider, String pCommand, int pMaxLength) {
      List<FormattedCharSequence> list = Lists.newArrayList();
      int i = 0;
      int j = -1;
      CommandContextBuilder<SharedSuggestionProvider> commandcontextbuilder = pProvider.getContext().getLastChild();

      for(ParsedArgument<SharedSuggestionProvider, ?> parsedargument : commandcontextbuilder.getArguments().values()) {
         ++j;
         if (j >= ARGUMENT_STYLES.size()) {
            j = 0;
         }

         int k = Math.max(parsedargument.getRange().getStart() - pMaxLength, 0);
         if (k >= pCommand.length()) {
            break;
         }

         int l = Math.min(parsedargument.getRange().getEnd() - pMaxLength, pCommand.length());
         if (l > 0) {
            list.add(FormattedCharSequence.forward(pCommand.substring(i, k), LITERAL_STYLE));
            list.add(FormattedCharSequence.forward(pCommand.substring(k, l), ARGUMENT_STYLES.get(j)));
            i = l;
         }
      }

      if (pProvider.getReader().canRead()) {
         int i1 = Math.max(pProvider.getReader().getCursor() - pMaxLength, 0);
         if (i1 < pCommand.length()) {
            int j1 = Math.min(i1 + pProvider.getReader().getRemainingLength(), pCommand.length());
            list.add(FormattedCharSequence.forward(pCommand.substring(i, i1), LITERAL_STYLE));
            list.add(FormattedCharSequence.forward(pCommand.substring(i1, j1), UNPARSED_STYLE));
            i = j1;
         }
      }

      list.add(FormattedCharSequence.forward(pCommand.substring(i), LITERAL_STYLE));
      return FormattedCharSequence.composite(list);
   }

   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
      if (!this.renderSuggestions(pGuiGraphics, pMouseX, pMouseY)) {
         this.renderUsage(pGuiGraphics);
      }

   }

   public boolean renderSuggestions(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
      if (this.suggestions != null) {
         this.suggestions.render(pGuiGraphics, pMouseX, pMouseY);
         return true;
      } else {
         return false;
      }
   }

   public void renderUsage(GuiGraphics pGuiGraphics) {
      int i = 0;

      for(FormattedCharSequence formattedcharsequence : this.commandUsage) {
         int j = this.anchorToBottom ? this.screen.height - 14 - 13 - 12 * i : 72 + 12 * i;
         pGuiGraphics.fill(this.commandUsagePosition - 1, j, this.commandUsagePosition + this.commandUsageWidth + 1, j + 12, this.fillColor);
         pGuiGraphics.drawString(this.font, formattedcharsequence, this.commandUsagePosition, j + 2, -1);
         ++i;
      }

   }

   public Component getNarrationMessage() {
      return (Component)(this.suggestions != null ? CommonComponents.NEW_LINE.copy().append(this.suggestions.getNarrationMessage()) : CommonComponents.EMPTY);
   }

   @OnlyIn(Dist.CLIENT)
   public class SuggestionsList {
      private final Rect2i rect;
      private final String originalContents;
      private final List<Suggestion> suggestionList;
      private int offset;
      private int current;
      private Vec2 lastMouse = Vec2.ZERO;
      private boolean tabCycles;
      private int lastNarratedEntry;

      SuggestionsList(int pXPos, int pYPos, int pWidth, List<Suggestion> pSuggestionList, boolean pNarrateFirstSuggestion) {
         int i = pXPos - 1;
         int j = CommandSuggestions.this.anchorToBottom ? pYPos - 3 - Math.min(pSuggestionList.size(), CommandSuggestions.this.suggestionLineLimit) * 12 : pYPos;
         this.rect = new Rect2i(i, j, pWidth + 1, Math.min(pSuggestionList.size(), CommandSuggestions.this.suggestionLineLimit) * 12);
         this.originalContents = CommandSuggestions.this.input.getValue();
         this.lastNarratedEntry = pNarrateFirstSuggestion ? -1 : 0;
         this.suggestionList = pSuggestionList;
         this.select(0);
      }

      public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
         int i = Math.min(this.suggestionList.size(), CommandSuggestions.this.suggestionLineLimit);
         int j = -5592406;
         boolean flag = this.offset > 0;
         boolean flag1 = this.suggestionList.size() > this.offset + i;
         boolean flag2 = flag || flag1;
         boolean flag3 = this.lastMouse.x != (float)pMouseX || this.lastMouse.y != (float)pMouseY;
         if (flag3) {
            this.lastMouse = new Vec2((float)pMouseX, (float)pMouseY);
         }

         if (flag2) {
            pGuiGraphics.fill(this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), CommandSuggestions.this.fillColor);
            pGuiGraphics.fill(this.rect.getX(), this.rect.getY() + this.rect.getHeight(), this.rect.getX() + this.rect.getWidth(), this.rect.getY() + this.rect.getHeight() + 1, CommandSuggestions.this.fillColor);
            if (flag) {
               for(int k = 0; k < this.rect.getWidth(); ++k) {
                  if (k % 2 == 0) {
                     pGuiGraphics.fill(this.rect.getX() + k, this.rect.getY() - 1, this.rect.getX() + k + 1, this.rect.getY(), -1);
                  }
               }
            }

            if (flag1) {
               for(int i1 = 0; i1 < this.rect.getWidth(); ++i1) {
                  if (i1 % 2 == 0) {
                     pGuiGraphics.fill(this.rect.getX() + i1, this.rect.getY() + this.rect.getHeight(), this.rect.getX() + i1 + 1, this.rect.getY() + this.rect.getHeight() + 1, -1);
                  }
               }
            }
         }

         boolean flag4 = false;

         for(int l = 0; l < i; ++l) {
            Suggestion suggestion = this.suggestionList.get(l + this.offset);
            pGuiGraphics.fill(this.rect.getX(), this.rect.getY() + 12 * l, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * l + 12, CommandSuggestions.this.fillColor);
            if (pMouseX > this.rect.getX() && pMouseX < this.rect.getX() + this.rect.getWidth() && pMouseY > this.rect.getY() + 12 * l && pMouseY < this.rect.getY() + 12 * l + 12) {
               if (flag3) {
                  this.select(l + this.offset);
               }

               flag4 = true;
            }

            pGuiGraphics.drawString(CommandSuggestions.this.font, suggestion.getText(), this.rect.getX() + 1, this.rect.getY() + 2 + 12 * l, l + this.offset == this.current ? -256 : -5592406);
         }

         if (flag4) {
            Message message = this.suggestionList.get(this.current).getTooltip();
            if (message != null) {
               pGuiGraphics.renderTooltip(CommandSuggestions.this.font, ComponentUtils.fromMessage(message), pMouseX, pMouseY);
            }
         }

      }

      public boolean mouseClicked(int pMouseX, int pMouseY, int pMouseButton) {
         if (!this.rect.contains(pMouseX, pMouseY)) {
            return false;
         } else {
            int i = (pMouseY - this.rect.getY()) / 12 + this.offset;
            if (i >= 0 && i < this.suggestionList.size()) {
               this.select(i);
               this.useSuggestion();
            }

            return true;
         }
      }

      public boolean mouseScrolled(double pDelta) {
         int i = (int)(CommandSuggestions.this.minecraft.mouseHandler.xpos() * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledWidth() / (double)CommandSuggestions.this.minecraft.getWindow().getScreenWidth());
         int j = (int)(CommandSuggestions.this.minecraft.mouseHandler.ypos() * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledHeight() / (double)CommandSuggestions.this.minecraft.getWindow().getScreenHeight());
         if (this.rect.contains(i, j)) {
            this.offset = Mth.clamp((int)((double)this.offset - pDelta), 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
            return true;
         } else {
            return false;
         }
      }

      public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
         if (pKeyCode == 265) {
            this.cycle(-1);
            this.tabCycles = false;
            return true;
         } else if (pKeyCode == 264) {
            this.cycle(1);
            this.tabCycles = false;
            return true;
         } else if (pKeyCode == 258) {
            if (this.tabCycles) {
               this.cycle(Screen.hasShiftDown() ? -1 : 1);
            }

            this.useSuggestion();
            return true;
         } else if (pKeyCode == 256) {
            CommandSuggestions.this.hide();
            return true;
         } else {
            return false;
         }
      }

      public void cycle(int pChange) {
         this.select(this.current + pChange);
         int i = this.offset;
         int j = this.offset + CommandSuggestions.this.suggestionLineLimit - 1;
         if (this.current < i) {
            this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
         } else if (this.current > j) {
            this.offset = Mth.clamp(this.current + CommandSuggestions.this.lineStartOffset - CommandSuggestions.this.suggestionLineLimit, 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
         }

      }

      public void select(int pIndex) {
         this.current = pIndex;
         if (this.current < 0) {
            this.current += this.suggestionList.size();
         }

         if (this.current >= this.suggestionList.size()) {
            this.current -= this.suggestionList.size();
         }

         Suggestion suggestion = this.suggestionList.get(this.current);
         CommandSuggestions.this.input.setSuggestion(CommandSuggestions.calculateSuggestionSuffix(CommandSuggestions.this.input.getValue(), suggestion.apply(this.originalContents)));
         if (this.lastNarratedEntry != this.current) {
            CommandSuggestions.this.minecraft.getNarrator().sayNow(this.getNarrationMessage());
         }

      }

      public void useSuggestion() {
         Suggestion suggestion = this.suggestionList.get(this.current);
         CommandSuggestions.this.keepSuggestions = true;
         CommandSuggestions.this.input.setValue(suggestion.apply(this.originalContents));
         int i = suggestion.getRange().getStart() + suggestion.getText().length();
         CommandSuggestions.this.input.setCursorPosition(i);
         CommandSuggestions.this.input.setHighlightPos(i);
         this.select(this.current);
         CommandSuggestions.this.keepSuggestions = false;
         this.tabCycles = true;
      }

      Component getNarrationMessage() {
         this.lastNarratedEntry = this.current;
         Suggestion suggestion = this.suggestionList.get(this.current);
         Message message = suggestion.getTooltip();
         return message != null ? Component.translatable("narration.suggestion.tooltip", this.current + 1, this.suggestionList.size(), suggestion.getText(), message) : Component.translatable("narration.suggestion", this.current + 1, this.suggestionList.size(), suggestion.getText());
      }
   }
}