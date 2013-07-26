/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.otf.tcc.datastore;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Collection;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;


/**
 * This memory warning system will call the listener when we
 * exceed the percentage of available memory specified.  There
 * should only be one instance of this object created, since the
 * usage threshold can only be set to one number.
 */
public class BdbMemoryMonitor {
  private final Collection<LowMemoryListener> listeners =
      new ArrayList<>();

  public interface LowMemoryListener {
    public void memoryUsageLow(long usedMemory, long maxMemory);
  }

  public BdbMemoryMonitor() {
//    MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
//    NotificationEmitter emitter = (NotificationEmitter) mbean;
//    emitter.addNotificationListener(new NotificationListener() {
//      public void handleNotification(Notification n, Object hb) {
//        if (n.getType().equals(
//            MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
//          long maxMemory = tenuredGenPool.getUsage().getMax();
//          long usedMemory = tenuredGenPool.getUsage().getUsed();
//          for (LowMemoryListener listener : listeners) {
//            listener.memoryUsageLow(usedMemory, maxMemory);
//          }
//        }
//      }
//    }, null, null);
  }

  public boolean addListener(LowMemoryListener listener) {
    return listeners.add(listener);
  }

  public boolean removeListener(LowMemoryListener listener) {
    return listeners.remove(listener);
  }

  private static final MemoryPoolMXBean tenuredGenPool =
      findTenuredGenPool();

  public static void setPercentageUsageThreshold(double percentage) {
    if (percentage <= 0.0 || percentage > 1.0) {
      throw new IllegalArgumentException("Percentage not in range");
    }
    long maxMemory = tenuredGenPool.getUsage().getMax();
    long warningThreshold = (long) (maxMemory * percentage);
    tenuredGenPool.setUsageThreshold(warningThreshold);
  }

  /**
   * Tenured Space Pool can be determined by it being of type
   * HEAP and by it being possible to set the usage threshold.
   */
  private static MemoryPoolMXBean findTenuredGenPool() {
    for (MemoryPoolMXBean pool :
        ManagementFactory.getMemoryPoolMXBeans()) {
      // I don't know whether this approach is better, or whether
      // we should rather check for the pool name "Tenured Gen"?
      if (pool.getType() == MemoryType.HEAP &&
          pool.isUsageThresholdSupported()) {
        return pool;
      }
    }
    throw new AssertionError("Could not find tenured space");
  }
}