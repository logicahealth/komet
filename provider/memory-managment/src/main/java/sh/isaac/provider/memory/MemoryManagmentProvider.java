/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.provider.memory;

import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.memory.HoldInMemoryCache;
import sh.isaac.api.memory.MemoryConfigurations;
import sh.isaac.api.memory.MemoryManagementService;
import sh.isaac.api.memory.SpineReference;
import sh.isaac.api.memory.WriteToDiskCache;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class MemoryManagmentProvider implements MemoryManagementService {
   /**
    * Sets the memory configuration.
    *
    * @param memoryConfiguration the new memory configuration
    */
   @Override
   public void setMemoryConfiguration(MemoryConfigurations memoryConfiguration) {
      System.out.println("Setting memory configuration to: " + memoryConfiguration);

      switch (memoryConfiguration) {
      case CLASSIFY:

         // reclaim as much memory as possible...
         HoldInMemoryCache.clearCache();
         WriteToDiskCache.flushAndClearCache();
         break;

      case IMPORT:
      default:
      }
   }

    @Override
    public int managedMemoryInBytes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addSpine(SpineReference spine) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
   
}
