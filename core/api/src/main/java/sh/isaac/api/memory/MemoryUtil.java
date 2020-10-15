/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.memory;

//~--- JDK imports ------------------------------------------------------------

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 4/8/15.
 */
public class MemoryUtil {
   /** The Constant memorymbean. */

   // private static final Logger log = LogManager.getLogger();
   private static final MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
   private static final Logger LOG = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   /**
    * Dump memory info.
    */
   public static void dumpMemoryInfo() {
      try {
         LOG.info("\nDUMPING MEMORY INFO\n");

         // Read MemoryMXBean
         final MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();

         LOG.info("Heap Memory Usage: " + memorymbean.getHeapMemoryUsage());
         LOG.info("Non-Heap Memory Usage: " + memorymbean.getNonHeapMemoryUsage());

         // Read Garbage Collection information
         final List<GarbageCollectorMXBean> gcmbeans = ManagementFactory.getGarbageCollectorMXBeans();

         for (final GarbageCollectorMXBean gcmbean: gcmbeans) {
            LOG.info("\nName: " + gcmbean.getName());
            LOG.info("Collection count: " + gcmbean.getCollectionCount());
            LOG.info("Collection time: " + gcmbean.getCollectionTime());
            LOG.info("Memory Pools: ");

            final String[] memoryPoolNames = gcmbean.getMemoryPoolNames();

            for (String memoryPoolName : memoryPoolNames) {
               LOG.info("\t" + memoryPoolName);
            }
         }

         // Read Memory Pool Information
         LOG.info("Memory Pools Info");

         final List<MemoryPoolMXBean> mempoolsmbeans = ManagementFactory.getMemoryPoolMXBeans();

         for (final MemoryPoolMXBean mempoolmbean: mempoolsmbeans) {
            LOG.info("\nName: " + mempoolmbean.getName());
            LOG.info("Usage: " + mempoolmbean.getUsage());
            LOG.info("Collection Usage: " + mempoolmbean.getCollectionUsage());
            LOG.info("Peak Usage: " + mempoolmbean.getPeakUsage());
            LOG.info("Type: " + mempoolmbean.getType());
            LOG.info("Memory Manager Names: ");

            final String[] memManagerNames = mempoolmbean.getMemoryManagerNames();

            for (String memManagerName : memManagerNames) {
               LOG.info("\t" + memManagerName);
            }

            LOG.info("\n");
         }
      } catch (final java.lang.Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Start listener.
    */
   public static void startListener() {
      final MemoryMXBean        mbean    = ManagementFactory.getMemoryMXBean();
      final NotificationEmitter emitter  = (NotificationEmitter) mbean;
      final MyListener          listener = new MyListener();

      emitter.addNotificationListener(listener, null, null);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the heap percent use.
    *
    * @return the heap percent use
    */
   public static String getHeapPercentUse() {
      final MemoryUsage   usage         = memorymbean.getHeapMemoryUsage();
      final double        heapCommitted = usage.getCommitted();
      final double        maxHeap       = usage.getMax();
      final double        percentUsed   = 100.0d * usage.getUsed() / heapCommitted;
      final double        sizeUsedInGB  = 1.0d * usage.getUsed() / 1000000000;
      final double        committedInGB = heapCommitted / 1000000000;
      final double        maxInGB       = maxHeap / 1000000000;
      final StringBuilder sb            = new StringBuilder();
      try (Formatter formatter = new Formatter(sb, Locale.US)) {
         formatter.format(" Heap used: %1$,3.2f/%2$,3.2f GB (%3$,3.1f%%) %4$,3.2f GB requested max",
                 sizeUsedInGB,
                 committedInGB,
                 percentUsed,
                 maxInGB);
      }
      return sb.toString();
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The listener interface for receiving my events.
    * The class that is interested in processing a my
    * event implements this interface, and the object created
    * with that class is registered with a component using the
    * component's <code>addMyListener<code> method. When
    * the my event occurs, that object's appropriate
    * method is invoked.
    *
    * @see MyEvent
    */
   static class MyListener
            implements javax.management.NotificationListener {
      /**
       * Handle notification.
       *
       * @param notif the notif
       * @param handback the handback
       */
      @Override
      public void handleNotification(Notification notif, Object handback) {
         // handle notification
         LOG.info(" Memory Notification: " + notif);
      }
   }
}

