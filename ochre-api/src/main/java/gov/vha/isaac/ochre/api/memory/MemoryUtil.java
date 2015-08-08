package gov.vha.isaac.ochre.api.memory;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by kec on 4/8/15.
 */
public class MemoryUtil {

    //private static final Logger log = LogManager.getLogger();
    private static final MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();

    static class MyListener implements javax.management.NotificationListener {

        public void handleNotification(Notification notif, Object handback) {
            // handle notification
            System.out.println(" Memory Notification: " + notif);
        }
    }

    public static void startListener() {
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        NotificationEmitter emitter = (NotificationEmitter) mbean;
        MyListener listener = new MyListener();
        emitter.addNotificationListener(listener, null, null);
    }

    public static String getHeapPercentUse() {
        MemoryUsage usage = memorymbean.getHeapMemoryUsage();
        double heapCommitted = usage.getCommitted();
        double maxHeap = usage.getMax();
        double percentUsed = 100.0d * usage.getUsed() / heapCommitted;
        double sizeUsedInGB = 1.0d * usage.getUsed() / 1000000000;
        double committedInGB = heapCommitted / 1000000000;
        double maxInGB = maxHeap / 1000000000;
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(" Heap used: %1$,3.2f/%2$,3.2f GB (%3$,3.1f%%) %4$,3.2f GB requested max", 
                sizeUsedInGB, committedInGB, percentUsed, maxInGB);
        formatter.close();
        return sb.toString();

    }

    public static void dumpMemoryInfo() {
        try {
            System.out.println("\nDUMPING MEMORY INFO\n");
            // Read MemoryMXBean
            MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
            System.out.println("Heap Memory Usage: " + memorymbean.getHeapMemoryUsage());
            System.out.println("Non-Heap Memory Usage: " + memorymbean.getNonHeapMemoryUsage());

            // Read Garbage Collection information
            List<GarbageCollectorMXBean> gcmbeans = ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean gcmbean : gcmbeans) {
                System.out.println("\nName: " + gcmbean.getName());
                System.out.println("Collection count: " + gcmbean.getCollectionCount());
                System.out.println("Collection time: " + gcmbean.getCollectionTime());
                System.out.println("Memory Pools: ");
                String[] memoryPoolNames = gcmbean.getMemoryPoolNames();
                for (int i = 0; i < memoryPoolNames.length; i++) {
                    System.out.println("\t" + memoryPoolNames[i]);
                }
            }

            // Read Memory Pool Information
            System.out.println("Memory Pools Info");
            List<MemoryPoolMXBean> mempoolsmbeans = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean mempoolmbean : mempoolsmbeans) {
                System.out.println("\nName: " + mempoolmbean.getName());
                System.out.println("Usage: " + mempoolmbean.getUsage());
                System.out.println("Collection Usage: " + mempoolmbean.getCollectionUsage());
                System.out.println("Peak Usage: " + mempoolmbean.getPeakUsage());
                System.out.println("Type: " + mempoolmbean.getType());
                System.out.println("Memory Manager Names: ");
                String[] memManagerNames = mempoolmbean.getMemoryManagerNames();
                for (int i = 0; i < memManagerNames.length; i++) {
                    System.out.println("\t" + memManagerNames[i]);
                }
                System.out.println("\n");
            }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

}
