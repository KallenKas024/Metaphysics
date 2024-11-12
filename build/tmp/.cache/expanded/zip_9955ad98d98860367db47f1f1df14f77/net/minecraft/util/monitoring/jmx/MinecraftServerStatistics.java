package net.minecraft.util.monitoring.jmx;

import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public final class MinecraftServerStatistics implements DynamicMBean {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftServer server;
   private final MBeanInfo mBeanInfo;
   private final Map<String, MinecraftServerStatistics.AttributeDescription> attributeDescriptionByName = Stream.of(new MinecraftServerStatistics.AttributeDescription("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class), new MinecraftServerStatistics.AttributeDescription("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", Long.TYPE)).collect(Collectors.toMap((p_18332_) -> {
      return p_18332_.name;
   }, Function.identity()));

   private MinecraftServerStatistics(MinecraftServer pServer) {
      this.server = pServer;
      MBeanAttributeInfo[] ambeanattributeinfo = this.attributeDescriptionByName.values().stream().map(MinecraftServerStatistics.AttributeDescription::asMBeanAttributeInfo).toArray((p_145923_) -> {
         return new MBeanAttributeInfo[p_145923_];
      });
      this.mBeanInfo = new MBeanInfo(MinecraftServerStatistics.class.getSimpleName(), "metrics for dedicated server", ambeanattributeinfo, (MBeanConstructorInfo[])null, (MBeanOperationInfo[])null, new MBeanNotificationInfo[0]);
   }

   public static void registerJmxMonitoring(MinecraftServer pServer) {
      try {
         ManagementFactory.getPlatformMBeanServer().registerMBean(new MinecraftServerStatistics(pServer), new ObjectName("net.minecraft.server:type=Server"));
      } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException malformedobjectnameexception) {
         LOGGER.warn("Failed to initialise server as JMX bean", (Throwable)malformedobjectnameexception);
      }

   }

   private float getAverageTickTime() {
      return this.server.getAverageTickTime();
   }

   private long[] getTickTimes() {
      return this.server.tickTimes;
   }

   @Nullable
   public Object getAttribute(String pName) {
      MinecraftServerStatistics.AttributeDescription minecraftserverstatistics$attributedescription = this.attributeDescriptionByName.get(pName);
      return minecraftserverstatistics$attributedescription == null ? null : minecraftserverstatistics$attributedescription.getter.get();
   }

   public void setAttribute(Attribute pAttribute) {
   }

   public AttributeList getAttributes(String[] pAttributes) {
      List<Attribute> list = Arrays.stream(pAttributes).map(this.attributeDescriptionByName::get).filter(Objects::nonNull).map((p_145925_) -> {
         return new Attribute(p_145925_.name, p_145925_.getter.get());
      }).collect(Collectors.toList());
      return new AttributeList(list);
   }

   public AttributeList setAttributes(AttributeList pAttributes) {
      return new AttributeList();
   }

   @Nullable
   public Object invoke(String pActionName, Object[] pParams, String[] pSignature) {
      return null;
   }

   public MBeanInfo getMBeanInfo() {
      return this.mBeanInfo;
   }

   static final class AttributeDescription {
      final String name;
      final Supplier<Object> getter;
      private final String description;
      private final Class<?> type;

      AttributeDescription(String pName, Supplier<Object> pGetter, String pDescription, Class<?> pType) {
         this.name = pName;
         this.getter = pGetter;
         this.description = pDescription;
         this.type = pType;
      }

      private MBeanAttributeInfo asMBeanAttributeInfo() {
         return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
      }
   }
}