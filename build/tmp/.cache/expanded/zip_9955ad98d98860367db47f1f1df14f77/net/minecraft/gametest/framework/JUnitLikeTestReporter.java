package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JUnitLikeTestReporter implements TestReporter {
   private final Document document;
   private final Element testSuite;
   private final Stopwatch stopwatch;
   private final File destination;

   public JUnitLikeTestReporter(File pDestination) throws ParserConfigurationException {
      this.destination = pDestination;
      this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      this.testSuite = this.document.createElement("testsuite");
      Element element = this.document.createElement("testsuite");
      element.appendChild(this.testSuite);
      this.document.appendChild(element);
      this.testSuite.setAttribute("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
      this.stopwatch = Stopwatch.createStarted();
   }

   private Element createTestCase(GameTestInfo pTestInfo, String pName) {
      Element element = this.document.createElement("testcase");
      element.setAttribute("name", pName);
      element.setAttribute("classname", pTestInfo.getStructureName());
      element.setAttribute("time", String.valueOf((double)pTestInfo.getRunTime() / 1000.0D));
      this.testSuite.appendChild(element);
      return element;
   }

   public void onTestFailed(GameTestInfo pTestInfo) {
      String s = pTestInfo.getTestName();
      String s1 = pTestInfo.getError().getMessage();
      Element element;
      if (pTestInfo.isRequired()) {
         element = this.document.createElement("failure");
         element.setAttribute("message", s1);
      } else {
         element = this.document.createElement("skipped");
         element.setAttribute("message", s1);
      }

      Element element1 = this.createTestCase(pTestInfo, s);
      element1.appendChild(element);
   }

   public void onTestSuccess(GameTestInfo pTestInfo) {
      String s = pTestInfo.getTestName();
      this.createTestCase(pTestInfo, s);
   }

   public void finish() {
      this.stopwatch.stop();
      this.testSuite.setAttribute("time", String.valueOf((double)this.stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0D));

      try {
         this.save(this.destination);
      } catch (TransformerException transformerexception) {
         throw new Error("Couldn't save test report", transformerexception);
      }
   }

   public void save(File pDestination) throws TransformerException {
      TransformerFactory transformerfactory = TransformerFactory.newInstance();
      Transformer transformer = transformerfactory.newTransformer();
      DOMSource domsource = new DOMSource(this.document);
      StreamResult streamresult = new StreamResult(pDestination);
      transformer.transform(domsource, streamresult);
   }
}