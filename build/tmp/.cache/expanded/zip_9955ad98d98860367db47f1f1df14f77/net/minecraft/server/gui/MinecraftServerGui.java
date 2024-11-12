package net.minecraft.server.gui;

import com.google.common.collect.Lists;
import com.mojang.logging.LogQueues;
import com.mojang.logging.LogUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.server.dedicated.DedicatedServer;
import org.slf4j.Logger;

public class MinecraftServerGui extends JComponent {
   private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TITLE = "Minecraft server";
   private static final String SHUTDOWN_TITLE = "Minecraft server - shutting down!";
   private final DedicatedServer server;
   private Thread logAppenderThread;
   private final Collection<Runnable> finalizers = Lists.newArrayList();
   final AtomicBoolean isClosing = new AtomicBoolean();

   public static MinecraftServerGui showFrameFor(final DedicatedServer pServer) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception exception) {
      }

      final JFrame jframe = new JFrame("Minecraft server");
      final MinecraftServerGui minecraftservergui = new MinecraftServerGui(pServer);
      jframe.setDefaultCloseOperation(2);
      jframe.add(minecraftservergui);
      jframe.pack();
      jframe.setLocationRelativeTo((Component)null);
      jframe.setVisible(true);
      jframe.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent p_139944_) {
            if (!minecraftservergui.isClosing.getAndSet(true)) {
               jframe.setTitle("Minecraft server - shutting down!");
               pServer.halt(true);
               minecraftservergui.runFinalizers();
            }

         }
      });
      minecraftservergui.addFinalizer(jframe::dispose);
      minecraftservergui.start();
      return minecraftservergui;
   }

   private MinecraftServerGui(DedicatedServer pServer) {
      this.server = pServer;
      this.setPreferredSize(new Dimension(854, 480));
      this.setLayout(new BorderLayout());

      try {
         this.add(this.buildChatPanel(), "Center");
         this.add(this.buildInfoPanel(), "West");
      } catch (Exception exception) {
         LOGGER.error("Couldn't build server GUI", (Throwable)exception);
      }

   }

   public void addFinalizer(Runnable pFinalizer) {
      this.finalizers.add(pFinalizer);
   }

   /**
    * Generates new StatsComponent and returns it.
    */
   private JComponent buildInfoPanel() {
      JPanel jpanel = new JPanel(new BorderLayout());
      StatsComponent statscomponent = new StatsComponent(this.server);
      this.finalizers.add(statscomponent::close);
      jpanel.add(statscomponent, "North");
      jpanel.add(this.buildPlayerPanel(), "Center");
      jpanel.setBorder(new TitledBorder(new EtchedBorder(), "Stats"));
      return jpanel;
   }

   /**
    * Generates new PlayerListComponent and returns it.
    */
   private JComponent buildPlayerPanel() {
      JList<?> jlist = new PlayerListComponent(this.server);
      JScrollPane jscrollpane = new JScrollPane(jlist, 22, 30);
      jscrollpane.setBorder(new TitledBorder(new EtchedBorder(), "Players"));
      return jscrollpane;
   }

   private JComponent buildChatPanel() {
      JPanel jpanel = new JPanel(new BorderLayout());
      JTextArea jtextarea = new JTextArea();
      JScrollPane jscrollpane = new JScrollPane(jtextarea, 22, 30);
      jtextarea.setEditable(false);
      jtextarea.setFont(MONOSPACED);
      JTextField jtextfield = new JTextField();
      jtextfield.addActionListener((p_276357_) -> {
         String s = jtextfield.getText().trim();
         if (!s.isEmpty()) {
            this.server.handleConsoleInput(s, this.server.createCommandSourceStack());
         }

         jtextfield.setText("");
      });
      jtextarea.addFocusListener(new FocusAdapter() {
         public void focusGained(FocusEvent p_139949_) {
         }
      });
      jpanel.add(jscrollpane, "Center");
      jpanel.add(jtextfield, "South");
      jpanel.setBorder(new TitledBorder(new EtchedBorder(), "Log and chat"));
      this.logAppenderThread = new Thread(() -> {
         String s;
         while((s = LogQueues.getNextLogEvent("ServerGuiConsole")) != null) {
            this.print(jtextarea, jscrollpane, s);
         }

      });
      this.logAppenderThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      this.logAppenderThread.setDaemon(true);
      return jpanel;
   }

   private java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
   public void start() {
      this.logAppenderThread.start();
      latch.countDown();
   }

   public void close() {
      if (!this.isClosing.getAndSet(true)) {
         this.runFinalizers();
      }

   }

   void runFinalizers() {
      this.finalizers.forEach(Runnable::run);
   }

   public void print(JTextArea pTextArea, JScrollPane pScrollPane, String pLine) {
      try {
         latch.await();
      } catch (InterruptedException e){} //Prevent logging until after constructor has ended.
      if (!SwingUtilities.isEventDispatchThread()) {
         SwingUtilities.invokeLater(() -> {
            this.print(pTextArea, pScrollPane, pLine);
         });
      } else {
         Document document = pTextArea.getDocument();
         JScrollBar jscrollbar = pScrollPane.getVerticalScrollBar();
         boolean flag = false;
         if (pScrollPane.getViewport().getView() == pTextArea) {
            flag = (double)jscrollbar.getValue() + jscrollbar.getSize().getHeight() + (double)(MONOSPACED.getSize() * 4) > (double)jscrollbar.getMaximum();
         }

         try {
            document.insertString(document.getLength(), pLine, (AttributeSet)null);
         } catch (BadLocationException badlocationexception) {
         }

         if (flag) {
            jscrollbar.setValue(Integer.MAX_VALUE);
         }

      }
   }
}
