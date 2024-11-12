package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class JoinMultiplayerScreen extends Screen {
   public static final int BUTTON_ROW_WIDTH = 308;
   public static final int TOP_ROW_BUTTON_WIDTH = 100;
   public static final int LOWER_ROW_BUTTON_WIDTH = 74;
   public static final int FOOTER_HEIGHT = 64;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ServerStatusPinger pinger = new ServerStatusPinger();
   private final Screen lastScreen;
   protected ServerSelectionList serverSelectionList;
   private ServerList servers;
   private Button editButton;
   private Button selectButton;
   private Button deleteButton;
   /** The text to be displayed when the player's cursor hovers over a server listing. */
   @Nullable
   private List<Component> toolTip;
   private ServerData editingServer;
   private LanServerDetection.LanServerList lanServerList;
   @Nullable
   private LanServerDetection.LanServerDetector lanServerDetector;
   private boolean initedOnce;

   public JoinMultiplayerScreen(Screen pLastScreen) {
      super(Component.translatable("multiplayer.title"));
      this.lastScreen = pLastScreen;
   }

   protected void init() {
      if (this.initedOnce) {
         this.serverSelectionList.updateSize(this.width, this.height, 32, this.height - 64);
      } else {
         this.initedOnce = true;
         this.servers = new ServerList(this.minecraft);
         this.servers.load();
         this.lanServerList = new LanServerDetection.LanServerList();

         try {
            this.lanServerDetector = new LanServerDetection.LanServerDetector(this.lanServerList);
            this.lanServerDetector.start();
         } catch (Exception exception) {
            LOGGER.warn("Unable to start LAN server detection: {}", (Object)exception.getMessage());
         }

         this.serverSelectionList = new ServerSelectionList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
         this.serverSelectionList.updateOnlineServers(this.servers);
      }

      this.addWidget(this.serverSelectionList);
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), (p_99728_) -> {
         this.joinSelectedServer();
      }).width(100).build());
      Button button = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.direct"), (p_280868_) -> {
         this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
         this.minecraft.setScreen(new DirectJoinServerScreen(this, this::directJoinCallback, this.editingServer));
      }).width(100).build());
      Button button1 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.add"), (p_280869_) -> {
         this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
         this.minecraft.setScreen(new EditServerScreen(this, this::addServerCallback, this.editingServer));
      }).width(100).build());
      this.editButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.edit"), (p_99715_) -> {
         ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
         if (serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
            ServerData serverdata = ((ServerSelectionList.OnlineServerEntry)serverselectionlist$entry).getServerData();
            this.editingServer = new ServerData(serverdata.name, serverdata.ip, false);
            this.editingServer.copyFrom(serverdata);
            this.minecraft.setScreen(new EditServerScreen(this, this::editServerCallback, this.editingServer));
         }

      }).width(74).build());
      this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.delete"), (p_99710_) -> {
         ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
         if (serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
            String s = ((ServerSelectionList.OnlineServerEntry)serverselectionlist$entry).getServerData().name;
            if (s != null) {
               Component component = Component.translatable("selectServer.deleteQuestion");
               Component component1 = Component.translatable("selectServer.deleteWarning", s);
               Component component2 = Component.translatable("selectServer.deleteButton");
               Component component3 = CommonComponents.GUI_CANCEL;
               this.minecraft.setScreen(new ConfirmScreen(this::deleteCallback, component, component1, component2, component3));
            }
         }

      }).width(74).build());
      Button button2 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.refresh"), (p_99706_) -> {
         this.refreshServerList();
      }).width(74).build());
      Button button3 = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280867_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).width(74).build());
      GridLayout gridlayout = new GridLayout();
      GridLayout.RowHelper gridlayout$rowhelper = gridlayout.createRowHelper(1);
      LinearLayout linearlayout = gridlayout$rowhelper.addChild(new LinearLayout(308, 20, LinearLayout.Orientation.HORIZONTAL));
      linearlayout.addChild(this.selectButton);
      linearlayout.addChild(button);
      linearlayout.addChild(button1);
      gridlayout$rowhelper.addChild(SpacerElement.height(4));
      LinearLayout linearlayout1 = gridlayout$rowhelper.addChild(new LinearLayout(308, 20, LinearLayout.Orientation.HORIZONTAL));
      linearlayout1.addChild(this.editButton);
      linearlayout1.addChild(this.deleteButton);
      linearlayout1.addChild(button2);
      linearlayout1.addChild(button3);
      gridlayout.arrangeElements();
      FrameLayout.centerInRectangle(gridlayout, 0, this.height - 64, this.width, 64);
      this.onSelectedChange();
   }

   public void tick() {
      super.tick();
      List<LanServer> list = this.lanServerList.takeDirtyServers();
      if (list != null) {
         this.serverSelectionList.updateNetworkServers(list);
      }

      this.pinger.tick();
   }

   public void removed() {
      if (this.lanServerDetector != null) {
         this.lanServerDetector.interrupt();
         this.lanServerDetector = null;
      }

      this.pinger.removeAll();
      this.serverSelectionList.removed();
   }

   private void refreshServerList() {
      this.minecraft.setScreen(new JoinMultiplayerScreen(this.lastScreen));
   }

   private void deleteCallback(boolean p_99712_) {
      ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
      if (p_99712_ && serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
         this.servers.remove(((ServerSelectionList.OnlineServerEntry)serverselectionlist$entry).getServerData());
         this.servers.save();
         this.serverSelectionList.setSelected((ServerSelectionList.Entry)null);
         this.serverSelectionList.updateOnlineServers(this.servers);
      }

      this.minecraft.setScreen(this);
   }

   private void editServerCallback(boolean p_99717_) {
      ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
      if (p_99717_ && serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
         ServerData serverdata = ((ServerSelectionList.OnlineServerEntry)serverselectionlist$entry).getServerData();
         serverdata.name = this.editingServer.name;
         serverdata.ip = this.editingServer.ip;
         serverdata.copyFrom(this.editingServer);
         this.servers.save();
         this.serverSelectionList.updateOnlineServers(this.servers);
      }

      this.minecraft.setScreen(this);
   }

   private void addServerCallback(boolean p_99722_) {
      if (p_99722_) {
         ServerData serverdata = this.servers.unhide(this.editingServer.ip);
         if (serverdata != null) {
            serverdata.copyNameIconFrom(this.editingServer);
            this.servers.save();
         } else {
            this.servers.add(this.editingServer, false);
            this.servers.save();
         }

         this.serverSelectionList.setSelected((ServerSelectionList.Entry)null);
         this.serverSelectionList.updateOnlineServers(this.servers);
      }

      this.minecraft.setScreen(this);
   }

   private void directJoinCallback(boolean p_99726_) {
      if (p_99726_) {
         ServerData serverdata = this.servers.get(this.editingServer.ip);
         if (serverdata == null) {
            this.servers.add(this.editingServer, true);
            this.servers.save();
            this.join(this.editingServer);
         } else {
            this.join(serverdata);
         }
      } else {
         this.minecraft.setScreen(this);
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
      if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (pKeyCode == 294) {
         this.refreshServerList();
         return true;
      } else if (this.serverSelectionList.getSelected() != null) {
         if (CommonInputs.selected(pKeyCode)) {
            this.joinSelectedServer();
            return true;
         } else {
            return this.serverSelectionList.keyPressed(pKeyCode, pScanCode, pModifiers);
         }
      } else {
         return false;
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
      this.toolTip = null;
      this.renderBackground(pGuiGraphics);
      this.serverSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.toolTip != null) {
         pGuiGraphics.renderComponentTooltip(this.font, this.toolTip, pMouseX, pMouseY);
      }

   }

   public void joinSelectedServer() {
      ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
      if (serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
         this.join(((ServerSelectionList.OnlineServerEntry)serverselectionlist$entry).getServerData());
      } else if (serverselectionlist$entry instanceof ServerSelectionList.NetworkServerEntry) {
         LanServer lanserver = ((ServerSelectionList.NetworkServerEntry)serverselectionlist$entry).getServerData();
         this.join(new ServerData(lanserver.getMotd(), lanserver.getAddress(), true));
      }

   }

   private void join(ServerData pServer) {
      ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(pServer.ip), pServer, false);
   }

   public void setSelected(ServerSelectionList.Entry pSelected) {
      this.serverSelectionList.setSelected(pSelected);
      this.onSelectedChange();
   }

   protected void onSelectedChange() {
      this.selectButton.active = false;
      this.editButton.active = false;
      this.deleteButton.active = false;
      ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
      if (serverselectionlist$entry != null && !(serverselectionlist$entry instanceof ServerSelectionList.LANHeader)) {
         this.selectButton.active = true;
         if (serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
            this.editButton.active = true;
            this.deleteButton.active = true;
         }
      }

   }

   @Override
   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public ServerStatusPinger getPinger() {
      return this.pinger;
   }

   public void setToolTip(List<Component> pToolTip) {
      this.toolTip = pToolTip;
   }

   public ServerList getServers() {
      return this.servers;
   }
}
