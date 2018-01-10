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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.memory.HoldInMemoryCache;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.memory.MemoryManagementService;
import sh.isaac.api.memory.SpineReference;
import sh.isaac.api.memory.WriteToDiskCache;
import sh.isaac.provider.datastore.identifier.IdentifierProvider;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class MemoryManagmentProvider implements MemoryManagementService {

    private final ConcurrentHashMap<ApplicationStates, AtomicInteger> applicationStateMap
            = new ConcurrentHashMap<>();

    @Override
    public int managedMemoryInBytes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addSpine(SpineReference spine) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addState(ApplicationStates applicationState) {
        int stateCount = applicationStateMap.computeIfAbsent(applicationState, (t) -> {
            return new AtomicInteger();
        })
                .incrementAndGet();
        
        if (stateCount == 1) {
            // transition to new state
            switch (applicationState) {
                case CLASSIFYING:
                    setupClassificationState();
                    break;
                case IMPORTING:
                    setupImportingState();
                    break;
                case RUNNING:
                case STARTING:
                case STOPPING:
            }
            
        }
    }

    @Override
    public void removeState(ApplicationStates applicationState) {
        int stateCount = applicationStateMap.computeIfAbsent(applicationState, (t) -> {
            return new AtomicInteger();
        })
                .incrementAndGet();
        if (stateCount == 0) {
            // end of state
            switch (applicationState) {
                case CLASSIFYING:
                    cleanupClassificationState();
                    break;
                case IMPORTING:
                    cleanupImportingState();
                    break;
                case RUNNING:
                case STARTING:
                case STOPPING:
            }
        }
    }

    private void setupClassificationState() {
        // reclaim as much memory as possible...
        HoldInMemoryCache.clearCache();
        WriteToDiskCache.flushAndClearCache();        
    }
    
    private void cleanupClassificationState() {
        
    }
    
    private void setupImportingState() {
        
    }
    
    private void cleanupImportingState() {
        IdentifierProvider idProvider = Get.service(IdentifierProvider.class);
        // getIdProvider to release memory. 
    }
}
